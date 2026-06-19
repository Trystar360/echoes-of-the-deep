package com.echoes.block;

import com.echoes.block.entity.OctaveConduitBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Highest-throughput conduit. Reuses {@link ConduitBlock}'s network place/break
 * hooks; only the block entity differs (a much larger transfer cap).
 */
public class OctaveConduitBlock extends ConduitBlock {

    public OctaveConduitBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new OctaveConduitBlockEntity(pos, state);
    }
}
