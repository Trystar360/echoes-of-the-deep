package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.energy.ResonanceStorage;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/**
 * Radiation — the centrifugal, expansive half of the two-way universe. Draws Light
 * from the grid and pours it back into the world as life, accelerating nearby crops
 * and saplings (a powered bonemeal aura) and glowing while charged. The discharge
 * counterpart to the Resonant Coil.
 */
public class RadiatorBlockEntity extends BlockEntity implements ResonanceNode, Configurable {
    private static final long BUFFER = 3_000;
    private static final long COST = 300;       // Light per growth
    private static final int INTERVAL = 10;     // ticks between attempts
    private static final int V_RADIUS = 2, TRIES = 8;

    /** Radiators expose redstone behaviour, per-face I/O and an adjustable radius. */
    public static final ConfigSpec SPEC = ConfigSpec.builder()
            .redstone().sides()
            .tuning("config.echoes.tuning.radius", 1, 8, 1, 4)
            .build();

    private final ResonanceStorage buffer = new ResonanceStorage(BUFFER);
    private final BlockConfig config = new BlockConfig();
    private int timer;

    public RadiatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADIATOR, pos, state);
        config.applyDefaults(SPEC);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, RadiatorBlockEntity be) {
        if (!(world instanceof ServerLevel sw)) return;

        boolean powered = sw.isReceivingRedstonePower(pos);
        boolean active = be.buffer.getAmount() >= COST && be.config.redstone().allows(powered);
        int hRadius = be.config.tuningA();
        if (state.contains(BlockStateProperties.LIT) && state.get(BlockStateProperties.LIT) != active) {
            sw.setBlockState(pos, state.with(BlockStateProperties.LIT, active), Block.NOTIFY_ALL);
        }
        if (++be.timer < INTERVAL) return;
        be.timer = 0;
        if (!active) return;

        RandomSource rng = sw.getRandom();
        for (int i = 0; i < TRIES; i++) {
            BlockPos p = pos.add(rng.nextInt(hRadius * 2 + 1) - hRadius,
                    rng.nextInt(V_RADIUS * 2 + 1) - V_RADIUS,
                    rng.nextInt(hRadius * 2 + 1) - hRadius);
            BlockState s = sw.getBlockState(p);
            if (s.getBlock() instanceof BonemealableBlock f
                    && f.isFertilizable(sw, p, s) && f.canGrow(sw, rng, p, s)) {
                f.grow(sw, rng, p, s);
                sw.syncWorldEvent(2005, p, 0); // bonemeal particles
                be.buffer.extract(COST, false);
                be.markDirty();
                break;
            }
        }
    }

    // --- ResonanceNode (CONSUMER) ---
    @Override public int roleMask() { return NodeRole.of(NodeRole.CONSUMER); }
    @Override public long extract(long max, boolean simulate) { return 0; }
    @Override public long insert(long max, boolean simulate) { return buffer.insert(max, simulate); }
    @Override public long demand() { return buffer.getCapacity() - buffer.getAmount(); }
    @Override public int transferCap() { return 0; }
    @Override public BlockPos pos() { return getBlockPos(); }
    @Override public long storedRu() { return buffer.getAmount(); }
    @Override public long capacityRu() { return buffer.getCapacity(); }

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
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        buffer.readNbt(nbt);
        config.readNbt(nbt);
    }
}
