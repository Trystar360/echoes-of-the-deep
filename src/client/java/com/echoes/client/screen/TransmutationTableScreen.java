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
 * a dark panel, the dissolve / template / output slots, the banked Bound-Light readout,
 * a row of five Mote-withdraw buttons, and Dissolve / Condense / Condense-stack actions.
 */
public class TransmutationTableScreen extends AbstractContainerScreen<TransmutationTableScreenHandler> {
    private static final int PANEL = 0xF0202830, BORDER = 0xFF3A4A52, SLOT = 0xFF101418;
    private static final String[] LABELS = {"L", "T", "M", "D", "H"};

    public TransmutationTableScreen(TransmutationTableScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title, 176, 200);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        // Five Mote-withdraw buttons.
        int n = ModItems.MOTES.length, bw = 30, gap = 2, total = n * bw + (n - 1) * gap;
        int x0 = leftPos + (imageWidth - total) / 2, by = topPos + 60;
        for (int i = 0; i < n; i++) {
            final int tier = i;
            addRenderableWidget(Button.builder(Component.literal(LABELS[i]),
                            b -> click(tier))
                    .bounds(x0 + i * (bw + gap), by, bw, 18)
                    .tooltip(Tooltip.create(Component.translatable(ModItems.MOTES[tier].getDescriptionId())
                            .append(Component.literal(" — withdraw (" + TransmutationTableScreenHandler.moteValue(tier) + " LV)"))))
                    .build());
        }
        // Action row: Dissolve / Condense / Condense ×64.
        int ay = topPos + 86;
        addRenderableWidget(Button.builder(Component.translatable("screen.echoes.dissolve"),
                        b -> click(TransmutationTableScreenHandler.BTN_DISSOLVE))
                .bounds(leftPos + 8, ay, 50, 18)
                .tooltip(Tooltip.create(Component.translatable("screen.echoes.dissolve.tip"))).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.echoes.condense"),
                        b -> click(TransmutationTableScreenHandler.BTN_CONDENSE_1))
                .bounds(leftPos + 62, ay, 52, 18)
                .tooltip(Tooltip.create(Component.translatable("screen.echoes.condense.tip"))).build());
        addRenderableWidget(Button.builder(Component.literal("×64"),
                        b -> click(TransmutationTableScreenHandler.BTN_CONDENSE_STACK))
                .bounds(leftPos + 118, ay, 50, 18)
                .tooltip(Tooltip.create(Component.translatable("screen.echoes.condense.tip"))).build());
    }

    private void click(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
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
        int sy = TransmutationTableScreenHandler.SLOT_Y - 1;
        for (int sx : new int[]{TransmutationTableScreenHandler.INPUT_X,
                TransmutationTableScreenHandler.TEMPLATE_X, TransmutationTableScreenHandler.OUTPUT_X}) {
            g.fill(leftPos + sx - 1, topPos + sy, leftPos + sx + 17, topPos + sy + 18, SLOT);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        // Banked Bound-Light readout (panel-relative coordinates).
        g.text(font, Component.translatable("screen.echoes.bound_light", menu.boundLight()),
                8, 24, 0xFFE0E8EC, false);
    }
}
