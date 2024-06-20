package ru.kelcuprum.waterplayer.frontend.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.components.buttons.ButtonSprite;

public class ButtonWithSprite extends ButtonSprite {
    private final ResourceLocation icon;

    public ButtonWithSprite(int x, int y, int width, int height, ResourceLocation icon, Component label, OnPress onPress) {
        super(x, y, width, height, icon, label, onPress);
        this.icon = icon;
    }
    public void renderText(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blitSprite(icon, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        if (!this.getMessage().getString().isEmpty() && this.isHovered()) {
            guiGraphics.renderTooltip(AlinLib.MINECRAFT.font, this.getMessage(), mouseX, mouseY);
        }

    }
}
