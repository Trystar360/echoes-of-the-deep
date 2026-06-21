package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.block.entity.HarmonicFilterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Whitelists transport on its channel via a 3×3 ghost-slot screen. Empty-hand
 * right-click opens it; dye/sneak still tune the channel.
 */
public class HarmonicFilterBlock extends AbstractHorizontalDeviceBlock {

    public HarmonicFilterBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HarmonicFilterBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult onConfigure(Level world, BlockPos pos, Player player,
                                       AbstractChannelDeviceBlockEntity device, ItemStack held) {
        if (device instanceof MenuProvider factory) {
            player.openHandledScreen(factory);
        }
        return InteractionResult.SUCCESS;
    }
}
