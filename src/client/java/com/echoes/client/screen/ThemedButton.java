package com.echoes.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * A button that matches {@link GuiTheme}: a raised, beveled face that brightens
 * and gains an accent outline on hover, an inset look when used as an inert value
 * field ({@code active == false}), and a centered label. Replaces the flat vanilla
 * button on the mod's drawn screens. Extends {@link AbstractWidget} directly so the
 * whole face can be redrawn (the {@code AbstractButton} face is final).
 */
public class ThemedButton extends AbstractWidget {
    private final Runnable onPress;
    private boolean field;
    private Integer labelColor;

    public ThemedButton(int x, int y, int w, int h, Component msg, Runnable onPress) {
        super(x, y, w, h, msg);
        this.onPress = onPress;
    }

    public ThemedButton asField() { this.field = true; this.active = false; return this; }
    public ThemedButton labelColor(int argb) { this.labelColor = argb; return this; }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubled) {
        if (onPress != null) onPress.run();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float dt) {
        int x = getX(), y = getY(), w = width, h = height;
        boolean hover = isHoveredOrFocused() && active;

        if (field) {
            GuiTheme.inset(g, x, y, w, h, GuiTheme.FIELD_BG);
        } else if (!active) {
            g.fillGradient(x, y, x + w, y + h, GuiTheme.BTN_OFF_TOP, GuiTheme.BTN_OFF_BOT);
            GuiTheme.bevel(g, x, y, w, h);
        } else {
            g.fillGradient(x, y, x + w, y + h,
                    hover ? GuiTheme.BTN_HOVER_TOP : GuiTheme.BTN_TOP,
                    hover ? GuiTheme.BTN_HOVER_BOT : GuiTheme.BTN_BOT);
            GuiTheme.bevel(g, x, y, w, h);
            if (hover) g.outline(x, y, w, h, GuiTheme.ACCENT);
        }

        int color = labelColor != null ? labelColor
                : field ? GuiTheme.TEXT
                : active ? (hover ? 0xFFFFFFFF : GuiTheme.TEXT)
                : GuiTheme.LABEL;
        g.centeredText(Minecraft.getInstance().font, getMessage(), x + w / 2, y + (h - 8) / 2, color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        defaultButtonNarrationText(out);
    }
}
