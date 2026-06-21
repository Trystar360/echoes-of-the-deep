package com.echoes.block.entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.echoes.energy.ResonanceNode;
import com.echoes.registry.ModBlockEntities;
import com.echoes.wireless.RelayMode;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * The flagship wireless transport endpoint. It wraps the inventory / tank /
 * energy node on its facing side and broadcasts on its channel: {@code SEND}
 * pushes that block's contents onto the channel, {@code RECEIVE} pulls cargo off
 * the channel into it. Holds no buffer of its own.
 */
public class ResonantRelayBlockEntity extends AbstractChannelDeviceBlockEntity {

    private RelayMode mode = RelayMode.RECEIVE;

    public ResonantRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANT_RELAY, pos, state);
    }

    public RelayMode mode() { return mode; }

    public void cycleMode() {
        mode = mode.next();
        sync();
    }

    public Direction facing() {
        return getBlockState().getOrEmpty(BlockStateProperties.FACING).orElse(Direction.NORTH);
    }

    /** The block this relay reads from / writes to. */
    private BlockPos attachedPos() { return getBlockPos().offset(facing()); }

    /** 0 when disabled, otherwise a rough channel indicator (1–15) for comparators. */
    public int comparatorOutput() {
        return mode == RelayMode.DISABLED ? 0 : Math.min(15, channel() + 1);
    }

    // --- WirelessDevice transport ---
    @Override public RelayMode transportMode() { return mode; }

    @Override public @Nullable Storage<ItemVariant> wirelessItems() {
        return ItemStorage.SIDED.find(level, attachedPos(), facing().getOpposite());
    }

    @Override public @Nullable Storage<FluidVariant> wirelessFluids() {
        return FluidStorage.SIDED.find(level, attachedPos(), facing().getOpposite());
    }

    @Override public @Nullable ResonanceNode wirelessEnergy() {
        return level.getBlockEntity(attachedPos()) instanceof ResonanceNode n ? n : null;
    }

    @Override
    protected void writeExtra(CompoundTag nbt, HolderLookup.Provider lookup) {
        nbt.putInt("mode", mode.ordinal());
    }

    @Override
    protected void readExtra(CompoundTag nbt, HolderLookup.Provider lookup) {
        mode = RelayMode.byId(nbt.getIntOr("mode", 0));
    }
}
