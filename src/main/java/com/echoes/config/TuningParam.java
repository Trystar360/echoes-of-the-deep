package com.echoes.config;

/**
 * One block-specific integer dial shown in the configuration screen — e.g. a
 * Growth Radiator's effect radius or Verdant Loam's growth rate. The label is a
 * translation key; the value is clamped to [min, max] and stepped by {@code step}.
 */
public record TuningParam(String labelKey, int min, int max, int step, int def) {

    public int clamp(int v) {
        return Math.max(min, Math.min(max, v));
    }

    /** Step up/down and clamp. */
    public int adjust(int current, int direction) {
        return clamp(current + step * Integer.signum(direction));
    }
}
