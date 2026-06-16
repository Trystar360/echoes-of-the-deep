package com.echoes.energy;

import net.minecraft.util.math.BlockPos;

/**
 * Anything that touches RU implements this. The simulate flag on extract/insert
 * is what lets {@link ResonanceNetwork} plan a fair allocation before committing.
 */
public interface ResonanceNode {

    /** Bitmask of {@link NodeRole}. */
    int roleMask();

    /** PROVIDER/STORAGE give up to {@code max} RU. Returns amount actually moved. */
    long extract(long max, boolean simulate);

    /** CONSUMER/STORAGE take up to {@code max} RU. Returns amount actually moved. */
    long insert(long max, boolean simulate);

    /** CONSUMER's unmet want this tick (0 when satisfied or not a consumer). */
    long demand();

    /** CONDUIT throughput limit, RU/t (0 for non-conduits). */
    int transferCap();

    BlockPos pos();

    default boolean is(NodeRole role) {
        return NodeRole.has(roleMask(), role);
    }
}
