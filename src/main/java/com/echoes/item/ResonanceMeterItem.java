package com.echoes.item;

import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Handheld diagnostics. Right-click any Resonance device to read its role, stored
 * RU / capacity, live demand, and conduit throughput — RU is otherwise invisible
 * except via comparators.
 */
public class ResonanceMeterItem extends Item {

    public ResonanceMeterItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        if (ctx.getWorld().isClient) return ActionResult.SUCCESS;
        PlayerEntity player = ctx.getPlayer();
        if (player == null) return ActionResult.PASS;
        if (!(ctx.getWorld().getBlockEntity(ctx.getBlockPos()) instanceof ResonanceNode node)) {
            return ActionResult.PASS;
        }

        Text name = ctx.getWorld().getBlockState(ctx.getBlockPos()).getBlock().getName();
        player.sendMessage(Text.translatable("message.echoes.meter.header", name, roles(node)), false);

        if (node.capacityRu() > 0) {
            long stored = node.storedRu(), cap = node.capacityRu();
            int pct = (int) Math.round(100.0 * stored / cap);
            player.sendMessage(Text.translatable("message.echoes.meter.stored",
                    fmt(stored), fmt(cap), pct), false);
        }
        if (node.is(NodeRole.CONSUMER) && node.demand() > 0) {
            player.sendMessage(Text.translatable("message.echoes.meter.demand", fmt(node.demand())), false);
        }
        if (node.is(NodeRole.CONDUIT) && node.transferCap() > 0) {
            player.sendMessage(Text.translatable("message.echoes.meter.throughput", fmt(node.transferCap())), false);
        }
        return ActionResult.SUCCESS;
    }

    private static String roles(ResonanceNode node) {
        List<String> r = new ArrayList<>();
        if (node.is(NodeRole.PROVIDER)) r.add("Generator");
        if (node.is(NodeRole.CONSUMER)) r.add("Drawer");
        if (node.is(NodeRole.STORAGE)) r.add("Accumulator");
        if (node.is(NodeRole.CONDUIT)) r.add("Conductor");
        return String.join(", ", r);
    }

    private static String fmt(long value) {
        return String.format("%,d", value);
    }
}
