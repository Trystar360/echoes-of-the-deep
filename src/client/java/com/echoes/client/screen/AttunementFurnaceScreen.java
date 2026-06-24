package com.echoes.client.screen;

import com.echoes.screen.AttunementFurnaceScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

/** The Transmuter screen — Obsidian &amp; Gold, drawn programmatically (no baked texture). */
public class AttunementFurnaceScreen extends AbstractContainerScreen<AttunementFurnaceScreenHandler> {

    public AttunementFurnaceScreen(AttunementFurnaceScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, GuiPaint.f(title));
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY = -1000;
        addRenderableWidget(new ExpandingTab(leftPos, topPos + 6, GuiPaint.IN, GuiPaint.ICON_INFO,
                Component.translatable("screen.echoes.tab.info"), font,
                ExpandingTab.menuButton(menu.containerId, AttunementFurnaceScreenHandler.B_INFO)));
        addRenderableWidget(new ExpandingTab(leftPos, topPos + 28, GuiPaint.OUT, GuiPaint.ICON_CONFIG,
                Component.translatable("screen.echoes.tab.config"), font,
                ExpandingTab.menuButton(menu.containerId, AttunementFurnaceScreenHandler.B_CONFIG)));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        GuiPaint.panel(g, leftPos, topPos, imageWidth, imageHeight);
        GuiPaint.titleBanner(g, font, leftPos, topPos, imageWidth, getTitle(), GuiPaint.EMB_TRANSMUTER);
        GuiPaint.slot(g, leftPos + 56, topPos + 35, GuiPaint.IN);
        GuiPaint.slot(g, leftPos + 116, topPos + 35, GuiPaint.OUT);
        GuiPaint.progressArrow(g, leftPos + 80, topPos + 32, menu.progress(), menu.maxProgress());
        GuiPaint.playerSlots(g, leftPos + 8, topPos + 84, topPos + 142);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        GuiPaint.titleText(g, font, imageWidth, getTitle());
        GuiPaint.ioKeyV(g, font, 8, 22);
    }
}
