package com.echoes.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Shared GUI paint helpers, styled after classic tech-mod GUIs (Thermal Series): a
 * raised two-tone panel bevel, color-coded slot rings (so input / output / byproduct
 * read at a glance), and framed value bars. Colours stay in the mod's "deep resonance"
 * palette — teal Light, amber percussion, amethyst dimensional — rather than copying
 * any other mod's textures.
 */
final class GuiPaint {
    private GuiPaint() {}

    // Slot role colours (also the in/out flow colours on the Info panel).
    static final int IN  = 0xFF49C7B8;   // input — teal Light
    static final int OUT = 0xFFE0A24A;   // primary output — amber
    static final int AUX = 0xFFB07BE0;   // secondary / byproduct — amethyst
    static final int NEUTRAL = 0xFF5A6E78;

    static final int WELL = 0xFF101418;

    /** A 1px colour ring framing an 18×18 slot well whose 16×16 content starts at (sx, sy). */
    static void slotRing(GuiGraphicsExtractor g, int sx, int sy, int color) {
        int x0 = sx - 1, y0 = sy - 1, x1 = sx + 17, y1 = sy + 17;
        g.fill(x0, y0, x1, y0 + 1, color);       // top
        g.fill(x0, y1 - 1, x1, y1, color);       // bottom
        g.fill(x0, y0, x0 + 1, y1, color);       // left
        g.fill(x1 - 1, y0, x1, y1, color);       // right
    }

    /** Filled dark well + colour ring, for programmatically-drawn slots. */
    static void slot(GuiGraphicsExtractor g, int sx, int sy, int color) {
        g.fill(sx - 1, sy - 1, sx + 17, sy + 17, WELL);
        slotRing(g, sx, sy, color);
    }

    /** A raised two-tone panel (light top-left edge, dark bottom-right) like a Thermal frame. */
    static void bevelPanel(GuiGraphicsExtractor g, int x, int y, int w, int h,
                           int fill, int hi, int lo) {
        g.fill(x, y, x + w, y + h, fill);
        g.fill(x, y, x + w, y + 2, hi);              // top
        g.fill(x, y, x + 2, y + h, hi);              // left
        g.fill(x, y + h - 2, x + w, y + h, lo);      // bottom
        g.fill(x + w - 2, y, x + w, y + h, lo);      // right
    }

    /** A framed value bar: dark well, coloured fill (0..1), thin bezel. */
    static void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, double frac, int color) {
        g.fill(x, y, x + w, y + h, WELL);
        int fw = (int) Math.round((w - 2) * Math.max(0, Math.min(1, frac)));
        if (fw > 0) g.fill(x + 1, y + 1, x + 1 + fw, y + h - 1, color);
        // bezel
        g.fill(x, y, x + w, y + 1, 0xFF000000);
        g.fill(x, y, x + 1, y + h, 0xFF000000);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF2A3A42);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF2A3A42);
    }
}
