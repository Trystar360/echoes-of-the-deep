package com.echoes.client.screen;

import com.echoes.registry.ModItems;
import com.echoes.screen.TransmutationTableScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

/**
 * The transmutation terminal screen (Table and Tablet share it). Drawn programmatically in
 * the Obsidian &amp; Gold scheme: the dissolve / output slots and the banked Bound-Light
 * readout, a row of five Mote-withdraw buttons, and a ProjectE-style <b>knowledge grid</b> —
 * every item you've learned, shown as a clickable icon. Click a grid icon to create that
 * item (paying its Light Value); the grid pages when you've learned more than one screenful.
 */
public class TransmutationTableScreen extends AbstractContainerScreen<TransmutationTableScreenHandler> {
    private static final int GRID_BG = 0xFF101218;
    private static final String[] LABELS = {"L", "T", "M", "D", "H"};

    private TexturedButton pagePrev, pageNext;

    public TransmutationTableScreen(TransmutationTableScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, GuiPaint.f(title), 176, 236);
        this.inventoryLabelY = 148;
        this.titleLabelY = -1000;     // title drawn on the banner instead
    }

    @Override
    protected void init() {
        super.init();
        // Dissolve / learn — banks the input item's value and adds it to your knowledge.
        addRenderableWidget(TexturedButton.menu(leftPos + 30, topPos + 38, 138, 16,
                GuiPaint.f(Component.translatable("screen.echoes.dissolve")), font,
                menu.containerId, TransmutationTableScreenHandler.BTN_DISSOLVE));

        // Five Mote-withdraw buttons.
        int n = ModItems.MOTES.length, bw = 30, gap = 2, total = n * bw + (n - 1) * gap;
        int x0 = leftPos + (imageWidth - total) / 2, by = topPos + 58;
        for (int i = 0; i < n; i++) {
            addRenderableWidget(TexturedButton.menu(x0 + i * (bw + gap), by, bw, 16,
                    GuiPaint.f(LABELS[i]), font, menu.containerId, i));
        }

        // Knowledge-grid page controls.
        pagePrev = addRenderableWidget(TexturedButton.menu(leftPos + 138, topPos + 76, 14, 12,
                GuiPaint.f("<"), font, menu.containerId, TransmutationTableScreenHandler.BTN_PAGE_PREV));
        pageNext = addRenderableWidget(TexturedButton.menu(leftPos + 154, topPos + 76, 14, 12,
                GuiPaint.f(">"), font, menu.containerId, TransmutationTableScreenHandler.BTN_PAGE_NEXT));
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
        GuiPaint.panel(g, leftPos, topPos, imageWidth, imageHeight);
        GuiPaint.titleBanner(g, font, leftPos, topPos, imageWidth, getTitle(), GuiPaint.EMB_TRANSMUTE);

        // Color-coded slots: input (dissolve, ice-blue) and output (created items, gold).
        GuiPaint.slot(g, leftPos + TransmutationTableScreenHandler.INPUT_X, topPos + TransmutationTableScreenHandler.SLOT_Y, GuiPaint.IN);
        GuiPaint.slot(g, leftPos + TransmutationTableScreenHandler.OUTPUT_X, topPos + TransmutationTableScreenHandler.SLOT_Y, GuiPaint.OUT);

        // Knowledge grid backing panel + per-cell wells (neutral — these are learned items).
        int gx = leftPos + TransmutationTableScreenHandler.GRID_X, gy = topPos + TransmutationTableScreenHandler.GRID_Y;
        int gw = TransmutationTableScreenHandler.GRID_COLS * 18, gh = TransmutationTableScreenHandler.GRID_ROWS * 18;
        g.fill(gx - 1, gy - 1, gx + gw + 1, gy + gh + 1, GRID_BG);
        for (int row = 0; row < TransmutationTableScreenHandler.GRID_ROWS; row++)
            for (int col = 0; col < TransmutationTableScreenHandler.GRID_COLS; col++)
                GuiPaint.slotRing(g, gx + col * 18 + 1, gy + row * 18 + 1, GuiPaint.NEUTRAL);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        GuiPaint.titleText(g, font, imageWidth, getTitle());
        // Banked Bound-Light readout (panel-relative coordinates).
        g.text(font, GuiPaint.f(Component.translatable("screen.echoes.bound_light", menu.boundLight())),
                30, 22, GuiPaint.TEXT, false);
        // Knowledge header with page indicator + I/O key swatches.
        Component head = GuiPaint.f(Component.translatable("screen.echoes.knowledge")
                .append(Component.literal("  " + (menu.pageClient() + 1) + "/" + menu.pageCountClient())));
        g.text(font, head, 8, 78, GuiPaint.HEADER, false);
    }
}
