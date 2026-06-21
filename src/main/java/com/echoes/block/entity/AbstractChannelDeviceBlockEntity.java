package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.wireless.WirelessDevice;
import com.echoes.wireless.WirelessNetworkManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Common base for every wireless gadget: it carries a channel (now part of a full
 * {@link BlockConfig}), registers itself with the {@link WirelessNetworkManager}
 * once loaded, keeps the roster in sync when its channel changes, and persists the
 * configuration in NBT. Subclasses add the capability (transport, amplify, filter,
 * …) by overriding {@link WirelessDevice}.
 */
public abstract class AbstractChannelDeviceBlockEntity extends BlockEntity
        implements WirelessDevice, Configurable {

    /** Wireless devices expose channel, octave, redstone behaviour and per-face I/O. */
    public static final ConfigSpec SPEC = ConfigSpec.builder()
            .channel().octave().redstone().sides().build();

    protected final BlockConfig config = new BlockConfig();
    private boolean registered = false;

    protected AbstractChannelDeviceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Lightweight ticker: guarantees the device is on the roster once loaded. */
    public static void tick(Level world, BlockPos pos, BlockState state, AbstractChannelDeviceBlockEntity be) {
        if (be.registered || !(world instanceof ServerLevel)) return;
        WirelessNetworkManager.register(be);
        be.registered = true;
    }

    public int channel() { return config.channel(); }

    public void setChannel(int value) {
        config.setChannel(value);
        sync();
    }

    public void cycleChannel() { setChannel(config.channel() + 1); }

    /** Re-register with the manager and persist. Call after any networked change. */
    protected void sync() {
        setChanged();
        if (world instanceof ServerLevel) {
            WirelessNetworkManager.register(this);
            registered = true;
        }
    }

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Component configTitle() { return getCachedState().getBlock().getName(); }
    @Override public void onConfigChanged() { sync(); }

    // --- WirelessDevice ---
    @Override public BlockPos wirelessPos() { return getPos(); }
    @Override public ServerLevel wirelessWorld() { return (ServerLevel) world; }
    @Override public int wirelessChannel() { return config.channel(); }

    @Override
    public void markRemoved() {
        if (world instanceof ServerLevel sw) WirelessNetworkManager.unregister(sw, getPos());
        registered = false;
        super.markRemoved();
    }

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        config.writeNbt(nbt);
        writeExtra(nbt, lookup);
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        config.readNbt(nbt);
        if (nbt.contains("channel")) config.setChannel(nbt.getInt("channel")); // legacy saves
        registered = false; // re-register against the (possibly new) world roster
        readExtra(nbt, lookup);
    }

    protected void writeExtra(CompoundTag nbt, HolderLookup.Provider lookup) {}
    protected void readExtra(CompoundTag nbt, HolderLookup.Provider lookup) {}
}
