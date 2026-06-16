package com.echoes.block;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Shared behaviour for the wireless gadget family: a server ticker that keeps the
 * block on the channel roster, and a uniform tuning scheme —
 * <ul>
 *   <li>right-click with any <b>dye</b> → tune to that colour's channel</li>
 *   <li><b>sneak</b> + right-click (empty hand) → step the channel forward</li>
 *   <li>right-click otherwise → {@link #onConfigure device-specific action}</li>
 * </ul>
 */
public abstract class AbstractChannelDeviceBlock extends Block implements BlockEntityProvider {

    protected AbstractChannelDeviceBlock(Settings settings) {
        super(settings);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) return null;
        return (w, p, s, be) -> {
            if (be instanceof AbstractChannelDeviceBlockEntity d) AbstractChannelDeviceBlockEntity.tick(w, p, s, d);
        };
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof AbstractChannelDeviceBlockEntity device)) return ActionResult.PASS;

        ItemStack held = player.getMainHandStack();
        if (held.getItem() instanceof DyeItem dye) {
            device.setChannel(dye.getColor().getId());
            sendChannel(player, device.channel());
            return ActionResult.SUCCESS;
        }
        if (player.isSneaking() && held.isEmpty()) {
            device.cycleChannel();
            sendChannel(player, device.channel());
            return ActionResult.SUCCESS;
        }
        return onConfigure(world, pos, player, device, held);
    }

    /** Device-specific right-click (empty hand or a non-dye item). Default: report channel. */
    protected ActionResult onConfigure(World world, BlockPos pos, PlayerEntity player,
                                       AbstractChannelDeviceBlockEntity device, ItemStack held) {
        sendChannel(player, device.channel());
        return ActionResult.SUCCESS;
    }

    protected static void sendChannel(PlayerEntity player, int channel) {
        DyeColor color = DyeColor.byId(channel);
        player.sendMessage(Text.translatable("message.echoes.channel",
                Text.translatable("color.minecraft." + color.getName())), true);
    }

    protected static void sendStatus(PlayerEntity player, String translationKey, Object... args) {
        player.sendMessage(Text.translatable(translationKey, args), true);
    }
}
