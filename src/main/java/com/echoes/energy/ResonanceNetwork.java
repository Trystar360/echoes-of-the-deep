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
 * <p>Internal transfer between directly-touching blocks is <b>unlimited</b>. Once a
 * network contains at least one conduit, its per-tick pool is capped at the sum of
 * every conduit member's {@link ResonanceNode#transferCap()} — a Wave Conduit run
 * contributes 1,000 RU/t, Dense 16,000, Octave 64,000, so denser or higher-tier
 * wiring genuinely raises what a network can move (this is an aggregate budget, not
 * a routed flow simulation: it doesn't model a single skinny chokepoint segment
 * starving a network that is otherwise thick with conduit elsewhere). When supply
 * (after any such cap) can't meet demand, it falls back to a largest-remainder
 * proportional allocation, so under genuine scarcity every consumer gets a share
 * proportional to its demand and the remainder goes to the most-starved consumers
 * first — no starvation, no waste.
 *
 * <p><b>Staggered ticking</b> ({@link #tickInterval}) only skips how often the O(size)
 * distribution pass itself runs on colossal networks — it does not reduce steady-state
 * throughput the way a naive read of "runs 1-in-N ticks" might suggest, because a
 * consumer's {@code demand()} and a provider's simulated supply are both "how much has
 * accumulated since the last visit" quantities (each node keeps generating/draining
 * its own buffer every real tick via its own {@link net.minecraft.world.level.block.entity.BlockEntityTicker},
 * independent of this stagger), so a visit on tick N moves the full N-tick-accumulated
 * amount in one pass. The one real residual risk is a consumer whose own buffer is too
 * small to bank a full stagger window's worth of consumption without hitting empty —
 * {@link #MAX_TICK_INTERVAL} and the {@link #STAGGER_THRESHOLD} are kept conservative
 * enough that this shouldn't bite any realistically-sized base.
 */
public class ResonanceNetwork {
    /** Networks above this member count get staggered; below it, every tick runs. */
    private static final int STAGGER_THRESHOLD = 2048;
    /** Worst-case ticks between distribution passes on a staggered network. */
    private static final int MAX_TICK_INTERVAL = 2;

    public final int id;
    /** Every energy block entity in this connected component (generators, machines, cells, conduits). */
    public final Set<BlockPos> members = new HashSet<>();

    // Re-scanned lazily when dirty.
    private final List<ResonanceNode> providers = new ArrayList<>();
    private final List<ResonanceNode> consumers = new ArrayList<>();
    private final List<ResonanceNode> storages = new ArrayList<>();
    private boolean dirty = true;

    // Sum of every conduit member's transferCap(); -1 while the network has none
    // (direct-touching blocks stay unlimited, matching the no-conduit-needed design).
    private long conduitThroughputCap = -1;

    // Stagger huge networks so they don't all compute on the same tick.
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
        // Conduits carry no buffer and simply hold the component together, but
        // contribute their throughput cap to the network's aggregate pipe budget.
        long cap = 0;
        boolean hasConduit = false;
        for (BlockPos m : members) {
            ResonanceNode node = nodeAt(world, m);
            if (node == null) continue;               // unloaded chunk or already gone
            if (node.is(NodeRole.PROVIDER)) providers.add(node);
            if (node.is(NodeRole.CONSUMER)) consumers.add(node);
            if (node.is(NodeRole.STORAGE))  storages.add(node);
            int tc = node.transferCap();
            if (tc > 0) { cap += tc; hasConduit = true; }
        }
        this.conduitThroughputCap = hasConduit ? cap : -1;
        this.tickInterval = members.size() > STAGGER_THRESHOLD ? MAX_TICK_INTERVAL : 1;
        this.dirty = false;
    }

    /** Per-tick distribution. Call from the manager. */
    public void tick(ServerLevel world) {
        if ((id + world.getGameTime()) % tickInterval != 0) return;
        if (dirty) rescan(world);
        if (consumers.isEmpty() && storages.isEmpty()) return;

        // Staggered networks move tickInterval ticks' worth of budget per activation,
        // so the conduit cap stays a true per-tick average rather than quietly
        // dividing large networks' throughput by the stagger factor.
        long capBudget = conduitThroughputCap < 0 ? -1 : conduitThroughputCap * tickInterval;

        // 1. Simulate total available supply (providers first, then storage).
        long supply = 0;
        for (ResonanceNode p : providers) supply += p.extract(Long.MAX_VALUE, true);
        long providerSupply = supply;
        for (ResonanceNode s : storages) supply += s.extract(Long.MAX_VALUE, true);
        if (supply == 0) return;

        // 2. Gather demand (snapshot once — demand() is queried exactly this many
        // times per tick now, not re-read throughout the allocation below).
        long totalDemand = 0;
        List<ResonanceNode> active = new ArrayList<>();
        List<Long> demandList = new ArrayList<>();
        for (ResonanceNode c : consumers) {
            long d = c.demand();
            if (d > 0) { active.add(c); demandList.add(d); totalDemand += d; }
        }

        if (totalDemand == 0) {
            // Surplus: top up storage by lowest fill-ratio first, within the same
            // conduit throughput cap as any other transfer.
            long surplus = capBudget >= 0 ? Math.min(providerSupply, capBudget) : providerSupply;
            topUpStorage(surplus);
            return;
        }

        // 3. The only ceiling is what's available and — once the network has conduit
        // members — the aggregate throughput those conduits contribute.
        long pool = Math.min(supply, totalDemand);
        if (capBudget >= 0) pool = Math.min(pool, capBudget);

        // 4. Largest-remainder proportional allocation (see EnergyMath for the pure,
        // unit-tested implementation — this is where the overflow bug lived).
        long[] demandArr = new long[demandList.size()];
        for (int i = 0; i < demandArr.length; i++) demandArr[i] = demandList.get(i);
        long[] alloc = EnergyMath.allocate(pool, demandArr);

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

        long[] stored = new long[storages.size()];
        long[] capacity = new long[storages.size()];
        for (int i = 0; i < storages.size(); i++) {
            stored[i] = storages.get(i).storedRu();
            capacity[i] = storages.get(i).capacityRu();
        }
        // See EnergyMath for the pure, unit-tested implementation — this is where
        // the conservation bug lived (independently rate-capping each side let
        // total inserted diverge from total extracted).
        long[] delta = EnergyMath.balanceDeltas(stored, capacity, rate);

        // Two-phase, like the main distribution: extract from over-full nodes first
        // (bounded by what each actually has), then insert only what was actually
        // extracted — never more, so the balancer moves Light, it never manufactures
        // or destroys it.
        long available = 0;
        for (int i = 0; i < storages.size(); i++) {
            if (delta[i] < 0) available += storages.get(i).extract(-delta[i], false);
        }
        if (available <= 0) return;

        long remaining = available;
        for (int i = 0; i < storages.size() && remaining > 0; i++) {
            if (delta[i] > 0) remaining -= storages.get(i).insert(Math.min(delta[i], remaining), false);
        }
        // Rounding/room mismatches can leave a small remainder with nowhere the mean-fill
        // pass wanted it; rather than lose it, hand it to whichever storage still has room.
        if (remaining > 0) {
            for (ResonanceNode s : storages) {
                if (remaining <= 0) break;
                remaining -= s.insert(remaining, false);
            }
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
