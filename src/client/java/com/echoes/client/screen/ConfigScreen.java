package com.echoes.client.screen;

import com.echoes.config.ConfigSpec;
import com.echoes.config.RedstoneMode;
import com.echoes.config.SideMode;
import com.echoes.config.TuningParam;
import com.echoes.screen.ConfigScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
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
        super(handler, inv, title);
        this.spec = handler.spec();
        this.backgroundWidth = 214;
        this.backgroundHeight = 30 + rowCount() * 24 + 8;
        this.playerInventoryTitleY = -1000; // hide "Container" label (no slots)
        this.titleX = 8;
    }

    private int rowCount() {
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
                .dimensions(x() + 118, y, 64, 20).build();
        value.active = false;
        addDrawableChild(value);
        addButton(x() + 184, y, 20, Component.literal("+"), upId);
        return value;
    }

    private Button addButton(int bx, int by, int w, Component label, int buttonId) {
        Button b = Button.builder(label, btn -> {
            if (client != null && client.interactionManager != null) {
                client.interactionManager.clickButton(handler.syncId, buttonId);
            }
        }).dimensions(bx, by, w, 20).build();
        addDrawableChild(b);
        return b;
    }

    private int x() { return (width - backgroundWidth) / 2; }
    private int y() { return (height - backgroundHeight) / 2; }

    @Override
    protected void drawBackground(GuiGraphics ctx, float delta, int mouseX, int mouseY) {
        int x = x(), y = y();
        ctx.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xF00B1416);       // panel
        ctx.fill(x, y, x + backgroundWidth, y + 1, 0xFF2A4A4A);                       // top hi-light
        ctx.fill(x, y, x + 1, y + backgroundHeight, 0xFF2A4A4A);
        ctx.fill(x + backgroundWidth - 1, y, x + backgroundWidth, y + backgroundHeight, 0xFF050A0B);
        ctx.fill(x, y + backgroundHeight - 1, x + backgroundWidth, y + backgroundHeight, 0xFF050A0B);
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        refreshLabels();
        super.render(ctx, mouseX, mouseY, delta);
        drawRowTitles(ctx);
        drawMouseoverTooltip(ctx, mouseX, mouseY);
    }

    private void refreshLabels() {
        if (channelBtn != null) {
            int ch = handler.prop(ConfigScreenHandler.P_CHANNEL);
            DyeColor dye = DyeColor.byId(ch);
            channelBtn.setMessage(Component.translatable("color.minecraft." + dye.getName()));
        }
        if (octaveBtn != null) {
            int oc = Math.floorMod(handler.prop(ConfigScreenHandler.P_OCTAVE), ROMAN.length);
            octaveBtn.setMessage(Component.literal(ROMAN[oc]));
        }
        if (redstoneBtn != null) {
            RedstoneMode m = RedstoneMode.byId(handler.prop(ConfigScreenHandler.P_REDSTONE));
            redstoneBtn.setMessage(Component.translatable(m.translationKey()));
        }
        for (int i = 0; i < 6; i++) {
            if (sideBtns[i] == null) continue;
            SideMode sm = SideMode.byId(handler.prop(ConfigScreenHandler.P_SIDE0 + i));
            sideBtns[i].setMessage(Component.literal(FACE[i]).formatted(sideColor(sm)));
        }
        if (tuningABtn != null) tuningABtn.setMessage(Component.literal(String.valueOf(handler.prop(ConfigScreenHandler.P_TUNING_A))));
        if (tuningBBtn != null) tuningBBtn.setMessage(Component.literal(String.valueOf(handler.prop(ConfigScreenHandler.P_TUNING_B))));
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
    private void drawRowTitles(GuiGraphics ctx) {
        int color = 0xC8E6E6;
        if (channelBtn != null) label(ctx, "config.echoes.channel", channelBtn.getY(), color);
        if (octaveBtn != null) label(ctx, "config.echoes.octave", octaveBtn.getY(), color);
        if (redstoneBtn != null) label(ctx, "config.echoes.redstone", redstoneBtn.getY(), color);
        if (sideBtns[0] != null) label(ctx, "config.echoes.sides", sideBtns[0].getY(), color);
        TuningParam t0 = spec.tuning(0), t1 = spec.tuning(1);
        if (tuningABtn != null && t0 != null) labelKey(ctx, t0.labelKey(), tuningABtn.getY(), color);
        if (tuningBBtn != null && t1 != null) labelKey(ctx, t1.labelKey(), tuningBBtn.getY(), color);
    }

    private void label(GuiGraphics ctx, String key, int rowY, int color) {
        ctx.drawText(textRenderer, Component.translatable(key), x() + 10, rowY + 6, color, false);
    }

    private void labelKey(GuiGraphics ctx, String key, int rowY, int color) {
        ctx.drawText(textRenderer, Component.translatable(key), x() + 10, rowY + 6, color, false);
    }
}
