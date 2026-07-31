package com.echoes.wireless;

import com.echoes.config.RedstoneMode;

/**
 * Pure decision logic for TD-style redstone control on wireless devices.
 *
 * <p>Every channel device carries a {@link RedstoneMode} in its config, but a
 * wireless network has <em>two</em> power sources a device may care about:
 * the plain redstone signal at its own position (lever, torch, repeater) and
 * the channel's <em>wireless redstone bus</em> fed by Note Relays. A device is
 * treated as powered when either source is live, so a single Note Relay can
 * switch an entire channel's relays on or off from anywhere in the world.
 *
 * <p>Kept free of Minecraft types so the behaviour is unit-testable on the JVM.
 */
public final class RedstoneGate {

    private RedstoneGate() {}

    /** May a device with this mode act, given its local signal and the channel bus level? */
    public static boolean allowed(RedstoneMode mode, boolean localPowered, int busLevel) {
        return mode.allows(localPowered || busLevel > 0);
    }
}
