package com.echoes.client.screen;

import com.echoes.registry.ModItems;
import com.echoes.screen.TransmutationTableScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * The Transmutation Table screen. Drawn programmatically (no GUI sheet): a dark panel,
 * the dissolve/extract slots, the banked Bound-Light readout, and a row of five
 * withdraw buttons — one per Mote tone — that pay the pool out as currency.
 */
public class TransmutationTableScreen extends HandledScreen<TransmutationTableScreenHandler> {
    private static final int PANEL = 0xF0202830, BORDER = 0xFF3A4A52, SLOT = 0xFF101418;
    private static final String[] LABELS = {"L", "T", "M", "D", "H"};

    public TransmutationTableScreen(TransmutationTableScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int n = ModItems.MOTES.length;
        int bw = 30, gap = 2, total = n * bw + (n - 1) * gap;
        int x0 = x + (backgroundWidth - total) / 2;
        int by = y + 56;
        for (int i = 0; i < n; i++) {
            final int tier = i;
            ButtonWidget b = ButtonWidget.builder(Text.literal(LABELS[i]),
                            btn -> {
                                if (client != null && client.interactionManager != null) {
                                    client.interactionManager.clickButton(handler.syncId, tier);
                                }
                            })
                    .dimensions(x0 + i * (bw + gap), by, bw, 18)
                    .tooltip(Tooltip.of(Text.translatable(ModItems.MOTES[tier].getTranslationKey())
                            .append(Text.literal(" — " + TransmutationTableScreenHandler.moteValue(tier) + " LV"))))
                    .build();
            addDrawableChild(b);
        }
    }

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        ctx.fill(x, y, x + backgroundWidth, y + backgroundHeight, PANEL);
        ctx.drawBorder(x, y, backgroundWidth, backgroundHeight, BORDER);
        // dissolve + extract slot backgrounds (match handler slot positions)
        ctx.fill(x + 43, y + 34, x + 43 + 18, y + 34 + 18, SLOT);
        ctx.fill(x + 115, y + 34, x + 115 + 18, y + 34 + 18, SLOT);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        drawMouseoverTooltip(ctx, mouseX, mouseY);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        ctx.drawText(textRenderer, Text.translatable("screen.echoes.bound_light", handler.boundLight()),
                x + 8, y + 20, 0xE0E8EC, false);
    }

    @Override
    protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
        ctx.drawText(textRenderer, title, this.titleX, this.titleY, 0xE0E8EC, false);
        ctx.drawText(textRenderer, playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY,
                0xC0C8CC, false);
    }
}
