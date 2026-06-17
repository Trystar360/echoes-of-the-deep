package com.echoes.wireless;

import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Routes items, fluids, Resonance (RU), and redstone between wireless devices
 * that share a channel — no physical conduit required.
 *
 * <p>The roster is server-global and keyed by {@link GlobalPos}, so an
 * {@link WirelessDevice#isRepeater() Echo Repeater} can pool a channel across
 * dimensions. Without a repeater, a channel only connects devices within the same
 * dimension. Devices register on load/placement and unregister on removal, so the
 * tick path only ever visits channels that actually have at least two devices,
 * and each channel is bounded by a per-tick budget (widened by Amplifiers, hard
 * capped) so large builds can't stall the server tick.
 *
 * <p>Like {@link com.echoes.energy.ResonanceNetworkManager} the roster is
 * in-memory and rebuilt from the world as chunks load; every device's channel and
 * mode live in its block-entity NBT, so re-registration restores the network.
 */
public final class WirelessNetworkManager {
    private WirelessNetworkManager() {}

    /** 16 channels, one per dye colour. */
    public static final int CHANNELS = 16;

    /**
     * Opt-in "Hush Cost": when enabled, broadcasting items/fluids drains a little
     * RU per active sender from the channel's energy providers, tying logistics
     * back into the energy economy. Off by default to keep the base relay cheap.
     */
    public static boolean HUSH_COST = false;
    private static final long HUSH_RU_PER_SENDER = 20;

    // Per-tick throughput. "Powerful but cheap": one pair moves a healthy trickle;
    // more senders widen the pipe and Amplifiers multiply it, up to a hard ceiling.
    private static final long ITEMS_PER_SENDER = 8,                  MAX_ITEMS = 64;
    private static final long FLUID_PER_SENDER = FluidConstants.BUCKET, MAX_FLUID = 8 * FluidConstants.BUCKET;
    private static final long RU_PER_SENDER    = 1_000,              MAX_RU    = 16_000;
    private static final int  MAX_AMPLIFY      = 16; // budget multiplier ceiling

    // channel -> devices on it (across all dimensions); plus a flat index by position.
    private static final List<Set<WirelessDevice>> BY_CHANNEL = new ArrayList<>(CHANNELS);
    private static final Map<GlobalPos, Holder> DEVICES = new HashMap<>();

    static {
        for (int i = 0; i < CHANNELS; i++) BY_CHANNEL.add(new HashSet<>());
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> tick());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            DEVICES.clear();
            for (Set<WirelessDevice> s : BY_CHANNEL) s.clear();
        });
    }

    // --- roster (called by device block entities) ---

    /** Add or update a device. Idempotent; safe to call every tick from the BE. */
    public static void register(WirelessDevice device) {
        int channel = clampChannel(device.wirelessChannel());
        GlobalPos key = keyOf(device);
        Holder holder = DEVICES.get(key);
        if (holder == null) {
            DEVICES.put(key, new Holder(device, channel));
            BY_CHANNEL.get(channel).add(device);
            return;
        }
        if (holder.device != device) { // BE was reloaded into a fresh instance
            BY_CHANNEL.get(holder.channel).remove(holder.device);
            holder.device = device;
            BY_CHANNEL.get(channel).add(device);
        } else if (holder.channel != channel) {
            BY_CHANNEL.get(holder.channel).remove(device);
            BY_CHANNEL.get(channel).add(device);
        }
        holder.channel = channel;
    }

    public static void unregister(ServerWorld world, BlockPos pos) {
        Holder holder = DEVICES.remove(GlobalPos.create(world.getRegistryKey(), pos.toImmutable()));
        if (holder != null) BY_CHANNEL.get(holder.channel).remove(holder.device);
    }

    private static GlobalPos keyOf(WirelessDevice d) {
        return GlobalPos.create(d.wirelessWorld().getRegistryKey(), d.wirelessPos().toImmutable());
    }

    // --- tick ---

    private static void tick() {
        for (int ch = 0; ch < CHANNELS; ch++) {
            Set<WirelessDevice> all = BY_CHANNEL.get(ch);
            if (all.size() < 2) continue;

            boolean repeater = false;
            for (WirelessDevice d : all) if (d.isRepeater()) { repeater = true; break; }

            if (repeater) {
                process(new ArrayList<>(all));
            } else {
                // Split by dimension: a channel only spans dimensions via a repeater.
                Map<RegistryKey<World>, List<WirelessDevice>> byDim = new HashMap<>();
                for (WirelessDevice d : all) {
                    byDim.computeIfAbsent(d.wirelessWorld().getRegistryKey(), k -> new ArrayList<>()).add(d);
                }
                for (List<WirelessDevice> group : byDim.values()) {
                    if (group.size() >= 2) process(group);
                }
            }
        }
    }

    private static void process(List<WirelessDevice> group) {
        boolean roundRobin = false;
        int amplifiers = 0;
        Set<Item> whitelist = null;
        List<WirelessDevice> senders = new ArrayList<>();
        List<WirelessDevice> receivers = new ArrayList<>();
        List<WirelessDevice> passive = new ArrayList<>();
        int redstone = -1;

        for (WirelessDevice d : group) {
            if (d.roundRobin()) roundRobin = true;
            if (d.isAmplifier()) amplifiers++;
            if (d.isPassiveStorage()) passive.add(d);
            Set<Item> w = d.itemWhitelist();
            if (w != null && !w.isEmpty()) {
                if (whitelist == null) whitelist = new HashSet<>();
                whitelist.addAll(w);
            }
            redstone = Math.max(redstone, d.redstoneOut());
            switch (d.transportMode()) {
                case SEND -> senders.add(d);
                case RECEIVE -> receivers.add(d);
                default -> {}
            }
        }

        // Wireless redstone bus: deliver the strongest broadcast to every device.
        int level = Math.max(0, redstone);
        for (WirelessDevice d : group) d.acceptRedstone(level);

        long mult = Math.min(MAX_AMPLIFY, 1L << Math.min(amplifiers, 4)); // 2^amplifiers, capped

        boolean canGive = !senders.isEmpty() || !passive.isEmpty();
        boolean canTake = !receivers.isEmpty() || !passive.isEmpty();
        if (canGive && canTake) {
            if (!(HUSH_COST && !senders.isEmpty() && !payHush(group, senders.size()))) {
                transferItems(senders, receivers, passive, mult, whitelist, roundRobin);
            }
        }
        if (!senders.isEmpty() && !receivers.isEmpty()) {
            transferFluids(senders, receivers, mult, whitelist, roundRobin);
            transferEnergy(senders, receivers, mult);
        }
    }

    private static void transferItems(List<WirelessDevice> senders, List<WirelessDevice> receivers,
                                      List<WirelessDevice> passive, long mult, Set<Item> whitelist, boolean roundRobin) {
        int givers = Math.max(senders.size(), 1);
        long budget = Math.min(MAX_ITEMS * mult, givers * ITEMS_PER_SENDER * mult);
        List<Storage<ItemVariant>> sendSources = collectItems(senders, true);
        List<Storage<ItemVariant>> recvTargets = collectItems(receivers, false);
        List<Storage<ItemVariant>> stores = collectItems(passive, false); // InventoryStorage does both

        final Set<Item> wl = whitelist;
        Predicate<ItemVariant> filter = wl == null ? v -> true : v -> wl.contains(v.getItem());

        // Pass 1: active senders feed receivers and buffer into passive stores.
        List<Storage<ItemVariant>> pass1Targets = new ArrayList<>(recvTargets);
        pass1Targets.addAll(stores);
        moveAll(sendSources, pass1Targets, budget, filter, roundRobin);

        // Pass 2: passive stores drain into receivers (never store->store, no shuffle).
        moveAll(stores, recvTargets, budget, filter, roundRobin);
    }

    private static List<Storage<ItemVariant>> collectItems(List<WirelessDevice> devices, boolean extract) {
        List<Storage<ItemVariant>> out = new ArrayList<>();
        for (WirelessDevice d : devices) {
            Storage<ItemVariant> s = d.wirelessItems();
            if (s == null) continue;
            if (extract ? s.supportsExtraction() : s.supportsInsertion()) out.add(s);
        }
        return out;
    }

    private static void transferFluids(List<WirelessDevice> senders, List<WirelessDevice> receivers,
                                       long mult, Set<Item> whitelist, boolean roundRobin) {
        long budget = Math.min(MAX_FLUID * mult, senders.size() * FLUID_PER_SENDER * mult);
        List<Storage<FluidVariant>> sources = new ArrayList<>();
        List<Storage<FluidVariant>> targets = new ArrayList<>();
        for (WirelessDevice d : senders) {
            Storage<FluidVariant> s = d.wirelessFluids();
            if (s != null && s.supportsExtraction()) sources.add(s);
        }
        for (WirelessDevice d : receivers) {
            Storage<FluidVariant> t = d.wirelessFluids();
            if (t != null && t.supportsInsertion()) targets.add(t);
        }
        // A Harmonic Filter constrains fluids only when it whitelists fluid containers:
        // match each fluid by its bucket item. An item-only whitelist leaves fluids free.
        Predicate<FluidVariant> filter = v -> true;
        if (whitelist != null) {
            boolean anyFluid = false;
            for (Item it : whitelist) { if (it instanceof net.minecraft.item.BucketItem) { anyFluid = true; break; } }
            if (anyFluid) {
                final Set<Item> wl = whitelist;
                filter = v -> wl.contains(v.getFluid().getBucketItem());
            }
        }
        moveAll(sources, targets, budget, filter, roundRobin);
    }

    /** Generic Transfer-API distribution: drain sources into targets up to budget. */
    private static <T> void moveAll(List<Storage<T>> sources, List<Storage<T>> targets,
                                    long budget, Predicate<T> filter, boolean roundRobin) {
        if (sources.isEmpty() || targets.isEmpty() || budget <= 0) return;
        long perTarget = roundRobin ? Math.max(1, budget / targets.size()) : budget;
        try (Transaction tx = Transaction.openOuter()) {
            long moved = 0;
            outer:
            for (Storage<T> target : targets) {
                long local = 0;
                for (Storage<T> source : sources) {
                    if (moved >= budget) break outer;
                    if (local >= perTarget) break;
                    long cap = Math.min(budget - moved, perTarget - local);
                    long m = StorageUtil.move(source, target, filter, cap, tx);
                    moved += m;
                    local += m;
                }
            }
            tx.commit();
        }
    }

    /** RU is not a Transfer-API resource, so bridge the devices' custom energy nodes. */
    private static void transferEnergy(List<WirelessDevice> senders, List<WirelessDevice> receivers, long mult) {
        long budget = Math.min(MAX_RU * mult, senders.size() * RU_PER_SENDER * mult);
        List<ResonanceNode> sources = new ArrayList<>();
        List<ResonanceNode> targets = new ArrayList<>();
        for (WirelessDevice d : senders) {
            ResonanceNode n = d.wirelessEnergy();
            if (n != null && (n.is(NodeRole.PROVIDER) || n.is(NodeRole.STORAGE))) sources.add(n);
        }
        for (WirelessDevice d : receivers) {
            ResonanceNode n = d.wirelessEnergy();
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

    /** Opt-in energy tax for cargo broadcasts. Returns whether it was paid. */
    private static boolean payHush(List<WirelessDevice> group, int senderCount) {
        long cost = (long) senderCount * HUSH_RU_PER_SENDER;
        List<ResonanceNode> wells = new ArrayList<>();
        for (WirelessDevice d : group) {
            ResonanceNode n = d.wirelessEnergy();
            if (n != null && (n.is(NodeRole.PROVIDER) || n.is(NodeRole.STORAGE))) wells.add(n);
        }
        if (wells.isEmpty()) return true; // nothing to draw from: broadcasting is free
        long avail = 0;
        for (ResonanceNode n : wells) { if (avail >= cost) break; avail += n.extract(cost - avail, true); }
        if (avail < cost) return false;
        long paid = 0;
        for (ResonanceNode n : wells) { if (paid >= cost) break; paid += n.extract(cost - paid, false); }
        return true;
    }

    // --- read-only views for the Channel Atlas ---

    /** Per-channel device count across all dimensions. */
    public static int[] channelCounts() {
        int[] counts = new int[CHANNELS];
        for (int i = 0; i < CHANNELS; i++) counts[i] = BY_CHANNEL.get(i).size();
        return counts;
    }

    /** A human summary of one channel's roster: {total, senders, receivers, modifiers}. */
    public static int[] channelRoster(int channel) {
        int total = 0, send = 0, recv = 0, mod = 0;
        for (WirelessDevice d : BY_CHANNEL.get(clampChannel(channel))) {
            total++;
            switch (d.transportMode()) {
                case SEND -> send++;
                case RECEIVE -> recv++;
                default -> {}
            }
            if (d.isAmplifier() || d.isRepeater() || d.roundRobin()
                    || (d.itemWhitelist() != null && !d.itemWhitelist().isEmpty())) mod++;
        }
        return new int[]{total, send, recv, mod};
    }

    private static int clampChannel(int channel) {
        return ((channel % CHANNELS) + CHANNELS) % CHANNELS;
    }

    /** Tracks a device's last-registered channel for O(1) index maintenance. */
    private static final class Holder {
        WirelessDevice device;
        int channel;

        Holder(WirelessDevice device, int channel) {
            this.device = device;
            this.channel = channel;
        }
    }
}
