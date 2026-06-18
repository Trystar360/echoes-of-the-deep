package com.echoes.block.entity;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Radiation — the centrifugal, expansive half of the two-way universe. Draws Light
 * from the grid and pours it back into the world as life, accelerating nearby crops
 * and saplings (a powered bonemeal aura) and glowing while charged. The discharge
 * counterpart to the Generative Coil.
 */
public class RadiatorBlockEntity extends BlockEntity implements ResonanceNode {
    private static final long BUFFER = 3_000;
    private static final long COST = 300;       // Light per growth
    private static final int INTERVAL = 10;     // ticks between attempts
    private static final int H_RADIUS = 4, V_RADIUS = 2, TRIES = 8;

    private final ResonanceStorage buffer = new ResonanceStorage(BUFFER);
    private int timer;

    public RadiatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADIATOR, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, RadiatorBlockEntity be) {
        if (!(world instanceof ServerWorld sw)) return;

        boolean active = be.buffer.getAmount() >= COST;
        if (state.contains(Properties.LIT) && state.get(Properties.LIT) != active) {
            sw.setBlockState(pos, state.with(Properties.LIT, active), Block.NOTIFY_ALL);
        }
        if (++be.timer < INTERVAL) return;
        be.timer = 0;
        if (!active) return;

        Random rng = sw.getRandom();
        for (int i = 0; i < TRIES; i++) {
            BlockPos p = pos.add(rng.nextInt(H_RADIUS * 2 + 1) - H_RADIUS,
                    rng.nextInt(V_RADIUS * 2 + 1) - V_RADIUS,
                    rng.nextInt(H_RADIUS * 2 + 1) - H_RADIUS);
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

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        buffer.writeNbt(nbt);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        buffer.readNbt(nbt);
    }
}
