package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
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
 * Shared behaviour for the wireless gadget family: a uniform tuning scheme —
 * <ul>
 *   <li>right-click with any <b>dye</b> → tune to that colour's channel</li>
 *   <li><b>sneak</b> + right-click (empty hand) → step the channel forward</li>
 *   <li>right-click otherwise → {@link #onConfigure device-specific action}</li>
 * </ul>
 * No {@link net.minecraft.world.level.block.entity.BlockEntityTicker} is
 * registered for this family — {@link AbstractChannelDeviceBlockEntity#onLoad()}
 * joins the channel roster the moment the block entity becomes part of a loaded
 * level, so there's nothing left for a ticker to poll for.
 */
public abstract class AbstractChannelDeviceBlock extends Block implements EntityBlock {

    protected AbstractChannelDeviceBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected void setPlacedBy(Level world, BlockPos pos, BlockState state,
            net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        com.echoes.config.Configurable.claimOnPlace(world, pos, placer);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof AbstractChannelDeviceBlockEntity device)) return InteractionResult.PASS;

        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof DyeItem) {
            net.minecraft.world.item.DyeColor color = held.get(net.minecraft.core.component.DataComponents.DYE);
            if (color != null) {
                device.setChannel(color.getId());
                sendChannel(player, device.channel());
                return InteractionResult.SUCCESS;
            }
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
