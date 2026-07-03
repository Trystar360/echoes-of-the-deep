package com.echoes.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for the two real bugs the mod audit found in this exact
 * logic: a {@code long*long} overflow in {@link EnergyMath#allocate} and a
 * conservation bug (inserted diverging from extracted) in
 * {@link EnergyMath#balanceDeltas}.
 */
class EnergyMathTest {

    // --- allocate ---

    @Test
    void allocateCapsEachShareAtItsOwnDemand() {
        long[] demands = {100, 50, 25};
        long[] alloc = EnergyMath.allocate(80, demands);
        long sum = 0;
        for (int i = 0; i < alloc.length; i++) {
            assertTrue(alloc[i] <= demands[i], "share must never exceed demand");
            sum += alloc[i];
        }
        assertEquals(80, sum, "the whole pool is distributed when demand exceeds it");
    }

    @Test
    void allocateNeverExceedsTotalDemandWhenPoolIsLarger() {
        long[] demands = {10, 20, 5};
        assertArrayEquals(demands, EnergyMath.allocate(1000, demands));
    }

    @Test
    void allocateHandlesZeroDemand() {
        assertArrayEquals(new long[]{0, 0, 0}, EnergyMath.allocate(100, new long[]{0, 0, 0}));
    }

    @Test
    void allocateHandlesEmptyInput() {
        assertArrayEquals(new long[0], EnergyMath.allocate(100, new long[0]));
    }

    @Test
    void allocateHandlesZeroOrNegativePool() {
        assertArrayEquals(new long[]{0, 0}, EnergyMath.allocate(0, new long[]{10, 10}));
        assertArrayEquals(new long[]{0, 0}, EnergyMath.allocate(-5, new long[]{10, 10}));
    }

    @Test
    void allocateDropsNoRemainder() {
        // A pool that doesn't divide evenly among equal demands must still be
        // distributed in full — no unit of the pool goes missing to truncation.
        long[] alloc = EnergyMath.allocate(10, new long[]{10, 10, 10});
        long sum = 0;
        for (long a : alloc) sum += a;
        assertEquals(10, sum);
    }

    @Test
    void allocateStaysCorrectAtExtremeScale() {
        // Regression: pool*demand used to overflow long at this scale (huge
        // capacitor banks / capacitor cell chains).
        long huge = Long.MAX_VALUE / 2;
        long[] alloc = EnergyMath.allocate(huge, new long[]{huge, huge});
        long sum = 0;
        for (long a : alloc) sum += a;
        assertEquals(huge, sum);
    }

    // --- balanceDeltas ---

    @Test
    void balanceDeltasConserveTotal() {
        long[] delta = EnergyMath.balanceDeltas(new long[]{1000, 0}, new long[]{1000, 1000}, 10_000);
        long sum = 0;
        for (long d : delta) sum += d;
        assertEquals(0, sum, "moving Light around must net to zero");
    }

    @Test
    void balanceDeltasRespectsRateCap() {
        long[] delta = EnergyMath.balanceDeltas(new long[]{1000, 0}, new long[]{1000, 1000}, 100);
        assertEquals(-100, delta[0]);
        assertEquals(100, delta[1]);
    }

    @Test
    void balanceDeltasNoOpWhenAlreadyBalanced() {
        long[] delta = EnergyMath.balanceDeltas(new long[]{500, 500}, new long[]{1000, 1000}, 100);
        assertEquals(0, delta[0]);
        assertEquals(0, delta[1]);
    }

    @Test
    void balanceDeltasHandlesZeroCapacity() {
        assertArrayEquals(new long[]{0, 0}, EnergyMath.balanceDeltas(new long[]{0, 0}, new long[]{0, 0}, 100));
    }
}
