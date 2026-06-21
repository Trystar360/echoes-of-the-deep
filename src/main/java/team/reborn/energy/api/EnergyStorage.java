package team.reborn.energy.api;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * <b>Compile-only stub of Team Reborn Energy's {@code EnergyStorage} API.</b>
 *
 * <p>This is NOT the real Team Reborn Energy class — it is a faithful, minimal mirror of the
 * public {@code team.reborn.energy.api.EnergyStorage} interface (insert/extract/getAmount/
 * getCapacity plus the {@code SIDED} block lookup) used so {@link com.echoes.compat}'s energy
 * bridge can compile against 26.1 Mojang mappings while no 26.1-mapped Team Reborn Energy
 * artifact exists.
 *
 * <p>The {@code jar} task <b>excludes {@code team/reborn/**}</b>, so this class is never shipped.
 * At runtime the real Team Reborn Energy mod supplies {@code EnergyStorage} (and the bridge is
 * only registered when {@code team_reborn_energy} is loaded), so there is never a duplicate
 * class on the classpath. Method signatures match the real API exactly, so a bridge compiled
 * against this stub correctly implements the real interface at runtime.
 */
public interface EnergyStorage {

    /** Sided block access to energy storages (1 RU = 1 E). */
    BlockApiLookup<EnergyStorage, @Nullable Direction> SIDED =
            BlockApiLookup.get(Identifier.fromNamespaceAndPath("teamreborn", "sided_energy"),
                    EnergyStorage.class, Direction.class);

    /** True unless {@link #insert} will always return 0. */
    default boolean supportsInsertion() {
        return true;
    }

    /** Insert up to {@code maxAmount} energy; returns the amount actually inserted. */
    long insert(long maxAmount, TransactionContext transaction);

    /** True unless {@link #extract} will always return 0. */
    default boolean supportsExtraction() {
        return true;
    }

    /** Extract up to {@code maxAmount} energy; returns the amount actually extracted. */
    long extract(long maxAmount, TransactionContext transaction);

    /** Current stored energy. */
    long getAmount();

    /** Maximum storable energy. */
    long getCapacity();
}
