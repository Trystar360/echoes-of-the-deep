package com.echoes.block;

import com.echoes.block.entity.DenseConduitBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * High-throughput conduit. Reuses {@link ConduitBlock}'s network place/break hooks;
 * only the block entity differs (a larger transfer cap).
 */
public class DenseConduitBlock extends ConduitBlock {

    public DenseConduitBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DenseConduitBlockEntity(pos, state);
    }
}
