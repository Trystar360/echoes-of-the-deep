package com.echoes.item;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.config.Configurable;
import com.echoes.screen.ConfigScreenFactory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;

/**
 * The device wrench. Right-click any configurable device to open its
 * configuration screen (channel/octave, redstone, per-face I/O and tuning).
 * Sneak + right-click a wireless device to copy its channel into the tuner.
 */
public class FrequencyTunerItem extends Item {

    public FrequencyTunerItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        if (context.getWorld().isClient) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        BlockEntity be = context.getWorld().getBlockEntity(context.getBlockPos());
        ItemStack tuner = context.getStack();

        // Sneak on a wireless device still does the quick channel-copy.
        if (player.isSneaking() && be instanceof AbstractChannelDeviceBlockEntity device) {
            store(tuner, device.channel());
            player.sendMessage(Component.translatable("message.echoes.tuner.copied", colorName(device.channel())), true);
            return InteractionResult.SUCCESS;
        }

        // Otherwise, open the configuration screen for any configurable device.
        if (be instanceof Configurable cfg) {
            player.openHandledScreen(new ConfigScreenFactory(cfg, context.getBlockPos().toImmutable()));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static void store(ItemStack stack, int channel) {
        CustomData comp = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = comp == null ? new CompoundTag() : comp.copyNbt();
        nbt.putInt("channel", channel);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }

    private static Component colorName(int channel) {
        return Component.translatable("color.minecraft." + DyeColor.byId(channel).getName());
    }
}
