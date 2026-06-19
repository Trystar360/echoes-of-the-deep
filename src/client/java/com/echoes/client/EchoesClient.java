package com.echoes.client;

import com.echoes.client.screen.AttunementFurnaceScreen;
import com.echoes.client.screen.ConfigScreen;
import com.echoes.client.screen.CrusherScreen;
import com.echoes.client.screen.HarmonicFilterScreen;
import com.echoes.registry.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class EchoesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreens.CRUSHER, CrusherScreen::new);
        HandledScreens.register(ModScreens.ATTUNEMENT_FURNACE, AttunementFurnaceScreen::new);
        HandledScreens.register(ModScreens.HARMONIC_FILTER, HarmonicFilterScreen::new);
        HandledScreens.register(ModScreens.CONFIG, ConfigScreen::new);

        // Append a one-line description to every Echoes block/item that defines one
        // (lang key "tooltip.echoes.desc.<path>"). Guarded by hasTranslation so any
        // item without a description simply gets no extra line.
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            Identifier id = Registries.ITEM.getId(stack.getItem());
            if (!"echoes".equals(id.getNamespace())) {
                return;
            }
            String key = "tooltip.echoes.desc." + id.getPath();
            if (I18n.hasTranslation(key)) {
                lines.add(Text.translatable(key).formatted(Formatting.GRAY));
            }
        });
    }
}
