package com.echoes.energy;

/**
 * A node may hold several roles at once (a Reinforced Resonator is both
 * PROVIDER and STORAGE), so roles are combined as a bitset.
 */
public enum NodeRole {
    PROVIDER(1),   // emits captured ambient RU into the network
    CONSUMER(2),   // machines that spend RU
    STORAGE(4),    // capacitors / cells that buffer RU
    CONDUIT(8);    // carries RU, no buffer of its own

    public final int bit;

    NodeRole(int bit) {
        this.bit = bit;
    }

    public static int of(NodeRole... roles) {
        int mask = 0;
        for (NodeRole r : roles) mask |= r.bit;
        return mask;
    }

    public static boolean has(int mask, NodeRole role) {
        return (mask & role.bit) != 0;
    }
}
