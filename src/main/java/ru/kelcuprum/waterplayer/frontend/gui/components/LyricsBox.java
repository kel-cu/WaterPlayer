package ru.kelcuprum.waterplayer.frontend.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import ru.kelcuprum.alinlib.AlinLib;

import java.util.List;

import static com.mojang.blaze3d.pipeline.MainTarget.DEFAULT_HEIGHT;
import static ru.kelcuprum.alinlib.gui.GuiUtils.DEFAULT_WIDTH;

public class LyricsBox extends AbstractWidget {
    protected Component lyrics;
    protected double position = 1;

    public LyricsBox(int x, int y, Component label) {
        this(x, y, DEFAULT_WIDTH(), DEFAULT_HEIGHT, label);
    }

    ///
    public LyricsBox(int x, int y, int width, int height, Component label) {
        super(x, y, width, height, label);
        this.active = false;
    }


    @Override
    public void setX(int x) {
        super.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
    }
    int textHeight = 0;
    int lastTextHeight = 0;
    int textSize = (AlinLib.MINECRAFT.font.lineHeight + 3);
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        List<FormattedCharSequence> list = AlinLib.MINECRAFT.font.split(lyrics, width - 12);
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0x75000000);
        guiGraphics.enableScissor(getX(), getY(), getX() + width, getY() + height);
        int y = getY() + 6;
        textHeight = ((list.size()+1)*textSize)+12;
        if(lastTextHeight != textHeight){
            scrollAmount = Mth.clamp(scrollAmount, 0, textHeight-height-textSize);
            lastTextHeight -= lastTextHeight-textHeight;
            if(height > textHeight) scrollAmount = 0;
        }

        for (FormattedCharSequence text : list) {
            guiGraphics.drawCenteredString(AlinLib.MINECRAFT.font, text, getX() + (getWidth() / 2), (int) (y-scrollAmount), -1);
            y+=textSize;
        }
        guiGraphics.disableScissor();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
    double scrollRate = 9.0;
    double scrollAmount = 0;
    @Override
    //#if MC >= 12002
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        //#elseif MC < 12002
        //$$ public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        //#endif
        scrollAmount = Mth.clamp(scrollAmount- scrollY*scrollRate, 0, lastTextHeight-height-textSize);
        return true;
    }
    Component lastLyrics;
    public LyricsBox setLyrics(Component lyrics) {
        if(lastLyrics != lyrics) lastLyrics = lyrics;
        this.lyrics = lyrics;
        return this;
    }
}
