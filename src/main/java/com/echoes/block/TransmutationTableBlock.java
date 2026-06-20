package com.echoes.block;

import com.echoes.block.entity.TransmutationTableBlockEntity;
import com.echoes.screen.TransmutationTableScreenHandler;
import com.echoes.transmute.TransmutationState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The balanced-interchange altar — a terminal into the opening player's Bound-Light
 * account (pool + attuned tones). Dissolve matter into Light Value, withdraw it as Mote
 * coins, or condense an attuned item back out. The portable {@code TransmutationTablet}
 * opens the same account.
 */
public class TransmutationTableBlock extends Block implements BlockEntityProvider {

    public TransmutationTableBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TransmutationTableBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            // Migrate any Light banked by the pre-account version into the opening player.
            if (world.getBlockEntity(pos) instanceof TransmutationTableBlockEntity be && be.legacyLight() > 0
                    && world instanceof ServerWorld sw) {
                TransmutationState.get(sw).of(player.getUuid()).light += be.drainLegacyLight();
                TransmutationState.get(sw).markDirty();
            }
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inv, p) -> new TransmutationTableScreenHandler(syncId, inv),
                    Text.translatable("block.echoes.transmutation_table")));
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())
                && world.getBlockEntity(pos) instanceof TransmutationTableBlockEntity be) {
            be.dropBankedLight(world, pos); // legacy banked Light → Mote coins, never silently lost
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
