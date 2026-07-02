package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.energy.ResonanceStorage;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Captures ambient RU emitted by Resonance events and offers it to the network.
 * PROVIDER + STORAGE: its buffer doubles as the network's first source.
 */
public class ResonatorBlockEntity extends BlockEntity implements ResonanceNode, Configurable {
    public static final long DEFAULT_CAPACITY = 10_000;

    /** The Coil exposes redstone behaviour. */
    public static final ConfigSpec SPEC = ConfigSpec.builder().redstone().build();

    private final ResonanceStorage storage = new ResonanceStorage(DEFAULT_CAPACITY);
    private final BlockConfig config = new BlockConfig();
    private boolean indexed = false;

    public ResonatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONATOR, pos, state);
        config.applyDefaults(SPEC);
    }

    /** Lightweight ticker: guarantees this Resonator is on {@link com.echoes.energy.ResonatorIndex} once loaded. */
    public static void tick(Level level, BlockPos pos, BlockState state, ResonatorBlockEntity be) {
        if (be.indexed || !(level instanceof net.minecraft.server.level.ServerLevel sw)) return;
        com.echoes.energy.ResonatorIndex.register(sw, pos);
        be.indexed = true;
    }

    @Override
    public void setRemoved() {
        if (level instanceof net.minecraft.server.level.ServerLevel sw) {
            com.echoes.energy.ResonatorIndex.unregister(sw, getBlockPos());
        }
        indexed = false;
        super.setRemoved();
    }

    /** Called by ResonanceEvents when a nearby event fires. */
    public void absorbAmbient(int ru) {
        storage.absorb(ru);
        setChanged();
    }

    public ResonanceStorage storage() { return storage; }

    // --- ResonanceNode ---
    @Override public int roleMask() { return NodeRole.of(NodeRole.PROVIDER, NodeRole.STORAGE); }
    @Override public long extract(long max, boolean simulate) { return storage.extract(max, simulate); }
    @Override public long insert(long max, boolean simulate) { return storage.insert(max, simulate); }
    @Override public long demand() { return 0; }
    @Override public int transferCap() { return 0; }
    @Override public BlockPos pos() { return getBlockPos(); }
    @Override public long storedRu() { return storage.getAmount(); }
    @Override public long capacityRu() { return storage.getCapacity(); }

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Component configTitle() { return getBlockState().getBlock().getName(); }
    @Override public void onConfigChanged() { setChanged(); }

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        storage.writeNbt(nbt);
        config.writeNbt(nbt);
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        storage.readNbt(nbt);
        config.readNbt(nbt);
    }
}
