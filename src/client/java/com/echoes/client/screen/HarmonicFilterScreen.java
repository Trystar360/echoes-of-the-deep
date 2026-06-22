package com.echoes.client.screen;

import com.echoes.screen.HarmonicFilterScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

/** The Wave Filter screen — Obsidian &amp; Gold, drawn programmatically (no baked texture). */
public class HarmonicFilterScreen extends AbstractContainerScreen<HarmonicFilterScreenHandler> {

    public HarmonicFilterScreen(HarmonicFilterScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, GuiPaint.f(title));
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new ExpandingTab(leftPos, topPos + 6, GuiPaint.IN, "i",
                Component.translatable("screen.echoes.tab.info"), font,
                ExpandingTab.menuButton(menu.containerId, HarmonicFilterScreenHandler.B_INFO)));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        GuiPaint.panel(g, leftPos, topPos, imageWidth, imageHeight);
        // The 3×3 whitelist grid is a filter (input-side); ring it ice-blue.
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                GuiPaint.slot(g, leftPos + 62 + c * 18, topPos + 18 + r * 18, GuiPaint.IN);
        GuiPaint.playerSlots(g, leftPos + 8, topPos + 84, topPos + 142);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        GuiPaint.ioKeyV(g, font, 8, 20);
    }
}
