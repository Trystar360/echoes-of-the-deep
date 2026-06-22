package com.echoes.client.screen;

import com.echoes.EchoesMod;
import com.echoes.screen.HarmonicFilterScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class HarmonicFilterScreen extends AbstractContainerScreen<HarmonicFilterScreenHandler> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "textures/gui/wave_filter.png");

    public HarmonicFilterScreen(HarmonicFilterScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title);
        this.titleLabelY = -1000;       // drawn bright in the header band
        this.inventoryLabelY = -1000;   // hide the dark vanilla "Inventory" label
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, title, 8, 5, GuiTheme.TEXT, false);
    }
}
