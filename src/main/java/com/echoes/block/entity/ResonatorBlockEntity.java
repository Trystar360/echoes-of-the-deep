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
 * Captures ambient RU emitted by Resonance events and offers it to the network.
 * PROVIDER + STORAGE: its buffer doubles as the network's first source. Tune it to
 * an octave and array it on the antinode lattice and it converts the sound it hears
 * far more efficiently — captured Light climbing from ×1 toward ×3.
 */
public class ResonatorBlockEntity extends BlockEntity implements ResonanceNode, ResonanceCoupler, Configurable {
    public static final long DEFAULT_CAPACITY = 10_000;
    /** An arrayed Coil converts captured sound better: coupling 0..4 → ×1..×3. */
    private static final double RESONANCE_GAIN = 0.5;
    private static final double RESONANCE_MAX_BONUS = 2.0;
    private static final int RESCAN_INTERVAL = 40;

    /** The Coil exposes its octave (the array it joins), redstone behaviour, and per-face I/O. */
    public static final ConfigSpec SPEC = ConfigSpec.builder().octave().redstone().sides().build();

    private final ResonanceStorage storage = new ResonanceStorage(DEFAULT_CAPACITY);
    private final BlockConfig config = new BlockConfig();
    private double resonance = 1.0;

    public ResonatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONATOR, pos, state);
        config.applyDefaults(SPEC);
    }

    /** Recomputes the resonance multiplier periodically so captured sound is amplified live. */
    public static void tick(Level level, BlockPos pos, BlockState state, ResonatorBlockEntity be) {
        if (level.isClientSide() || !(level instanceof ServerLevel sw)) return;
        if ((sw.getGameTime() + Math.floorMod(pos.hashCode(), RESCAN_INTERVAL)) % RESCAN_INTERVAL == 0) {
            be.resonance = ResonanceField.scan(sw, pos, be.config.octave())
                    .multiplier(RESONANCE_GAIN, RESONANCE_MAX_BONUS);
        }
        if (be.resonance >= 1.5 && sw.getGameTime() % 20 == 0 && !be.storage.isEmpty()) {
            ResonanceField.ringParticles(sw, pos);
        }
    }

    /** Called by ResonanceEvents when a nearby event fires. */
    public void absorbAmbient(int ru) {
        if (!config.redstone().allows(level != null && level.hasNeighborSignal(getBlockPos()))) return;
        storage.absorb(Math.round(ru * resonance));
        setChanged();
    }

    @Override public int couplingOctave() { return config.octave(); }
    @Override public double resonanceMultiplier() { return resonance; }

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
