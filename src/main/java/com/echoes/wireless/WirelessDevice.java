package com.echoes.wireless;

import com.echoes.energy.ResonanceNode;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.item.Item;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Anything that joins a wireless channel implements this. The
 * {@link WirelessNetworkManager} only ever sees devices through this interface,
 * so adding a new channel gadget is just a new block entity that overrides the
 * handful of capability hooks it cares about.
 *
 * <p>All hooks have sensible "inert" defaults; a plain transport endpoint only
 * needs {@link #wirelessChannel()}, {@link #transportMode()}, and whichever of
 * the storage/energy getters apply to it.
 */
public interface WirelessDevice {

    BlockPos wirelessPos();

    ServerLevel wirelessWorld();

    /** 0–15, one per dye colour. */
    int wirelessChannel();

    /** SEND / RECEIVE / DISABLED for transport endpoints; DISABLED for modifiers. */
    default RelayMode transportMode() { return RelayMode.DISABLED; }

    // --- cargo endpoints (null when this device doesn't carry that type) ---

    @Nullable default Storage<ItemVariant> wirelessItems() { return null; }

    /**
     * A buffer that has no send/receive mode of its own (e.g. the Resonant Chest):
     * it accepts from senders and yields to receivers, but never trades with other
     * passive stores, so a channel of chests doesn't shuffle items pointlessly.
     */
    default boolean isPassiveStorage() { return false; }

    @Nullable default Storage<FluidVariant> wirelessFluids() { return null; }

    @Nullable default ResonanceNode wirelessEnergy() { return null; }

    // --- channel modifiers ---

    /** Resonant Amplifier: widens the channel's per-tick transfer budget. */
    default boolean isAmplifier() { return false; }

    /** Echo Repeater: pools this channel across every dimension it appears in. */
    default boolean isRepeater() { return false; }

    /** Resonant Splitter (enabled): switch the channel to even round-robin sharing. */
    default boolean roundRobin() { return false; }

    /** Wave Filter: item types this device whitelists (empty/null = no constraint). */
    @Nullable default Set<Item> itemWhitelist() { return null; }

    // --- wireless redstone (Note Relay) ---

    /** SEND note relay: redstone level it is broadcasting (-1 if not a note sender). */
    default int redstoneOut() { return -1; }

    /** RECEIVE note relay: deliver the channel's redstone level to the block. */
    default void acceptRedstone(int level) {}
}
