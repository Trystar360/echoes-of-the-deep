package com.echoes.block.entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.echoes.registry.ModBlockEntities;
import com.echoes.wireless.RelayMode;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;

/**
 * A wireless redstone bus. SEND broadcasts the redstone power it receives onto
 * its channel; RECEIVE emits the channel's strongest broadcast as redstone power.
 * Turns each channel into a frequency you can wire signals across.
 */
public class NoteRelayBlockEntity extends AbstractChannelDeviceBlockEntity {

    private RelayMode mode = RelayMode.RECEIVE;
    private int output; // last delivered channel level (RECEIVE mode)

    public NoteRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NOTE_RELAY, pos, state);
    }

    public RelayMode mode() { return mode; }

    public void cycleMode() {
        mode = mode.next();
        if (mode != RelayMode.RECEIVE) setOutput(0);
        sync();
    }

    /** Power this relay currently emits to its neighbours. */
    public int redstonePower() { return mode == RelayMode.RECEIVE ? output : 0; }

    // --- WirelessDevice (redstone) ---
    @Override public int redstoneOut() {
        return mode == RelayMode.SEND && world != null ? world.getReceivedRedstonePower(getBlockPos()) : -1;
    }

    @Override public void acceptRedstone(int level) {
        if (mode == RelayMode.RECEIVE) setOutput(level);
    }

    private void setOutput(int level) {
        if (level == output || world == null) return;
        output = level;
        setChanged();
        BlockState state = getBlockState();
        boolean powered = output > 0;
        if (state.contains(BlockStateProperties.POWERED) && state.get(BlockStateProperties.POWERED) != powered) {
            world.setBlockAndUpdate(getBlockPos(), state.setValue(BlockStateProperties.POWERED, powered));
        }
        world.updateNeighborsAlways(getBlockPos(), state.getBlock());
    }

    @Override
    protected void writeExtra(CompoundTag nbt, HolderLookup.Provider lookup) {
        nbt.putInt("mode", mode.ordinal());
        nbt.putInt("output", output);
    }

    @Override
    protected void readExtra(CompoundTag nbt, HolderLookup.Provider lookup) {
        mode = RelayMode.byId(nbt.getInt("mode"));
        output = nbt.getInt("output");
    }
}
