package com.echoes.client.screen;

import com.echoes.config.ConfigSpec;
import com.echoes.config.RedstoneMode;
import com.echoes.config.SideMode;
import com.echoes.config.TuningParam;
import com.echoes.screen.ConfigScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.ChatFormatting;

/**
 * Slot-less, texture-less configuration screen drawn from the device's
 * {@link ConfigSpec}. Each control's value display is refreshed every frame from
 * the synced property delegate; +/- buttons fire vanilla button clicks.
 */
public class ConfigScreen extends AbstractContainerScreen<ConfigScreenHandler> {
    private static final String[] FACE = { "D", "U", "N", "S", "W", "E" };
    private static final String[] ROMAN = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX" };

    private final ConfigSpec spec;
    private Button channelBtn, octaveBtn, redstoneBtn, tuningABtn, tuningBBtn;
    private final Button[] sideBtns = new Button[6];

    public ConfigScreen(ConfigScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title, 214, 30 + rowCount(handler.spec()) * 24 + 8);
        this.spec = handler.spec();
        this.inventoryLabelY = -1000; // hide "Container" label (no slots)
        this.titleLabelX = 8;
    }

    private static int rowCount(ConfigSpec spec) {
        int n = 0;
        if (spec.channel()) n++;
        if (spec.octave()) n++;
        if (spec.redstone()) n++;
        if (spec.sides()) n++;
        n += spec.tunings().size();
        return Math.max(n, 1);
    }

    @Override
    protected void init() {
        super.init();
        int y = y() + 26;
        if (spec.channel()) {
            channelBtn = addValueRow(y, ConfigScreenHandler.B_CHANNEL_DOWN, ConfigScreenHandler.B_CHANNEL_UP);
            y += 24;
        }
        if (spec.octave()) {
            octaveBtn = addValueRow(y, ConfigScreenHandler.B_OCTAVE_DOWN, ConfigScreenHandler.B_OCTAVE_UP);
            y += 24;
        }
        if (spec.redstone()) {
            redstoneBtn = addButton(x() + 96, y, 108, Component.empty(), ConfigScreenHandler.B_REDSTONE);
            y += 24;
        }
        if (spec.sides()) {
            for (int i = 0; i < 6; i++) {
                sideBtns[i] = addButton(x() + 70 + i * 23, y, 21, Component.literal(FACE[i]),
                        ConfigScreenHandler.B_SIDE_BASE + i);
            }
            y += 24;
        }
        if (spec.tuning(0) != null) {
            tuningABtn = addValueRow(y, ConfigScreenHandler.B_TUNING_A_DOWN, ConfigScreenHandler.B_TUNING_A_UP);
            y += 24;
        }
        if (spec.tuning(1) != null) {
            tuningBBtn = addValueRow(y, ConfigScreenHandler.B_TUNING_B_DOWN, ConfigScreenHandler.B_TUNING_B_UP);
            y += 24;
        }
    }

    /** A centered value button flanked by [-] and [+]. The center button is inert (display only). */
    private Button addValueRow(int y, int downId, int upId) {
        addButton(x() + 96, y, 20, Component.literal("-"), downId);
        Button value = Button.builder(Component.empty(), b -> {})
                .bounds(x() + 118, y, 64, 20).build();
        value.active = false;
        addRenderableWidget(value);
        addButton(x() + 184, y, 20, Component.literal("+"), upId);
        return value;
    }

    private Button addButton(int bx, int by, int w, Component label, int buttonId) {
        Button b = Button.builder(label, btn -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
            }
        }).bounds(bx, by, w, 20).build();
        addRenderableWidget(b);
        return b;
    }

    private int x() { return (width - imageWidth) / 2; }
    private int y() { return (height - imageHeight) / 2; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        refreshLabels();
        GuiPaint.bevelPanel(g, x(), y(), imageWidth, imageHeight, 0xF00B1416, 0xFF2A4A4A, 0xFF050A0B);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
        // Row titles must be drawn in the label (foreground) pass — text drawn in
        // extractBackground renders beneath the opaque panel fill and is invisible.
        drawRowTitles(g);
    }

    private void refreshLabels() {
        if (channelBtn != null) {
            int ch = menu.prop(ConfigScreenHandler.P_CHANNEL);
            DyeColor dye = DyeColor.byId(ch);
            channelBtn.setMessage(Component.translatable("color.minecraft." + dye.getName()));
        }
        if (octaveBtn != null) {
            int oc = Math.floorMod(menu.prop(ConfigScreenHandler.P_OCTAVE), ROMAN.length);
            octaveBtn.setMessage(Component.literal(ROMAN[oc]));
        }
        if (redstoneBtn != null) {
            RedstoneMode m = RedstoneMode.byId(menu.prop(ConfigScreenHandler.P_REDSTONE));
            redstoneBtn.setMessage(Component.translatable(m.translationKey()));
        }
        for (int i = 0; i < 6; i++) {
            if (sideBtns[i] == null) continue;
            SideMode sm = SideMode.byId(menu.prop(ConfigScreenHandler.P_SIDE0 + i));
            sideBtns[i].setMessage(Component.literal(FACE[i]).withStyle(sideColor(sm)));
        }
        if (tuningABtn != null) tuningABtn.setMessage(Component.literal(String.valueOf(menu.prop(ConfigScreenHandler.P_TUNING_A))));
        if (tuningBBtn != null) tuningBBtn.setMessage(Component.literal(String.valueOf(menu.prop(ConfigScreenHandler.P_TUNING_B))));
    }

    private static ChatFormatting sideColor(SideMode m) {
        return switch (m) {
            case DISABLED -> ChatFormatting.DARK_GRAY;
            case INPUT -> ChatFormatting.AQUA;
            case OUTPUT -> ChatFormatting.GOLD;
            case BOTH -> ChatFormatting.GREEN;
        };
    }

    /** Left-hand label for each row, drawn relative to its button's Y. */
    private void drawRowTitles(GuiGraphicsExtractor g) {
        int color = 0xFFC8E6E6;
        if (channelBtn != null) label(g, "config.echoes.channel", channelBtn.getY(), color);
        if (octaveBtn != null) label(g, "config.echoes.octave", octaveBtn.getY(), color);
        if (redstoneBtn != null) label(g, "config.echoes.redstone", redstoneBtn.getY(), color);
        if (sideBtns[0] != null) label(g, "config.echoes.sides", sideBtns[0].getY(), color);
        TuningParam t0 = spec.tuning(0), t1 = spec.tuning(1);
        if (tuningABtn != null && t0 != null) label(g, t0.labelKey(), tuningABtn.getY(), color);
        if (tuningBBtn != null && t1 != null) label(g, t1.labelKey(), tuningBBtn.getY(), color);
    }

    private void label(GuiGraphicsExtractor g, String key, int rowY, int color) {
        // extractLabels is panel-relative (the pose is translated to leftPos/topPos),
        // so convert the button's absolute Y into panel space.
        g.text(font, Component.translatable(key), 10, rowY - topPos + 6, color, false);
    }
}
