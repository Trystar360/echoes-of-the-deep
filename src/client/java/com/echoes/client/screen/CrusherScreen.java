package com.echoes.client.screen;

import com.echoes.EchoesMod;
import com.echoes.screen.CrusherScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class CrusherScreen extends AbstractContainerScreen<CrusherScreenHandler> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "textures/gui/compressor.png");

    public CrusherScreen(CrusherScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int max = menu.maxProgress();
        if (max > 0) {
            int w = menu.progress() * 24 / max;
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 79, topPos + 34, 176, 0, w, 16, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, Component.literal(menu.storedRu() + " Light"), 8, 6, 0x404040, false);
    }
}
