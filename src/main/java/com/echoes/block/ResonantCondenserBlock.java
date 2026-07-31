package com.echoes.block;

import com.echoes.block.entity.ResonantCondenserBlockEntity;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * The Resonant Condenser block (ProjectE's Energy Condenser). No GUI: held
 * items configure the target tone, empty-hand clicks report status, and
 * hoppers do all the item plumbing. Ownership follows the standard machine
 * claim rules — the bound account is the owner's transmutation ledger.
 */
public class ResonantCondenserBlock extends Block implements EntityBlock {

    public ResonantCondenserBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantCondenserBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        com.echoes.config.Configurable.claimOnPlace(world, pos, placer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide() || type != ModBlockEntities.RESONANT_CONDENSER) return null;
        return (w, p, s, be) -> ResonantCondenserBlockEntity.tick(w, p, s, (ResonantCondenserBlockEntity) be);
    }

    /** Empty hand: status readout (sneak = clear target). */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof ResonantCondenserBlockEntity be)) return InteractionResult.PASS;
        if (!checkAccess(be, player)) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) {
            be.clearTarget();
            player.sendOverlayMessage(Component.translatable("message.echoes.condenser.cleared"));
            return InteractionResult.SUCCESS;
        }
        long light = be.ownerLight((ServerLevel) world);
        if (be.target().isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.echoes.condenser.status.empty", light));
        } else {
            Component name = BuiltInRegistries.ITEM.getOptional(net.minecraft.resources.Identifier.parse(be.target()))
                    .map(i -> (Component) new ItemStack(i).getHoverName())
                    .orElse(Component.literal(be.target()));
            player.sendOverlayMessage(Component.translatable("message.echoes.condenser.status.target", name, light));
        }
        return InteractionResult.SUCCESS;
    }

    /** Held item: set the condenser's target tone (item not consumed). */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof ResonantCondenserBlockEntity be)) return InteractionResult.PASS;
        if (!checkAccess(be, player)) return InteractionResult.SUCCESS;
        be.setTarget(stack.getItem());
        player.sendOverlayMessage(Component.translatable("message.echoes.condenser.target", stack.getHoverName()));
        return InteractionResult.SUCCESS;
    }

    /** Standard machine ownership: first user claims, strangers are locked out. */
    private boolean checkAccess(ResonantCondenserBlockEntity be, Player player) {
        if (be.getConfig().owner() == null) {
            be.getConfig().claim(player.getUUID());
            be.onConfigChanged();
        }
        if (!be.getConfig().canAccess(player.getUUID())) {
            player.sendOverlayMessage(Component.translatable("message.echoes.locked"));
            return false;
        }
        return true;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (world.getBlockEntity(pos) instanceof ResonantCondenserBlockEntity be) {
            Containers.dropContents(world, pos, be.getItems());
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
}
