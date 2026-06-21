package com.echoes.energy;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A simple bounded RU buffer. Block entities embed one of these and delegate
 * their extract/insert to it. All math is clamped and uses long to stay safe
 * across large capacitor banks.
 */
public class ResonanceStorage {
    private long amount;
    private long capacity;

    public ResonanceStorage(long capacity) {
        this.capacity = capacity;
    }

    public long getAmount() { return amount; }
    public long getCapacity() { return capacity; }
    public boolean isEmpty() { return amount <= 0; }
    public boolean isFull() { return amount >= capacity; }

    public void setCapacity(long capacity) {
        this.capacity = Math.max(0, capacity);
        if (amount > this.capacity) amount = this.capacity;
    }

    /** @return amount actually inserted. */
    public long insert(long max, boolean simulate) {
        long accepted = Math.min(max, capacity - amount);
        if (accepted < 0) accepted = 0;
        if (!simulate) amount += accepted;
        return accepted;
    }

    /** @return amount actually extracted. */
    public long extract(long max, boolean simulate) {
        long removed = Math.min(max, amount);
        if (removed < 0) removed = 0;
        if (!simulate) amount -= removed;
        return removed;
    }

    /** Used by ambient-capture providers; bypasses the simulate contract. */
    public void absorb(long ru) {
        amount = Math.min(capacity, amount + Math.max(0, ru));
    }

    /** Set the stored amount directly (clamped). Used for transaction snapshots. */
    public void setAmount(long value) {
        amount = Math.max(0, Math.min(capacity, value));
    }

    /** 0..15 for comparator output. */
    public int comparatorOutput() {
        if (capacity == 0) return 0;
        return (int) Math.round(15.0 * amount / capacity);
    }

    public void writeNbt(ValueOutput out) {
        out.putLong("ru", amount);
        out.putLong("ruCap", capacity);
    }

    public void readNbt(ValueInput in) {
        amount = in.getLongOr("ru", 0L);
        capacity = in.getLongOr("ruCap", capacity);
        if (amount > capacity) amount = capacity;
    }
}
