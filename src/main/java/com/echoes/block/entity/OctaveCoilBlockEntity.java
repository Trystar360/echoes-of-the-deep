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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * The higher-octave generator — a Stillness Core wound up an octave with charged
 * Radiant matter. It draws a far stronger trickle of Light from the stillness and
 * banks far more of it (PROVIDER + STORAGE), the baseline generator for a maturing
 * grid. Tunable generation via the config GUI (the coil's "rate").
 */
public class OctaveCoilBlockEntity extends BlockEntity implements ResonanceNode, Configurable {
    public static final long CAPACITY = 300_000;
    private static final int BASE_GEN_PER_TICK = 24;

    /** The Coil exposes redstone behaviour, per-face I/O, and a tunable generation rate. */
    public static final ConfigSpec SPEC = ConfigSpec.builder()
            .redstone().sides()
            .tuning("config.echoes.tuning.rate", 1, 4, 1, 2)
            .build();

    private final ResonanceStorage storage = new ResonanceStorage(CAPACITY);
    private final BlockConfig config = new BlockConfig();

    public OctaveCoilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OCTAVE_COIL, pos, state);
        config.applyDefaults(SPEC);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, OctaveCoilBlockEntity be) {
        if (world.isClient || be.storage.isFull()) return;
        if (world instanceof ServerLevel sw && !be.config.redstone().allows(sw.isReceivingRedstonePower(pos))) return;
        // Tuning "rate" 1..4 scales the base generation.
        be.storage.absorb(BASE_GEN_PER_TICK * be.config.tuningA());
        be.markDirty();
    }

    public ResonanceStorage storage() { return storage; }

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
