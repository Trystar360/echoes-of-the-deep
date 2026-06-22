package com.echoes.energy;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A connected component of energy nodes — any block entity that touches RU
 * (generators, machines, cells, conduits) — joined face-to-face. Blocks that
 * physically touch share one network with no conduit between them; conduits are
 * ordinary members that exist to span gaps. Persistent across ticks; mutated
 * incrementally by {@link ResonanceNetworkManager} on topology changes.
 *
 * <p>Internal transfer is <b>unlimited</b>: each tick the whole network's supply
 * is offered to its consumers with no throughput ceiling. When supply can't meet
 * demand it falls back to a largest-remainder proportional allocation, so under
 * genuine scarcity every consumer gets a share proportional to its demand and the
 * remainder goes to the most-starved consumers first — no starvation, no waste.
 */
public class ResonanceNetwork {
    public final int id;
    /** Every energy block entity in this connected component (generators, machines, cells, conduits). */
    public final Set<BlockPos> members = new HashSet<>();

    // Re-scanned lazily when dirty.
    private final List<ResonanceNode> providers = new ArrayList<>();
    private final List<ResonanceNode> consumers = new ArrayList<>();
    private final List<ResonanceNode> storages = new ArrayList<>();
    private boolean dirty = true;

    // Stagger large networks so they don't all compute on the same tick.
    private int tickInterval = 1;

    public ResonanceNetwork(int id) {
        this.id = id;
    }

    public void markDirty() { dirty = true; }

    public void rescan(ServerLevel world) {
        providers.clear();
        consumers.clear();
        storages.clear();

        // Classify every loaded member by role (a node may hold several roles).
        // Conduits carry no buffer and simply hold the component together.
        for (BlockPos m : members) {
            ResonanceNode node = nodeAt(world, m);
            if (node == null) continue;               // unloaded chunk or already gone
            if (node.is(NodeRole.PROVIDER)) providers.add(node);
            if (node.is(NodeRole.CONSUMER)) consumers.add(node);
            if (node.is(NodeRole.STORAGE))  storages.add(node);
        }
        this.tickInterval = members.size() > 512 ? 4 : 1;
        this.dirty = false;
    }

    /** Per-tick distribution. Call from the manager. */
    public void tick(ServerLevel world) {        if ((id + world.getGameTime()) % tickInterval != 0) return;
        if (dirty) rescan(world);
        if (consumers.isEmpty() && storages.isEmpty()) return;

        // 1. Simulate total available supply (providers first, then storage).
        long supply = 0;
        for (ResonanceNode p : providers) supply += p.extract(Long.MAX_VALUE, true);
        long providerSupply = supply;
        for (ResonanceNode s : storages) supply += s.extract(Long.MAX_VALUE, true);
        if (supply == 0) return;

        // 2. Gather demand.
        long totalDemand = 0;
        List<ResonanceNode> active = new ArrayList<>();
        for (ResonanceNode c : consumers) {
            long d = c.demand();
            if (d > 0) { active.add(c); totalDemand += d; }
        }

        if (totalDemand == 0) {
            // Surplus: top up storage by lowest fill-ratio first.
            topUpStorage(providerSupply);
            return;
        }

        // 3. Unlimited internal throughput — the only ceiling is what's available.
        long pool = Math.min(supply, totalDemand);

        // 4. Largest-remainder proportional allocation.
        long[] alloc = new long[active.size()];
        long distributed = 0;
        for (int i = 0; i < active.size(); i++) {
            long share = Math.min(active.get(i).demand(), pool * active.get(i).demand() / totalDemand);
            alloc[i] = share;
            distributed += share;
        }
        long leftover = pool - distributed;
        if (leftover > 0) {
            // InteractionHand remainder to the most-starved (largest unmet) consumers first.
            Integer[] order = new Integer[active.size()];
            for (int i = 0; i < order.length; i++) order[i] = i;
            final long[] a = alloc;
            java.util.Arrays.sort(order, Comparator.comparingLong(
                    i -> -(active.get(i).demand() - a[i])));
            int idx = 0;
            while (leftover > 0 && order.length > 0) {
                int i = order[idx % order.length];
                long room = active.get(i).demand() - alloc[i];
                if (room > 0) { alloc[i]++; leftover--; }
                idx++;
                if (idx > order.length * 2L && allMaxed(active, alloc)) break;
            }
        }

        // 5. Commit. Pull from providers first, storage to cover the shortfall.
        long needed = 0;
        for (long v : alloc) needed += v;
        long pulled = drawFromSources(needed);
        // Distribute exactly what we pulled (defensive against rounding).
        long give = Math.min(pulled, needed);
        for (int i = 0; i < active.size() && give > 0; i++) {
            long g = Math.min(alloc[i], give);
            active.get(i).insert(g, false);
            give -= g;
        }
    }

    private boolean allMaxed(List<ResonanceNode> active, long[] alloc) {
        for (int i = 0; i < active.size(); i++)
            if (alloc[i] < active.get(i).demand()) return false;
        return true;
    }

    private long drawFromSources(long needed) {
        long pulled = 0;
        for (ResonanceNode p : providers) {
            if (pulled >= needed) break;
            pulled += p.extract(needed - pulled, false);
        }
        for (ResonanceNode s : storages) {
            if (pulled >= needed) break;
            pulled += s.extract(needed - pulled, false);
        }
        return pulled;
    }

    private void topUpStorage(long surplus) {
        if (surplus <= 0 || storages.isEmpty()) return;
        // lowest fill-ratio first so banks charge evenly (the emptiest cell fills first)
        storages.sort(Comparator.comparingDouble(s -> (double) s.storedRu() / Math.max(1L, s.capacityRu())));
        // pull the surplus out of providers and stash it
        long pulled = 0;
        for (ResonanceNode p : providers) {
            if (pulled >= surplus) break;
            pulled += p.extract(surplus - pulled, false);
        }
        long remaining = pulled;
        for (ResonanceNode s : storages) {
            if (remaining <= 0) break;
            remaining -= s.insert(remaining, false);
        }
    }

    /**
     * Rhythmic balanced interchange: nudge every storage node toward the network's
     * mean fill ratio (Light is conserved — over-full give, under-full regive), so
     * no Resonance Cell hoards. Driven by the Balancer.
     */
    public void balanceStorages(ServerLevel world, long rate) {
        if (dirty) rescan(world);
        if (storages.size() < 2) return;
        long totalStored = 0, totalCap = 0;
        for (ResonanceNode s : storages) { totalStored += s.storedRu(); totalCap += s.capacityRu(); }
        if (totalCap <= 0) return;
        double ratio = (double) totalStored / totalCap;
        for (ResonanceNode s : storages) {
            long diff = Math.round(s.capacityRu() * ratio) - s.storedRu();
            long step = Math.min(Math.abs(diff), rate);
            if (diff > 0) s.insert(step, false);
            else if (diff < 0) s.extract(step, false);
        }
    }

    // --- helpers ---

    public static ResonanceNode nodeAt(ServerLevel world, BlockPos pos) {
        if (!world.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return null;
        return world.getBlockEntity(pos) instanceof ResonanceNode n ? n : null;
    }

    public static BlockPos[] neighbors(BlockPos p) {
        return new BlockPos[]{
                p.above(), p.below(), p.north(), p.south(), p.east(), p.west()
        };
    }
}
