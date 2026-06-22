package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceCoupler;
import com.echoes.energy.ResonanceField;
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
 * The still magnetic centre of zero — Russell's fulcrum from which all motion
 * springs. It draws only a slow trickle of Light itself (PROVIDER + STORAGE), but
 * it is the <em>anchor</em> of a standing-wave array: every generator tuned to its
 * octave counts the Core as a stronger antinode, so you build your resonant lattice
 * around it. "All motion springs from the still centre."
 */
public class StillnessCoreBlockEntity extends BlockEntity implements ResonanceNode, ResonanceCoupler, Configurable {
    public static final long CAPACITY = 50_000;
    private static final int GEN_PER_TICK = 4;
    /** The Core's own output barely climbs — it gives by anchoring, not by output. */
    private static final double RESONANCE_GAIN = 0.25;
    private static final double RESONANCE_MAX_BONUS = 1.0;
    private static final int RESCAN_INTERVAL = 40;

    /** The Core exposes its octave (the array it anchors), redstone behaviour, and per-face I/O. */
    public static final ConfigSpec SPEC = ConfigSpec.builder().octave().redstone().sides().build();

    private final ResonanceStorage storage = new ResonanceStorage(CAPACITY);
    private final BlockConfig config = new BlockConfig();
    private double resonance = 1.0;

    public StillnessCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STILLNESS_CORE, pos, state);
        config.applyDefaults(SPEC);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StillnessCoreBlockEntity be) {
        if (level.isClientSide() || !(level instanceof ServerLevel sw)) return;
        if (!be.config.redstone().allows(sw.hasNeighborSignal(pos))) return;

        if ((sw.getGameTime() + Math.floorMod(pos.hashCode(), RESCAN_INTERVAL)) % RESCAN_INTERVAL == 0) {
            be.resonance = ResonanceField.scan(sw, pos, be.config.octave())
                    .multiplier(RESONANCE_GAIN, RESONANCE_MAX_BONUS);
        }
        if (be.storage.isFull()) return;
        be.storage.absorb(Math.max(1, (int) Math.round(GEN_PER_TICK * be.resonance)));
        be.setChanged();
    }

    @Override public int couplingOctave() { return config.octave(); }
    @Override public boolean isResonanceAnchor() { return true; }
    @Override public double resonanceMultiplier() { return resonance; }

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
