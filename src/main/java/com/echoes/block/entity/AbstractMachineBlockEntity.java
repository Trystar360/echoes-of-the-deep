package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.energy.ResonanceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Common base for the RU-consuming crafting machines (Crusher, Attunement Furnace):
 * an internal buffer, a {@link BlockConfig}, redstone-gated progress, and the shared
 * CONSUMER/Configurable/menu boilerplate. Subclasses own their own inventory layout
 * and the actual recipe/crafting logic via {@link #doWork}, which the ticker only
 * ever calls when redstone allows — there is no other entry point, so a subclass
 * cannot forget the gate the way {@code CrusherBlockEntity} and
 * {@code AttunementFurnaceBlockEntity} both once did.
 */
public abstract class AbstractMachineBlockEntity extends BlockEntity
        implements ImplementedInventory, ResonanceNode, MenuProvider, Configurable {

    /** Every machine exposes redstone behaviour and per-face I/O. */
    public static final ConfigSpec SPEC = ConfigSpec.builder().redstone().sides().build();

    protected final ResonanceStorage buffer;
    protected final BlockConfig config = new BlockConfig();
    protected int progress;    // ticks accumulated toward the current recipe
    protected int maxProgress; // recipe-dependent; subclasses set this in doWork

    protected final ContainerData props = new ContainerData() {
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
        @Override public int getCount() { return 3; }
    };

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long bufferCapacity) {
        super(type, pos, state);
        this.buffer = new ResonanceStorage(bufferCapacity);
    }

    /** Redstone-gated work, called once per server tick. Recipe matching, output-room
     * checks, and crafting all belong here — call {@link #resetProgress()} on any
     * early return so a stalled machine doesn't show stale progress. */
    protected abstract void doWork(ServerLevel sw);

    /** Whether this machine currently has a matched recipe (drives {@link #demand()}). */
    protected abstract boolean hasWork();

    protected void resetProgress() {
        if (progress != 0) { progress = 0; setChanged(); }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AbstractMachineBlockEntity be) {
        if (!(level instanceof ServerLevel sw)) return;
        if (!be.config.redstone().allows(sw.hasNeighborSignal(pos))) {
            be.resetProgress();
            return;
        }
        be.doWork(sw);
    }

    // --- ResonanceNode (CONSUMER) ---
    @Override public int roleMask() { return NodeRole.of(NodeRole.CONSUMER); }
    @Override public long extract(long max, boolean simulate) { return 0; }
    @Override public long insert(long max, boolean simulate) { return buffer.insert(max, simulate); }
    @Override public long demand() {
        if (level == null || !hasWork()) return 0;
        return buffer.getCapacity() - buffer.getAmount();
    }
    @Override public int transferCap() { return 0; }
    @Override public BlockPos pos() { return getBlockPos(); }
    @Override public long storedRu() { return buffer.getAmount(); }
    @Override public long capacityRu() { return buffer.getCapacity(); }

    // --- MenuProvider ---
    @Override public Component getDisplayName() { return getBlockState().getBlock().getName(); }

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Component configTitle() { return getBlockState().getBlock().getName(); }
    @Override public void onConfigChanged() { setChanged(); }

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        buffer.writeNbt(nbt);
        config.writeNbt(nbt);
        nbt.putInt("progress", progress);
        writeExtra(nbt);
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        buffer.readNbt(nbt);
        config.readNbt(nbt);
        progress = nbt.getIntOr("progress", 0);
        readExtra(nbt);
    }

    /** Subclasses persist their own inventory (and any other extra state) here. */
    protected void writeExtra(ValueOutput nbt) {}
    protected void readExtra(ValueInput nbt) {}
}
