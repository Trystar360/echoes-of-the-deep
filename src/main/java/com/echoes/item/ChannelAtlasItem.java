package com.echoes.item;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.wireless.WirelessNetworkManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

/**
 * Read-out tool for wireless networks. Right-click a device to print its channel
 * roster; right-click the air for an overview of every active channel.
 */
public class ChannelAtlasItem extends Item {

    public ChannelAtlasItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        if (context.getWorld().isClient) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (!(context.getWorld().getBlockEntity(context.getBlockPos())
                instanceof AbstractChannelDeviceBlockEntity device)) {
            return InteractionResult.PASS;
        }
        int ch = device.channel();
        int[] r = WirelessNetworkManager.channelRoster(ch);
        player.sendMessage(Component.translatable("message.echoes.atlas.roster",
                colorName(ch), r[0], r[1], r[2], r[3]), false);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClient) {
            int[] counts = WirelessNetworkManager.channelCounts();
            boolean any = false;
            user.sendMessage(Component.translatable("message.echoes.atlas.header"), false);
            for (int ch = 0; ch < counts.length; ch++) {
                if (counts[ch] == 0) continue;
                any = true;
                user.sendMessage(Component.translatable("message.echoes.atlas.line", colorName(ch), counts[ch]), false);
            }
            if (!any) user.sendMessage(Component.translatable("message.echoes.atlas.empty"), false);
        }
        return InteractionResult.SUCCESS;
    }

    private static Component colorName(int channel) {
        return Component.translatable("color.minecraft." + DyeColor.byId(channel).getName());
    }
}
