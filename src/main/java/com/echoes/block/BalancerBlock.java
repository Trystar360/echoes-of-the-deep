package com.echoes.block;

import com.echoes.block.entity.BalancerBlockEntity;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** Equalizes Light across the storage on its network — balanced interchange. */
public class BalancerBlock extends Block implements BlockEntityProvider {

    public BalancerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BalancerBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient || type != ModBlockEntities.BALANCER) return null;
        return (w, p, s, be) -> BalancerBlockEntity.tick(w, p, s, (BalancerBlockEntity) be);
    }
}
