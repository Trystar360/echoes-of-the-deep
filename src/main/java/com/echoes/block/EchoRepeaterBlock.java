package com.echoes.block;

import com.echoes.block.entity.EchoRepeaterBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/** Pools its channel across every dimension it appears in (cross-dimension transport). */
public class EchoRepeaterBlock extends AbstractChannelDeviceBlock {

    public EchoRepeaterBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EchoRepeaterBlockEntity(pos, state);
    }
}
