package com.echoes.screen;

import com.echoes.config.Configurable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Opens the shared {@link ConfigScreenHandler} for a {@link Configurable} device,
 * sending its position as the screen-opening data so both sides resolve the same
 * block entity.
 */
public record ConfigScreenFactory(Configurable target, BlockPos pos)
        implements ExtendedScreenHandlerFactory<BlockPos> {

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return pos;
    }

    @Override
    public Text getDisplayName() {
        return target.configTitle();
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ConfigScreenHandler(syncId, inv, pos);
    }
}
