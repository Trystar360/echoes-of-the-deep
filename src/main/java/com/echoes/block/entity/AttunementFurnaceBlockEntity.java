package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import com.echoes.screen.AttunementFurnaceScreenHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Optional;

/**
 * Smelts any vanilla furnace recipe using Resonance instead of fuel. Slot 0 =
 * input (hopper-insertable from the top), slot 1 = output. It is a CONSUMER node:
 * a small internal buffer is refilled from the grid via {@link #demand()}.
 */
public class AttunementFurnaceBlockEntity extends AbstractMachineBlockEntity {

    private static final int INPUT = 0, OUTPUT = 1;
    private static final long INTERNAL_BUFFER = 1_000;
    private static final int PROCESS_TICKS = 100;     // faster than a vanilla furnace
    private static final long ENERGY_PER_TICK = 4;    // 400 RU per smelt

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    // currentRecipe() is queried several times per tick (this BE's own doWork() plus
    // ResonanceNetwork's demand-gathering passes), so cache the match and only re-query
    // the recipe manager when the input stack actually changes.
    private ItemStack cachedInput = ItemStack.EMPTY;
    private Optional<RecipeHolder<SmeltingRecipe>> cachedRecipe = Optional.empty();

    public AttunementFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ATTUNEMENT_FURNACE, pos, state, INTERNAL_BUFFER);
        maxProgress = PROCESS_TICKS;
    }

    @Override public NonNullList<ItemStack> getItems() { return items; }

    @Override
    protected void doWork(ServerLevel sw) {
        Optional<RecipeHolder<SmeltingRecipe>> match = currentRecipe();
        ItemStack result = match.map(this::resultOf).orElse(ItemStack.EMPTY);
        if (result.isEmpty() || !hasOutputRoom(result)) {
            resetProgress();
            return;
        }

        maxProgress = PROCESS_TICKS;
        if (buffer.extract(ENERGY_PER_TICK, true) >= ENERGY_PER_TICK) {
            buffer.extract(ENERGY_PER_TICK, false);
            progress++;
            setChanged();
            if (progress >= maxProgress) {
                craft(result);
                progress = 0;
            }
        }
    }

    @Override
    protected boolean hasWork() { return !currentRecipe().isEmpty(); }

    private Optional<RecipeHolder<SmeltingRecipe>> currentRecipe() {
        ItemStack input = getItem(INPUT);
        if (input.isEmpty()) { cachedInput = ItemStack.EMPTY; cachedRecipe = Optional.empty(); return cachedRecipe; }
        if (!(level instanceof ServerLevel sw)) return Optional.empty();
        if (!ItemStack.isSameItemSameComponents(input, cachedInput)) {
            cachedInput = input.copy();
            cachedRecipe = sw.recipeAccess().getRecipeFor(
                    RecipeType.SMELTING, new SingleRecipeInput(input), level);
        }
        return cachedRecipe;
    }

    private ItemStack resultOf(RecipeHolder<SmeltingRecipe> entry) {
        if (!(level instanceof ServerLevel sw)) return ItemStack.EMPTY;
        return entry.value().assemble(new SingleRecipeInput(getItem(INPUT)));
    }

    private boolean hasOutputRoom(ItemStack result) {
        ItemStack out = getItem(OUTPUT);
        if (out.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(out, result)) return false;
        return out.getCount() + result.getCount() <= out.getMaxStackSize();
    }

    private void craft(ItemStack result) {
        getItem(INPUT).shrink(1);
        if (getItem(OUTPUT).isEmpty()) setItem(OUTPUT, result.copy());
        else getItem(OUTPUT).grow(result.getCount());
        setChanged();
    }

    // --- sided access: top inserts input; other faces extract output. The per-face
    // I/O modes from the config screen gate both directions on top of that (a null
    // direction means internal/wireless access, which is never gated).
    @Override public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? new int[]{INPUT} : new int[]{OUTPUT};
    }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == INPUT && (dir == null || config.side(dir).canInput());
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == OUTPUT && (dir == null || config.side(dir).canOutput());
    }

    // --- screen ---
    @Override public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new AttunementFurnaceScreenHandler(syncId, inv, this, props, getBlockPos());
    }

    @Override
    protected void writeExtra(ValueOutput nbt) {
        net.minecraft.world.ContainerHelper.saveAllItems(nbt, items);
    }

    @Override
    protected void readExtra(ValueInput nbt) {
        net.minecraft.world.ContainerHelper.loadAllItems(nbt, items);
    }
}
