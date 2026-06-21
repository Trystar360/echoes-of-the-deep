package com.echoes.block;

import com.echoes.block.entity.PolarityFieldBlockEntity;
import com.echoes.energy.ResonanceNetworkManager;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** The two poles in one device: right-click toggles Attract (pull) / Repel (push). */
public class PolarityFieldBlock extends Block implements EntityBlock {

    public PolarityFieldBlock(Properties settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(BlockStateProperties.LIT, false));
    }

    @Override
    protected void appendProperties(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.LIT);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PolarityFieldBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient || type != ModBlockEntities.POLARITY_FIELD) return null;
        return (w, p, s, be) -> PolarityFieldBlockEntity.tick(w, p, s, (PolarityFieldBlockEntity) be);
    }

    @Override
    protected InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClient) return InteractionResult.SUCCESS;
        if (world.getBlockEntity(pos) instanceof PolarityFieldBlockEntity be) {
            boolean attract = be.toggle();
            player.sendMessage(Component.translatable(attract
                    ? "message.echoes.polarity.attract" : "message.echoes.polarity.repel"), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onBlockAdded(BlockState state, Level world, BlockPos pos, BlockState old, boolean notify) {
        if (world instanceof ServerLevel sw && !old.isOf(this)) {
            ResonanceNetworkManager.get(sw).onAttachedNodeChanged(pos.immutable());
        }
    }

    @Override
    public void onStateReplaced(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && world instanceof ServerLevel sw) {
            ResonanceNetworkManager.get(sw).onAttachedNodeChanged(pos.immutable());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
