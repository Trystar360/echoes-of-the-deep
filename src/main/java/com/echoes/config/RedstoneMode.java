package com.echoes.config;

/** How a configurable device reacts to a redstone signal. */
public enum RedstoneMode {
    /** Always runs, ignoring redstone. */
    ALWAYS,
    /** Only runs while powered. */
    NEEDS_REDSTONE,
    /** Only runs while NOT powered. */
    DISABLED_BY_REDSTONE;

    private static final RedstoneMode[] VALUES = values();

    public int id() { return ordinal(); }

    public static RedstoneMode byId(int id) {
        return VALUES[((id % VALUES.length) + VALUES.length) % VALUES.length];
    }

    public RedstoneMode next() { return byId(ordinal() + 1); }

    /** Given the block's current powered state, may this device act? */
    public boolean allows(boolean powered) {
        return switch (this) {
            case ALWAYS -> true;
            case NEEDS_REDSTONE -> powered;
            case DISABLED_BY_REDSTONE -> !powered;
        };
    }

    public String translationKey() {
        return "config.echoes.redstone." + name().toLowerCase();
    }
}
