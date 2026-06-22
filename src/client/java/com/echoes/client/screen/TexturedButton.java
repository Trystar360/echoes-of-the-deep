package com.echoes.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A GUI button drawn from the mod's custom 9-sliced sprites (Obsidian &amp; Gold) instead
 * of the vanilla button texture, with its label in the custom GUI font. Three states map
 * to three sprites (normal / hover / disabled). Used in place of vanilla {@code Button}s
 * across the mod's screens.
 */
final class TexturedButton extends AbstractWidget {
    private static final Identifier NORMAL = Identifier.fromNamespaceAndPath("echoes", "widget/button");
    private static final Identifier HOVER = Identifier.fromNamespaceAndPath("echoes", "widget/button_highlighted");
    private static final Identifier DISABLED = Identifier.fromNamespaceAndPath("echoes", "widget/button_disabled");

    private final Font font;
    private Runnable onPress;

    TexturedButton(int x, int y, int w, int h, Component label, Font font, Runnable onPress) {
        super(x, y, w, h, label);
        this.font = font;
        this.onPress = onPress;
    }

    /** Convenience: a button that fires menu button {@code buttonId} on the current menu. */
    static TexturedButton menu(int x, int y, int w, int h, Component label, Font font, int containerId, int buttonId) {
        return new TexturedButton(x, y, w, h, label, font, () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameMode != null) mc.gameMode.handleInventoryButtonClick(containerId, buttonId);
        });
    }

    void setOnPress(Runnable r) { this.onPress = r; }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        Identifier sprite = !active ? DISABLED : (isHoveredOrFocused() ? HOVER : NORMAL);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX(), getY(), getWidth(), getHeight());
        int color = active ? GuiPaint.BUTTON_TEXT : GuiPaint.DIM;
        int tx = getX() + (getWidth() - font.width(getMessage())) / 2;
        int ty = getY() + (getHeight() - 8) / 2;
        g.text(font, getMessage(), tx, ty, color, false);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (onPress != null) onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
