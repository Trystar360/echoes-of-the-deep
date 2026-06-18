package com.echoes.block;

import com.echoes.block.entity.RadiatorBlockEntity;
import com.echoes.energy.ResonanceNetworkManager;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** Radiates Light into the world as a crop-growth aura; glows while charged. */
public class RadiatorBlock extends Block implements BlockEntityProvider {

    public RadiatorBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(Properties.LIT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.LIT);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RadiatorBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient || type != ModBlockEntities.RADIATOR) return null;
        return (w, p, s, be) -> RadiatorBlockEntity.tick(w, p, s, (RadiatorBlockEntity) be);
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
}
