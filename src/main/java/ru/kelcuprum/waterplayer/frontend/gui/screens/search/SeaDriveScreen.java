package ru.kelcuprum.waterplayer.frontend.gui.screens.search;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.gui.GuiUtils;

public class SeaDriveScreen extends Screen {
    protected final Screen parent;
    public SeaDriveScreen(Screen parent) {
        super(Component.literal("HowTo"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        assert this.minecraft != null;
        if(this.minecraft.level == null) guiGraphics.fill(0, 0, width, height, 0xFFFDFFFD);
        int pW = (int) (width*0.45);
        int seaDriveSize = Math.min((width - (43+pW+4)), (int) (height*0.64));
        int seaDrivePos = (43+pW+4)+((width-(43+pW+4))/2)-(seaDriveSize/2);
        guiGraphics.fill(39, 0, 41, height,0xFFD0DFB7);
        guiGraphics.fill(43, 0, 43+pW, height,0xFFD0DFB7);
        guiGraphics.fill(43+pW+2, 0, 43+pW+4, height,0xFFD0DFB7);

        guiGraphics.fill(0, 0, width, 37, 0xFF90B763);
        guiGraphics.fill(0, 39, width, 41, 0xFF90B763);
        guiGraphics.blitSprite(GuiUtils.getResourceLocation("waterplayer", "search"), seaDrivePos, (int) (height*0.55)-seaDriveSize/2, seaDriveSize, seaDriveSize);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        double scale = (double) 15 / (font.lineHeight-2);
        guiGraphics.pose().scale((float) scale, (float) scale, (float) scale);
        guiGraphics.drawString(font, "§lHow", (int) (16/scale), (int) (12/scale), 0xFF425933, false);
        guiGraphics.drawString(font, "§lTo", (int) (16/scale)+font.width("§lHow"), (int) (12/scale), 0xFFFCFFFA, false);
        guiGraphics.pose().scale(1f, 1f, 1f);
    }

    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
