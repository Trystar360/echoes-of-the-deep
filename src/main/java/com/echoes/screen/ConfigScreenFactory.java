package com.echoes.screen;

import com.echoes.config.Configurable;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

/**
 * Opens the shared {@link ConfigScreenHandler} for a {@link Configurable} device,
 * sending its position as the screen-opening data so both sides resolve the same
 * block entity.
 */
public record ConfigScreenFactory(Configurable target, BlockPos pos)
        implements ExtendedMenuProvider<BlockPos> {

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return pos;
    }

    @Override
    public Component getDisplayName() {
        return target.configTitle();
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new ConfigScreenHandler(syncId, inv, pos);
    }
}
