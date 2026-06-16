package com.echoes.block.entity;

import com.echoes.registry.ModBlockEntities;
import com.echoes.wireless.RelayMode;
import com.echoes.wireless.WirelessNetworkManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * A wireless transport node. It is tuned to a {@code channel} (0–15, one per dye
 * colour) and a {@link RelayMode}; the {@link WirelessNetworkManager} pairs it
 * with every other relay on the same channel and moves items, fluids, and RU
 * between the blocks they face.
 *
 * <p>The relay holds no buffer of its own — it is a pure broadcast endpoint over
 * whatever inventory/tank/energy node sits on its facing side.
 */
public class ResonantRelayBlockEntity extends BlockEntity {

    private int channel = 0;
    private RelayMode mode = RelayMode.RECEIVE;
    private boolean registered = false;

    public ResonantRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANT_RELAY, pos, state);
    }

    /** Lightweight ticker: guarantees the relay is on the roster once loaded. */
    public static void tick(World world, BlockPos pos, BlockState state, ResonantRelayBlockEntity be) {
        if (be.registered || !(world instanceof ServerWorld sw)) return;
        WirelessNetworkManager.get(sw).register(pos, be.channel, be.mode, be.facing());
        be.registered = true;
    }

    public int channel() { return channel; }
    public RelayMode mode() { return mode; }

    public Direction facing() {
        return getCachedState().getOrEmpty(Properties.FACING).orElse(Direction.NORTH);
    }

    public void cycleMode() {
        mode = mode.next();
        syncToNetwork();
    }

    public void setChannel(int channel) {
        this.channel = ((channel % WirelessNetworkManager.CHANNELS) + WirelessNetworkManager.CHANNELS)
                % WirelessNetworkManager.CHANNELS;
        syncToNetwork();
    }

    public void cycleChannel() {
        setChannel(channel + 1);
    }

    private void syncToNetwork() {
        markDirty();
        if (world instanceof ServerWorld sw) {
            WirelessNetworkManager.get(sw).register(pos, channel, mode, facing());
            registered = true;
        }
    }

    /** 0 when disabled, otherwise a rough channel indicator (1–15) for comparators. */
    public int comparatorOutput() {
        return mode == RelayMode.DISABLED ? 0 : Math.min(15, channel + 1);
    }

    @Override
    public void markRemoved() {
        if (world instanceof ServerWorld sw) {
            WirelessNetworkManager.get(sw).unregister(pos);
        }
        registered = false;
        super.markRemoved();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        nbt.putInt("channel", channel);
        nbt.putInt("mode", mode.ordinal());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        channel = nbt.getInt("channel");
        mode = RelayMode.byId(nbt.getInt("mode"));
        registered = false; // re-register against the (possibly new) world roster
    }
}
