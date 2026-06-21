package com.echoes.block;

import com.echoes.block.entity.EchoRepeaterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/** Pools its channel across every dimension it appears in (cross-dimension transport). */
public class EchoRepeaterBlock extends AbstractHorizontalDeviceBlock {

    public EchoRepeaterBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EchoRepeaterBlockEntity(pos, state);
    }
}
