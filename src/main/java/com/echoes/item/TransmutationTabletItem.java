package com.echoes.item;

import com.echoes.screen.TransmutationTableScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * The portable transmutation terminal: opens the same per-player Bound-Light account
 * (pool + attuned tones) as the Transmutation Table, from anywhere in your inventory.
 */
public class TransmutationTabletItem extends Item {
    public TransmutationTabletItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            user.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inv, p) -> new TransmutationTableScreenHandler(syncId, inv),
                    Text.translatable("item.echoes.transmutation_tablet")));
        }
        return ActionResult.SUCCESS;
    }
}
