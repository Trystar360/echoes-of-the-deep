package com.echoes.item;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.wireless.WirelessNetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Read-out tool for wireless networks. Right-click a device to print its channel
 * roster; right-click the air for an overview of every active channel.
 */
public class ChannelAtlasItem extends Item {

    public ChannelAtlasItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().isClient) return ActionResult.SUCCESS;
        PlayerEntity player = context.getPlayer();
        if (player == null) return ActionResult.PASS;
        if (!(context.getWorld().getBlockEntity(context.getBlockPos())
                instanceof AbstractChannelDeviceBlockEntity device)) {
            return ActionResult.PASS;
        }
        int ch = device.channel();
        int[] r = WirelessNetworkManager.channelRoster(ch);
        player.sendMessage(Text.translatable("message.echoes.atlas.roster",
                colorName(ch), r[0], r[1], r[2], r[3]), false);
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            int[] counts = WirelessNetworkManager.channelCounts();
            boolean any = false;
            user.sendMessage(Text.translatable("message.echoes.atlas.header"), false);
            for (int ch = 0; ch < counts.length; ch++) {
                if (counts[ch] == 0) continue;
                any = true;
                user.sendMessage(Text.translatable("message.echoes.atlas.line", colorName(ch), counts[ch]), false);
            }
            if (!any) user.sendMessage(Text.translatable("message.echoes.atlas.empty"), false);
        }
        return ActionResult.SUCCESS;
    }

    private static Text colorName(int channel) {
        return Text.translatable("color.minecraft." + DyeColor.byId(channel).getName());
    }
}
