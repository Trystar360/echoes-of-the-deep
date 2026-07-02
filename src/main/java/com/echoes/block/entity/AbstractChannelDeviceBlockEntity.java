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
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

/**
 * Common base for every wireless gadget: it carries a channel (now part of a full
 * {@link BlockConfig}), registers itself with the {@link WirelessNetworkManager}
 * once loaded, keeps the roster in sync when its channel changes, and persists the
 * configuration in NBT. Subclasses add the capability (transport, amplify, filter,
 * …) by overriding {@link WirelessDevice}.
 */
public abstract class AbstractChannelDeviceBlockEntity extends BlockEntity
        implements WirelessDevice, Configurable {

    /** Wireless devices expose channel, octave, and redstone behaviour. Per-face
     * I/O is only offered by subclasses with a real inventory to gate (the
     * Resonant Chest) — on everything else it would be a decoy control. */
    public static final ConfigSpec SPEC = ConfigSpec.builder()
            .channel().octave().redstone().build();

    protected final BlockConfig config = new BlockConfig();
    private boolean registered = false;

    protected AbstractChannelDeviceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Registers with the roster the moment this instance actually becomes part of a
     * loaded level — both on placement and on chunk load — instead of a per-tick
     * ticker whose entire job was polling one boolean until that same moment. No
     * {@link net.minecraft.world.level.block.entity.BlockEntityTicker} is registered
     * for this block family at all now (see {@code AbstractChannelDeviceBlock}).
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (!registered && level instanceof ServerLevel) {
            WirelessNetworkManager.register(this);
            registered = true;
        }
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
        if (level instanceof ServerLevel) {
            WirelessNetworkManager.register(this);
            registered = true;
        }
    }

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Component configTitle() { return getBlockState().getBlock().getName(); }
    @Override public void onConfigChanged() { sync(); }

    // --- WirelessDevice ---
    @Override public BlockPos wirelessPos() { return getBlockPos(); }
    @Override public ServerLevel wirelessWorld() { return (ServerLevel) level; }
    @Override public int wirelessChannel() { return config.channel(); }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel sw) WirelessNetworkManager.unregister(sw, getBlockPos());
        registered = false;
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        config.writeNbt(nbt);
        writeExtra(nbt);
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        config.readNbt(nbt);
        if (nbt.contains("channel")) config.setChannel(nbt.getIntOr("channel", 0)); // legacy saves
        registered = false; // re-register against the (possibly new) level roster
        readExtra(nbt);
    }

    protected void writeExtra(ValueOutput nbt) {}
    protected void readExtra(ValueInput nbt) {}
}
