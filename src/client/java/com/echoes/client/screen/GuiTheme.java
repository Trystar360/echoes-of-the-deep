package com.echoes.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Shared look-and-feel for the mod's procedurally-drawn screens: a brighter
 * "lit steel" panel with a vertical gradient, crisp 2px bevels, a teal accent
 * header, and beveled inset slots. Mirrors the GUI palette in {@code gen_textures.py}
 * so textured and drawn screens read as one design language.
 */
public final class GuiTheme {
    private GuiTheme() {}

    // Panel + chrome
    public static final int PANEL_TOP = 0xFF485860;
    public static final int PANEL_BOT = 0xFF303D44;
    public static final int HEADER    = 0xFF222F35;
    public static final int ACCENT    = 0xFF56E2D4;   // bright teal
    public static final int ACCENT_DIM= 0xFF10524E;
    public static final int AMBER     = 0xFFD69842;   // warm secondary accent

    // Bevels
    public static final int HI  = 0xFF8EA6AC;
    public static final int HI2 = 0xFF687E84;
    public static final int SH  = 0xFF141C21;
    public static final int SH2 = 0xFF28343A;

    // Slots
    public static final int SLOT    = 0xFF1E282C;
    public static final int SLOT_SH = 0xFF0D1317;
    public static final int SLOT_HI = 0xFF70868C;

    // Text + buttons
    public static final int TEXT  = 0xFFD2EAEC;
    public static final int LABEL = 0xFFAAC8CD;
    public static final int BTN_TOP = 0xFF46565E, BTN_BOT = 0xFF323F46;
    public static final int BTN_HOVER_TOP = 0xFF5C7480, BTN_HOVER_BOT = 0xFF42545E;
    public static final int BTN_OFF_TOP = 0xFF333E44, BTN_OFF_BOT = 0xFF262F34;
    public static final int FIELD_BG = 0xFF141C20;

    /** Full panel: gradient body, header band, accent line, and outer bevel. */
    public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h, int header, int accent) {
        g.fillGradient(x, y, x + w, y + h, PANEL_TOP, PANEL_BOT);
        g.fill(x, y, x + w, y + header, HEADER);
        g.fill(x, y + header, x + w, y + header + 1, accent);
        g.fill(x, y + header + 1, x + w, y + header + 2, ACCENT_DIM);
        bevel(g, x, y, w, h);
    }

    /** Raised 2px bevel: bright top-left, dark bottom-right. */
    public static void bevel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + 1, HI);
        g.fill(x, y, x + 1, y + h, HI);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, HI2);
        g.fill(x + 1, y + 1, x + 2, y + h - 1, HI2);
        g.fill(x + w - 1, y, x + w, y + h, SH);
        g.fill(x, y + h - 1, x + w, y + h, SH);
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, SH2);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, SH2);
    }

    /** A beveled inset slot. {@code x,y} is the top-left of the 18×18 frame. */
    public static void slot(GuiGraphicsExtractor g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, SLOT);
        g.fill(x, y, x + 18, y + 1, SLOT_SH);
        g.fill(x, y, x + 1, y + 18, SLOT_SH);
        g.fill(x, y + 17, x + 18, y + 18, SLOT_HI);
        g.fill(x + 17, y, x + 18, y + 18, SLOT_HI);
    }

    /** A small inset well (value fields, gauges). */
    public static void inset(GuiGraphicsExtractor g, int x, int y, int w, int h, int fill) {
        g.fill(x, y, x + w, y + h, fill);
        g.fill(x, y, x + w, y + 1, SLOT_SH);
        g.fill(x, y, x + 1, y + h, SLOT_SH);
        g.fill(x, y + h - 1, x + w, y + h, SLOT_HI);
        g.fill(x + w - 1, y, x + w, y + h, SLOT_HI);
    }
}
