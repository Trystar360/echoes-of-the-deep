package com.echoes.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/**
 * Shared GUI styling — one source of truth for the mod's screens. A warm
 * <b>parchment, wood &amp; leather</b> book aesthetic: parchment panels in a wood frame,
 * dark-leather slot wells, dark-ink text and burgundy headers, with blue / amber / violet
 * I/O accents. Panels, slots and buttons are 9-sliced sprites
 * (assets/echoes/textures/gui/sprites/widget); the rest is drawn with the custom GUI font.
 */
final class GuiPaint {
    private GuiPaint() {}

    static final Identifier SP_PANEL = Identifier.fromNamespaceAndPath("echoes", "widget/panel");
    static final Identifier SP_SLOT  = Identifier.fromNamespaceAndPath("echoes", "widget/slot");

    // --- Parchment & Wood palette ---
    static final int TEXT        = 0xFF3A2A18;   // dark ink on parchment
    static final int HEADER      = 0xFF7C2E1C;   // burgundy headers
    static final int BUTTON_TEXT = 0xFFF0E2C2;   // light text on wood buttons
    static final int DIM         = 0xFF8A765A;   // faded ink
    static final int EDGE_HI     = 0xFF8A6E3A;   // wood highlight (bar bezel)
    static final int EDGE_LO     = 0xFF2A1A0E;   // wood shadow
    static final int WELL        = 0xFF241412;   // leather well (bar backgrounds)
    static final int PANEL       = 0xF0E4D0A4;   // parchment (fallback fill)

    // Role accents (slot rings + Info flow + the colour key).
    static final int IN  = 0xFF3E8FD0;   // input  — blue
    static final int OUT = 0xFFE08A2C;   // output — amber
    static final int AUX = 0xFFA964D8;   // byproduct / augment — violet
    static final int NEUTRAL = 0xFF7A6A52;

    /** The mod's custom GUI font (a bitmap font provider, assets/echoes/font/gui.json). */
    static final Identifier FONT = Identifier.fromNamespaceAndPath("echoes", "gui");
    private static final FontDescription FONT_DESC = new FontDescription.Resource(FONT);

    /** Wrap a component in the custom GUI font. */
    static Component f(Component c) { return c.copy().withStyle(Style.EMPTY.withFont(FONT_DESC)); }
    static Component f(String s) { return Component.literal(s).withStyle(Style.EMPTY.withFont(FONT_DESC)); }

    /** A 1px colour ring framing an 18×18 slot well whose 16×16 content starts at (sx, sy). */
    static void slotRing(GuiGraphicsExtractor g, int sx, int sy, int color) {
        int x0 = sx - 1, y0 = sy - 1, x1 = sx + 17, y1 = sy + 17;
        g.fill(x0, y0, x1, y0 + 1, color);       // top
        g.fill(x0, y1 - 1, x1, y1, color);       // bottom
        g.fill(x0, y0, x0 + 1, y1, color);       // left
        g.fill(x1 - 1, y0, x1, y1, color);       // right
    }

    /** A leather slot well (sprite) + colour ring, for content top-left at (sx, sy). */
    static void slot(GuiGraphicsExtractor g, int sx, int sy, int color) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, SP_SLOT, sx - 1, sy - 1, 18, 18);
        slotRing(g, sx, sy, color);
    }

    /** A raised two-tone panel — fallback only (parchment panels use the sprite). */
    static void bevelPanel(GuiGraphicsExtractor g, int x, int y, int w, int h,
                           int fill, int hi, int lo) {
        g.fill(x, y, x + w, y + h, fill);
        g.fill(x, y, x + w, y + 2, hi);
        g.fill(x, y, x + 2, y + h, hi);
        g.fill(x, y + h - 2, x + w, y + h, lo);
        g.fill(x + w - 2, y, x + w, y + h, lo);
    }

    /** The standard parchment-in-a-wood-frame panel. */
    static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, SP_PANEL, x, y, w, h);
    }

    /** A framed value bar: leather well, coloured fill (0..1), thin bezel. */
    static void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, double frac, int color) {
        g.fill(x, y, x + w, y + h, WELL);
        int fw = (int) Math.round((w - 2) * Math.max(0, Math.min(1, frac)));
        if (fw > 0) g.fill(x + 1, y + 1, x + 1 + fw, y + h - 1, color);
        g.fill(x, y, x + w, y + 1, EDGE_LO);
        g.fill(x, y, x + 1, y + h, EDGE_LO);
        g.fill(x, y + h - 1, x + w, y + h, EDGE_HI);
        g.fill(x + w - 1, y, x + w, y + h, EDGE_HI);
    }

    /** Wells for the 3×9 main inventory (content top-left at left,invY) and hotbar (left,hotY). */
    static void playerSlots(GuiGraphicsExtractor g, int left, int invY, int hotY) {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                slot(g, left + c * 18, invY + r * 18, NEUTRAL);
        for (int c = 0; c < 9; c++) slot(g, left + c * 18, hotY, NEUTRAL);
    }

    /** A processing arrow (16-wide bar + head) at (x, y), filled amber by progress/max. */
    static void progressArrow(GuiGraphicsExtractor g, int x, int y, int prog, int max) {
        boolean run = max > 0 && prog > 0;
        g.fill(x, y + 5, x + 16, y + 11, WELL);                     // bar track
        int w = max > 0 ? Math.min(16, prog * 16 / max) : 0;
        if (w > 0) g.fill(x, y + 6, x + w, y + 10, OUT);
        for (int i = 0; i <= 5; i++)                                // arrowhead
            g.fill(x + 16, y + 3 + i, x + 16 + (6 - i), y + 4 + i, run ? OUT : WELL);
    }

    // --- I/O colour key ---
    private static final int[] KEY_COLORS = { IN, OUT, AUX };
    private static final String[] KEY_KEYS = {
            "screen.echoes.key.in", "screen.echoes.key.out", "screen.echoes.key.aux" };

    /** A compact horizontal input/output colour key (swatch + label per role) at (x, y). */
    static void ioKey(GuiGraphicsExtractor g, Font font, int x, int y) {
        int cx = x;
        for (int i = 0; i < KEY_COLORS.length; i++) {
            g.fill(cx, y, cx + 9, y + 9, EDGE_LO);
            g.fill(cx + 1, y + 1, cx + 8, y + 8, KEY_COLORS[i]);
            Component label = f(Component.translatable(KEY_KEYS[i]));
            g.text(font, label, cx + 12, y + 1, TEXT, false);
            cx += 12 + font.width(label) + 8;
        }
    }

    /** Vertical I/O colour key (one role per row) starting at (x, y), panel-relative. */
    static void ioKeyV(GuiGraphicsExtractor g, Font font, int x, int y) {
        for (int i = 0; i < KEY_COLORS.length; i++) {
            int ry = y + i * 11;
            g.fill(x, ry, x + 9, ry + 9, EDGE_LO);
            g.fill(x + 1, ry + 1, x + 8, ry + 8, KEY_COLORS[i]);
            g.text(font, f(Component.translatable(KEY_KEYS[i])), x + 12, ry + 1, TEXT, false);
        }
    }
}
