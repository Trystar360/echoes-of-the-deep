package com.echoes.energy;

import com.echoes.block.entity.ResonatorBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

/**
 * Turns world activity into stored RU. A Resonance event (mob death, anvil
 * land, note block, ...) is claimed once by the nearest Resonator in range.
 *
 * <p>The sound→RU table is data-driven (data/echoes/resonance_sources.json);
 * the mixin on World#playSound and the LivingEntity death hook funnel into
 * {@link #emit}.
 */
public final class ResonanceEvents {
    public static final int RADIUS = 8;

    private ResonanceEvents() {}

    public static void emit(ServerWorld world, Vec3d pos, int amount) {
        if (amount <= 0) return;
        nearestResonator(world, pos, RADIUS).ifPresent(res -> res.absorbAmbient(amount));
    }

    private static Optional<ResonatorBlockEntity> nearestResonator(ServerWorld world, Vec3d pos, int radius) {
        BlockPos origin = BlockPos.ofFloored(pos);
        ResonatorBlockEntity best = null;
        double bestDist = Double.MAX_VALUE;
        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    m.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (world.getBlockEntity(m) instanceof ResonatorBlockEntity res) {
                        double d = m.getSquaredDistance(pos.x, pos.y, pos.z);
                        if (d < bestDist) { bestDist = d; best = res; }
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }
}
