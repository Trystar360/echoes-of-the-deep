package com.echoes.wireless;

import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Routes items, fluids, and Resonance (RU) between {@code Resonant Relay}s that
 * share a channel — no physical conduit required.
 *
 * <p>One instance per {@link ServerWorld}. Relays register themselves on load /
 * placement and unregister on removal, so the roster is maintained incrementally
 * and the tick path only ever touches channels that actually have both a sender
 * and a receiver. Each channel is bounded by a per-tick transfer budget that
 * scales with the number of senders but is capped, so a huge build can't stall
 * the server tick.
 *
 * <p>This mirrors {@link com.echoes.energy.ResonanceNetworkManager} (in-memory,
 * rebuilt from the world as chunks load). A shipping mod would persist channel
 * assignments via a {@code PersistentState}; the relay's NBT already carries the
 * channel, so re-registration on chunk load restores the roster automatically.
 */
public final class WirelessNetworkManager {
    private static final Map<ServerWorld, WirelessNetworkManager> INSTANCES = new HashMap<>();

    /** 16 channels, one per dye colour. */
    public static final int CHANNELS = 16;

    // Per-tick throughput. "Powerful but cheap": a single pair moves a healthy
    // trickle; more senders widen the pipe, up to a hard ceiling per channel.
    private static final long ITEMS_PER_SENDER = 8,                  MAX_ITEMS = 64;
    private static final long FLUID_PER_SENDER = FluidConstants.BUCKET, MAX_FLUID = 8 * FluidConstants.BUCKET;
    private static final long RU_PER_SENDER    = 1_000,              MAX_RU    = 16_000;

    private final ServerWorld world;
    // channel -> relays on it; plus a flat index for O(1) updates.
    private final List<List<RelayInfo>> byChannel = new ArrayList<>(CHANNELS);
    private final Map<BlockPos, RelayInfo> relays = new HashMap<>();

    private WirelessNetworkManager(ServerWorld world) {
        this.world = world;
        for (int i = 0; i < CHANNELS; i++) byChannel.add(new ArrayList<>());
    }

    public static WirelessNetworkManager get(ServerWorld world) {
        return INSTANCES.computeIfAbsent(world, WirelessNetworkManager::new);
    }

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(world -> get(world).tick());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> INSTANCES.clear());
    }

    // --- roster (called by the relay block entity) ---

    /** Add or update a relay. Idempotent; safe to call every tick from the BE. */
    public void register(BlockPos pos, int channel, RelayMode mode, Direction facing) {
        channel = clampChannel(channel);
        RelayInfo info = relays.get(pos);
        if (info == null) {
            info = new RelayInfo(pos.toImmutable(), channel, mode, facing);
            relays.put(info.pos, info);
            byChannel.get(channel).add(info);
            return;
        }
        if (info.channel != channel) {
            byChannel.get(info.channel).remove(info);
            info.channel = channel;
            byChannel.get(channel).add(info);
        }
        info.mode = mode;
        info.facing = facing;
    }

    public void unregister(BlockPos pos) {
        RelayInfo info = relays.remove(pos);
        if (info != null) byChannel.get(info.channel).remove(info);
    }

    // --- tick ---

    private void tick() {
        for (int ch = 0; ch < CHANNELS; ch++) {
            List<RelayInfo> group = byChannel.get(ch);
            if (group.size() < 2) continue;

            List<RelayInfo> senders = new ArrayList<>();
            List<RelayInfo> receivers = new ArrayList<>();
            for (RelayInfo r : group) {
                if (r.mode == RelayMode.SEND) senders.add(r);
                else if (r.mode == RelayMode.RECEIVE) receivers.add(r);
            }
            if (senders.isEmpty() || receivers.isEmpty()) continue;

            transferItems(senders, receivers);
            transferFluids(senders, receivers);
            transferEnergy(senders, receivers);
        }
    }

    private void transferItems(List<RelayInfo> senders, List<RelayInfo> receivers) {
        long budget = Math.min(MAX_ITEMS, senders.size() * ITEMS_PER_SENDER);
        List<Storage<ItemVariant>> sources = new ArrayList<>();
        List<Storage<ItemVariant>> targets = new ArrayList<>();
        for (RelayInfo r : senders) {
            Storage<ItemVariant> s = ItemStorage.SIDED.find(world, r.attachedPos(), r.facing.getOpposite());
            if (s != null && s.supportsExtraction()) sources.add(s);
        }
        for (RelayInfo r : receivers) {
            Storage<ItemVariant> t = ItemStorage.SIDED.find(world, r.attachedPos(), r.facing.getOpposite());
            if (t != null && t.supportsInsertion()) targets.add(t);
        }
        moveAll(sources, targets, budget);
    }

    private void transferFluids(List<RelayInfo> senders, List<RelayInfo> receivers) {
        long budget = Math.min(MAX_FLUID, senders.size() * FLUID_PER_SENDER);
        List<Storage<FluidVariant>> sources = new ArrayList<>();
        List<Storage<FluidVariant>> targets = new ArrayList<>();
        for (RelayInfo r : senders) {
            Storage<FluidVariant> s = FluidStorage.SIDED.find(world, r.attachedPos(), r.facing.getOpposite());
            if (s != null && s.supportsExtraction()) sources.add(s);
        }
        for (RelayInfo r : receivers) {
            Storage<FluidVariant> t = FluidStorage.SIDED.find(world, r.attachedPos(), r.facing.getOpposite());
            if (t != null && t.supportsInsertion()) targets.add(t);
        }
        moveAll(sources, targets, budget);
    }

    /** Generic Transfer-API distribution: drain sources into targets up to budget. */
    private <T> void moveAll(List<Storage<T>> sources, List<Storage<T>> targets, long budget) {
        if (sources.isEmpty() || targets.isEmpty() || budget <= 0) return;
        try (Transaction tx = Transaction.openOuter()) {
            long moved = 0;
            for (Storage<T> target : targets) {
                for (Storage<T> source : sources) {
                    if (moved >= budget) break;
                    moved += StorageUtil.move(source, target, v -> true, budget - moved, tx);
                }
                if (moved >= budget) break;
            }
            tx.commit();
        }
    }

    /** RU is not a Transfer-API resource, so bridge the relay's custom energy nodes. */
    private void transferEnergy(List<RelayInfo> senders, List<RelayInfo> receivers) {
        long budget = Math.min(MAX_RU, senders.size() * RU_PER_SENDER);
        List<ResonanceNode> sources = new ArrayList<>();
        List<ResonanceNode> targets = new ArrayList<>();
        for (RelayInfo r : senders) {
            ResonanceNode n = energyNode(r);
            if (n != null && (n.is(NodeRole.PROVIDER) || n.is(NodeRole.STORAGE))) sources.add(n);
        }
        for (RelayInfo r : receivers) {
            ResonanceNode n = energyNode(r);
            if (n != null && (n.is(NodeRole.CONSUMER) || n.is(NodeRole.STORAGE))) targets.add(n);
        }
        if (sources.isEmpty() || targets.isEmpty()) return;

        long available = 0;
        for (ResonanceNode s : sources) {
            if (available >= budget) break;
            available += s.extract(budget - available, true);
        }
        long capacity = 0;
        for (ResonanceNode t : targets) {
            if (capacity >= available) break;
            capacity += t.insert(available - capacity, true);
        }
        long toMove = Math.min(available, capacity);
        if (toMove <= 0) return;

        long pulled = 0;
        for (ResonanceNode s : sources) {
            if (pulled >= toMove) break;
            pulled += s.extract(toMove - pulled, false);
        }
        long pushed = 0;
        for (ResonanceNode t : targets) {
            if (pushed >= pulled) break;
            pushed += t.insert(pulled - pushed, false);
        }
    }

    private ResonanceNode energyNode(RelayInfo r) {
        return world.getBlockEntity(r.attachedPos()) instanceof ResonanceNode n ? n : null;
    }

    private static int clampChannel(int channel) {
        return ((channel % CHANNELS) + CHANNELS) % CHANNELS;
    }

    /** Mutable per-relay record kept in the roster. */
    private static final class RelayInfo {
        final BlockPos pos;
        int channel;
        RelayMode mode;
        Direction facing;

        RelayInfo(BlockPos pos, int channel, RelayMode mode, Direction facing) {
            this.pos = pos;
            this.channel = channel;
            this.mode = mode;
            this.facing = facing;
        }

        /** The block this relay reads from / writes to. */
        BlockPos attachedPos() {
            return pos.offset(facing);
        }
    }
}
