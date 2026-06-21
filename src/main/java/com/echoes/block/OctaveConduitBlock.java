package com.echoes.block;

import com.echoes.block.entity.OctaveConduitBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Highest-throughput conduit. Reuses {@link ConduitBlock}'s network place/break
 * hooks; only the block entity differs (a much larger transfer cap).
 */
public class OctaveConduitBlock extends ConduitBlock {

    public OctaveConduitBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OctaveConduitBlockEntity(pos, state);
    }
}
