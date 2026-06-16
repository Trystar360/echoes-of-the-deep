package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.block.entity.ResonantChestBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A storage block that is natively on a channel — senders fill it, receivers drain
 * it, no separate relay needed. Empty-hand right-click opens it; dye/sneak tune
 * the channel.
 */
public class ResonantChestBlock extends AbstractChannelDeviceBlock {

    public ResonantChestBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantChestBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onConfigure(World world, BlockPos pos, PlayerEntity player,
                                       AbstractChannelDeviceBlockEntity device, ItemStack held) {
        if (device instanceof NamedScreenHandlerFactory factory) {
            player.openHandledScreen(factory);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof ResonantChestBlockEntity be) {
                ItemScatterer.spawn(world, pos, be.getItems());
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
