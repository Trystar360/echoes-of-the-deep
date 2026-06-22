package com.echoes.energy;

/**
 * A generator that takes part in standing-wave coupling. Generators tuned to the
 * same {@link #couplingOctave() octave} reinforce one another when placed on the
 * antinode lattice for that octave (see {@link ResonanceField}).
 */
public interface ResonanceCoupler {

    /** Which octave this generator is tuned to (0..{@code BlockConfig.OCTAVES-1}). */
    int couplingOctave();

    /**
     * Anchors (the Stillness Core — Russell's still centre) count as a stronger
     * antinode, empowering every partner that shares their octave. Most generators
     * return {@code false}.
     */
    default boolean isResonanceAnchor() { return false; }

    /** Last computed resonance multiplier applied to this generator's output (≥ 1.0). */
    default double resonanceMultiplier() { return 1.0; }
}
