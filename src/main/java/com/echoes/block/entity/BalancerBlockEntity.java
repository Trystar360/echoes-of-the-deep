package com.echoes.block.entity;

import com.echoes.energy.ResonanceNetworkManager;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Rhythmic balanced interchange made a block: every few ticks it nudges all storage
 * on the adjacent network toward the same fill ratio, so the grid breathes evenly
 * and no Accumulator hoards. Place it against a Wave Conduit.
 */
public class BalancerBlockEntity extends BlockEntity {
    private static final int INTERVAL = 10;
    private static final long RATE = 2_000;

    private int timer;

    public BalancerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BALANCER, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BalancerBlockEntity be) {
        if (!(world instanceof ServerWorld sw)) return;
        if (++be.timer < INTERVAL) return;
        be.timer = 0;
        ResonanceNetworkManager.get(sw).balanceAround(pos, RATE);
    }
}
