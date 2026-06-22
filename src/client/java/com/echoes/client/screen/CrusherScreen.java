package com.echoes.client.screen;

import com.echoes.screen.CrusherScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

/** The Compressor screen — Obsidian &amp; Gold, drawn programmatically (no baked texture). */
public class CrusherScreen extends AbstractContainerScreen<CrusherScreenHandler> {

    public CrusherScreen(CrusherScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, GuiPaint.f(title));
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new ExpandingTab(leftPos, topPos + 6, GuiPaint.IN, "i",
                Component.translatable("screen.echoes.tab.info"), font,
                ExpandingTab.menuButton(menu.containerId, CrusherScreenHandler.B_INFO)));
        addRenderableWidget(new ExpandingTab(leftPos, topPos + 28, GuiPaint.OUT, "C",
                Component.translatable("screen.echoes.tab.config"), font,
                ExpandingTab.menuButton(menu.containerId, CrusherScreenHandler.B_CONFIG)));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        GuiPaint.panel(g, leftPos, topPos, imageWidth, imageHeight);

        // Color-coded slots: input (ice-blue) -> output (gold) + byproduct (amethyst).
        GuiPaint.slot(g, leftPos + 56, topPos + 35, GuiPaint.IN);
        GuiPaint.slot(g, leftPos + 116, topPos + 35, GuiPaint.OUT);
        GuiPaint.slot(g, leftPos + 116, topPos + 57, GuiPaint.AUX);
        // Augment column (right): two amethyst wells.
        GuiPaint.slot(g, leftPos + CrusherScreenHandler.AUG0_X, topPos + CrusherScreenHandler.AUG0_Y, GuiPaint.AUX);
        GuiPaint.slot(g, leftPos + CrusherScreenHandler.AUG1_X, topPos + CrusherScreenHandler.AUG1_Y, GuiPaint.AUX);
        // Processing arrow (input -> output).
        GuiPaint.progressArrow(g, leftPos + 80, topPos + 32, menu.progress(), menu.maxProgress());
        // Player inventory wells.
        GuiPaint.playerSlots(g, leftPos + 8, topPos + 84, topPos + 142);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        Component ru = GuiPaint.f(Component.literal(menu.storedRu() + " Light"));
        g.text(font, ru, imageWidth - font.width(ru) - 8, 6, GuiPaint.HEADER, false);
        GuiPaint.ioKeyV(g, font, 8, 20);
    }
}
