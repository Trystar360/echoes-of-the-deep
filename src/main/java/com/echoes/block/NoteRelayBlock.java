package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.block.entity.NoteRelayBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Wireless redstone bus. SEND broadcasts the redstone power it receives onto its
 * channel; RECEIVE emits the channel's strongest broadcast. Empty-hand right-click
 * cycles the mode; dye/sneak tune the channel.
 */
public class NoteRelayBlock extends AbstractChannelDeviceBlock {

    public NoteRelayBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(Properties.POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.POWERED);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NoteRelayBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onConfigure(World world, BlockPos pos, PlayerEntity player,
                                       AbstractChannelDeviceBlockEntity device, ItemStack held) {
        if (device instanceof NoteRelayBlockEntity note) {
            note.cycleMode();
            sendStatus(player, "message.echoes.relay.mode",
                    Text.translatable("message.echoes.relay.mode." + note.mode().name().toLowerCase()));
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return world.getBlockEntity(pos) instanceof NoteRelayBlockEntity note ? note.redstonePower() : 0;
    }
}
