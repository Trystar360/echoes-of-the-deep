package com.echoes.compat;

import com.echoes.EchoesMod;
import com.echoes.registry.ModBlockEntities;
import team.reborn.energy.api.EnergyStorage;

/**
 * Optional Team Reborn Energy bridge. Loaded only when {@code team_reborn_energy}
 * is present (see {@link EchoesMod}), so the class — and its TR Energy references —
 * are never touched otherwise. Exposes the RU buffers of the Resonator, Conduit
 * Coupler, and the two Resonance Cells as {@link EnergyStorage}, so the Resonance
 * grid interoperates with other tech mods (1 RU = 1 E).
 *
 * <p>The {@code teamreborn:energy} artifact is a compile-only dependency that is
 * never bundled; this code binds at runtime to whatever Team Reborn Energy build the
 * player has installed.
 */
public final class TeamRebornEnergyCompat {
    private TeamRebornEnergyCompat() {}

    public static void register() {
        // The Resonator and Conduit Coupler are the grid's natural I/O points; the
        // Resonance Cell and Greater Resonance Cell are buffers other mods can fill/drain.
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, dir) -> new ResonanceEnergyBridge(be.storage(), be), ModBlockEntities.RESONATOR);
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, dir) -> new ResonanceEnergyBridge(be.storage(), be), ModBlockEntities.CONDUIT_COUPLER);
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, dir) -> new ResonanceEnergyBridge(be.storage(), be), ModBlockEntities.RESONANCE_CAPACITOR);
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, dir) -> new ResonanceEnergyBridge(be.storage(), be), ModBlockEntities.GREATER_ACCUMULATOR);
        EchoesMod.LOGGER.info("Team Reborn Energy bridge enabled (Resonator, Conduit Coupler, Resonance Cells).");
    }
}
