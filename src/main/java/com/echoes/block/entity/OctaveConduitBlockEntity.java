package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

/**
 * The highest-octave carrier. Identical wiring to {@link ConduitBlockEntity} and
 * {@link DenseConduitBlockEntity} but contributes a far larger per-tick budget to
 * its network — for feeding whole banks of hungry consumers from a single line.
 */
public class OctaveConduitBlockEntity extends ConduitBlockEntity {
    public static final int OCTAVE_TRANSFER = 64_000;

    public OctaveConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OCTAVE_CONDUIT, pos, state);
    }

    @Override public int transferCap() { return OCTAVE_TRANSFER; }
}
