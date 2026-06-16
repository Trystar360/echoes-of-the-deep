package com.echoes.block;

import com.echoes.block.entity.ConduitBlockEntity;
import com.echoes.energy.ResonanceNetworkManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Carries RU. Topology changes notify the per-world {@link ResonanceNetworkManager},
 * which keeps networks merged/split incrementally — no per-tick flood fill.
 */
public class ConduitBlock extends Block implements BlockEntityProvider {

    public ConduitBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ConduitBlockEntity(pos, state);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState old, boolean notify) {
        if (world instanceof ServerWorld sw && !old.isOf(this)) {
            ResonanceNetworkManager.get(sw).onConduitPlaced(pos.toImmutable());
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && world instanceof ServerWorld sw) {
            ResonanceNetworkManager.get(sw).onConduitBroken(pos.toImmutable());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
