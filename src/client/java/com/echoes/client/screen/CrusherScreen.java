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
        this.titleLabelY = -1000;       // drawn bright in the header band
        this.inventoryLabelY = -1000;   // hide the dark vanilla "Inventory" label
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        // Light gauge (fills bottom-up, sprite at atlas 200,0 — 7×40).
        int cap = menu.maxRu();
        if (cap > 0) {
            int fh = Math.min(40, (int) (menu.storedRu() * 40L / cap));
            if (fh > 0)
                g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 21, topPos + 24 + (40 - fh),
                        200, 40 - fh, 7, fh, 256, 256);
        }
        int max = menu.maxProgress();
        if (max > 0) {
            int w = menu.progress() * 24 / max;
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 79, topPos + 34, 176, 0, w, 16, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, title, 8, 6, GuiTheme.TEXT, false);
        // Right-aligned in the title row so it doesn't overlap the block-name title.
        Component ru = Component.literal(menu.storedRu() + " Light");
        g.text(font, ru, imageWidth - font.width(ru) - 8, 6, GuiTheme.ACCENT, false);
    }
}
