package com.echoes.item;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

/** A Resonant hoe — tuned hard, per Walter Russell's balanced interchange. */
public class ResonantHoeItem extends HoeItem {
    public ResonantHoeItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("tooltip.echoes.resonant.lore").formatted(ChatFormatting.AQUA, ChatFormatting.ITALIC));
    }
}
