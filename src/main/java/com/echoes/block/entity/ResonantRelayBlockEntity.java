package com.echoes.block.entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.echoes.energy.ResonanceNode;
import com.echoes.registry.ModBlockEntities;
import com.echoes.wireless.RelayMode;
import com.echoes.wireless.ServoFilter;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.level.block.state.BlockState;
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

    /** Servo filter (item ids) — a SEND relay with entries only extracts those items. */
    private final java.util.LinkedHashSet<String> filterIds = new java.util.LinkedHashSet<>();

    /** Current servo filter as item ids (may be empty = unfiltered). */
    public java.util.List<String> filterList() { return ServoFilter.list(filterIds); }

    /** Toggle one item in the servo filter; returns what happened for the chat message. */
    public ServoFilter.Toggle toggleFilter(net.minecraft.world.item.Item item) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString();
        ServoFilter.Toggle r = ServoFilter.toggle(filterIds, id);
        if (r != ServoFilter.Toggle.FULL) sync();
        return r;
    }

    public void cycleMode() {
        mode = mode.next();
        sync();
    }

    public Direction facing() {
        return getBlockState().getOptionalValue(BlockStateProperties.FACING).orElse(Direction.NORTH);
    }

    /** The block this relay reads from / writes to. */
    private BlockPos attachedPos() { return getBlockPos().relative(facing()); }

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

    /** Servo filter as resolved items (null when unfiltered); SEND relays only. */
    @Override public @Nullable java.util.Set<net.minecraft.world.item.Item> extractionFilter() {
        if (filterIds.isEmpty()) return null;
        java.util.Set<net.minecraft.world.item.Item> out = new java.util.HashSet<>();
        for (String id : filterIds) {
            net.minecraft.core.registries.BuiltInRegistries.ITEM
                    .getOptional(net.minecraft.resources.Identifier.parse(id))
                    .ifPresent(out::add);
        }
        return out.isEmpty() ? null : out;
    }

    @Override
    protected void writeExtra(ValueOutput nbt) {
        nbt.putInt("mode", mode.ordinal());
        nbt.putString("filter", ServoFilter.join(filterIds));
    }

    @Override
    protected void readExtra(ValueInput nbt) {
        mode = RelayMode.byId(nbt.getIntOr("mode", 0));
        filterIds.clear();
        filterIds.addAll(ServoFilter.parse(nbt.getStringOr("filter", "")));
    }
}
