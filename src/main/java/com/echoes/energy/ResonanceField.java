package com.echoes.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Standing-wave coupling between generators. Generators tuned to the same octave
 * reinforce one another, but only when they sit on the <em>antinode lattice</em>
 * for that octave — the resonant spacing is {@code 2 + octave} blocks. A partner
 * placed exactly on an antinode contributes fully; off by more than a block, not
 * at all. So a clean axis-aligned lattice at the right pitch resonates, while a
 * sloppy clump does almost nothing.
 *
 * <p>The scan is chunk-local (it never force-loads chunks) and meant to be called
 * at most every few dozen ticks per generator, with the result cached.
 */
public final class ResonanceField {
    private ResonanceField() {}

    /** How far apart partners can be and still couple (covers the widest octave). */
    public static final int COUPLING_RADIUS = 10;
    /** Coupling saturates here — roughly four perfectly-placed partners. */
    public static final double MAX_COUPLING = 4.0;
    /** A Stillness Core counts as this much of an antinode (the still centre). */
    public static final double ANCHOR_WEIGHT = 1.5;

    /** The antinode spacing, in blocks, for a given octave. */
    public static int resonantSpacing(int octave) {
        return 2 + octave;
    }

    /** Result of a coupling scan, surfaced to generators and the Light Meter. */
    public record Coupling(int partners, double coupling, int spacing) {
        public static final Coupling NONE = new Coupling(0, 0.0, 0);

        /** Resonance multiplier: {@code 1 + min(maxBonus, gain * coupling)}. */
        public double multiplier(double gain, double maxBonus) {
            return 1.0 + Math.min(maxBonus, gain * coupling);
        }
    }

    /**
     * Scan for in-tune partners around {@code origin} and sum their antinode
     * alignment into a coupling value.
     */
    public static Coupling scan(ServerLevel level, BlockPos origin, int octave) {
        int spacing = resonantSpacing(octave);
        double sum = 0.0;
        int partners = 0;

        int minCx = (origin.getX() - COUPLING_RADIUS) >> 4, maxCx = (origin.getX() + COUPLING_RADIUS) >> 4;
        int minCz = (origin.getZ() - COUPLING_RADIUS) >> 4, maxCz = (origin.getZ() + COUPLING_RADIUS) >> 4;
        double r2 = (double) COUPLING_RADIUS * COUPLING_RADIUS;

        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                var chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue; // don't force-load
                for (var entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos p = entry.getKey();
                    if (p.equals(origin)) continue;
                    BlockEntity be = entry.getValue();
                    if (!(be instanceof ResonanceCoupler coupler)) continue;
                    if (coupler.couplingOctave() != octave) continue;

                    double d2 = origin.distSqr(p);
                    if (d2 > r2) continue;
                    double dist = Math.sqrt(d2);
                    double align = Math.max(0.0, 1.0 - Math.abs(dist - spacing));
                    if (align <= 0.0) continue;
                    if (coupler.isResonanceAnchor()) align *= ANCHOR_WEIGHT;
                    sum += align;
                    partners++;
                }
            }
        }
        return new Coupling(partners, Math.min(MAX_COUPLING, sum), spacing);
    }

    /** A couple of note particles drifting up — a generator visibly "ringing" in an array. */
    public static void ringParticles(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.NOTE,
                pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                1, 0.25, 0.0, 0.25, 1.0);
    }
}
