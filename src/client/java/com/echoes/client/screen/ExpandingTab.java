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
 * A book-style side tab that sits collapsed as a small carved-wood icon button and
 * <b>expands in place</b> on hover to reveal its label, then fires its action when clicked.
 * Anchored by its right edge to the GUI's left border, so it grows leftward over empty space
 * without shifting the icon. Drawn on the wood button sprite via {@link GuiPaint}.
 */
final class ExpandingTab extends AbstractWidget {
    private static final int H = 20, ICON_W = 20, PAD = 5;
    private static final Identifier BTN = Identifier.fromNamespaceAndPath("echoes", "widget/button");

    private final int rightEdge;     // x of the tab's right edge (the GUI border)
    private final int accent;
    private final Identifier icon;
    private final Font font;
    private final Runnable onPress;

    ExpandingTab(int rightEdge, int y, int accent, Identifier icon, Component label, Font font, Runnable onPress) {
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

        // Wood pill body (button sprite) with an accent strip down the icon edge.
        g.blitSprite(RenderPipelines.GUI_TEXTURED, BTN, x, y, w, H);
        if (expanded) {
            g.text(font, GuiPaint.f(getMessage().copy()), x + PAD, y + (H - 8) / 2, GuiPaint.BUTTON_TEXT, false);
            int ix = rightEdge - ICON_W;
            g.fill(ix, y + 2, ix + 1, y + H - 2, accent);
        }
        // Picture icon, centred in the right-hand icon cell.
        int ix = rightEdge - ICON_W;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, icon, ix + (ICON_W - 12) / 2, y + (H - 12) / 2, 12, 12);
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
