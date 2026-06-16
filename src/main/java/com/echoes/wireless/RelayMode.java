package com.echoes.wireless;

/**
 * What a {@link com.echoes.block.entity.ResonantRelayBlockEntity} does with the
 * inventory/storage it is attached to.
 *
 * <ul>
 *   <li>{@code SEND}    — pulls resources <em>out</em> of the attached block and
 *       broadcasts them onto its channel.</li>
 *   <li>{@code RECEIVE} — pulls resources <em>off</em> its channel and pushes them
 *       <em>into</em> the attached block.</li>
 *   <li>{@code DISABLED}— inert; stays on the network roster but moves nothing.</li>
 * </ul>
 */
public enum RelayMode {
    RECEIVE,
    SEND,
    DISABLED;

    /** Right-click cycle order: RECEIVE → SEND → DISABLED → RECEIVE. */
    public RelayMode next() {
        return switch (this) {
            case RECEIVE -> SEND;
            case SEND -> DISABLED;
            case DISABLED -> RECEIVE;
        };
    }

    public static RelayMode byId(int id) {
        RelayMode[] v = values();
        return v[((id % v.length) + v.length) % v.length];
    }
}
