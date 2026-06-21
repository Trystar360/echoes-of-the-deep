package com.echoes.config;

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
}
