package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.block.entity.ResonantChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A storage block that is natively on a channel — senders fill it, receivers drain
 * it, no separate relay needed. Empty-hand right-click opens it; dye/sneak tune
 * the channel.
 */
public class ResonantChestBlock extends AbstractHorizontalDeviceBlock {

    public ResonantChestBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantChestBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult onConfigure(Level world, BlockPos pos, Player player,
                                       AbstractChannelDeviceBlockEntity device, ItemStack held) {
        if (device instanceof MenuProvider factory) {
            player.openHandledScreen(factory);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof ResonantChestBlockEntity be) {
                Containers.spawn(world, pos, be.getItems());
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
