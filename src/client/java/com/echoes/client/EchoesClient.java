package com.echoes.client;

import com.echoes.client.screen.AttunementFurnaceScreen;
import com.echoes.client.screen.ConfigScreen;
import com.echoes.client.screen.CrusherScreen;
import com.echoes.client.screen.HarmonicFilterScreen;
import com.echoes.client.screen.InfoScreen;
import com.echoes.client.screen.TransmutationTableScreen;
import com.echoes.registry.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

public class EchoesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModScreens.CRUSHER, CrusherScreen::new);
        MenuScreens.register(ModScreens.ATTUNEMENT_FURNACE, AttunementFurnaceScreen::new);
        MenuScreens.register(ModScreens.HARMONIC_FILTER, HarmonicFilterScreen::new);
        MenuScreens.register(ModScreens.CONFIG, ConfigScreen::new);
        MenuScreens.register(ModScreens.INFO, InfoScreen::new);
        MenuScreens.register(ModScreens.TRANSMUTATION_TABLE, TransmutationTableScreen::new);

        // Append a one-line description to every Echoes block/item that defines one
        // (lang key "tooltip.echoes.desc.<path>"). Guarded by I18n#exists so any
        // item without a description simply gets no extra line.
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!"echoes".equals(id.getNamespace())) {
                return;
            }
            String key = "tooltip.echoes.desc." + id.getPath();
            if (I18n.exists(key)) {
                lines.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
            }
        });

        // 26.1: cutout / cutout-mipped render layers are declared per-model via the
        // model JSON "render_type" field (the Fabric BlockRenderLayerMap was removed),
        // so flora, trapdoor, lantern and leaves carry it in their models.
    }
}
