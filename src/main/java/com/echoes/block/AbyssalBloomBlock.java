package com.echoes.block;

import com.echoes.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

/**
 * The Abyssal Bloom — tier-3 resource crop, the deepest note of the farm line.
 * Bred from Radiant Bloom Seeds charged with a Harmonic Mote, it inherits the
 * tier-2 pickiness (living Verdant Loam only) and adds its own: it is a
 * Deep-Dark crop that grows only in darkness (raw light 4 or less). Sunlight
 * or a lit farm simply stalls it — a tier-3 field is a deliberate, shaded
 * build. Its harvest, Abyssal Essence, sits one full octave above Radiant
 * Essence in the Bound-Light scale and condenses into endgame matter
 * (Harmonic Motes, Netherite Scrap). Emits no light of its own: it drinks the
 * dark.
 */
public class AbyssalBloomBlock extends RadiantBloomBlock {

    /** Maximum raw brightness the bloom tolerates above itself while growing. */
    private static final int MAX_GROWTH_LIGHT = 4;

    public AbyssalBloomBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.ABYSSAL_BLOOM_SEEDS;
    }

    /** Tier-3 gating: darkness feeds the bloom; light stalls the tick entirely. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos.above(), 0) > MAX_GROWTH_LIGHT) return;
        super.randomTick(state, level, pos, random);
    }
}
