package com.echoes.client.screen;

import com.echoes.EchoesMod;
import com.echoes.screen.AttunementFurnaceScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class AttunementFurnaceScreen extends AbstractContainerScreen<AttunementFurnaceScreenHandler> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "textures/gui/transmuter.png");

    public AttunementFurnaceScreen(AttunementFurnaceScreenHandler handler, Inventory inv, Component title) {
        super(handler, inv, title);
    }

    @Override
    protected void drawBackground(GuiGraphics ctx, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        ctx.drawTexture(RenderType::getGuiTextured, TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);

        int max = handler.maxProgress();
        if (max > 0) {
            int w = handler.progress() * 24 / max;
            ctx.drawTexture(RenderType::getGuiTextured, TEXTURE, x + 79, y + 34, 176, 0, w, 16, 256, 256);
        }
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        drawMouseoverTooltip(ctx, mouseX, mouseY);
        ctx.drawText(textRenderer, Component.literal(handler.storedRu() + " Light"),
                (width - backgroundWidth) / 2 + 8, (height - backgroundHeight) / 2 + 6, 0x7FE9DD, false);
    }
}
