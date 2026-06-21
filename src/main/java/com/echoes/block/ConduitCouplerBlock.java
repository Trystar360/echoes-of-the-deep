package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.block.entity.ConduitCouplerBlockEntity;
import com.echoes.energy.ResonanceNetworkManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Bridges the wired Resonance grid and a wireless channel. Place it against a
 * Tuning Conduit; empty-hand right-click cycles the bridge direction (Send pushes
 * wired RU onto the channel, Receive pulls channel RU into the grid).
 */
public class ConduitCouplerBlock extends AbstractHorizontalDeviceBlock {

    public ConduitCouplerBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConduitCouplerBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult onConfigure(Level world, BlockPos pos, Player player,
                                       AbstractChannelDeviceBlockEntity device, ItemStack held) {
        if (device instanceof ConduitCouplerBlockEntity coupler) {
            coupler.cycleMode();
            sendStatus(player, "message.echoes.relay.mode",
                    Component.translatable("message.echoes.relay.mode." + coupler.mode().name().toLowerCase()));
        }
        return InteractionResult.SUCCESS;
    }

    // --- wired Resonance network membership (it joins the grid as a STORAGE node) ---
    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState old, boolean notify) {
        if (world instanceof ServerLevel sw && !old.is(this)) {
            ResonanceNetworkManager.get(sw).onAttachedNodeChanged(pos.immutable());
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        ResonanceNetworkManager.get(world).onAttachedNodeChanged(pos.immutable());
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
}
