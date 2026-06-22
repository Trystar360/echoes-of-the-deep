package com.echoes.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/**
 * Shared helper: builds the AE2-style "i" Info tab button for the left edge of a machine
 * screen. Clicking it fires the menu's {@code B_INFO} button, which the server handler
 * turns into an Info panel for that block — so machines reach their live Info view with a
 * click, the same way passive blocks reach it with a right-click. The caller adds the
 * returned button to its own widget list.
 */
final class MachineTabs {
    private MachineTabs() {}

    static Button infoButton(int leftPos, int topPos, int containerId, int infoButtonId) {
        return Button.builder(Component.literal("i"), b -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.gameMode != null) mc.gameMode.handleInventoryButtonClick(containerId, infoButtonId);
                })
                .bounds(leftPos - 22, topPos + 6, 20, 20)
                .tooltip(Tooltip.create(Component.translatable("screen.echoes.tab.info")))
                .build();
    }
}
