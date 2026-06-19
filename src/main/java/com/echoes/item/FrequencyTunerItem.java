package com.echoes.item;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.config.Configurable;
import com.echoes.screen.ConfigScreenFactory;
import net.minecraft.block.entity.BlockEntity;
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
 * The device wrench. Right-click any configurable device to open its
 * configuration screen (channel/octave, redstone, per-face I/O and tuning).
 * Sneak + right-click a wireless device to copy its channel into the tuner.
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
        BlockEntity be = context.getWorld().getBlockEntity(context.getBlockPos());
        ItemStack tuner = context.getStack();

        // Sneak on a wireless device still does the quick channel-copy.
        if (player.isSneaking() && be instanceof AbstractChannelDeviceBlockEntity device) {
            store(tuner, device.channel());
            player.sendMessage(Text.translatable("message.echoes.tuner.copied", colorName(device.channel())), true);
            return ActionResult.SUCCESS;
        }

        // Otherwise, open the configuration screen for any configurable device.
        if (be instanceof Configurable cfg) {
            player.openHandledScreen(new ConfigScreenFactory(cfg, context.getBlockPos().toImmutable()));
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
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
