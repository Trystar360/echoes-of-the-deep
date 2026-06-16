package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.block.entity.ConduitCouplerBlockEntity;
import com.echoes.energy.ResonanceNetworkManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Bridges the wired Resonance grid and a wireless channel. Place it against a
 * Tuning Conduit; empty-hand right-click cycles the bridge direction (Send pushes
 * wired RU onto the channel, Receive pulls channel RU into the grid).
 */
public class ConduitCouplerBlock extends AbstractHorizontalDeviceBlock {

    public ConduitCouplerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ConduitCouplerBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onConfigure(World world, BlockPos pos, PlayerEntity player,
                                       AbstractChannelDeviceBlockEntity device, ItemStack held) {
        if (device instanceof ConduitCouplerBlockEntity coupler) {
            coupler.cycleMode();
            sendStatus(player, "message.echoes.relay.mode",
                    Text.translatable("message.echoes.relay.mode." + coupler.mode().name().toLowerCase()));
        }
        return ActionResult.SUCCESS;
    }

    // --- wired Resonance network membership (it joins the grid as a STORAGE node) ---
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState old, boolean notify) {
        if (world instanceof ServerWorld sw && !old.isOf(this)) {
            ResonanceNetworkManager.get(sw).onAttachedNodeChanged(pos.toImmutable());
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && world instanceof ServerWorld sw) {
            ResonanceNetworkManager.get(sw).onAttachedNodeChanged(pos.toImmutable());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
