package com.echoes.block.entity;

import com.echoes.energy.ResonanceNode;
import com.echoes.registry.ModBlockEntities;
import com.echoes.wireless.RelayMode;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
        return getCachedState().getOrEmpty(Properties.FACING).orElse(Direction.NORTH);
    }

    /** The block this relay reads from / writes to. */
    private BlockPos attachedPos() { return getPos().offset(facing()); }

    /** 0 when disabled, otherwise a rough channel indicator (1–15) for comparators. */
    public int comparatorOutput() {
        return mode == RelayMode.DISABLED ? 0 : Math.min(15, channel + 1);
    }

    // --- WirelessDevice transport ---
    @Override public RelayMode transportMode() { return mode; }

    @Override public @Nullable Storage<ItemVariant> wirelessItems() {
        return ItemStorage.SIDED.find(world, attachedPos(), facing().getOpposite());
    }

    @Override public @Nullable Storage<FluidVariant> wirelessFluids() {
        return FluidStorage.SIDED.find(world, attachedPos(), facing().getOpposite());
    }

    @Override public @Nullable ResonanceNode wirelessEnergy() {
        return world.getBlockEntity(attachedPos()) instanceof ResonanceNode n ? n : null;
    }

    @Override
    protected void writeExtra(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.putInt("mode", mode.ordinal());
    }

    @Override
    protected void readExtra(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        mode = RelayMode.byId(nbt.getInt("mode"));
    }
}
