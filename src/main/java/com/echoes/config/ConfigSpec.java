package com.echoes.config;

import java.util.List;

/**
 * Static descriptor of which controls a device's configuration screen shows.
 * Lives on the block-entity class (one shared instance per block type), so the
 * client can read it straight off the client-side block entity without syncing.
 *
 * <p>Up to two {@link TuningParam}s are supported (mirrors the fixed property
 * layout in {@link com.echoes.screen.ConfigScreenHandler}).
 */
public record ConfigSpec(boolean channel, boolean octave, boolean redstone,
                         boolean sides, List<TuningParam> tunings) {

    public static final int MAX_TUNINGS = 2;

    public ConfigSpec {
        if (tunings.size() > MAX_TUNINGS) {
            throw new IllegalArgumentException("at most " + MAX_TUNINGS + " tuning params");
        }
    }

    public static Builder builder() { return new Builder(); }

    public TuningParam tuning(int i) {
        return i < tunings.size() ? tunings.get(i) : null;
    }

    public static final class Builder {
        private boolean channel, octave, redstone, sides;
        private final java.util.ArrayList<TuningParam> tunings = new java.util.ArrayList<>();

        public Builder channel()  { this.channel = true; return this; }
        public Builder octave()   { this.octave = true; return this; }
        public Builder redstone() { this.redstone = true; return this; }
        public Builder sides()    { this.sides = true; return this; }

        public Builder tuning(String labelKey, int min, int max, int step, int def) {
            tunings.add(new TuningParam(labelKey, min, max, step, def));
            return this;
        }

        public ConfigSpec build() {
            return new ConfigSpec(channel, octave, redstone, sides, List.copyOf(tunings));
        }
    }
}
