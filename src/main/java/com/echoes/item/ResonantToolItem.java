package com.echoes.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.function.Consumer;

/**
 * A Resonant tool — tuned hard, per Walter Russell's balanced interchange.
 *
 * <p>26.1: tool behaviour (pickaxe/axe/sword/…) is supplied via {@link Item.Properties}
 * builders (e.g. {@code .pickaxe(material, dmg, speed)}); the dedicated {@code PickaxeItem}
 * /{@code SwordItem} subclasses were removed. This single class carries the shared lore
 * tooltip for every Resonant tool variant.
 */
public class ResonantToolItem extends Item {
    public ResonantToolItem(Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag type) {
        tooltip.accept(Component.translatable("tooltip.echoes.resonant.lore")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
    }
}
