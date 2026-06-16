package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.block.entity.HarmonicFilterBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Whitelists item types on its channel. Right-click with an item to add its type;
 * empty-hand right-click clears the list. (Dye/sneak still tune the channel.)
 */
public class HarmonicFilterBlock extends AbstractChannelDeviceBlock {

    public HarmonicFilterBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HarmonicFilterBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onConfigure(World world, BlockPos pos, PlayerEntity player,
                                       AbstractChannelDeviceBlockEntity device, ItemStack held) {
        if (device instanceof HarmonicFilterBlockEntity filter) {
            if (held.isEmpty()) {
                filter.clearFilter();
                sendStatus(player, "message.echoes.filter.cleared");
            } else {
                int size = filter.addItem(held.getItem());
                if (size < 0) {
                    sendStatus(player, "message.echoes.filter.full");
                } else {
                    sendStatus(player, "message.echoes.filter.added", held.getName(), size);
                }
            }
        }
        return ActionResult.SUCCESS;
    }
}
