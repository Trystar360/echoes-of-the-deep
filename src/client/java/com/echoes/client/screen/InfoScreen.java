package com.echoes.client.screen;

import com.echoes.energy.NodeRole;
import com.echoes.screen.InfoScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

/**
 * Read-only inspection screen for an energy block — the Info panel reached by
 * right-clicking the block. Texture-less, drawn from the synced {@link InfoScreenHandler}
 * snapshot: the block's role, an <b>in → buffer → out</b> flow graphic, its Light buffer,
 * and the totals of the network it belongs to. An AE2-style tab strip down the left edge
 * jumps to the device's Config screen, or back to its Function menu for machines.
 */
public class InfoScreen extends AbstractContainerScreen<InfoScreenHandler> {
    private static final int PANEL = 0xF00B1416, EDGE_HI = 0xFF2A4A4A, EDGE_LO = 0xFF050A0B;
    private static final int WELL = 0xFF101418, ACCENT = 0xFF7FE9DD, DIM = 0xFF3A4A52;
    private static final int FILL = 0xFF49C7B8, TEXT = 0xFFC8E6E6;

    public InfoScreen(InfoScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title, 176, 172);
        this.inventoryLabelY = -1000; // no slots
        this.titleLabelX = 8;
    }

    @Override
    protected void init() {
        super.init();
        // AE2-style vertical tab strip on the left edge. Info is the current tab (inert);
        // Config / Function appear when the block supports them.
        int tx = leftPos - 22, ty = topPos + 6;
        Button info = Button.builder(Component.literal("i"), b -> {}).bounds(tx, ty, 20, 20).build();
        info.active = false;
        addRenderableWidget(info);
        ty += 24;
        if (menu.configurable()) {
            addRenderableWidget(Button.builder(Component.literal("C"), b -> click(InfoScreenHandler.B_TAB_CONFIG))
                    .bounds(tx, ty, 20, 20)
                    .tooltip(Tooltip.create(Component.translatable("screen.echoes.tab.config"))).build());
            ty += 24;
        }
        if (menu.machine()) {
            addRenderableWidget(Button.builder(Component.literal("M"), b -> click(InfoScreenHandler.B_TAB_FUNCTION))
                    .bounds(tx, ty, 20, 20)
                    .tooltip(Tooltip.create(Component.translatable("screen.echoes.tab.function"))).build());
        }
    }

    private void click(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        int x = leftPos, y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, PANEL);
        g.fill(x, y, x + imageWidth, y + 1, EDGE_HI);
        g.fill(x, y, x + 1, y + imageHeight, EDGE_HI);
        g.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, EDGE_LO);
        g.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, EDGE_LO);

        boolean inLit  = menu.has(NodeRole.CONSUMER) || menu.has(NodeRole.STORAGE) || menu.has(NodeRole.CONDUIT);
        boolean outLit = menu.has(NodeRole.PROVIDER) || menu.has(NodeRole.STORAGE) || menu.has(NodeRole.CONDUIT);

        // Flow graphic: [IN] ▸ [ buffer ] ▸ [OUT]
        int fy = y + 42, fh = 24;
        chip(g, x + 8, fy, 26, fh, inLit);                    // IN chip
        arrow(g, x + 36, fy + fh / 2, inLit);                 // ▸ into buffer
        int bx0 = x + 50, bx1 = x + 126;
        g.fill(bx0, fy, bx1, fy + fh, WELL);                  // buffer well
        long cap = menu.capacity(), stored = menu.stored();
        if (cap > 0) {
            int w = (int) ((bx1 - bx0 - 2) * Math.min(1.0, (double) stored / cap));
            g.fill(bx0 + 1, fy + 1, bx0 + 1 + w, fy + fh - 1, FILL);
        } else {
            // bufferless (conduit / machine): show a pass-through line
            g.fill(bx0 + 2, fy + fh / 2 - 1, bx1 - 2, fy + fh / 2 + 1, ACCENT);
        }
        arrow(g, x + 128, fy + fh / 2, outLit);               // ▸ out
        chip(g, x + 142, fy, 26, fh, outLit);                 // OUT chip

        // Network banked bar.
        int ny = y + 118;
        g.fill(x + 8, ny, x + 168, ny + 10, WELL);
        long ncap = menu.netCapacity(), nst = menu.netStored();
        if (ncap > 0) {
            int w = (int) ((160 - 2) * Math.min(1.0, (double) nst / ncap));
            g.fill(x + 9, ny + 1, x + 9 + w, ny + 9, FILL);
        }

        // Hover tooltips on the flow graphic and network bar (exact numbers).
        if (hover(mouseX, mouseY, x + 8, fy, 26, fh))
            tip(g, mouseX, mouseY, Component.translatable("screen.echoes.info.in"));
        else if (hover(mouseX, mouseY, x + 142, fy, 26, fh))
            tip(g, mouseX, mouseY, Component.translatable("screen.echoes.info.out"));
        else if (hover(mouseX, mouseY, bx0, fy, bx1 - bx0, fh))
            tip(g, mouseX, mouseY, Component.translatable("screen.echoes.info.buffer", grp(stored), grp(cap)));
        else if (hover(mouseX, mouseY, x + 8, ny, 160, 10))
            tip(g, mouseX, mouseY, Component.translatable("screen.echoes.info.netbar", grp(nst), grp(ncap), menu.netMembers()));
    }

    private static boolean hover(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void tip(GuiGraphicsExtractor g, int mouseX, int mouseY, Component c) {
        g.setComponentTooltipForNextFrame(font, java.util.List.of(c), mouseX, mouseY);
    }

    /** Exact, grouped number (e.g. 150,000). */
    private static String grp(long v) { return String.format("%,d", v); }

    private void chip(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean lit) {
        g.fill(x, y, x + w, y + h, WELL);
        int c = lit ? ACCENT : DIM;
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    /** A small right-pointing arrow centred on (x, cy). */
    private void arrow(GuiGraphicsExtractor g, int x, int cy, boolean lit) {
        int c = lit ? ACCENT : DIM;
        for (int i = 0; i < 6; i++) g.fill(x + i, cy - (6 - i), x + i + 1, cy + (6 - i), c);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        g.text(font, Component.translatable("screen.echoes.info.role", roleText()), 8, 26, ACCENT, false);

        // IN / OUT chip captions.
        g.text(font, Component.literal("IN"), 14, 50, TEXT, false);
        g.text(font, Component.literal("OUT"), 145, 50, TEXT, false);

        // Buffer readout (or "pass-through" for bufferless nodes).
        Component buf = menu.capacity() > 0
                ? Component.literal(fmt(menu.stored()) + " / " + fmt(menu.capacity()) + " L")
                : Component.translatable("screen.echoes.info.passthrough");
        center(g, buf, 88, 76, TEXT);

        // Network summary.
        g.text(font, Component.translatable("screen.echoes.info.network", menu.netMembers()), 8, 104, TEXT, false);
        center(g, Component.literal(fmt(menu.netStored()) + " / " + fmt(menu.netCapacity()) + " L"), 88, 132, TEXT);

        if (menu.has(NodeRole.CONDUIT) && menu.throughput() > 0) {
            g.text(font, Component.translatable("screen.echoes.info.throughput", fmt(menu.throughput())), 8, 150, TEXT, false);
        }
    }

    private void center(GuiGraphicsExtractor g, Component c, int cx, int y, int color) {
        g.text(font, c, cx - font.width(c) / 2, y, color, false);
    }

    private String roleText() {
        StringBuilder sb = new StringBuilder();
        if (menu.has(NodeRole.PROVIDER)) append(sb, "Generator");
        if (menu.has(NodeRole.CONSUMER)) append(sb, "Machine");
        if (menu.has(NodeRole.STORAGE)) append(sb, "Storage");
        if (menu.has(NodeRole.CONDUIT)) append(sb, "Conduit");
        return sb.isEmpty() ? "—" : sb.toString();
    }

    private static void append(StringBuilder sb, String s) {
        if (!sb.isEmpty()) sb.append(" · ");
        sb.append(s);
    }

    /** Compact number: 12, 3.4k, 5.1M, 2.0B. */
    private static String fmt(long v) {
        if (v < 1000) return Long.toString(v);
        if (v < 1_000_000) return trim(v / 1000.0) + "k";
        if (v < 1_000_000_000) return trim(v / 1_000_000.0) + "M";
        return trim(v / 1_000_000_000.0) + "B";
    }

    private static String trim(double d) {
        return (d >= 100 ? Long.toString(Math.round(d)) : String.format("%.1f", d));
    }
}
