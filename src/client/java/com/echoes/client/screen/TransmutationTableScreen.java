package com.echoes.client.screen;

import com.echoes.registry.ModItems;
import com.echoes.screen.TransmutationTableScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

/**
 * The transmutation terminal screen (Table and Tablet share it). Drawn programmatically:
 * a dark panel with the dissolve / output slots and the banked Bound-Light readout, a row
 * of five Mote-withdraw buttons, and a ProjectE-style <b>knowledge grid</b> — every item
 * you've learned, shown as a clickable icon. Click a grid icon to create that item (paying
 * its Light Value); the grid pages when you've learned more than one screenful.
 */
public class TransmutationTableScreen extends AbstractContainerScreen<TransmutationTableScreenHandler> {
    private static final int PANEL = 0xF0202830, BORDER = 0xFF3A4A52, SLOT = 0xFF101418;
    private static final int GRID_BG = 0xFF181F26, ACCENT = 0xFF7FE9DD;
    private static final String[] LABELS = {"L", "T", "M", "D", "H"};

    private Button pagePrev, pageNext;

    public TransmutationTableScreen(TransmutationTableScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title, 176, 236);
        this.inventoryLabelY = 148;
    }

    @Override
    protected void init() {
        super.init();
        // Dissolve / learn — banks the input item's value and adds it to your knowledge.
        addRenderableWidget(Button.builder(Component.translatable("screen.echoes.dissolve"),
                        b -> click(TransmutationTableScreenHandler.BTN_DISSOLVE))
                .bounds(leftPos + 30, topPos + 38, 138, 16)
                .tooltip(Tooltip.create(Component.translatable("screen.echoes.dissolve.tip"))).build());

        // Five Mote-withdraw buttons.
        int n = ModItems.MOTES.length, bw = 30, gap = 2, total = n * bw + (n - 1) * gap;
        int x0 = leftPos + (imageWidth - total) / 2, by = topPos + 58;
        for (int i = 0; i < n; i++) {
            final int tier = i;
            addRenderableWidget(Button.builder(Component.literal(LABELS[i]), b -> click(tier))
                    .bounds(x0 + i * (bw + gap), by, bw, 16)
                    .tooltip(Tooltip.create(Component.translatable(ModItems.MOTES[tier].getDescriptionId())
                            .append(Component.literal(" — withdraw (" + TransmutationTableScreenHandler.moteValue(tier) + " LV)"))))
                    .build());
        }

        // Knowledge-grid page controls.
        pagePrev = addRenderableWidget(Button.builder(Component.literal("<"),
                        b -> click(TransmutationTableScreenHandler.BTN_PAGE_PREV))
                .bounds(leftPos + 138, topPos + 76, 14, 12).build());
        pageNext = addRenderableWidget(Button.builder(Component.literal(">"),
                        b -> click(TransmutationTableScreenHandler.BTN_PAGE_NEXT))
                .bounds(leftPos + 154, topPos + 76, 14, 12).build());
    }

    private void click(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        boolean paged = menu.pageCountClient() > 1;
        if (pagePrev != null) pagePrev.active = paged && menu.pageClient() > 0;
        if (pageNext != null) pageNext.active = paged && menu.pageClient() < menu.pageCountClient() - 1;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL);
        // border (4 edges — GuiGraphicsExtractor has no drawBorder helper)
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, BORDER);
        g.fill(leftPos, topPos + imageHeight - 1, leftPos + imageWidth, topPos + imageHeight, BORDER);
        g.fill(leftPos, topPos, leftPos + 1, topPos + imageHeight, BORDER);
        g.fill(leftPos + imageWidth - 1, topPos, leftPos + imageWidth, topPos + imageHeight, BORDER);

        // Input (dissolve) + output slot wells.
        int sy = TransmutationTableScreenHandler.SLOT_Y - 1;
        for (int sx : new int[]{TransmutationTableScreenHandler.INPUT_X, TransmutationTableScreenHandler.OUTPUT_X}) {
            g.fill(leftPos + sx - 1, topPos + sy, leftPos + sx + 17, topPos + sy + 18, SLOT);
        }

        // Knowledge grid backing panel + per-cell wells.
        int gx = leftPos + TransmutationTableScreenHandler.GRID_X, gy = topPos + TransmutationTableScreenHandler.GRID_Y;
        int gw = TransmutationTableScreenHandler.GRID_COLS * 18, gh = TransmutationTableScreenHandler.GRID_ROWS * 18;
        g.fill(gx - 1, gy - 1, gx + gw + 1, gy + gh + 1, GRID_BG);
        for (int row = 0; row < TransmutationTableScreenHandler.GRID_ROWS; row++)
            for (int col = 0; col < TransmutationTableScreenHandler.GRID_COLS; col++)
                g.fill(gx + col * 18, gy + row * 18, gx + col * 18 + 16, gy + row * 18 + 16, SLOT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        // Banked Bound-Light readout (panel-relative coordinates).
        g.text(font, Component.translatable("screen.echoes.bound_light", menu.boundLight()),
                30, 22, 0xFFE0E8EC, false);
        // Knowledge header with page indicator.
        Component head = Component.translatable("screen.echoes.knowledge")
                .append(Component.literal("  " + (menu.pageClient() + 1) + "/" + menu.pageCountClient()));
        g.text(font, head, 8, 78, ACCENT, false);
    }
}
