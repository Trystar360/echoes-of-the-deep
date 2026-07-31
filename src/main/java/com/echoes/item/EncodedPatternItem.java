package com.echoes.item;

import com.echoes.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Encoded Pattern — the AE2-style pattern, distilled. A 3x3 item layout saved
 * onto a card: right-click a Resonant Fabricator with a Blank Pattern to save
 * its current grid; right-click with an Encoded Pattern to load the saved
 * layout into the machine's template memory. A fabricator with a template
 * keeps its grid slots restocked from adjacent inventories (and, through a
 * Resonant Chest, from the whole wireless network) — the grid stops being
 * storage and becomes a pure recipe declaration, which is the part of AE2's
 * Pattern Provider that actually mattered.
 *
 * <p>The layout is stored as nine item ids (keys {@code g0}..{@code g8},
 * row-major) in the item's custom data component. Empty slots simply have no
 * key. Stack size 1: a pattern is a card, not a container.
 */
public class EncodedPatternItem extends Item {

    public static final int SLOTS = 9;

    public EncodedPatternItem(Properties settings) {
        super(settings);
    }

    private static String key(int slot) { return "g" + slot; }

    /** Build an encoded pattern from a 9-slot grid (empty slots are omitted). */
    public static ItemStack encode(List<ItemStack> grid) {
        if (grid.size() < SLOTS) return ItemStack.EMPTY;
        boolean any = false;
        for (int i = 0; i < SLOTS; i++) if (!grid.get(i).isEmpty()) { any = true; break; }
        if (!any) return ItemStack.EMPTY;
        ItemStack out = new ItemStack(ModItems.ENCODED_PATTERN);
        CustomData.update(DataComponents.CUSTOM_DATA, out, tag -> {
            for (int i = 0; i < SLOTS; i++) {
                ItemStack s = grid.get(i);
                if (!s.isEmpty()) {
                    tag.putString(key(i), BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
                }
            }
        });
        return out;
    }

    /**
     * The saved layout as nine item ids ("" = empty slot), or null if this
     * stack carries no pattern data.
     */
    public static List<String> layout(ItemStack pattern) {
        CustomData data = pattern.get(DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) return null;
        CompoundTag tag = data.copyTag();
        List<String> out = new ArrayList<>(SLOTS);
        boolean any = false;
        for (int i = 0; i < SLOTS; i++) {
            String id = tag.getStringOr(key(i), "");
            if (!id.isEmpty()) any = true;
            out.add(id);
        }
        return any ? out : null;
    }

    public static boolean isEncoded(ItemStack stack) {
        return stack.getItem() instanceof EncodedPatternItem && layout(stack) != null;
    }

    /** Count of non-empty slots in the saved layout (0 if unencoded). */
    public static int filledSlots(ItemStack pattern) {
        List<String> layout = layout(pattern);
        if (layout == null) return 0;
        int n = 0;
        for (String id : layout) if (!id.isEmpty()) n++;
        return n;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag type) {
        List<String> layout = layout(stack);
        if (layout == null) {
            tooltip.accept(Component.translatable("tooltip.echoes.pattern.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        tooltip.accept(Component.translatable("tooltip.echoes.pattern.slots", filledSlots(stack))
                .withStyle(ChatFormatting.AQUA));
        int shown = 0;
        for (String id : layout) {
            if (id.isEmpty() || shown >= 3) continue;
            var item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id));
            if (item.isPresent()) {
                tooltip.accept(Component.literal(" - ")
                        .append(Component.translatable(item.get().getDescriptionId()))
                        .withStyle(ChatFormatting.DARK_GRAY));
                shown++;
            }
        }
        if (filledSlots(stack) > shown) {
            tooltip.accept(Component.translatable("tooltip.echoes.pattern.more", filledSlots(stack) - shown)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.accept(Component.translatable("tooltip.echoes.pattern.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
