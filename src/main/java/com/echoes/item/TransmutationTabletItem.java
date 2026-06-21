package com.echoes.item;

import com.echoes.screen.TransmutationTableScreenHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

/**
 * The portable transmutation terminal: opens the same per-player Bound-Light account
 * (pool + attuned tones) as the Transmutation Table, from anywhere in your inventory.
 */
public class TransmutationTabletItem extends Item {
    public TransmutationTabletItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClient) {
            user.openHandledScreen(new SimpleMenuProvider(
                    (syncId, inv, p) -> new TransmutationTableScreenHandler(syncId, inv),
                    Component.translatable("item.echoes.transmutation_tablet")));
        }
        return InteractionResult.SUCCESS;
    }
}
