package com.echoes.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * An AE2/Thermal-style side tab that sits collapsed as a small icon and <b>expands in
 * place</b> on hover to reveal its label, then fires its action when clicked. Anchored by
 * its right edge to the GUI's left border, so it grows leftward over empty space without
 * shifting the icon. Drawn in the mod's deep-resonance palette via {@link GuiPaint}.
 */
final class ExpandingTab extends AbstractWidget {
    private static final int H = 20, ICON_W = 20, PAD = 5;
    private static final int BODY = 0xF00B1416;

    private final int rightEdge;     // x of the tab's right edge (the GUI border)
    private final int accent;
    private final String icon;
    private final Font font;
    private final Runnable onPress;

    ExpandingTab(int rightEdge, int y, int accent, String icon, Component label, Font font, Runnable onPress) {
        super(rightEdge - ICON_W, y, ICON_W, H, label);
        this.rightEdge = rightEdge;
        this.accent = accent;
        this.icon = icon;
        this.font = font;
        this.onPress = onPress;
    }

    private int expandedWidth() {
        return ICON_W + PAD + font.width(getMessage()) + PAD;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        boolean expanded = isHoveredOrFocused();
        int w = expanded ? expandedWidth() : ICON_W;
        setWidth(w);
        setX(rightEdge - w);
        int x = getX(), y = getY();

        // Body pill with an accent-tinted top/left bevel.
        GuiPaint.bevelPanel(g, x, y, w, H, BODY, accent, 0xFF050A0B);
        if (expanded) {
            // label to the left, a divider, then the icon cell on the right
            g.text(font, getMessage(), x + PAD, y + (H - 8) / 2, 0xFFC8E6E6, false);
            int ix = rightEdge - ICON_W;
            g.fill(ix, y + 2, ix + 1, y + H - 2, accent);
        }
        // Icon glyph, centred in the right-hand icon cell.
        int ix = rightEdge - ICON_W;
        g.text(font, Component.literal(icon), ix + (ICON_W - font.width(icon)) / 2, y + (H - 8) / 2, accent, false);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }

    /** Convenience: fire the menu button {@code buttonId} on the current screen's menu. */
    static Runnable menuButton(int containerId, int buttonId) {
        return () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameMode != null) mc.gameMode.handleInventoryButtonClick(containerId, buttonId);
        };
    }
}
