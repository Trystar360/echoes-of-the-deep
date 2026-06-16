package com.echoes.block;

import com.echoes.block.entity.ResonantRelayBlockEntity;
import com.echoes.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The flagship of the wireless transport family. Place it against a chest, tank,
 * machine, or Resonator; tune two or more relays to the same channel and they
 * resonate, beaming items, fluids, and RU between the blocks they face — no
 * conduit in between.
 *
 * <p>Interaction (right-click, no GUI needed):
 * <ul>
 *   <li>empty hand — cycle mode (Receive → Send → Disabled)</li>
 *   <li>sneak + empty hand — step the channel forward one colour</li>
 *   <li>any dye — jump straight to that colour's channel</li>
 * </ul>
 */
public class ResonantRelayBlock extends Block implements BlockEntityProvider {

    public ResonantRelayBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(Properties.FACING, net.minecraft.util.math.Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.FACING);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        // Face into the block we were placed against, so we wrap its inventory.
        return getDefaultState().with(Properties.FACING, ctx.getSide().getOpposite());
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ResonantRelayBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient || type != ModBlockEntities.RESONANT_RELAY) return null;
        return (w, p, s, be) -> ResonantRelayBlockEntity.tick(w, p, s, (ResonantRelayBlockEntity) be);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof ResonantRelayBlockEntity relay)) return ActionResult.PASS;

        ItemStack held = player.getMainHandStack();
        if (held.getItem() instanceof DyeItem dye) {
            DyeColor color = dye.getColor();
            relay.setChannel(color.getId());
            player.sendMessage(Text.translatable("message.echoes.relay.channel",
                    Text.translatable("color.minecraft." + color.getName())), true);
        } else if (player.isSneaking()) {
            relay.cycleChannel();
            DyeColor color = DyeColor.byId(relay.channel());
            player.sendMessage(Text.translatable("message.echoes.relay.channel",
                    Text.translatable("color.minecraft." + color.getName())), true);
        } else {
            relay.cycleMode();
            player.sendMessage(Text.translatable("message.echoes.relay.mode",
                    Text.translatable("message.echoes.relay.mode." + relay.mode().name().toLowerCase())), true);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof ResonantRelayBlockEntity relay
                ? relay.comparatorOutput() : 0;
    }
}
