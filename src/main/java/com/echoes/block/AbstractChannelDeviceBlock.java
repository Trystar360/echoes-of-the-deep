package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Shared behaviour for the wireless gadget family: a server ticker that keeps the
 * block on the channel roster, and a uniform tuning scheme —
 * <ul>
 *   <li>right-click with any <b>dye</b> → tune to that colour's channel</li>
 *   <li><b>sneak</b> + right-click (empty hand) → step the channel forward</li>
 *   <li>right-click otherwise → {@link #onConfigure device-specific action}</li>
 * </ul>
 */
public abstract class AbstractChannelDeviceBlock extends Block implements EntityBlock {

    protected AbstractChannelDeviceBlock(Properties settings) {
        super(settings);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide()) return null;
        return (w, p, s, be) -> {
            if (be instanceof AbstractChannelDeviceBlockEntity d) AbstractChannelDeviceBlockEntity.tick(w, p, s, d);
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof AbstractChannelDeviceBlockEntity device)) return InteractionResult.PASS;

        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof DyeItem dye) {
            device.setChannel(dye.getColor().getId());
            sendChannel(player, device.channel());
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown() && held.isEmpty()) {
            device.cycleChannel();
            sendChannel(player, device.channel());
            return InteractionResult.SUCCESS;
        }
        return onConfigure(world, pos, player, device, held);
    }

    /** Device-specific right-click (empty hand or a non-dye item). Default: report channel. */
    protected InteractionResult onConfigure(Level world, BlockPos pos, Player player,
                                       AbstractChannelDeviceBlockEntity device, ItemStack held) {
        sendChannel(player, device.channel());
        return InteractionResult.SUCCESS;
    }

    protected static void sendChannel(Player player, int channel) {
        DyeColor color = DyeColor.byId(channel);
        player.sendOverlayMessage(Component.translatable("message.echoes.channel",
                Component.translatable("color.minecraft." + color.getName())));
    }

    protected static void sendStatus(Player player, String translationKey, Object... args) {
        player.sendOverlayMessage(Component.translatable(translationKey, args));
    }
}
