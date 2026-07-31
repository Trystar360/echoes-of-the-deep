package com.echoes.block;

import com.echoes.registry.ModBlocks;
import com.echoes.registry.ModItems;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

/**
 * The Radiant Bloom — tier-2 resource crop (Mystical-Agriculture-style seed
 * upgrade). Bred from Echo Bloom Seeds charged with Radiant Dust, it is
 * pickier than its parent: it only takes root in living Verdant Loam, and its
 * harvest — Radiant Essence — condenses straight into Radiant Ingots, the
 * high-octave material. Bonemealable like any crop, so the same two growth
 * auras accelerate it; the Loam requirement means a tier-2 farm is always a
 * deliberate build, never an accident.
 */
public class RadiantBloomBlock extends EchoBloomBlock {

    public RadiantBloomBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.RADIANT_BLOOM_SEEDS;
    }

    /** Tier-2 gating: living soil only — no plain farmland. */
    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
        return floor.is(ModBlocks.VERDANT_LOAM);
    }
}
