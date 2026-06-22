package com.echoes.config;

import java.util.UUID;

/** Who may open and configure a device. */
public enum SecurityMode {
    /** Anyone can open and configure. */
    PUBLIC,
    /** Only the owner can open or configure. */
    PRIVATE;

    private static final SecurityMode[] VALUES = values();

    public int id() { return ordinal(); }

    public static SecurityMode byId(int id) {
        return VALUES[((id % VALUES.length) + VALUES.length) % VALUES.length];
    }

    public SecurityMode next() { return byId(ordinal() + 1); }

    /** May {@code who} access a device owned by {@code owner} under this mode? */
    public boolean allows(UUID owner, UUID who) {
        return this == PUBLIC || owner == null || owner.equals(who);
    }

    public String translationKey() {
        return "config.echoes.security." + name().toLowerCase();
    }
}
