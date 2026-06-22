package com.echoes.screen;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

/**
 * Opens the read-only {@link InfoScreenHandler} for an energy block, sending the
 * block's position so both sides inspect the same block entity. {@code title} is
 * the block's display name.
 */
public record InfoScreenFactory(Component title, BlockPos pos)
        implements ExtendedMenuProvider<BlockPos> {

    @Override public BlockPos getScreenOpeningData(ServerPlayer player) { return pos; }

    @Override public Component getDisplayName() { return title; }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new InfoScreenHandler(syncId, inv, pos);
    }
}
