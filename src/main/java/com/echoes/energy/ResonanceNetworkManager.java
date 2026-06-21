package com.echoes.energy;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Owns all conduit networks for a single {@link ServerLevel}. Networks are
 * persistent objects mutated incrementally on topology changes — we never
 * flood-fill on the tick path. The only expensive operation is a split check
 * when a conduit is removed, which is rare (player-driven).
 *
 * <p>One instance per world, held in a static map keyed by world. (For a
 * shipping mod, attach this to the world via a Cardinal Components world
 * component or a SavedData; kept simple here.)
 */
public class ResonanceNetworkManager {
    private static final Map<ServerLevel, ResonanceNetworkManager> INSTANCES = new HashMap<>();

    private final ServerLevel world;
    private final Map<Integer, ResonanceNetwork> networks = new HashMap<>();
    private final Map<BlockPos, Integer> posToNetwork = new HashMap<>();
    private int nextId = 1;
    private ResonanceNetworkState state;

    private ResonanceNetworkManager(ServerLevel world) {
        this.world = world;
        load();
    }

    /** Restore persisted network topology (conduit sets) on first access per world. */
    private void load() {
        state = world.getPersistentStateManager().getOrCreate(ResonanceNetworkState.TYPE, ResonanceNetworkState.KEY);
        nextId = Math.max(nextId, state.nextId);
        for (Map.Entry<Integer, java.util.Set<BlockPos>> e : state.networks.entrySet()) {
            ResonanceNetwork net = new ResonanceNetwork(e.getKey());
            net.conduits.addAll(e.getValue());
            net.markDirty(); // nodes re-scanned from the world on the next tick
            networks.put(net.id, net);
            for (BlockPos p : e.getValue()) posToNetwork.put(p, net.id);
            nextId = Math.max(nextId, net.id + 1);
        }
    }

    /** Mirror the live topology into the SavedData. Called after every change. */
    private void syncState() {
        if (state == null) return;
        state.networks.clear();
        for (ResonanceNetwork net : networks.values()) {
            state.networks.put(net.id, new java.util.HashSet<>(net.conduits));
        }
        state.nextId = nextId;
        state.markDirty();
    }

    public static ResonanceNetworkManager get(ServerLevel world) {
        return INSTANCES.computeIfAbsent(world, ResonanceNetworkManager::new);
    }

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(world -> get(world).tick());
    }

    private void tick() {
        for (ResonanceNetwork net : networks.values()) {
            net.tick(world);
        }
    }

    // --- topology events (called from conduit block onPlaced / onBroken) ---

    /** A conduit was placed. Create / join / merge with neighboring networks. */
    public void onConduitPlaced(BlockPos pos) {
        Set<Integer> adjacent = new HashSet<>();
        for (BlockPos n : ResonanceNetwork.neighbors(pos)) {
            Integer id = posToNetwork.get(n);
            if (id != null) adjacent.add(id);
        }

        ResonanceNetwork net;
        if (adjacent.isEmpty()) {
            net = new ResonanceNetwork(nextId++);
            networks.put(net.id, net);
        } else {
            // join the first, merge the rest into it
            var it = adjacent.iterator();
            net = networks.get(it.next());
            while (it.hasNext()) merge(net, networks.get(it.next()));
        }
        net.conduits.add(pos);
        posToNetwork.put(pos, net.id);
        markNeighborsDirty(pos, net);
        syncState();
    }

    /** A conduit was removed. Remove it, then check whether the network split. */
    public void onConduitBroken(BlockPos pos) {
        if (doConduitBroken(pos)) syncState();
    }

    private boolean doConduitBroken(BlockPos pos) {
        Integer id = posToNetwork.remove(pos);
        if (id == null) return false;
        ResonanceNetwork net = networks.get(id);
        if (net == null) return true;
        net.conduits.remove(pos);

        // Seeds = surviving conduit neighbors.
        List<BlockPos> seeds = new ArrayList<>();
        for (BlockPos n : ResonanceNetwork.neighbors(pos))
            if (net.conduits.contains(n)) seeds.add(n);

        if (net.conduits.isEmpty()) { networks.remove(id); return true; }
        if (seeds.size() <= 1) { net.markDirty(); return true; }

        // Flood-fill from each seed; if they don't all reach each other, split.
        List<Set<BlockPos>> components = new ArrayList<>();
        Set<BlockPos> globalVisited = new HashSet<>();
        for (BlockPos seed : seeds) {
            if (globalVisited.contains(seed)) continue;
            components.add(floodFill(seed, net.conduits, globalVisited));
        }
        if (components.size() <= 1) { net.markDirty(); return true; }

        // Split: keep the largest as the original, spin up new ones for the rest.
        networks.remove(id);
        for (BlockPos p : net.conduits) posToNetwork.remove(p);
        for (Set<BlockPos> comp : components) {
            ResonanceNetwork fresh = new ResonanceNetwork(nextId++);
            fresh.conduits.addAll(comp);
            networks.put(fresh.id, fresh);
            for (BlockPos p : comp) posToNetwork.put(p, fresh.id);
            fresh.markDirty();
        }
        return true;
    }

    /** A machine/storage/provider next to a conduit changed — re-scan attached nodes. */
    public void onAttachedNodeChanged(BlockPos pos) {
        for (BlockPos n : ResonanceNetwork.neighbors(pos)) {
            Integer id = posToNetwork.get(n);
            if (id != null) networks.get(id).markDirty();
        }
    }

    /** Equalize storage on every network adjacent to {@code pos} (the Balancer). */
    public void balanceAround(BlockPos pos, long rate) {
        java.util.Set<Integer> seen = new HashSet<>();
        for (BlockPos n : ResonanceNetwork.neighbors(pos)) {
            Integer id = posToNetwork.get(n);
            if (id != null && seen.add(id)) {
                ResonanceNetwork net = networks.get(id);
                if (net != null) net.balanceStorages(world, rate);
            }
        }
    }

    private void merge(ResonanceNetwork keep, ResonanceNetwork drop) {
        if (keep == drop || drop == null) return;
        for (BlockPos p : drop.conduits) {
            keep.conduits.add(p);
            posToNetwork.put(p, keep.id);
        }
        networks.remove(drop.id);
        keep.markDirty();
    }

    private void markNeighborsDirty(BlockPos pos, ResonanceNetwork net) {
        net.markDirty();
    }

    private Set<BlockPos> floodFill(BlockPos start, Set<BlockPos> domain, Set<BlockPos> globalVisited) {
        Set<BlockPos> comp = new HashSet<>();
        Deque<BlockPos> stack = new ArrayDeque<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            BlockPos cur = stack.pop();
            if (!comp.add(cur)) continue;
            globalVisited.add(cur);
            for (BlockPos n : ResonanceNetwork.neighbors(cur))
                if (domain.contains(n) && !comp.contains(n)) stack.push(n);
        }
        return comp;
    }
}
