package com.echoes.block.entity;

import com.echoes.config.BlockConfig;
import com.echoes.config.Configurable;
import com.echoes.config.ConfigSpec;
import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

/**
 * Pure carrier. Holds no buffer; only contributes its throughput cap to the
 * owning network's transfer budget. Network membership is tracked by
 * ResonanceNetworkManager, not here. The Dense Conduit subclass just reports a
 * larger {@link #transferCap()}.
 */
public class ConduitBlockEntity extends BlockEntity implements ResonanceNode, Configurable {
    public static final int DEFAULT_TRANSFER = 1_000;

    /** Conduits expose redstone behaviour and per-face I/O. */
    public static final ConfigSpec SPEC = ConfigSpec.builder().redstone().sides().build();

    protected final BlockConfig config = new BlockConfig();

    public ConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONDUIT, pos, state);
        config.applyDefaults(SPEC);
    }

    protected ConduitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        config.applyDefaults(SPEC);
    }

    @Override public int roleMask() { return NodeRole.of(NodeRole.CONDUIT); }
    @Override public long extract(long max, boolean simulate) { return 0; }
    @Override public long insert(long max, boolean simulate) { return 0; }
    @Override public long demand() { return 0; }
    @Override public int transferCap() { return DEFAULT_TRANSFER; }
    @Override public BlockPos pos() { return getBlockPos(); }

    // --- Configurable ---
    @Override public BlockConfig getConfig() { return config; }
    @Override public ConfigSpec getConfigSpec() { return SPEC; }
    @Override public Component configTitle() { return getBlockState().getBlock().getName(); }
    @Override public void onConfigChanged() { setChanged(); }

    @Override
    protected void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        config.writeNbt(nbt);
    }

    @Override
    protected void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        config.readNbt(nbt);
    }
}
