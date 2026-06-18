package com.echoes.item;

import com.echoes.energy.ResonanceNode;
import com.echoes.registry.ModComponents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Sound-powered flight. Hold right-click to thrust upward (and negate fall damage)
 * while spending RU from the device's internal buffer; sprinting adds a forward
 * boost. Recharge by right-clicking a Resonator, Capacitor, or Conduit Coupler —
 * it siphons RU out of that block.
 */
public class ResonanceThrustersItem extends Item {
    // Tuned strong on purpose — "rhythmic balanced interchange": the device gives
    // back as freely as the grid pours in. Huge reserve, cheap to fly, very fast.
    public static final int CAPACITY = 1_000_000;
    private static final int DRAIN_PER_TICK = 8;
    private static final int RECHARGE_PER_USE = 200_000;
    private static final double FLY_SPEED = 0.85, SPRINT_SPEED = 1.45, LIFT = 0.06;

    public ResonanceThrustersItem(Settings settings) {
        super(settings);
    }

    public static int ru(ItemStack stack) { return stack.getOrDefault(ModComponents.STORED_RU, 0); }

    /** True if this living entity is a player carrying charged thrusters — no fall damage. */
    public static boolean shieldsFall(net.minecraft.entity.LivingEntity entity) {
        if (!(entity instanceof PlayerEntity p)) return false;
        for (ItemStack s : p.getInventory().main) {
            if (s.getItem() instanceof ResonanceThrustersItem && ru(s) > 0) return true;
        }
        return p.getOffHandStack().getItem() instanceof ResonanceThrustersItem
                && ru(p.getOffHandStack()) > 0;
    }
    private static void setRu(ItemStack stack, int value) {
        stack.set(ModComponents.STORED_RU, Math.max(0, Math.min(CAPACITY, value)));
    }

    /** Right-click a buffered Resonance block to siphon RU into the thrusters. */
    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World world = ctx.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(world.getBlockEntity(ctx.getBlockPos()) instanceof ResonanceNode node) || node.storedRu() <= 0) {
            return ActionResult.PASS;
        }
        ItemStack stack = ctx.getStack();
        int space = CAPACITY - ru(stack);
        if (space <= 0) return ActionResult.SUCCESS;
        long pulled = node.extract(Math.min(space, RECHARGE_PER_USE), false);
        if (pulled > 0) {
            setRu(stack, ru(stack) + (int) pulled);
            if (node instanceof BlockEntity be) be.markDirty();
            PlayerEntity p = ctx.getPlayer();
            if (p != null) p.sendMessage(Text.translatable("message.echoes.thrusters.charge",
                    fmt(ru(stack)), fmt(CAPACITY)), true);
        }
        return ActionResult.SUCCESS;
    }

    /** Begin thrusting (continuous use) if there's charge. */
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (ru(stack) <= 0 && !user.isCreative()) return ActionResult.FAIL;
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override public UseAction getUseAction(ItemStack stack) { return UseAction.BOW; }
    @Override public int getMaxUseTime(ItemStack stack, LivingEntity user) { return 72_000; }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        boolean creative = user instanceof PlayerEntity p && p.isCreative();
        if (ru(stack) <= 0 && !creative) {
            user.stopUsingItem();
            return;
        }
        // Full directional flight: ride wherever you look. Sneak hovers in place.
        double speed = user.isSprinting() ? SPRINT_SPEED : FLY_SPEED;
        if (user.isSneaking()) {
            user.setVelocity(user.getVelocity().multiply(0.6, 0.0, 0.6)); // hover/brake
        } else {
            Vec3d look = user.getRotationVector();
            user.setVelocity(look.x * speed, look.y * speed + LIFT, look.z * speed);
        }
        user.velocityModified = true;
        user.fallDistance = 0;

        if (!world.isClient && !creative) {
            setRu(stack, ru(stack) - DRAIN_PER_TICK);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.echoes.thrusters.charge", fmt(ru(stack)), fmt(CAPACITY))
                .formatted(Formatting.AQUA));
        tooltip.add(Text.translatable("tooltip.echoes.thrusters.hint").formatted(Formatting.DARK_GRAY));
    }

    private static String fmt(int value) { return String.format("%,d", value); }
}
