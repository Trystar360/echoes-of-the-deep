package com.echoes.block.entity;

import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.energy.ResonanceStorage;
import com.echoes.registry.ModBlockEntities;
import com.echoes.screen.AttunementFurnaceScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Smelts any vanilla furnace recipe using Resonance instead of fuel. Slot 0 =
 * input (hopper-insertable from the top), slot 1 = output. It is a CONSUMER node:
 * a small internal buffer is refilled from the grid via {@link #demand()}.
 */
public class AttunementFurnaceBlockEntity extends BlockEntity
        implements ImplementedInventory, ResonanceNode, NamedScreenHandlerFactory,
        com.echoes.config.Configurable {

    private static final int INPUT = 0, OUTPUT = 1;
    private static final long INTERNAL_BUFFER = 1_000;
    private static final int PROCESS_TICKS = 100;     // faster than a vanilla furnace
    private static final long ENERGY_PER_TICK = 4;    // 400 RU per smelt

    /** The Attunement Furnace exposes redstone behaviour and per-face I/O. */
    public static final com.echoes.config.ConfigSpec SPEC =
            com.echoes.config.ConfigSpec.builder().redstone().sides().build();

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private final ResonanceStorage buffer = new ResonanceStorage(INTERNAL_BUFFER);
    private final com.echoes.config.BlockConfig config = new com.echoes.config.BlockConfig();
    private int progress;
    private int maxProgress = PROCESS_TICKS;

    private final PropertyDelegate props = new PropertyDelegate() {
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

    public AttunementFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ATTUNEMENT_FURNACE, pos, state);
    }

    @Override public DefaultedList<ItemStack> getItems() { return items; }

    public static void tick(World world, BlockPos pos, BlockState state, AttunementFurnaceBlockEntity be) {
        if (world.isClient) return;

        Optional<RecipeEntry<SmeltingRecipe>> match = be.currentRecipe();
        ItemStack result = match.map(m -> be.resultOf(m)).orElse(ItemStack.EMPTY);
        if (result.isEmpty() || !be.hasOutputRoom(result)) {
            if (be.progress != 0) { be.progress = 0; be.markDirty(); }
            return;
        }

        be.maxProgress = PROCESS_TICKS;
        if (be.buffer.extract(ENERGY_PER_TICK, true) >= ENERGY_PER_TICK) {
            be.buffer.extract(ENERGY_PER_TICK, false);
            be.progress++;
            be.markDirty();
            if (be.progress >= be.maxProgress) {
                be.craft(result);
                be.progress = 0;
            }
        }
    }

    private Optional<RecipeEntry<SmeltingRecipe>> currentRecipe() {
        if (getStack(INPUT).isEmpty() || !(world instanceof ServerWorld sw)) return Optional.empty();
        return sw.getRecipeManager().getFirstMatch(
                RecipeType.SMELTING, new SingleStackRecipeInput(getStack(INPUT)), world);
    }

    private ItemStack resultOf(RecipeEntry<SmeltingRecipe> entry) {
        if (!(world instanceof ServerWorld sw)) return ItemStack.EMPTY;
        return entry.value().craft(new SingleStackRecipeInput(getStack(INPUT)), sw.getRegistryManager());
    }

    private boolean hasOutputRoom(ItemStack result) {
        ItemStack out = getStack(OUTPUT);
        if (out.isEmpty()) return true;
        if (!ItemStack.areItemsAndComponentsEqual(out, result)) return false;
        return out.getCount() + result.getCount() <= out.getMaxCount();
    }

    private void craft(ItemStack result) {
        getStack(INPUT).decrement(1);
        if (getStack(OUTPUT).isEmpty()) setStack(OUTPUT, result.copy());
        else getStack(OUTPUT).increment(result.getCount());
        markDirty();
    }

    // --- sided access: top inserts input; other faces extract output ---
    @Override public int[] getAvailableSlots(Direction side) {
        return side == Direction.UP ? new int[]{INPUT} : new int[]{OUTPUT};
    }
    @Override public boolean canInsert(int slot, ItemStack stack, Direction dir) { return slot == INPUT; }
    @Override public boolean canExtract(int slot, ItemStack stack, Direction dir) { return slot == OUTPUT; }

    // --- ResonanceNode (CONSUMER) ---
    @Override public int roleMask() { return NodeRole.of(NodeRole.CONSUMER); }
    @Override public long extract(long max, boolean simulate) { return 0; }
    @Override public long insert(long max, boolean simulate) { return buffer.insert(max, simulate); }
    @Override public long demand() {
        if (world == null || currentRecipe().isEmpty()) return 0;
        return buffer.getCapacity() - buffer.getAmount();
    }
    @Override public int transferCap() { return 0; }
    @Override public BlockPos pos() { return getPos(); }
    @Override public long storedRu() { return buffer.getAmount(); }
    @Override public long capacityRu() { return buffer.getCapacity(); }

    // --- screen ---
    @Override public Text getDisplayName() { return Text.translatable("block.echoes.transmuter"); }
    @Override public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new AttunementFurnaceScreenHandler(syncId, inv, this, props);
    }

    // --- Configurable ---
    @Override public com.echoes.config.BlockConfig getConfig() { return config; }
    @Override public com.echoes.config.ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Text configTitle() { return getCachedState().getBlock().getName(); }
    @Override public void onConfigChanged() { markDirty(); }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        net.minecraft.inventory.Inventories.writeNbt(nbt, items, lookup);
        buffer.writeNbt(nbt);
        config.writeNbt(nbt);
        nbt.putInt("progress", progress);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        net.minecraft.inventory.Inventories.readNbt(nbt, items, lookup);
        buffer.readNbt(nbt);
        config.readNbt(nbt);
        progress = nbt.getInt("progress");
    }
}
