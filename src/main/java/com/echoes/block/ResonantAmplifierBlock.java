package com.echoes.block;

import com.echoes.block.entity.ResonantAmplifierBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/** Widens the per-tick transfer budget of its channel. Each one doubles it (capped). */
public class ResonantAmplifierBlock extends AbstractHorizontalDeviceBlock {

    public ResonantAmplifierBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantAmplifierBlockEntity(pos, state);
    }
}
