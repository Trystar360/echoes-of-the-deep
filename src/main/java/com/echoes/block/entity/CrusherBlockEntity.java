package com.echoes.block.entity;

import com.echoes.recipe.CrushingRecipe;
import com.echoes.recipe.ModRecipes;
import com.echoes.registry.ModBlockEntities;
import com.echoes.screen.CrusherScreenHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Optional;

/**
 * Consumes RU from the network to crush raw ore into dust (ore-doubling).
 * Slot 0 = input (hopper-insertable from the top), slot 1 = output.
 *
 * <p>A small internal buffer lets it keep working for a tick or two if the grid
 * briefly dips; it pulls its per-tick energy need from the network via demand().
 */
public class CrusherBlockEntity extends AbstractMachineBlockEntity {

    private static final int INPUT = 0, OUTPUT = 1, BYPRODUCT = 2, AUGMENT0 = 3, AUGMENT1 = 4;
    public static final int AUGMENT_FIRST = AUGMENT0, AUGMENT_LAST = AUGMENT1;
    private static final long INTERNAL_BUFFER = 1_000;

    /** True for the three items that may go in an augment slot. */
    public static boolean isAugment(ItemStack s) {
        return s.is(com.echoes.registry.ModItems.ACCELERATION_COIL)
                || s.is(com.echoes.registry.ModItems.EFFICIENCY_DAMPER)
                || s.is(com.echoes.registry.ModItems.YIELD_RESONATOR);
    }

    private final NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private long energyPerTick; // recipe.energy / maxProgress, cached

    // currentRecipe() is queried several times per tick (this BE's own doWork() plus
    // ResonanceNetwork's demand-gathering passes), so cache the match and only re-query
    // the recipe manager when the input stack actually changes.
    private ItemStack cachedInput = ItemStack.EMPTY;
    private Optional<RecipeHolder<CrushingRecipe>> cachedRecipe = Optional.empty();

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUSHER, pos, state, INTERNAL_BUFFER);
    }

    @Override public NonNullList<ItemStack> getItems() { return items; }

    /** Total number of a given augment item across both augment slots. */
    private int augCount(net.minecraft.world.item.Item type) {
        int n = 0;
        for (int s = AUGMENT0; s <= AUGMENT1; s++) {
            ItemStack a = getItem(s);
            if (a.is(type)) n += a.getCount();
        }
        return n;
    }

    @Override
    protected void doWork(ServerLevel sw) {
        Optional<RecipeHolder<CrushingRecipe>> match = currentRecipe();
        if (match.isEmpty() || !hasOutputRoom(match.get().value())) {
            resetProgress();
            return;
        }

        CrushingRecipe recipe = match.get().value();
        // Augments tune the recipe: acceleration cuts the time (costing more Light per
        // craft), efficiency cuts the Light cost.
        int accel = Math.min(8, augCount(com.echoes.registry.ModItems.ACCELERATION_COIL));
        int eff   = Math.min(8, augCount(com.echoes.registry.ModItems.EFFICIENCY_DAMPER));
        double speed = 1.0 + 0.5 * accel;                         // up to 5× faster
        double costMul = (1.0 + 0.25 * accel) * Math.max(0.2, 1.0 - 0.2 * eff);
        maxProgress = Math.max(1, (int) Math.round(recipe.processingTime() / speed));
        long energyPerCraft = Math.max(1, Math.round(recipe.energy() * costMul));
        energyPerTick = Math.max(1, energyPerCraft / maxProgress);

        // Spend energy from the internal buffer; the network refills it via demand().
        if (buffer.extract(energyPerTick, true) >= energyPerTick) {
            buffer.extract(energyPerTick, false);
            progress++;
            setChanged();
            if (progress >= maxProgress) {
                craft(recipe);
                progress = 0;
            }
        }
    }

    @Override
    protected boolean hasWork() { return !currentRecipe().isEmpty(); }

    private Optional<RecipeHolder<CrushingRecipe>> currentRecipe() {
        ItemStack input = getItem(INPUT);
        if (input.isEmpty()) { cachedInput = ItemStack.EMPTY; cachedRecipe = Optional.empty(); return cachedRecipe; }
        if (!(level instanceof ServerLevel sw)) return Optional.empty();
        if (!ItemStack.isSameItemSameComponents(input, cachedInput)) {
            cachedInput = input.copy();
            cachedRecipe = sw.recipeAccess().getRecipeFor(
                    ModRecipes.CRUSHING_TYPE, new SingleRecipeInput(input), level);
        }
        return cachedRecipe;
    }

    private boolean hasOutputRoom(CrushingRecipe recipe) {
        ItemStack out = getItem(OUTPUT);
        ItemStack result = recipe.result();
        if (out.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(out, result)) return false;
        return out.getCount() + result.getCount() <= out.getMaxStackSize();
    }

    private void craft(CrushingRecipe recipe) {
        getItem(INPUT).shrink(1);
        ItemStack result = recipe.result();
        if (getItem(OUTPUT).isEmpty()) setItem(OUTPUT, result.copy());
        else getItem(OUTPUT).grow(result.getCount());

        // Roll the optional byproduct (e.g. resonant slag) into the third slot.
        // Yield Resonators raise the odds.
        ItemStack sec = recipe.secondary();
        float chance = Math.min(1.0f, recipe.secondaryChance()
                + 0.25f * Math.min(8, augCount(com.echoes.registry.ModItems.YIELD_RESONATOR)));
        if (!sec.isEmpty() && level != null && level.getRandom().nextFloat() < chance) {
            ItemStack slot = getItem(BYPRODUCT);
            if (slot.isEmpty()) {
                setItem(BYPRODUCT, sec.copy());
            } else if (ItemStack.isSameItemSameComponents(slot, sec)
                    && slot.getCount() + sec.getCount() <= slot.getMaxStackSize()) {
                slot.grow(sec.getCount());
            }
            // else: byproduct slot is full/mismatched — the roll is forfeited, machine keeps running.
        }
        setChanged();
    }

    // --- sided access: top inserts input; other faces extract output + byproduct.
    // The per-face I/O modes from the config screen gate both directions on top of
    // that (a null direction means internal/wireless access, which is never gated).
    @Override public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? new int[]{INPUT} : new int[]{OUTPUT, BYPRODUCT};
    }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == INPUT && (dir == null || config.side(dir).canInput());
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return (slot == OUTPUT || slot == BYPRODUCT) && (dir == null || config.side(dir).canOutput());
    }

    // --- screen ---
    @Override public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new CrusherScreenHandler(syncId, inv, this, props, getBlockPos());
    }

    @Override public void setChanged() { super.setChanged(); }

    @Override
    protected void writeExtra(ValueOutput nbt) {
        net.minecraft.world.ContainerHelper.saveAllItems(nbt, items);
    }

    @Override
    protected void readExtra(ValueInput nbt) {
        net.minecraft.world.ContainerHelper.loadAllItems(nbt, items);
    }
}
