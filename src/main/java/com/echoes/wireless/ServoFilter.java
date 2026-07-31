package com.echoes.wireless;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Pure (Minecraft-free) core of the per-relay <b>servo filter</b> — Thermal
 * Dynamics' servo whitelist, Echoes-style: a SEND relay with a non-empty filter
 * only extracts the listed items from its attached inventory, while the
 * channel-wide Wave Filter keeps constraining the whole channel. The two
 * compose by intersection (an item must pass both).
 *
 * <p>Filters persist as a single comma-separated string of item ids, so this
 * class owns the parse/join/toggle/allows logic where it can be unit-tested
 * without bootstrapping registries.
 */
public final class ServoFilter {
    private ServoFilter() {}

    /** Maximum number of item ids one relay filter can hold. */
    public static final int CAP = 9;

    /** Result of a {@link #toggle} call. */
    public enum Toggle { ADDED, REMOVED, FULL }

    /** Parse the persisted form ("" → empty set); blanks and duplicates are dropped. */
    public static LinkedHashSet<String> parse(String raw) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (raw == null || raw.isEmpty()) return out;
        for (String id : raw.split(",")) {
            String t = id.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    /** Serialise for NBT ("" when empty). */
    public static String join(Collection<String> ids) {
        return String.join(",", ids);
    }

    /** True when {@code filter} is null/empty (no constraint) or contains {@code id}. */
    public static boolean allows(Collection<String> filter, String id) {
        return filter == null || filter.isEmpty() || filter.contains(id);
    }

    /**
     * Toggle one id: present → removed; absent → added unless the filter is at
     * {@link #CAP}, in which case nothing changes.
     */
    public static Toggle toggle(LinkedHashSet<String> filter, String id) {
        if (filter.remove(id)) return Toggle.REMOVED;
        if (filter.size() >= CAP) return Toggle.FULL;
        filter.add(id);
        return Toggle.ADDED;
    }

    /** Defensive copy as an immutable list (for messages/tests). */
    public static List<String> list(Collection<String> filter) {
        return List.copyOf(filter == null ? new ArrayList<String>() : filter);
    }
}
