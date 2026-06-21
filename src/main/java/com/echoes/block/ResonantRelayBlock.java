package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.block.entity.ResonantRelayBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Tune two or more relays to the same channel and they resonate, beaming items,
 * fluids, and RU between the blocks they face — no conduit required. Empty-hand
 * right-click cycles the mode (Receive → Send → Disabled); dye/sneak set the
 * channel (see {@link AbstractChannelDeviceBlock}). Comparator-readable.
 */
public class ResonantRelayBlock extends AbstractChannelDeviceBlock {

    public ResonantRelayBlock(Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(BlockStateProperties.FACING, ctx.getClickedFace().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantRelayBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult onConfigure(Level world, BlockPos pos, Player player,
                                       AbstractChannelDeviceBlockEntity device, ItemStack held) {
        if (device instanceof ResonantRelayBlockEntity relay) {
            relay.cycleMode();
            sendStatus(player, "message.echoes.relay.mode",
                    net.minecraft.network.chat.Component.translatable(
                            "message.echoes.relay.mode." + relay.mode().name().toLowerCase()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, net.minecraft.core.Direction direction) {
        return world.getBlockEntity(pos) instanceof ResonantRelayBlockEntity relay
                ? relay.comparatorOutput() : 0;
    }
}
