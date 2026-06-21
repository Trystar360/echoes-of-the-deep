package com.echoes.block;

import com.echoes.block.entity.StillnessCoreBlockEntity;
import com.echoes.energy.ResonanceNetworkManager;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** The still centre: a slow Light generator that feeds the grid as a PROVIDER. */
public class StillnessCoreBlock extends Block implements EntityBlock {

    public StillnessCoreBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StillnessCoreBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide() || type != ModBlockEntities.STILLNESS_CORE) return null;
        return (w, p, s, be) -> StillnessCoreBlockEntity.tick(w, p, s, (StillnessCoreBlockEntity) be);
    }

    @Override
    public void onBlockAdded(BlockState state, Level world, BlockPos pos, BlockState old, boolean notify) {
        if (world instanceof ServerLevel sw && !old.is(this)) {
            ResonanceNetworkManager.get(sw).onAttachedNodeChanged(pos.immutable());
        }
    }

    @Override
    public void onStateReplaced(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && world instanceof ServerLevel sw) {
            ResonanceNetworkManager.get(sw).onAttachedNodeChanged(pos.immutable());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override public boolean hasComparatorOutput(BlockState state) { return true; }

    @Override
    public int getComparatorOutput(BlockState state, Level world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof StillnessCoreBlockEntity be
                ? be.storage().comparatorOutput() : 0;
    }
}
