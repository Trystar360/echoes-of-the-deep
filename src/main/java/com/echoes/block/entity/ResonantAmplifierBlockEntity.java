package com.echoes.block.entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

/** Widens the per-tick transfer budget of its channel. Each one doubles it (capped). */
public class ResonantAmplifierBlockEntity extends AbstractChannelDeviceBlockEntity {

    public ResonantAmplifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANT_AMPLIFIER, pos, state);
    }

    @Override public boolean isAmplifier() { return true; }
}
