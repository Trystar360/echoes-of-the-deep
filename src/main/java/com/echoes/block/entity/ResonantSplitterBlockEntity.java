package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;

/**
 * Switches its channel's distribution policy. Enabled (default), cargo is shared
 * evenly round-robin across receivers; disabled, it falls back to fill-first.
 */
public class ResonantSplitterBlockEntity extends AbstractChannelDeviceBlockEntity {

    private boolean roundRobin = true;

    public ResonantSplitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANT_SPLITTER, pos, state);
    }

    @Override public boolean roundRobin() { return roundRobin; }

    public boolean toggle() {
        roundRobin = !roundRobin;
        sync();
        return roundRobin;
    }

    @Override
    protected void writeExtra(CompoundTag nbt, HolderLookup.Provider lookup) {
        nbt.putBoolean("roundRobin", roundRobin);
    }

    @Override
    protected void readExtra(CompoundTag nbt, HolderLookup.Provider lookup) {
        roundRobin = !nbt.contains("roundRobin") || nbt.getBoolean("roundRobin");
    }
}
