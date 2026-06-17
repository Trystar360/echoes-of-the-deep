package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * A higher-throughput conduit. Identical wiring to {@link ConduitBlockEntity} but
 * contributes a much larger per-tick budget to its network — for feeding many or
 * hungry consumers without laying huge conduit bundles.
 */
public class DenseConduitBlockEntity extends ConduitBlockEntity {
    public static final int DENSE_TRANSFER = 16_000;

    public DenseConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DENSE_CONDUIT, pos, state);
    }

    @Override public int transferCap() { return DENSE_TRANSFER; }
}
