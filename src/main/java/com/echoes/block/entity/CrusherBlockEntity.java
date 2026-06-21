package com.echoes.block.entity;

import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.energy.ResonanceStorage;
import com.echoes.recipe.CrushingRecipe;
import com.echoes.recipe.ModRecipes;
import com.echoes.registry.ModBlockEntities;
import com.echoes.screen.CrusherScreenHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.network.chat.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Consumes RU from the network to crush raw ore into dust (ore-doubling).
 * Slot 0 = input (hopper-insertable from the top), slot 1 = output.
 *
 * <p>A small internal buffer lets it keep working for a tick or two if the grid
 * briefly dips; it pulls its per-tick energy need from the network via demand().
 */
public class CrusherBlockEntity extends BlockEntity
        implements ImplementedInventory, ResonanceNode, MenuProvider,
        com.echoes.config.Configurable {

    private static final int INPUT = 0, OUTPUT = 1, BYPRODUCT = 2;
    private static final long INTERNAL_BUFFER = 1_000;

    /** The Crusher exposes redstone behaviour and per-face I/O. */
    public static final com.echoes.config.ConfigSpec SPEC =
            com.echoes.config.ConfigSpec.builder().redstone().sides().build();

    private final NonNullList<ItemStack> items = NonNullList.ofSize(3, ItemStack.EMPTY);
    private final ResonanceStorage buffer = new ResonanceStorage(INTERNAL_BUFFER);
    private final com.echoes.config.BlockConfig config = new com.echoes.config.BlockConfig();

    private int progress;       // ticks accumulated toward current recipe
    private int maxProgress;    // recipe.processingTime, cached
    private long energyPerTick; // recipe.energy / maxProgress, cached

    private final ContainerData props = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> (int) buffer.getAmount();
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {
            switch (i) { case 0 -> progress = v; case 1 -> maxProgress = v; }
        }
        @Override public int size() { return 3; }
    };

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUSHER, pos, state);
    }

    @Override public NonNullList<ItemStack> getItems() { return items; }

    // --- tick (registered as a BlockEntityTicker) ---
    public static void tick(Level world, BlockPos pos, BlockState state, CrusherBlockEntity be) {
        if (world.isClient) return;

        Optional<RecipeHolder<CrushingRecipe>> match = be.currentRecipe();
        if (match.isEmpty() || !be.hasOutputRoom(match.get().value())) {
            if (be.progress != 0) { be.progress = 0; be.markDirty(); }
            return;
        }

        CrushingRecipe recipe = match.get().value();
        be.maxProgress = recipe.processingTime();
        be.energyPerTick = Math.max(1, recipe.energy() / recipe.processingTime());

        // Spend energy from the internal buffer; the network refills it via demand().
        if (be.buffer.extract(be.energyPerTick, true) >= be.energyPerTick) {
            be.buffer.extract(be.energyPerTick, false);
            be.progress++;
            be.markDirty();
            if (be.progress >= be.maxProgress) {
                be.craft(recipe);
                be.progress = 0;
            }
        }
    }

    private Optional<RecipeHolder<CrushingRecipe>> currentRecipe() {
        if (getStack(INPUT).isEmpty()) return Optional.empty();
        if (!(world instanceof net.minecraft.server.level.ServerLevel sw)) return Optional.empty();
        return sw.getRecipeManager().getFirstMatch(
                ModRecipes.CRUSHING_TYPE, new SingleRecipeInput(getStack(INPUT)), world);
    }

    private boolean hasOutputRoom(CrushingRecipe recipe) {
        ItemStack out = getStack(OUTPUT);
        ItemStack result = recipe.result();
        if (out.isEmpty()) return true;
        if (!ItemStack.areItemsAndComponentsEqual(out, result)) return false;
        return out.getCount() + result.getCount() <= out.getMaxCount();
    }

    private void craft(CrushingRecipe recipe) {
        getStack(INPUT).decrement(1);
        ItemStack result = recipe.result();
        if (getStack(OUTPUT).isEmpty()) setStack(OUTPUT, result.copy());
        else getStack(OUTPUT).increment(result.getCount());

        // Roll the optional byproduct (e.g. resonant slag) into the third slot.
        ItemStack sec = recipe.secondary();
        if (!sec.isEmpty() && world != null && world.getRandom().nextFloat() < recipe.secondaryChance()) {
            ItemStack slot = getStack(BYPRODUCT);
            if (slot.isEmpty()) {
                setStack(BYPRODUCT, sec.copy());
            } else if (ItemStack.areItemsAndComponentsEqual(slot, sec)
                    && slot.getCount() + sec.getCount() <= slot.getMaxCount()) {
                slot.increment(sec.getCount());
            }
            // else: byproduct slot is full/mismatched — the roll is forfeited, machine keeps running.
        }
        markDirty();
    }

    // --- sided access: top inserts input; other faces extract output + byproduct ---
    @Override public int[] getAvailableSlots(Direction side) {
        return side == Direction.UP ? new int[]{INPUT} : new int[]{OUTPUT, BYPRODUCT};
    }
    @Override public boolean canInsert(int slot, ItemStack stack, Direction dir) { return slot == INPUT; }
    @Override public boolean canExtract(int slot, ItemStack stack, Direction dir) { return slot == OUTPUT || slot == BYPRODUCT; }

    // --- ResonanceNode (CONSUMER) ---
    @Override public int roleMask() { return NodeRole.of(NodeRole.CONSUMER); }
    @Override public long extract(long max, boolean simulate) { return 0; }
    @Override public long insert(long max, boolean simulate) { return buffer.insert(max, simulate); }
    @Override public long demand() {
        // Want enough to top the internal buffer, but only when there's work to do.
        if (world == null || currentRecipe().isEmpty()) return 0;
        return buffer.getCapacity() - buffer.getAmount();
    }
    @Override public int transferCap() { return 0; }
    @Override public BlockPos pos() { return getPos(); }
    @Override public long storedRu() { return buffer.getAmount(); }
    @Override public long capacityRu() { return buffer.getCapacity(); }

    // --- screen ---
    @Override public Component getDisplayName() { return Component.translatable("block.echoes.compressor"); }
    @Override public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new CrusherScreenHandler(syncId, inv, this, props);
    }

    @Override public void markDirty() { super.markDirty(); }

    // --- Configurable ---
    @Override public com.echoes.config.BlockConfig getConfig() { return config; }
    @Override public com.echoes.config.ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Component configTitle() { return getCachedState().getBlock().getName(); }
    @Override public void onConfigChanged() { markDirty(); }

    @Override
    protected void writeNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
        super.writeNbt(nbt, lookup);
        net.minecraft.world.ContainerHelper.writeNbt(nbt, items, lookup);
        buffer.writeNbt(nbt);
        config.writeNbt(nbt);
        nbt.putInt("progress", progress);
    }

    @Override
    protected void readNbt(CompoundTag nbt, HolderLookup.Provider lookup) {
        super.readNbt(nbt, lookup);
        net.minecraft.world.ContainerHelper.readNbt(nbt, items, lookup);
        buffer.readNbt(nbt);
        config.readNbt(nbt);
        progress = nbt.getInt("progress");
    }
}
