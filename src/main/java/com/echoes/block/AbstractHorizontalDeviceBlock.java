package com.echoes.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * A channel device that orients horizontally toward the player when placed, so its
 * glowing "front" face reads correctly in the world. Used by every gadget except
 * the Resonant Relay, which tracks a full 6-way {@code FACING} (it points into the
 * block it wraps).
 */
public abstract class AbstractHorizontalDeviceBlock extends AbstractChannelDeviceBlock {

    protected AbstractHorizontalDeviceBlock(Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite());
    }
}
