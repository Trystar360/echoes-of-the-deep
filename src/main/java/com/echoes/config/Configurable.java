package com.echoes.config;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Implemented by every functional block entity that exposes a configuration
 * screen. The {@link ConfigSpec} is static per block type (read by the client
 * straight off its own block entity); the {@link BlockConfig} holds the live,
 * NBT-persisted values.
 */
public interface Configurable {
    BlockConfig getConfig();

    ConfigSpec getConfigSpec();

    /** Display name shown at the top of the configuration screen. */
    net.minecraft.network.chat.Component configTitle();

    /** Hook fired server-side after any value changes (re-sync networks, mark dirty…). */
    default void onConfigChanged() {}

    /**
     * Claim ownership for whoever placed this block, if it's a fresh (unclaimed)
     * device — call from every {@code Configurable}-backed {@code Block}'s
     * {@code setPlacedBy}. Ownership otherwise falls back to "first opener claims
     * it" ({@code ConfigScreenHandler}), which leaves a window right after
     * placement where a stranger who reaches the device first (e.g. with a
     * Frequency Tuner) becomes its owner instead of the person who built it.
     */
    static void claimOnPlace(Level level, BlockPos pos, LivingEntity placer) {
        if (level.isClientSide() || !(placer instanceof Player player)) return;
        if (level.getBlockEntity(pos) instanceof Configurable cfg && cfg.getConfig().owner() == null) {
            cfg.getConfig().claim(player.getUUID());
            cfg.onConfigChanged();
        }
    }
}
