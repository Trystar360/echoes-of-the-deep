package com.echoes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * A channel device that orients horizontally toward the player when placed, so its
 * glowing "front" face reads correctly in the world. Used by every gadget except
 * the Resonant Relay, which tracks a full 6-way {@code FACING} (it points into the
 * block it wraps).
 */
public abstract class AbstractHorizontalDeviceBlock extends AbstractChannelDeviceBlock {

    protected AbstractHorizontalDeviceBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
}
