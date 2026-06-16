package com.echoes.item;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;

/**
 * Copies a channel between wireless devices without juggling dyes. Sneak +
 * right-click a device to copy its channel into the tuner; right-click another
 * device to paste it.
 */
public class FrequencyTunerItem extends Item {

    public FrequencyTunerItem(Settings settings) {
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

        ItemStack tuner = context.getStack();
        if (player.isSneaking()) {
            store(tuner, device.channel());
            player.sendMessage(Text.translatable("message.echoes.tuner.copied", colorName(device.channel())), true);
        } else {
            device.setChannel(read(tuner));
            player.sendMessage(Text.translatable("message.echoes.tuner.pasted", colorName(device.channel())), true);
        }
        return ActionResult.SUCCESS;
    }

    private static int read(ItemStack stack) {
        NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
        return comp == null ? 0 : comp.copyNbt().getInt("channel");
    }

    private static void store(ItemStack stack, int channel) {
        NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = comp == null ? new NbtCompound() : comp.copyNbt();
        nbt.putInt("channel", channel);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    private static Text colorName(int channel) {
        return Text.translatable("color.minecraft." + DyeColor.byId(channel).getName());
    }
}
