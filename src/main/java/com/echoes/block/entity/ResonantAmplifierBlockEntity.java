package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/** Widens the per-tick transfer budget of its channel. Each one doubles it (capped). */
public class ResonantAmplifierBlockEntity extends AbstractChannelDeviceBlockEntity {

    public ResonantAmplifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANT_AMPLIFIER, pos, state);
    }

    @Override public boolean isAmplifier() { return true; }
}
