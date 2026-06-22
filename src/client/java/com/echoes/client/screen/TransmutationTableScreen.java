package com.echoes.client.screen;

import com.echoes.registry.ModItems;
import com.echoes.screen.TransmutationTableScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

/**
 * The transmutation terminal screen (Table and Tablet share it). Drawn with the
 * shared {@link GuiTheme}: a lit-steel panel, beveled slots, an inset Bound-Light
 * readout, a row of five Mote-withdraw buttons, and Dissolve / Condense actions.
 */
public class TransmutationTableScreen extends AbstractContainerScreen<TransmutationTableScreenHandler> {
    private static final String[] LABELS = {"L", "T", "M", "D", "H"};

    public TransmutationTableScreen(TransmutationTableScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title, 176, 200);
        this.titleLabelY = -1000;       // drawn bright in the header band
        this.inventoryLabelY = -1000;
    }

    @Override
    protected void init() {
        super.init();
        // Five Mote-withdraw buttons.
        int n = ModItems.MOTES.length, bw = 30, gap = 2, total = n * bw + (n - 1) * gap;
        int x0 = leftPos + (imageWidth - total) / 2, by = topPos + 60;
        for (int i = 0; i < n; i++) {
            final int tier = i;
            ThemedButton b = new ThemedButton(x0 + i * (bw + gap), by, bw, 18,
                    Component.literal(LABELS[i]), () -> click(tier));
            b.setTooltip(Tooltip.create(Component.translatable(ModItems.MOTES[tier].getDescriptionId())
                    .append(Component.literal(" — withdraw (" + TransmutationTableScreenHandler.moteValue(tier) + " LV)"))));
            addRenderableWidget(b);
        }
        // Action row: Dissolve / Condense / Condense ×64.
        int ay = topPos + 86;
        action(leftPos + 8, ay, 50, Component.translatable("screen.echoes.dissolve"),
                TransmutationTableScreenHandler.BTN_DISSOLVE, "screen.echoes.dissolve.tip");
        action(leftPos + 62, ay, 52, Component.translatable("screen.echoes.condense"),
                TransmutationTableScreenHandler.BTN_CONDENSE_1, "screen.echoes.condense.tip");
        action(leftPos + 118, ay, 50, Component.literal("×64"),
                TransmutationTableScreenHandler.BTN_CONDENSE_STACK, "screen.echoes.condense.tip");
    }

    private void action(int x, int y, int w, Component label, int id, String tipKey) {
        ThemedButton b = new ThemedButton(x, y, w, 18, label, () -> click(id));
        b.setTooltip(Tooltip.create(Component.translatable(tipKey)));
        addRenderableWidget(b);
    }

    private void click(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        GuiTheme.panel(g, leftPos, topPos, imageWidth, imageHeight, 18, GuiTheme.ACCENT);
        // Bound-Light readout well.
        GuiTheme.inset(g, leftPos + 6, topPos + 22, imageWidth - 12, 12, GuiTheme.FIELD_BG);
        // Machine slots.
        int sy = TransmutationTableScreenHandler.SLOT_Y - 1;
        for (int sx : new int[]{TransmutationTableScreenHandler.INPUT_X,
                TransmutationTableScreenHandler.TEMPLATE_X, TransmutationTableScreenHandler.OUTPUT_X}) {
            GuiTheme.slot(g, leftPos + sx - 1, topPos + sy);
        }
        // Player inventory + hotbar.
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                GuiTheme.slot(g, leftPos + 7 + c * 18, topPos + 117 + r * 18);
        for (int c = 0; c < 9; c++)
            GuiTheme.slot(g, leftPos + 7 + c * 18, topPos + 175);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, title, 8, 6, GuiTheme.TEXT, false);
        g.text(font, Component.translatable("screen.echoes.bound_light", menu.boundLight()),
                10, 24, GuiTheme.ACCENT, false);
    }
}
