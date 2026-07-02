package com.echoes.block;

import com.echoes.block.entity.AttunementFurnaceBlockEntity;
import com.echoes.energy.ResonanceNetworkManager;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.MenuProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Smelts vanilla furnace recipes with RU drawn from the network. */
public class AttunementFurnaceBlock extends Block implements EntityBlock {

    public AttunementFurnaceBlock(Properties settings) {
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
        return new AttunementFurnaceBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide() || type != ModBlockEntities.ATTUNEMENT_FURNACE) return null;
        return (w, p, s, be) -> AttunementFurnaceBlockEntity.tick(w, p, s, (AttunementFurnaceBlockEntity) be);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof MenuProvider factory) {
            if (world.getBlockEntity(pos) instanceof com.echoes.config.Configurable cfg) {
                // First opener owns an unclaimed machine (mirrors the config screen),
                // so the builder claims theirs by using it before a stranger can.
                if (cfg.getConfig().owner() == null) {
                    cfg.getConfig().claim(player.getUUID());
                    cfg.onConfigChanged(); // persist the claim
                }
                if (!cfg.getConfig().canAccess(player.getUUID())) {
                    player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.echoes.locked"));
                    return InteractionResult.SUCCESS;
                }
            }
            player.openMenu(factory);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState old, boolean notify) {
        if (world instanceof ServerLevel sw && !old.is(this)) {
            ResonanceNetworkManager.get(sw).onAttachedNodeChanged(pos.immutable());
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (world.getBlockEntity(pos) instanceof AttunementFurnaceBlockEntity be) {
            Containers.dropContents(world, pos, be.getItems());
        }
        ResonanceNetworkManager.get(world).onAttachedNodeChanged(pos.immutable());
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
}
