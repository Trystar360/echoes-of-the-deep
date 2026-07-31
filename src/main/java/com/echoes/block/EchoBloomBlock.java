package com.echoes.block;

import com.echoes.registry.ModBlocks;
import com.echoes.registry.ModItems;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

/**
 * The Echo Bloom — Mystical-Agriculture-style resource crop. Grown from Echo
 * Bloom Seeds (on farmland or, better, Verdant Loam), it matures through 8
 * stages and yields Echo Essence, which condenses into Radiant Dust. As a
 * vanilla {@link CropBlock} it is bonemealable, so both the Verdant Loam and
 * the Growth Radiator auras accelerate it — the botanical end of the mod's
 * "spend Light to grow matter" interchange.
 */
public class EchoBloomBlock extends CropBlock {

    public EchoBloomBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.ECHO_BLOOM_SEEDS;
    }

    /** Farmland works, but living soil is the natural bed. */
    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
        return super.mayPlaceOn(floor, world, pos) || floor.is(ModBlocks.VERDANT_LOAM);
    }
}
