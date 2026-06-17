package com.echoes.block.entity;

import com.echoes.energy.NodeRole;
import com.echoes.energy.ResonanceNode;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

/**
 * Pure carrier. Holds no buffer; only contributes its throughput cap to the
 * owning network's transfer budget. Network membership is tracked by
 * ResonanceNetworkManager, not here. The Dense Conduit subclass just reports a
 * larger {@link #transferCap()}.
 */
public class ConduitBlockEntity extends BlockEntity implements ResonanceNode {
    public static final int DEFAULT_TRANSFER = 1_000;

    public ConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONDUIT, pos, state);
    }

    protected ConduitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override public int roleMask() { return NodeRole.of(NodeRole.CONDUIT); }
    @Override public long extract(long max, boolean simulate) { return 0; }
    @Override public long insert(long max, boolean simulate) { return 0; }
    @Override public long demand() { return 0; }
    @Override public int transferCap() { return DEFAULT_TRANSFER; }
    @Override public BlockPos pos() { return getPos(); }
}
