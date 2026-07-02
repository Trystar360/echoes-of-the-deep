package com.echoes.block;

import com.echoes.block.entity.ConduitBlockEntity;
import com.echoes.energy.ResonanceNetworkManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Carries RU. Topology changes notify the per-world {@link ResonanceNetworkManager},
 * which keeps networks merged/split incrementally — no per-tick flood fill.
 */
public class ConduitBlock extends Block implements EntityBlock {

    public ConduitBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConduitBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState old, boolean notify) {
        if (world instanceof ServerLevel sw && !old.is(this)) {
            ResonanceNetworkManager.get(sw).onConduitPlaced(pos.immutable());
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        ResonanceNetworkManager.get(world).onConduitBroken(pos.immutable());
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
}
