package com.echoes.block;

import com.echoes.block.entity.TransmutationTableBlockEntity;
import com.echoes.screen.TransmutationTableScreenHandler;
import com.echoes.transmute.TransmutationState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * The balanced-interchange altar — a terminal into the opening player's Bound-Light
 * account (pool + attuned tones). Dissolve matter into Light Value, withdraw it as Mote
 * coins, or condense an attuned item back out. The portable {@code TransmutationTablet}
 * opens the same account.
 */
public class TransmutationTableBlock extends Block implements EntityBlock {

    public TransmutationTableBlock(Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransmutationTableBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide()) {
            // Migrate any Light banked by the pre-account version into the opening player.
            if (world.getBlockEntity(pos) instanceof TransmutationTableBlockEntity be && be.legacyLight() > 0
                    && world instanceof ServerLevel sw) {
                TransmutationState.get(sw).of(player.getUUID()).light += be.drainLegacyLight();
                TransmutationState.get(sw).setDirty();
            }
            player.openMenu(new SimpleMenuProvider(
                    (syncId, inv, p) -> new TransmutationTableScreenHandler(syncId, inv),
                    Component.translatable("block.echoes.transmutation_table")));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (world.getBlockEntity(pos) instanceof TransmutationTableBlockEntity be) {
            be.dropBankedLight(world, pos); // legacy banked Light -> Mote coins, never silently lost
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
}
