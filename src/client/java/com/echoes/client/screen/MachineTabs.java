package com.echoes.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/**
 * Shared helper: builds the AE2/Thermal-style vertical tab buttons for the left edge of a
 * machine screen. Each tab fires one of the menu's {@code B_*} buttons, which the
 * server-side handler — which knows the block position — turns into the matching panel
 * (Info / Config) for that block. So the client never needs the position threaded to it,
 * and machines reach their live Info and Config views with a click. The caller adds the
 * returned buttons to its own widget list (addRenderableWidget is protected).
 */
final class MachineTabs {
    private MachineTabs() {}

    /** A tab button at vertical slot {@code index} (0 = top) down the left edge. */
    static Button tab(int leftPos, int topPos, int index, String label, String tooltipKey,
                      int containerId, int buttonId) {
        return Button.builder(Component.literal(label), b -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.gameMode != null) mc.gameMode.handleInventoryButtonClick(containerId, buttonId);
                })
                .bounds(leftPos - 22, topPos + 6 + index * 22, 20, 20)
                .tooltip(Tooltip.create(Component.translatable(tooltipKey)))
                .build();
    }
}
