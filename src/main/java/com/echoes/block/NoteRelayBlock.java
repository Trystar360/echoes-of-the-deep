package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.block.entity.NoteRelayBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Wireless redstone bus. SEND broadcasts the redstone power it receives onto its
 * channel; RECEIVE emits the channel's strongest broadcast. Empty-hand right-click
 * cycles the mode; dye/sneak tune the channel.
 */
public class NoteRelayBlock extends AbstractHorizontalDeviceBlock {

    public NoteRelayBlock(Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, net.minecraft.core.Direction.NORTH)
                .setValue(BlockStateProperties.POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.POWERED);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NoteRelayBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult onConfigure(Level world, BlockPos pos, Player player,
                                       AbstractChannelDeviceBlockEntity device, ItemStack held) {
        if (device instanceof NoteRelayBlockEntity note) {
            note.cycleMode();
            sendStatus(player, "message.echoes.relay.mode",
                    Component.translatable("message.echoes.relay.mode." + note.mode().name().toLowerCase()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return world.getBlockEntity(pos) instanceof NoteRelayBlockEntity note ? note.redstonePower() : 0;
    }
}
