package com.echoes.block;

import com.echoes.block.entity.FabricatorBlockEntity;
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

/**
 * Autocrafts the shaped recipe laid out in its 3x3 grid, drawing RU from the
 * network — the AE2-style crafting endpoint of the resonance grid. Ingredients
 * arrive by hopper/pipe (or hand), finished items leave through any face whose
 * I/O mode allows output.
 */
public class FabricatorBlock extends Block implements EntityBlock {

    public FabricatorBlock(Properties settings) {
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
        return new FabricatorBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state,
            net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        com.echoes.config.Configurable.claimOnPlace(world, pos, placer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide() || type != ModBlockEntities.FABRICATOR) return null;
        return (w, p, s, be) -> com.echoes.block.entity.AbstractMachineBlockEntity.tick(
                w, p, s, (com.echoes.block.entity.AbstractMachineBlockEntity) be);
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

    /**
     * Pattern cards: Blank Pattern saves the fabricator's current 3x3 grid onto
     * a card (AE2's "encode pattern"); an Encoded Pattern loads its layout into
     * the machine's template memory, which then keeps the grid restocked from
     * adjacent inventories / the wireless network. Everything else falls
     * through to the normal GUI open.
     */
    @Override
    protected InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state,
            Level world, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        boolean isBlank = stack.is(com.echoes.registry.ModItems.BLANK_PATTERN);
        boolean isEncoded = stack.is(com.echoes.registry.ModItems.ENCODED_PATTERN);
        if (!isBlank && !isEncoded) {
            return useWithoutItem(state, world, pos, player, hit);
        }
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof FabricatorBlockEntity be)) return InteractionResult.PASS;
        if (be instanceof com.echoes.config.Configurable cfg) {
            if (cfg.getConfig().owner() == null) {
                cfg.getConfig().claim(player.getUUID());
                cfg.onConfigChanged();
            }
            if (!cfg.getConfig().canAccess(player.getUUID())) {
                player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.echoes.locked"));
                return InteractionResult.SUCCESS;
            }
        }
        if (isBlank) {
            net.minecraft.world.item.ItemStack encoded = be.savePattern();
            if (encoded.isEmpty()) {
                player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.echoes.pattern.nothing"));
                return InteractionResult.SUCCESS;
            }
            stack.shrink(1);
            player.getInventory().placeItemBackInInventory(encoded);
            player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.echoes.pattern.saved"));
        } else {
            if (be.loadPattern(stack)) {
                player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.echoes.pattern.loaded"));
            } else {
                player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.echoes.pattern.invalid"));
            }
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
        if (world.getBlockEntity(pos) instanceof FabricatorBlockEntity be) {
            Containers.dropContents(world, pos, be.getItems());
        }
        ResonanceNetworkManager.get(world).onAttachedNodeChanged(pos.immutable());
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
}
