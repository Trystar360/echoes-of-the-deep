package com.echoes.data;

import com.echoes.config.RedstoneMode;
import com.echoes.wireless.RedstoneGate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TD-style redstone control on the wireless network: a device is powered when
 * it has a live local signal OR the channel's wireless redstone bus is live,
 * and its RedstoneMode decides whether it may act in that state.
 */
class RedstoneGateTest {

    @Test
    void alwaysModeIgnoresAllSignals() {
        assertTrue(RedstoneGate.allowed(RedstoneMode.ALWAYS, false, 0));
        assertTrue(RedstoneGate.allowed(RedstoneMode.ALWAYS, true, 0));
        assertTrue(RedstoneGate.allowed(RedstoneMode.ALWAYS, false, 15));
    }

    @Test
    void needsRedstoneRunsOnLocalSignal() {
        assertTrue(RedstoneGate.allowed(RedstoneMode.NEEDS_REDSTONE, true, 0));
        assertFalse(RedstoneGate.allowed(RedstoneMode.NEEDS_REDSTONE, false, 0));
    }

    @Test
    void needsRedstoneRunsOnWirelessBus() {
        // A Note Relay on the channel can switch this device on remotely.
        assertTrue(RedstoneGate.allowed(RedstoneMode.NEEDS_REDSTONE, false, 1));
        assertTrue(RedstoneGate.allowed(RedstoneMode.NEEDS_REDSTONE, false, 15));
    }

    @Test
    void disabledByRedstoneStopsOnEitherSource() {
        assertTrue(RedstoneGate.allowed(RedstoneMode.DISABLED_BY_REDSTONE, false, 0));
        assertFalse(RedstoneGate.allowed(RedstoneMode.DISABLED_BY_REDSTONE, true, 0));
        assertFalse(RedstoneGate.allowed(RedstoneMode.DISABLED_BY_REDSTONE, false, 7));
    }

    @Test
    void busLevelZeroIsNotPower() {
        assertFalse(RedstoneGate.allowed(RedstoneMode.NEEDS_REDSTONE, false, 0));
        assertTrue(RedstoneGate.allowed(RedstoneMode.DISABLED_BY_REDSTONE, false, 0));
    }

    @Test
    void redstoneModeRoundTripsThroughIds() {
        for (RedstoneMode m : RedstoneMode.values()) {
            assertTrue(RedstoneMode.byId(m.id()) == m, "byId must round-trip " + m);
            assertTrue(m.next().next().next() == m, "next() x3 must cycle back to " + m);
        }
    }
}
