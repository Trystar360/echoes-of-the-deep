package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

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
    protected void writeExtra(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.putBoolean("roundRobin", roundRobin);
    }

    @Override
    protected void readExtra(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        roundRobin = !nbt.contains("roundRobin") || nbt.getBoolean("roundRobin");
    }
}
