package com.echoes.energy;

import com.echoes.block.entity.ResonatorBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * Turns world activity into stored RU. A Resonance event (mob death, anvil
 * land, note block, ...) is claimed once by the nearest Resonator in range.
 *
 * <p>The sound→RU table is data-driven (data/echoes/resonance_sources.json);
 * the mixin on Level#playSound and the LivingEntity death hook funnel into
 * {@link #emit}. Candidate lookup is {@link ResonatorIndex}, not a chunk scan —
 * this fires often, so it's the hottest path in the mod.
 */
public final class ResonanceEvents {
    public static final int RADIUS = 8;

    private ResonanceEvents() {}

    public static void emit(ServerLevel world, Vec3 pos, int amount) {
        if (amount <= 0) return;
        ResonatorBlockEntity res = ResonatorIndex.nearest(world, pos, RADIUS);
        if (res != null) res.absorbAmbient(amount);
    }
}
