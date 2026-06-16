package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.block.entity.ResonantSplitterBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** Toggles its channel between even round-robin sharing and fill-first delivery. */
public class ResonantSplitterBlock extends AbstractHorizontalDeviceBlock {

    public ResonantSplitterBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantSplitterBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onConfigure(World world, BlockPos pos, PlayerEntity player,
                                       AbstractChannelDeviceBlockEntity device, ItemStack held) {
        if (device instanceof ResonantSplitterBlockEntity splitter) {
            boolean rr = splitter.toggle();
            sendStatus(player, rr ? "message.echoes.splitter.round_robin" : "message.echoes.splitter.fill_first");
        }
        return ActionResult.SUCCESS;
    }
}
