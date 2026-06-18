package com.echoes.block;

import com.echoes.block.entity.StillnessCoreBlockEntity;
import com.echoes.energy.ResonanceNetworkManager;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** The still centre: a slow Light generator that feeds the grid as a PROVIDER. */
public class StillnessCoreBlock extends Block implements BlockEntityProvider {

    public StillnessCoreBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StillnessCoreBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient || type != ModBlockEntities.STILLNESS_CORE) return null;
        return (w, p, s, be) -> StillnessCoreBlockEntity.tick(w, p, s, (StillnessCoreBlockEntity) be);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState old, boolean notify) {
        if (world instanceof ServerWorld sw && !old.isOf(this)) {
            ResonanceNetworkManager.get(sw).onAttachedNodeChanged(pos.toImmutable());
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && world instanceof ServerWorld sw) {
            ResonanceNetworkManager.get(sw).onAttachedNodeChanged(pos.toImmutable());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override public boolean hasComparatorOutput(BlockState state) { return true; }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof StillnessCoreBlockEntity be
                ? be.storage().comparatorOutput() : 0;
    }
}
