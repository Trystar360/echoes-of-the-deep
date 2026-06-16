package com.echoes.block;

import com.echoes.block.entity.ResonantAmplifierBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/** Widens the per-tick transfer budget of its channel. Each one doubles it (capped). */
public class ResonantAmplifierBlock extends AbstractChannelDeviceBlock {

    public ResonantAmplifierBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantAmplifierBlockEntity(pos, state);
    }
}
