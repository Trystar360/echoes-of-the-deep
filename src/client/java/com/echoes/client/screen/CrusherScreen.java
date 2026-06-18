package com.echoes.client.screen;

import com.echoes.EchoesMod;
import com.echoes.screen.CrusherScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CrusherScreen extends HandledScreen<CrusherScreenHandler> {
    private static final Identifier TEXTURE =
            Identifier.of(EchoesMod.MOD_ID, "textures/gui/crusher.png");

    public CrusherScreen(CrusherScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
    }

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        ctx.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);

        // progress arrow (sprite at u=176,v=0, 24x16 in the sheet)
        int max = handler.maxProgress();
        if (max > 0) {
            int w = handler.progress() * 24 / max;
            ctx.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x + 79, y + 34, 176, 0, w, 16, 256, 256);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        drawMouseoverTooltip(ctx, mouseX, mouseY);
        // RU readout
        ctx.drawText(textRenderer, Text.literal(handler.storedRu() + " Light"),
                (width - backgroundWidth) / 2 + 8, (height - backgroundHeight) / 2 + 6,
                0x404040, false);
    }
}
