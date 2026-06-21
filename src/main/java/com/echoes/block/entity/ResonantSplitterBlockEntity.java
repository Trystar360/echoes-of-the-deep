package com.echoes.block.entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
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
    protected void writeExtra(ValueOutput nbt) {
        nbt.putBoolean("roundRobin", roundRobin);
    }

    @Override
    protected void readExtra(ValueInput nbt) {
        roundRobin = nbt.getBooleanOr("roundRobin", true);
    }
}
