package com.echoes.compat;

import com.echoes.EchoesMod;
import com.echoes.registry.ModBlockEntities;
import team.reborn.energy.api.EnergyStorage;

/**
 * Optional Team Reborn Energy bridge. Loaded only when {@code team_reborn_energy}
 * is present (see {@link EchoesMod}), so the class — and its TR Energy references —
 * are never touched otherwise. Exposes the RU buffers of the Resonator and the
 * Conduit Coupler as {@link EnergyStorage}, so the Resonance grid interoperates
 * with other tech mods (1 RU = 1 E).
 */
public final class TeamRebornEnergyCompat {
    private TeamRebornEnergyCompat() {}

    public static void register() {
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, dir) -> new ResonanceEnergyBridge(be.storage(), be), ModBlockEntities.CONDUIT_COUPLER);
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, dir) -> new ResonanceEnergyBridge(be.storage(), be), ModBlockEntities.RESONATOR);
        EchoesMod.LOGGER.info("Team Reborn Energy bridge enabled (Resonator, Conduit Coupler).");
    }
}
