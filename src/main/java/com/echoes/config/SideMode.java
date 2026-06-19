package com.echoes.config;

/** Per-face transfer mode for items / Light / fluids. */
public enum SideMode {
    DISABLED,
    INPUT,
    OUTPUT,
    BOTH;

    private static final SideMode[] VALUES = values();

    public int id() { return ordinal(); }

    public static SideMode byId(int id) {
        return VALUES[((id % VALUES.length) + VALUES.length) % VALUES.length];
    }

    public SideMode next() { return byId(ordinal() + 1); }

    public boolean canInput()  { return this == INPUT  || this == BOTH; }
    public boolean canOutput() { return this == OUTPUT || this == BOTH; }

    public String translationKey() {
        return "config.echoes.side." + name().toLowerCase();
    }
}
