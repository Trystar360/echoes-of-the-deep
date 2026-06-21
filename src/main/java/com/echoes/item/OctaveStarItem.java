package com.echoes.item;

import com.echoes.registry.ModComponents;
import com.echoes.transmute.TransmutationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A portable <b>Bound Light</b> battery (the "Klein Star" of this economy). Right-click to
 * charge it from your Bound-Light account; sneak + right-click to pour it back. Carry Light
 * between bases, or hand a charged Star to another player — their sneak-use banks it into
 * <i>their</i> account. Six tiers of growing capacity.
 */
public class OctaveStarItem extends Item {
    private final int tier;
    private final long capacity;

    public OctaveStarItem(int tier, long capacity, Properties settings) {
        super(settings);
        this.tier = tier;
        this.capacity = capacity;
    }

    public static long stored(ItemStack stack) {
        return stack.getOrDefault(ModComponents.STORED_LIGHT, 0L);
    }

    private void setStored(ItemStack stack, long value) {
        stack.set(ModComponents.STORED_LIGHT, Math.max(0L, Math.min(capacity, value)));
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient || !(world instanceof ServerLevel sw)) return InteractionResult.SUCCESS;

        TransmutationState state = TransmutationState.get(sw);
        TransmutationState.Account account = state.of(user.getUuid());
        long cur = stored(stack);

        if (user.isSneaking()) {
            if (cur <= 0) return InteractionResult.PASS;       // discharge → account
            account.light += cur;
            setStored(stack, 0);
            state.markDirty();
            user.sendMessage(Component.translatable("message.echoes.star.emptied", fmt(cur)), true);
        } else {
            long move = Math.min(capacity - cur, account.light); // charge ← account
            if (move <= 0) return InteractionResult.PASS;
            account.light -= move;
            setStored(stack, cur + move);
            state.markDirty();
            user.sendMessage(Component.translatable("message.echoes.star.charged", fmt(cur + move), fmt(capacity)), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("tooltip.echoes.star.stored", fmt(stored(stack)), fmt(capacity))
                .formatted(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.echoes.star.hint").formatted(ChatFormatting.DARK_GRAY));
    }

    @Override public boolean isItemBarVisible(ItemStack stack) { return stored(stack) > 0; }
    @Override public int getItemBarStep(ItemStack stack) {
        return (int) Math.round(13.0 * stored(stack) / capacity);
    }
    @Override public int getItemBarColor(ItemStack stack) { return 0x36E2D4; } // resonance teal

    private static String fmt(long value) { return String.format("%,d", value); }
}
