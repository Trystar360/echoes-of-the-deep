package com.echoes.client;

import com.echoes.client.screen.AttunementFurnaceScreen;
import com.echoes.client.screen.CrusherScreen;
import com.echoes.client.screen.HarmonicFilterScreen;
import com.echoes.registry.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class EchoesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreens.CRUSHER, CrusherScreen::new);
        HandledScreens.register(ModScreens.ATTUNEMENT_FURNACE, AttunementFurnaceScreen::new);
        HandledScreens.register(ModScreens.HARMONIC_FILTER, HarmonicFilterScreen::new);
    }
}
