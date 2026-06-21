package com.echoes.client;

import com.echoes.client.screen.AttunementFurnaceScreen;
import com.echoes.client.screen.ConfigScreen;
import com.echoes.client.screen.CrusherScreen;
import com.echoes.client.screen.HarmonicFilterScreen;
import com.echoes.client.screen.TransmutationTableScreen;
import com.echoes.registry.ModBlocks;
import com.echoes.registry.ModScreens;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.rendertype.RenderType;
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
        MenuScreens.register(ModScreens.TRANSMUTATION_TABLE, TransmutationTableScreen::new);

        // Append a one-line description to every Echoes block/item that defines one
        // (lang key "tooltip.echoes.desc.<path>"). Guarded by hasTranslation so any
        // item without a description simply gets no extra line.
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            Identifier id = BuiltInRegistries.ITEM.getId(stack.getItem());
            if (!"echoes".equals(id.getNamespace())) {
                return;
            }
            String key = "tooltip.echoes.desc." + id.getPath();
            if (I18n.hasTranslation(key)) {
                lines.add(Component.translatable(key).formatted(ChatFormatting.GRAY));
            }
        });

        // Cutout flora + trapdoor; mipped cutout for leaves.
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.getCutout(),
                ModBlocks.LUMEWOOD_SAPLING, ModBlocks.LUMEBLOOM,
                ModBlocks.LUMEWOOD_TRAPDOOR, ModBlocks.LUME_LANTERN,
                ModBlocks.GREATER_ACCUMULATOR);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.getCutoutMipped(),
                ModBlocks.LUMEWOOD_LEAVES);
    }
}
