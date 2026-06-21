package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.block.entity.ResonantSplitterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Toggles its channel between even round-robin sharing and fill-first delivery. */
public class ResonantSplitterBlock extends AbstractHorizontalDeviceBlock {

    public ResonantSplitterBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantSplitterBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult onConfigure(Level world, BlockPos pos, Player player,
                                       AbstractChannelDeviceBlockEntity device, ItemStack held) {
        if (device instanceof ResonantSplitterBlockEntity splitter) {
            boolean rr = splitter.toggle();
            sendStatus(player, rr ? "message.echoes.splitter.round_robin" : "message.echoes.splitter.fill_first");
        }
        return InteractionResult.SUCCESS;
    }
}
