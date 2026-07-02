package com.echoes.energy;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
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
 * Owns all energy networks for a single {@link ServerLevel}. A network is the
 * connected component of energy block entities joined face-to-face: any blocks
 * that touch auto-join one network with no conduit between them, and conduits are
 * ordinary members that exist to span gaps. Networks are persistent objects
 * mutated incrementally on topology changes — we never flood-fill on the tick
 * path. The only expensive operation is a split check when a member is removed,
 * which is rare (player-driven).
 *
 * <p>One instance per world, held in a static map keyed by world.
 */
public class ResonanceNetworkManager {
    private static final Map<ServerLevel, ResonanceNetworkManager> INSTANCES = new HashMap<>();

    private final ServerLevel world;
    private final Map<Integer, ResonanceNetwork> networks = new HashMap<>();
    private final Map<BlockPos, Integer> posToNetwork = new HashMap<>();
    // Mirrors posToNetwork's keys, grouped by chunk, so a chunk unload can find and
    // invalidate the handful of affected networks without scanning every member of
    // every network in the world.
    private final Map<ChunkPos, Set<BlockPos>> byChunk = new HashMap<>();
    private int nextId = 1;
    private ResonanceNetworkState state;

    private ResonanceNetworkManager(ServerLevel world) {
        this.world = world;
        load();
    }

    /** Restore persisted network topology (member sets) on first access per world. */
    private void load() {
        state = world.getDataStorage().computeIfAbsent(ResonanceNetworkState.TYPE);
        nextId = Math.max(nextId, state.nextId);
        for (Map.Entry<Integer, java.util.Set<BlockPos>> e : state.networks.entrySet()) {
            ResonanceNetwork net = new ResonanceNetwork(e.getKey());
            net.members.addAll(e.getValue());
            net.markDirty(); // nodes re-scanned from the world on the next tick
            networks.put(net.id, net);
            for (BlockPos p : e.getValue()) {
                posToNetwork.put(p, net.id);
                indexAdd(p);
            }
            nextId = Math.max(nextId, net.id + 1);
        }
    }

    private void indexAdd(BlockPos pos) {
        byChunk.computeIfAbsent(new ChunkPos(pos), k -> new HashSet<>()).add(pos);
    }

    private void indexRemove(BlockPos pos) {
        ChunkPos key = new ChunkPos(pos);
        Set<BlockPos> set = byChunk.get(key);
        if (set == null) return;
        set.remove(pos);
        if (set.isEmpty()) byChunk.remove(key);
    }

    /**
     * A chunk unloaded: any network with a member there is holding at least one stale
     * block-entity reference in its cached provider/consumer/storage lists (vanilla
     * doesn't null those out — the in-memory instance is simply orphaned once its
     * chunk is saved and discarded), so mark it dirty. The next tick's rescan drops
     * unloaded members via {@link ResonanceNetwork#nodeAt}'s {@code hasChunk} check
     * before touching them again, instead of silently reading/writing an object whose
     * state no longer round-trips to disk.
     */
    private void onChunkUnload(ChunkPos pos) {
        Set<BlockPos> members = byChunk.get(pos);
        if (members == null) return;
        Set<Integer> affected = new HashSet<>();
        for (BlockPos p : members) {
            Integer id = posToNetwork.get(p);
            if (id != null) affected.add(id);
        }
        for (Integer id : affected) {
            ResonanceNetwork net = networks.get(id);
            if (net != null) net.markDirty();
        }
    }

    /** Mirror the live topology into the SavedData. Called after every change. */
    private void syncState() {
        if (state == null) return;
        state.networks.clear();
        for (ResonanceNetwork net : networks.values()) {
            state.networks.put(net.id, new java.util.HashSet<>(net.members));
        }
        state.nextId = nextId;
        state.setDirty();
    }

    public static ResonanceNetworkManager get(ServerLevel world) {
        return INSTANCES.computeIfAbsent(world, ResonanceNetworkManager::new);
    }

    public static void init() {
        ServerTickEvents.END_LEVEL_TICK.register(world -> get(world).tick());
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            // Look up the existing instance directly rather than get(world): a chunk
            // unload during server shutdown shouldn't resurrect a manager for a world
            // that's about to be discarded anyway.
            ResonanceNetworkManager mgr = INSTANCES.get(world);
            if (mgr != null) mgr.onChunkUnload(chunk.getPos());
        });
        // Each singleplayer world open/close cycle otherwise pins its ServerLevel (and
        // full network topology) in INSTANCES for the rest of the JVM's life.
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> INSTANCES.clear());
    }

    private void tick() {
        for (ResonanceNetwork net : networks.values()) {
            net.tick(world);
        }
    }

    // --- topology events ---
    //
    // Every energy block routes its place/break here. A single entry point,
    // onNodeChanged, auto-detects which happened by whether a node block entity
    // exists at the position now: present after setBlock => placed; absent after
    // removal => broken. So no block class needs to know the difference.

    /** An energy block was placed or broken at {@code pos}; reconcile the network. */
    public void onNodeChanged(BlockPos pos) {
        pos = pos.immutable();
        if (ResonanceNetwork.nodeAt(world, pos) != null) onNodePlaced(pos);
        else onNodeBroken(pos);
    }

    // Backwards-compatible aliases — existing blocks call these.
    public void onConduitPlaced(BlockPos pos)      { onNodeChanged(pos); }
    public void onConduitBroken(BlockPos pos)       { onNodeChanged(pos); }
    public void onAttachedNodeChanged(BlockPos pos) { onNodeChanged(pos); }

    /** A node appeared at {@code pos}. Create / join / merge with neighboring networks. */
    private void onNodePlaced(BlockPos pos) {
        Set<Integer> adjacent = new HashSet<>();
        Integer existing = posToNetwork.get(pos);
        if (existing != null) adjacent.add(existing);   // idempotent re-notify
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
        net.members.add(pos);
        posToNetwork.put(pos, net.id);
        indexAdd(pos);
        net.markDirty();
        syncState();
    }

    /** A node at {@code pos} was removed. Remove it, then check whether the network split. */
    private void onNodeBroken(BlockPos pos) {
        Integer id = posToNetwork.remove(pos);
        if (id == null) return;
        indexRemove(pos);
        ResonanceNetwork net = networks.get(id);
        if (net == null) { syncState(); return; }
        net.members.remove(pos);

        // Seeds = surviving member neighbors.
        List<BlockPos> seeds = new ArrayList<>();
        for (BlockPos n : ResonanceNetwork.neighbors(pos))
            if (net.members.contains(n)) seeds.add(n);

        if (net.members.isEmpty()) { networks.remove(id); syncState(); return; }
        if (seeds.size() <= 1) { net.markDirty(); syncState(); return; }

        // Flood-fill from each seed; if they don't all reach each other, split.
        List<Set<BlockPos>> components = new ArrayList<>();
        Set<BlockPos> globalVisited = new HashSet<>();
        for (BlockPos seed : seeds) {
            if (globalVisited.contains(seed)) continue;
            components.add(floodFill(seed, net.members, globalVisited));
        }
        if (components.size() <= 1) { net.markDirty(); syncState(); return; }

        // Split: spin up a fresh network for each disconnected component.
        networks.remove(id);
        for (BlockPos p : net.members) posToNetwork.remove(p);
        for (Set<BlockPos> comp : components) {
            ResonanceNetwork fresh = new ResonanceNetwork(nextId++);
            fresh.members.addAll(comp);
            networks.put(fresh.id, fresh);
            for (BlockPos p : comp) posToNetwork.put(p, fresh.id);
            fresh.markDirty();
        }
        syncState();
    }

    /** A snapshot of the network a position belongs to, for the Info screen. */
    public record NetInfo(int members, long stored, long capacity) {}

    /** Totals for the network containing {@code pos} (members loaded right now). */
    public NetInfo infoFor(BlockPos pos) {
        Integer id = posToNetwork.get(pos);
        ResonanceNetwork net = id == null ? null : networks.get(id);
        if (net == null) {                          // lone node not yet in a network
            ResonanceNode n = ResonanceNetwork.nodeAt(world, pos);
            return n == null ? new NetInfo(1, 0, 0) : new NetInfo(1, n.storedRu(), n.capacityRu());
        }
        long stored = 0, cap = 0;
        for (BlockPos m : net.members) {
            ResonanceNode n = ResonanceNetwork.nodeAt(world, m);
            if (n != null) { stored += n.storedRu(); cap += n.capacityRu(); }
        }
        return new NetInfo(net.members.size(), stored, cap);
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
        for (BlockPos p : drop.members) {
            keep.members.add(p);
            posToNetwork.put(p, keep.id);
        }
        networks.remove(drop.id);
        keep.markDirty();
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
