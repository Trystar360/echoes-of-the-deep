package com.echoes.item;

import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Handheld diagnostics. Right-click any Resonance device to read its role, stored
 * RU / capacity, live demand, and conduit throughput — RU is otherwise invisible
 * except via comparators.
 */
public class ResonanceMeterItem extends Item {

    public ResonanceMeterItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (!(ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof ResonanceNode node)) {
            return InteractionResult.PASS;
        }

        Component name = ctx.getLevel().getBlockState(ctx.getClickedPos()).getBlock().getName();
        player.sendSystemMessage(Component.translatable("message.echoes.meter.header", name, roles(node)));

        if (node.capacityRu() > 0) {
            long stored = node.storedRu(), cap = node.capacityRu();
            int pct = (int) Math.round(100.0 * stored / cap);
            player.sendSystemMessage(Component.translatable("message.echoes.meter.stored",
                    fmt(stored), fmt(cap), pct));
        }
        if (node.is(NodeRole.CONSUMER) && node.demand() > 0) {
            player.sendSystemMessage(Component.translatable("message.echoes.meter.demand", fmt(node.demand())));
        }
        if (node.is(NodeRole.CONDUIT) && node.transferCap() > 0) {
            player.sendSystemMessage(Component.translatable("message.echoes.meter.throughput", fmt(node.transferCap())));
        }
        return InteractionResult.SUCCESS;
    }

    private static String roles(ResonanceNode node) {
        List<String> r = new ArrayList<>();
        if (node.is(NodeRole.PROVIDER)) r.add("Generator");
        if (node.is(NodeRole.CONSUMER)) r.add("Drawer");
        if (node.is(NodeRole.STORAGE)) r.add("Resonance Cell");
        if (node.is(NodeRole.CONDUIT)) r.add("Conductor");
        return String.join(", ", r);
    }

    private static String fmt(long value) {
        return String.format("%,d", value);
    }
}
