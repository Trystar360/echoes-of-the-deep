package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/** Pools its channel across every dimension it appears in (cross-dimension transport). */
public class EchoRepeaterBlockEntity extends AbstractChannelDeviceBlockEntity {

    public EchoRepeaterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ECHO_REPEATER, pos, state);
    }

    @Override public boolean isRepeater() { return true; }
}
