package com.echoes.block;

import com.echoes.block.entity.RadiatorBlockEntity;
import com.echoes.energy.ResonanceNetworkManager;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Radiates Light into the world as a crop-growth aura; glows while charged. */
public class RadiatorBlock extends Block implements EntityBlock {

    public RadiatorBlock(Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.LIT);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadiatorBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide() || type != ModBlockEntities.RADIATOR) return null;
        return (w, p, s, be) -> RadiatorBlockEntity.tick(w, p, s, (RadiatorBlockEntity) be);
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
}
