package com.echoes.block;

import com.echoes.block.entity.ResonanceCapacitorBlockEntity;
import com.echoes.energy.ResonanceNetworkManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Bulk RU storage. Joins the grid as a STORAGE node; comparator reads its fill. */
public class ResonanceCapacitorBlock extends Block implements EntityBlock {

    public ResonanceCapacitorBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonanceCapacitorBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState old, boolean notify) {
        if (world instanceof ServerLevel sw && !old.is(this)) {
            ResonanceNetworkManager.get(sw).onAttachedNodeChanged(pos.immutable());
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        ResonanceNetworkManager.get(world).onAttachedNodeChanged(pos.immutable());
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override public boolean hasComparatorOutput(BlockState state) { return true; }

    @Override
    public int getComparatorOutput(BlockState state, Level world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof ResonanceCapacitorBlockEntity be
                ? be.storage().comparatorOutput() : 0;
    }
}
