package com.echoes.compat;

import com.echoes.energy.ResonanceStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;

/**
 * Exposes a {@link ResonanceStorage} (RU buffer) as a Team Reborn
 * {@link EnergyStorage}, 1 RU = 1 E. Lets other tech mods read and feed the
 * Resonance grid through a Resonator or Conduit Coupler. Transaction-safe via
 * {@link SnapshotParticipant}.
 */
public class ResonanceEnergyBridge extends SnapshotParticipant<Long> implements EnergyStorage {
    private final ResonanceStorage storage;
    private final BlockEntity be;

    public ResonanceEnergyBridge(ResonanceStorage storage, BlockEntity be) {
        this.storage = storage;
        this.be = be;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long accepted = storage.insert(maxAmount, true);
        if (accepted > 0) {
            updateSnapshots(transaction);
            storage.insert(accepted, false);
        }
        return accepted;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        long removed = storage.extract(maxAmount, true);
        if (removed > 0) {
            updateSnapshots(transaction);
            storage.extract(removed, false);
        }
        return removed;
    }

    @Override public long getAmount() { return storage.getAmount(); }
    @Override public long getCapacity() { return storage.getCapacity(); }

    @Override protected Long createSnapshot() { return storage.getAmount(); }
    @Override protected void readSnapshot(Long snapshot) { storage.setAmount(snapshot); }
    @Override protected void onFinalCommit() { be.markDirty(); }
}
