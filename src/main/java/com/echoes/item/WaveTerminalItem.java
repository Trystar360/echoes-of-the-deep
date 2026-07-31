package com.echoes.item;

import com.echoes.block.entity.AbstractChannelDeviceBlockEntity;
import com.echoes.block.entity.ResonantChestBlockEntity;
import com.echoes.wireless.WirelessDevice;
import com.echoes.wireless.WirelessNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Container;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * The Wave Terminal — AE2's wireless terminal, Echoes-style. Sneak-right-click any
 * channel device to bind the terminal to its channel; right-click anywhere else to
 * open that channel's Resonant Chest storage remotely, from anywhere in the
 * dimension.
 *
 * <p>The remote view is a live merged container (up to two chests / 54 slots) built
 * from the chests currently registered on the bound channel — items you take or
 * insert go straight into the real blocks, riding vanilla generic-container menus,
 * so there is no client-side code at all.
 *
 * <p>Only chests you may access (owner/claim rules) and whose redstone mode
 * currently allows operation are reachable, so the terminal can't bypass a locked
 * or redstone-disabled network.
 */
public class WaveTerminalItem extends Item {
    private static final String KEY_CHANNEL = "channel";
    private static final int MAX_CHESTS = 2;

    public WaveTerminalItem(Properties settings) {
        super(settings);
    }

    // --- binding ---

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || context.getLevel().isClientSide()) return InteractionResult.PASS;
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!(context.getLevel().getBlockEntity(context.getClickedPos())
                instanceof AbstractChannelDeviceBlockEntity device)) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(context.getHand());
        store(stack, device.channel());
        player.sendOverlayMessage(Component.translatable("message.echoes.terminal.bound",
                colorName(device.channel())));
        return InteractionResult.SUCCESS;
    }

    // --- remote open ---

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        int channel = boundChannel(stack);
        if (channel < 0) {
            user.sendOverlayMessage(Component.translatable("message.echoes.terminal.unbound"));
            return InteractionResult.SUCCESS;
        }

        List<ResonantChestBlockEntity> chests = reachableChests(world, user, channel);
        if (chests.isEmpty()) {
            user.sendOverlayMessage(Component.translatable("message.echoes.terminal.empty",
                    colorName(channel)));
            return InteractionResult.SUCCESS;
        }

        Container view = chests.size() == 1 ? chests.get(0)
                : new CompoundContainer(chests.get(0), chests.get(1));
        Component title = Component.translatable("item.echoes.wave_terminal");
        MenuProvider provider = new SimpleMenuProvider(
                (syncId, inv, p) -> chests.size() == 1
                        ? ChestMenu.threeRows(syncId, inv, view)
                        : ChestMenu.sixRows(syncId, inv, view),
                title);
        user.openMenu(provider);
        return InteractionResult.SUCCESS;
    }

    /**
     * Resonant Chests on {@code channel} in the player's dimension that the player
     * is allowed to access and whose redstone mode currently permits operation
     * (local signal — the wireless bus is recomputed inside the network tick).
     * Capped at {@value #MAX_CHESTS} chests so the merged view fits a six-row menu.
     */
    private static List<ResonantChestBlockEntity> reachableChests(Level world, Player player, int channel) {
        List<ResonantChestBlockEntity> out = new ArrayList<>(MAX_CHESTS);
        for (WirelessDevice d : WirelessNetworkManager.devicesOnChannel(channel)) {
            if (!(d instanceof ResonantChestBlockEntity chest)) continue;
            if (chest.isRemoved() || chest.wirelessWorld() != world) continue;
            if (!chest.getConfig().canAccess(player.getUUID())) continue;
            BlockPos pos = chest.wirelessPos();
            if (!chest.redstoneMode().allows(world.hasNeighborSignal(pos))) continue;
            out.add(chest);
            if (out.size() >= MAX_CHESTS) break;
        }
        return out;
    }

    /** The bound channel id (0–15), or -1 when this terminal is unbound. */
    public static int boundChannel(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) return -1;
        CompoundTag tag = data.copyTag();
        return tag.getInt(KEY_CHANNEL).orElse(-1);
    }

    private static void store(ItemStack stack, int channel) {
        CustomData comp = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = comp == null ? new CompoundTag() : comp.copyTag();
        nbt.putInt(KEY_CHANNEL, channel);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }

    private static Component colorName(int channel) {
        return Component.translatable("color.minecraft." + DyeColor.byId(channel).getName());
    }
}
