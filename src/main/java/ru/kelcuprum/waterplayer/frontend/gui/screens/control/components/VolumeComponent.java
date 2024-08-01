package ru.kelcuprum.waterplayer.frontend.gui.screens.control.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.localization.MusicHelper;

import static ru.kelcuprum.alinlib.gui.Colors.*;

public class VolumeComponent extends AbstractSliderButton {
    public VolumeComponent(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty(), (double) WaterPlayer.player.getVolume() /100);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        GuiUtils.getSelected().renderBackground$widget(guiGraphics, getX(), getY(), getWidth(), getHeight(), isActive(), isHoveredOrFocused());
        guiGraphics.fill(getX(), getY(), (int) (getX()+(getWidth()*value)), getY()+getHeight(), WHITE);
        int y = getY()+(getHeight()/2)-(isHoveredOrFocused() ? AlinLib.MINECRAFT.font.lineHeight/2 : 7);
        String volumeString = WaterPlayer.player.getVolume()+"%";
        if(isHoveredOrFocused()) guiGraphics.drawString(AlinLib.MINECRAFT.font, volumeString, getX()-6-AlinLib.MINECRAFT.font.width(volumeString), y, -1);
            else guiGraphics.blit(MusicHelper.getSpeakerVolumeIcon(), getX()-20, y, 0f, 0f, 14, 14, 14, 14);
    }

    @Override
    protected void updateMessage() {

    }

    private void setValueFromMouse(double d) {
        this.setValue((d - (double)(this.getX())) / (double)(this.width));
        applyValue();
    }
    @Override
    protected void onDrag(double d, double e, double f, double g) {
        this.setValueFromMouse(d);
    }
    @Override
    public void onClick(double d, double e) {
        this.setValueFromMouse(d);
    }
    private void setValue(double d) {
        double e = this.value;
        this.value = Mth.clamp(d, 0.0, 1.0);
        if (e != this.value) {
            this.applyValue();
        }

        this.updateMessage();
    }

    @Override
    protected void applyValue() {
        WaterPlayer.player.getAudioPlayer().setVolume((int) (100*value));
        WaterPlayer.config.setNumber("CURRENT_MUSIC_VOLUME", (int) (100*value));
    }
}
