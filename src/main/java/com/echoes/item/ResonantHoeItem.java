package com.echoes.item;

import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** A Resonant hoe — tuned hard, per Walter Russell's balanced interchange. */
public class ResonantHoeItem extends HoeItem {
    public ResonantHoeItem(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.echoes.resonant.lore").formatted(Formatting.AQUA, Formatting.ITALIC));
    }
}
