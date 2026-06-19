package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.wireless.WirelessDevice;
import com.echoes.wireless.WirelessNetworkManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public static void tick(World world, BlockPos pos, BlockState state, AbstractChannelDeviceBlockEntity be) {
        if (be.registered || !(world instanceof ServerWorld)) return;
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
        markDirty();
        if (world instanceof ServerWorld) {
            WirelessNetworkManager.register(this);
            registered = true;
        }
    }

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Text configTitle() { return getCachedState().getBlock().getName(); }
    @Override public void onConfigChanged() { sync(); }

    // --- WirelessDevice ---
    @Override public BlockPos wirelessPos() { return getPos(); }
    @Override public ServerWorld wirelessWorld() { return (ServerWorld) world; }
    @Override public int wirelessChannel() { return config.channel(); }

    @Override
    public void markRemoved() {
        if (world instanceof ServerWorld sw) WirelessNetworkManager.unregister(sw, getPos());
        registered = false;
        super.markRemoved();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        config.writeNbt(nbt);
        writeExtra(nbt, lookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        config.readNbt(nbt);
        if (nbt.contains("channel")) config.setChannel(nbt.getInt("channel")); // legacy saves
        registered = false; // re-register against the (possibly new) world roster
        readExtra(nbt, lookup);
    }

    protected void writeExtra(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {}
    protected void readExtra(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {}
}
