package com.echoes.block.entity;

import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.energy.ResonanceStorage;
import com.echoes.registry.ModBlockEntities;
import com.echoes.screen.AttunementFurnaceScreenHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Smelts any vanilla furnace recipe using Resonance instead of fuel. Slot 0 =
 * input (hopper-insertable from the top), slot 1 = output. It is a CONSUMER node:
 * a small internal buffer is refilled from the grid via {@link #demand()}.
 */
public class AttunementFurnaceBlockEntity extends BlockEntity
        implements ImplementedInventory, ResonanceNode, MenuProvider,
        com.echoes.config.Configurable {

    private static final int INPUT = 0, OUTPUT = 1;
    private static final long INTERNAL_BUFFER = 1_000;
    private static final int PROCESS_TICKS = 100;     // faster than a vanilla furnace
    private static final long ENERGY_PER_TICK = 4;    // 400 RU per smelt

    /** The Attunement Furnace exposes redstone behaviour and per-face I/O. */
    public static final com.echoes.config.ConfigSpec SPEC =
            com.echoes.config.ConfigSpec.builder().redstone().sides().build();

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final ResonanceStorage buffer = new ResonanceStorage(INTERNAL_BUFFER);
    private final com.echoes.config.BlockConfig config = new com.echoes.config.BlockConfig();
    private int progress;
    private int maxProgress = PROCESS_TICKS;

    private final ContainerData props = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> (int) buffer.getAmount();
                case 3 -> (int) buffer.getCapacity();
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {
            switch (i) { case 0 -> progress = v; case 1 -> maxProgress = v; }
        }
        @Override public int getCount() { return 4; }
    };

    public AttunementFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ATTUNEMENT_FURNACE, pos, state);
    }

    @Override public NonNullList<ItemStack> getItems() { return items; }

    public static void tick(Level level, BlockPos pos, BlockState state, AttunementFurnaceBlockEntity be) {
        if (level.isClientSide()) return;

        Optional<RecipeHolder<SmeltingRecipe>> match = be.currentRecipe();
        ItemStack result = match.map(m -> be.resultOf(m)).orElse(ItemStack.EMPTY);
        if (result.isEmpty() || !be.hasOutputRoom(result)) {
            if (be.progress != 0) { be.progress = 0; be.setChanged(); }
            return;
        }

        be.maxProgress = PROCESS_TICKS;
        if (be.buffer.extract(ENERGY_PER_TICK, true) >= ENERGY_PER_TICK) {
            be.buffer.extract(ENERGY_PER_TICK, false);
            be.progress++;
            be.setChanged();
            if (be.progress >= be.maxProgress) {
                be.craft(result);
                be.progress = 0;
            }
        }
    }

    private Optional<RecipeHolder<SmeltingRecipe>> currentRecipe() {
        if (getItem(INPUT).isEmpty() || !(level instanceof ServerLevel sw)) return Optional.empty();
        return sw.recipeAccess().getRecipeFor(
                RecipeType.SMELTING, new SingleRecipeInput(getItem(INPUT)), level);
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

    // --- sided access: top inserts input; other faces extract output ---
    @Override public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? new int[]{INPUT} : new int[]{OUTPUT};
    }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) { return slot == INPUT; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) { return slot == OUTPUT; }

    // --- ResonanceNode (CONSUMER) ---
    @Override public int roleMask() { return NodeRole.of(NodeRole.CONSUMER); }
    @Override public long extract(long max, boolean simulate) { return 0; }
    @Override public long insert(long max, boolean simulate) { return buffer.insert(max, simulate); }
    @Override public long demand() {
        if (level == null || currentRecipe().isEmpty()) return 0;
        return buffer.getCapacity() - buffer.getAmount();
    }
    @Override public int transferCap() { return 0; }
    @Override public BlockPos pos() { return getBlockPos(); }
    @Override public long storedRu() { return buffer.getAmount(); }
    @Override public long capacityRu() { return buffer.getCapacity(); }

    // --- screen ---
    @Override public Component getDisplayName() { return Component.translatable("block.echoes.transmuter"); }
    @Override public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new AttunementFurnaceScreenHandler(syncId, inv, this, props);
    }

    // --- Configurable ---
    @Override public com.echoes.config.BlockConfig getConfig() { return config; }
    @Override public com.echoes.config.ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Component configTitle() { return getBlockState().getBlock().getName(); }
    @Override public void onConfigChanged() { setChanged(); }

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        net.minecraft.world.ContainerHelper.saveAllItems(nbt, items);
        buffer.writeNbt(nbt);
        config.writeNbt(nbt);
        nbt.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        net.minecraft.world.ContainerHelper.loadAllItems(nbt, items);
        buffer.readNbt(nbt);
        config.readNbt(nbt);
        progress = nbt.getIntOr("progress", 0);
    }
}
