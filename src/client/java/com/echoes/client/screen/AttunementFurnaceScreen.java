package com.echoes.client.screen;

import com.echoes.EchoesMod;
import com.echoes.screen.AttunementFurnaceScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class AttunementFurnaceScreen extends AbstractContainerScreen<AttunementFurnaceScreenHandler> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "textures/gui/transmuter.png");

    public AttunementFurnaceScreen(AttunementFurnaceScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(MachineTabs.tab(leftPos, topPos, 0, "i", "screen.echoes.tab.info",
                menu.containerId, AttunementFurnaceScreenHandler.B_INFO));
        addRenderableWidget(MachineTabs.tab(leftPos, topPos, 1, "C", "screen.echoes.tab.config",
                menu.containerId, AttunementFurnaceScreenHandler.B_CONFIG));
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
        // Color-coded slots: input (teal) -> output (amber).
        GuiPaint.slotRing(g, leftPos + 56, topPos + 35, GuiPaint.IN);
        GuiPaint.slotRing(g, leftPos + 116, topPos + 35, GuiPaint.OUT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        // Right-aligned in the title row so it doesn't overlap the block-name title.
        Component ru = Component.literal(menu.storedRu() + " Light");
        g.text(font, ru, imageWidth - font.width(ru) - 8, 6, 0xFF7FE9DD, false);
    }
}
