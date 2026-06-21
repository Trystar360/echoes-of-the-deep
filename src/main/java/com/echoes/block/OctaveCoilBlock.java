package com.echoes.block;

import com.echoes.block.entity.OctaveCoilBlockEntity;
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

/** The higher-octave generator: a strong Light source that feeds the grid as a PROVIDER. */
public class OctaveCoilBlock extends Block implements EntityBlock {

    public OctaveCoilBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OctaveCoilBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide() || type != ModBlockEntities.OCTAVE_COIL) return null;
        return (w, p, s, be) -> OctaveCoilBlockEntity.tick(w, p, s, (OctaveCoilBlockEntity) be);
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
        return world.getBlockEntity(pos) instanceof OctaveCoilBlockEntity be
                ? be.storage().comparatorOutput() : 0;
    }
}
