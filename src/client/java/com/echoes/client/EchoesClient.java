package com.echoes.client;

import com.echoes.client.screen.CrusherScreen;
import com.echoes.registry.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class EchoesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreens.CRUSHER, CrusherScreen::new);
    }
}
