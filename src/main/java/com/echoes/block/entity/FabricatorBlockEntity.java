package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import com.echoes.screen.FabricatorScreenHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.Optional;

/**
 * The Resonant Fabricator: autocrafts whatever shaped recipe is laid out in its
 * 3x3 grid, spending Light (RU) per craft. Slots 0-8 = crafting matrix (real
 * items, consumed one per craft), slot 9 = output, slots 10-11 = augments
 * (Acceleration Coils speed it up, Efficiency Dampers cut the Light cost).
 *
 * <p>Grid items are real stacks, so hoppers/pipes can restock ingredients and
 * pull products — chain a Resonant Chest on a channel in front of it and the
 * wireless network feeds it automatically, the way AE2's network stocks a
 * Pattern Provider. The grid doubles as the "pattern": the machine only ever
 * crafts the recipe the layout currently matches.
 */
public class FabricatorBlockEntity extends AbstractMachineBlockEntity {

    public static final int GRID_FIRST = 0, GRID_LAST = 8;
    private static final int OUTPUT = 9, AUGMENT0 = 10, AUGMENT1 = 11;
    public static final int SIZE = 12;
    private static final long INTERNAL_BUFFER = 2_000;

    /** Base cost/time for one craft, before augments. */
    private static final long BASE_ENERGY = 300;
    private static final int BASE_TIME = 60;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private long energyPerTick; // cached

    // Cache the recipe match; only re-query when the grid actually changes.
    private List<ItemStack> cachedGrid = List.of();
    private Optional<RecipeHolder<CraftingRecipe>> cachedRecipe = Optional.empty();

    public FabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FABRICATOR, pos, state, INTERNAL_BUFFER);
    }

    @Override public NonNullList<ItemStack> getItems() { return items; }

    /** True for the two items that tune this machine (Acceleration Coil, Efficiency Damper). */
    public static boolean isAugment(ItemStack s) {
        return s.is(com.echoes.registry.ModItems.ACCELERATION_COIL)
                || s.is(com.echoes.registry.ModItems.EFFICIENCY_DAMPER);
    }

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
        Optional<RecipeHolder<CraftingRecipe>> match = currentRecipe(sw);
        if (match.isEmpty() || !hasOutputRoom(match.get().value())) {
            resetProgress();
            return;
        }

        int accel = Math.min(8, augCount(com.echoes.registry.ModItems.ACCELERATION_COIL));
        int eff   = Math.min(8, augCount(com.echoes.registry.ModItems.EFFICIENCY_DAMPER));
        double speed = 1.0 + 0.5 * accel;                         // up to 5x faster
        double costMul = (1.0 + 0.25 * accel) * Math.max(0.2, 1.0 - 0.2 * eff);
        maxProgress = Math.max(1, (int) Math.round(BASE_TIME / speed));
        long energyPerCraft = Math.max(1, Math.round(BASE_ENERGY * costMul));
        energyPerTick = Math.max(1, energyPerCraft / maxProgress);

        if (buffer.extract(energyPerTick, true) >= energyPerTick) {
            buffer.extract(energyPerTick, false);
            progress++;
            setChanged();
            if (progress >= maxProgress) {
                craft(sw, match.get().value());
                progress = 0;
            }
        }
    }

    @Override
    protected boolean hasWork() {
        return level instanceof ServerLevel sw && currentRecipe(sw).isPresent();
    }

    private Optional<RecipeHolder<CraftingRecipe>> currentRecipe(ServerLevel sw) {
        List<ItemStack> grid = items.subList(GRID_FIRST, GRID_LAST + 1);
        boolean empty = true;
        for (ItemStack s : grid) if (!s.isEmpty()) { empty = false; break; }
        if (empty) { cachedGrid = List.of(); cachedRecipe = Optional.empty(); return cachedRecipe; }
        if (!sameGrid(grid, cachedGrid)) {
            cachedGrid = grid.stream().map(ItemStack::copy).toList();
            cachedRecipe = sw.recipeAccess().getRecipeFor(
                    RecipeType.CRAFTING, CraftingInput.of(3, 3, grid), level);
        }
        return cachedRecipe;
    }

    private static boolean sameGrid(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!ItemStack.isSameItemSameComponents(a.get(i), b.get(i))) return false;
            if (a.get(i).getCount() != b.get(i).getCount()) return false;
        }
        return true;
    }

    private boolean hasOutputRoom(CraftingRecipe recipe) {
        if (!(level instanceof ServerLevel sw)) return false;
        ItemStack out = getItem(OUTPUT);
        ItemStack result = recipe.assemble(CraftingInput.of(3, 3, items.subList(GRID_FIRST, GRID_LAST + 1)));
        if (result.isEmpty()) return false;
        if (out.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(out, result)) return false;
        return out.getCount() + result.getCount() <= out.getMaxStackSize();
    }

    private void craft(ServerLevel sw, CraftingRecipe recipe) {
        CraftingInput input = CraftingInput.of(3, 3, items.subList(GRID_FIRST, GRID_LAST + 1));
        ItemStack result = recipe.assemble(input);
        if (result.isEmpty()) return;
        for (int s = GRID_FIRST; s <= GRID_LAST; s++) {
            if (!getItem(s).isEmpty()) getItem(s).shrink(1);
        }
        if (getItem(OUTPUT).isEmpty()) setItem(OUTPUT, result.copy());
        else getItem(OUTPUT).grow(result.getCount());
        setChanged();
    }

    // --- sided access: any face may insert into the grid or augments; only the
    // output slot may be extracted. Per-face I/O modes gate both directions
    // (a null direction is internal/wireless access, never gated).
    @Override public int[] getSlotsForFace(Direction side) {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, OUTPUT, AUGMENT0, AUGMENT1};
    }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        if (dir != null && !config.side(dir).canInput()) return false;
        if (slot >= GRID_FIRST && slot <= GRID_LAST) return true;
        return (slot == AUGMENT0 || slot == AUGMENT1) && isAugment(stack);
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == OUTPUT && (dir == null || config.side(dir).canOutput());
    }

    // --- screen ---
    @Override public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new FabricatorScreenHandler(syncId, inv, this, props, getBlockPos());
    }

    @Override public void setChanged() { super.setChanged(); }

    @Override
    protected void writeExtra(ValueOutput nbt) {
        net.minecraft.world.ContainerHelper.saveAllItems(nbt, items);
    }

    @Override
    protected void readExtra(ValueInput nbt) {
        net.minecraft.world.ContainerHelper.loadAllItems(nbt, items);
        cachedGrid = List.of();
        cachedRecipe = Optional.empty();
    }
}
