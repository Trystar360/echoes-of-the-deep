package com.echoes.client.screen;

import com.echoes.screen.FabricatorScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

/** The Fabricator screen — a 3x3 crafting matrix feeding an output well, drawn programmatically. */
public class FabricatorScreen extends AbstractContainerScreen<FabricatorScreenHandler> {

    public FabricatorScreen(FabricatorScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, GuiPaint.f(title));
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new ExpandingTab(leftPos, topPos + 6, GuiPaint.IN, "i",
                Component.translatable("screen.echoes.tab.info"), font,
                ExpandingTab.menuButton(menu.containerId, FabricatorScreenHandler.B_INFO)));
        addRenderableWidget(new ExpandingTab(leftPos, topPos + 28, GuiPaint.OUT, "C",
                Component.translatable("screen.echoes.tab.config"), font,
                ExpandingTab.menuButton(menu.containerId, FabricatorScreenHandler.B_CONFIG)));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        GuiPaint.panel(g, leftPos, topPos, imageWidth, imageHeight);

        // Crafting matrix (teal wells) -> output (gold).
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                GuiPaint.slot(g, leftPos + FabricatorScreenHandler.GRID_X + col * 18,
                        topPos + FabricatorScreenHandler.GRID_Y + row * 18, GuiPaint.IN);
        GuiPaint.slot(g, leftPos + FabricatorScreenHandler.OUT_X, topPos + FabricatorScreenHandler.OUT_Y, GuiPaint.OUT);
        // Augment column (right): two amethyst wells.
        GuiPaint.slot(g, leftPos + FabricatorScreenHandler.AUG0_X, topPos + FabricatorScreenHandler.AUG0_Y, GuiPaint.AUX);
        GuiPaint.slot(g, leftPos + FabricatorScreenHandler.AUG1_X, topPos + FabricatorScreenHandler.AUG1_Y, GuiPaint.AUX);
        // Processing arrow (matrix -> output).
        GuiPaint.progressArrow(g, leftPos + 92, topPos + 32, menu.progress(), menu.maxProgress());
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
