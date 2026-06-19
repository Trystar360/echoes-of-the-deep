package com.echoes.block;

import com.echoes.block.entity.VerdantLoamBlockEntity;
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

/** Living garden soil: a block entity drives the passive growth aura. */
public class VerdantLoamBlock extends Block implements BlockEntityProvider {

    public VerdantLoamBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VerdantLoamBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient || type != ModBlockEntities.VERDANT_LOAM) return null;
        return (w, p, s, be) -> VerdantLoamBlockEntity.tick(w, p, s, (VerdantLoamBlockEntity) be);
    }
}
