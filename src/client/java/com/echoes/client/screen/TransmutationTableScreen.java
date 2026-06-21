package com.echoes.client.screen;

import com.echoes.registry.ModItems;
import com.echoes.screen.TransmutationTableScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
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
        super(handler, inv, title);
        this.backgroundHeight = 200;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        // Five Mote-withdraw buttons.
        int n = ModItems.MOTES.length, bw = 30, gap = 2, total = n * bw + (n - 1) * gap;
        int x0 = x + (backgroundWidth - total) / 2, by = y + 60;
        for (int i = 0; i < n; i++) {
            final int tier = i;
            addDrawableChild(Button.builder(Component.literal(LABELS[i]),
                            b -> click(tier))
                    .dimensions(x0 + i * (bw + gap), by, bw, 18)
                    .tooltip(Tooltip.of(Component.translatable(ModItems.MOTES[tier].getTranslationKey())
                            .append(Component.literal(" — withdraw (" + TransmutationTableScreenHandler.moteValue(tier) + " LV)"))))
                    .build());
        }
        // Action row: Dissolve / Condense / Condense ×64.
        int ay = y + 86;
        addDrawableChild(Button.builder(Component.translatable("screen.echoes.dissolve"),
                        b -> click(TransmutationTableScreenHandler.BTN_DISSOLVE))
                .dimensions(x + 8, ay, 50, 18)
                .tooltip(Tooltip.of(Component.translatable("screen.echoes.dissolve.tip"))).build());
        addDrawableChild(Button.builder(Component.translatable("screen.echoes.condense"),
                        b -> click(TransmutationTableScreenHandler.BTN_CONDENSE_1))
                .dimensions(x + 62, ay, 52, 18)
                .tooltip(Tooltip.of(Component.translatable("screen.echoes.condense.tip"))).build());
        addDrawableChild(Button.builder(Component.literal("×64"),
                        b -> click(TransmutationTableScreenHandler.BTN_CONDENSE_STACK))
                .dimensions(x + 118, ay, 50, 18)
                .tooltip(Tooltip.of(Component.translatable("screen.echoes.condense.tip"))).build());
    }

    private void click(int id) {
        if (client != null && client.interactionManager != null) {
            client.interactionManager.clickButton(handler.syncId, id);
        }
    }

    @Override
    protected void drawBackground(GuiGraphics ctx, float delta, int mouseX, int mouseY) {
        ctx.fill(x, y, x + backgroundWidth, y + backgroundHeight, PANEL);
        ctx.drawBorder(x, y, backgroundWidth, backgroundHeight, BORDER);
        int sy = TransmutationTableScreenHandler.SLOT_Y - 1;
        for (int sx : new int[]{TransmutationTableScreenHandler.INPUT_X,
                TransmutationTableScreenHandler.TEMPLATE_X, TransmutationTableScreenHandler.OUTPUT_X}) {
            ctx.fill(x + sx - 1, y + sy, x + sx + 17, y + sy + 18, SLOT);
        }
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        drawMouseoverTooltip(ctx, mouseX, mouseY);
        ctx.drawText(textRenderer, Component.translatable("screen.echoes.bound_light", handler.boundLight()),
                x + 8, y + 24, 0xE0E8EC, false);
    }

    @Override
    protected void drawForeground(GuiGraphics ctx, int mouseX, int mouseY) {
        ctx.drawText(textRenderer, title, this.titleX, this.titleY, 0xE0E8EC, false);
        ctx.drawText(textRenderer, playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY,
                0xC0C8CC, false);
    }
}
