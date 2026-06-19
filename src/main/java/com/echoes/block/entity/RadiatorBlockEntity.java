package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.energy.ResonanceStorage;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

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

    public static void tick(World world, BlockPos pos, BlockState state, RadiatorBlockEntity be) {
        if (!(world instanceof ServerWorld sw)) return;

        boolean powered = sw.isReceivingRedstonePower(pos);
        boolean active = be.buffer.getAmount() >= COST && be.config.redstone().allows(powered);
        int hRadius = be.config.tuningA();
        if (state.contains(Properties.LIT) && state.get(Properties.LIT) != active) {
            sw.setBlockState(pos, state.with(Properties.LIT, active), Block.NOTIFY_ALL);
        }
        if (++be.timer < INTERVAL) return;
        be.timer = 0;
        if (!active) return;

        Random rng = sw.getRandom();
        for (int i = 0; i < TRIES; i++) {
            BlockPos p = pos.add(rng.nextInt(hRadius * 2 + 1) - hRadius,
                    rng.nextInt(V_RADIUS * 2 + 1) - V_RADIUS,
                    rng.nextInt(hRadius * 2 + 1) - hRadius);
            BlockState s = sw.getBlockState(p);
            if (s.getBlock() instanceof Fertilizable f
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
    @Override public BlockPos pos() { return getPos(); }
    @Override public long storedRu() { return buffer.getAmount(); }
    @Override public long capacityRu() { return buffer.getCapacity(); }

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Text configTitle() { return getCachedState().getBlock().getName(); }
    @Override public void onConfigChanged() { markDirty(); }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        buffer.writeNbt(nbt);
        config.writeNbt(nbt);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        buffer.readNbt(nbt);
        config.readNbt(nbt);
    }
}
