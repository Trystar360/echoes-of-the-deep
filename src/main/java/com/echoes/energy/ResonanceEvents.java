package com.echoes.energy;

import com.echoes.block.entity.ResonatorBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Turns world activity into stored RU. A Resonance event (mob death, anvil
 * land, note block, ...) is claimed once by the nearest Resonator in range.
 *
 * <p>The sound→RU table is data-driven (data/echoes/resonance_sources.json);
 * the mixin on Level#playSound and the LivingEntity death hook funnel into
 * {@link #emit}.
 */
public final class ResonanceEvents {
    public static final int RADIUS = 8;

    private ResonanceEvents() {}

    public static void emit(ServerLevel world, Vec3 pos, int amount) {
        if (amount <= 0) return;
        nearestResonator(world, pos, RADIUS).ifPresent(res -> res.absorbAmbient(amount));
    }

    private static Optional<ResonatorBlockEntity> nearestResonator(ServerLevel world, Vec3 pos, int radius) {
        BlockPos origin = BlockPos.containing(pos);
        double r2 = (double) radius * radius;
        ResonatorBlockEntity best = null;
        double bestDist = Double.MAX_VALUE;

        // Iterate only the block entities of the loaded chunks overlapping the radius
        // (at most a 2×2–3×3 chunk patch), instead of probing every block in a 17³ box.
        // Ambient sounds fire often, so this keeps the hot path cheap on busy servers.
        int minCx = (origin.getX() - radius) >> 4, maxCx = (origin.getX() + radius) >> 4;
        int minCz = (origin.getZ() - radius) >> 4, maxCz = (origin.getZ() + radius) >> 4;
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                var chunk = world.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue; // don't force-load chunks
                for (var entry : chunk.getBlockEntities().entrySet()) {
                    if (entry.getValue() instanceof ResonatorBlockEntity res) {
                        double d = pos.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(entry.getKey()));
                        if (d <= r2 && d < bestDist) { bestDist = d; best = res; }
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }
}
