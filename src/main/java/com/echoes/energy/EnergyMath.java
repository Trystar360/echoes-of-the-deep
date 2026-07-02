package com.echoes.energy;

import java.util.Arrays;
import java.util.Comparator;

/**
 * The pure numeric core of {@link ResonanceNetwork}'s distribution and balancing:
 * plain {@code long[]} in, plain {@code long[]} out, no Minecraft types. Kept
 * separate from {@link ResonanceNetwork} specifically so it's unit-testable without
 * a Minecraft/Mixin runtime — this is the exact logic that produced two real bugs
 * during the mod's audit (a {@code long*long} overflow in the allocation ratio, and
 * a conservation bug in the balancer), so it's the highest-value place to have
 * regression tests.
 */
public final class EnergyMath {
    private EnergyMath() {}

    /**
     * Largest-remainder proportional allocation: split {@code pool} across
     * {@code demands} so each index gets a share proportional to its demand, then
     * hands any remainder left by integer truncation to the most-starved (largest
     * unmet) entries first. No entry ever gets more than its own demand or than
     * {@code pool} allows; the returned shares sum to {@code min(pool, sum(demands))}.
     */
    public static long[] allocate(long pool, long[] demands) {
        int n = demands.length;
        long[] alloc = new long[n];
        if (n == 0 || pool <= 0) return alloc;

        long totalDemand = 0;
        for (long d : demands) totalDemand += Math.max(0, d);
        if (totalDemand <= 0) return alloc;

        // Ratio computed in double: pool and demand can each individually approach
        // Long.MAX_VALUE on huge capacitor banks, so a long*long product would overflow.
        long usablePool = Math.min(pool, totalDemand);
        long distributed = 0;
        for (int i = 0; i < n; i++) {
            long d = Math.max(0, demands[i]);
            long share = Math.min(d, (long) ((double) usablePool * d / totalDemand));
            alloc[i] = share;
            distributed += share;
        }

        long leftover = usablePool - distributed;
        if (leftover <= 0) return alloc;

        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;
        final long[] a = alloc;
        Arrays.sort(order, Comparator.comparingLong(i -> -(Math.max(0, demands[i]) - a[i])));

        int idx = 0;
        while (leftover > 0) {
            int i = order[idx % n];
            long room = Math.max(0, demands[i]) - alloc[i];
            if (room > 0) { alloc[i]++; leftover--; }
            idx++;
            if (idx > n * 2L && allMaxed(demands, alloc)) break;
        }
        return alloc;
    }

    private static boolean allMaxed(long[] demands, long[] alloc) {
        for (int i = 0; i < demands.length; i++)
            if (alloc[i] < Math.max(0, demands[i])) return false;
        return true;
    }

    /**
     * Rhythmic balanced interchange: the per-node delta (positive = should receive,
     * negative = should give) that nudges every node toward the network's mean fill
     * ratio, each individually capped at {@code rate}. Deltas always net to (at
     * most, before rate-capping) zero — this only computes intent, though; the
     * caller still has to actually extract/insert against real, possibly-stale
     * storages, and must treat the amount actually moved (not this target) as
     * authoritative when conserving totals, since a node's real {@code extract}
     * can return less than requested.
     */
    public static long[] balanceDeltas(long[] stored, long[] capacity, long rate) {
        int n = stored.length;
        long[] delta = new long[n];
        long totalStored = 0, totalCap = 0;
        for (int i = 0; i < n; i++) { totalStored += stored[i]; totalCap += capacity[i]; }
        if (totalCap <= 0) return delta;
        double ratio = (double) totalStored / totalCap;
        for (int i = 0; i < n; i++) {
            long diff = Math.round(capacity[i] * ratio) - stored[i];
            delta[i] = Math.max(-rate, Math.min(rate, diff));
        }
        return delta;
    }
}
