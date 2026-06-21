package com.echoes.block;

import com.echoes.block.entity.VerdantLoamBlockEntity;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Living garden soil: a block entity drives the passive growth aura. */
public class VerdantLoamBlock extends Block implements EntityBlock {

    public VerdantLoamBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VerdantLoamBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide() || type != ModBlockEntities.VERDANT_LOAM) return null;
        return (w, p, s, be) -> VerdantLoamBlockEntity.tick(w, p, s, (VerdantLoamBlockEntity) be);
    }
}
