package ru.kelcuprum.waterplayer.frontend.gui.screens.control.components;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.localization.Music;

import static ru.kelcuprum.alinlib.gui.Colors.*;
import static ru.kelcuprum.waterplayer.WaterPlayer.getTimestamp;

public class TimelineComponent extends AbstractSliderButton {
    public boolean showTime;
    public TimelineComponent(int x, int y, int width, int height, boolean showTime) {
        super(x, y, width, height, Component.empty(),  WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null ? (double) WaterPlayer.player.getAudioPlayer().getPlayingTrack().getPosition() /WaterPlayer.player.getAudioPlayer().getPlayingTrack().getDuration() : 0);
        this.showTime = showTime;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
        if(track == null) {
            value = 0;
            this.active = false;
        }
        else {
            this.active = true;
            value = WaterPlayer.player.getAudioPlayer().getPlayingTrack ().getInfo().isStream ? 1 : (double) track.getPosition() / track.getDuration();
        }
        GuiUtils.getSelected().renderBackground$widget(guiGraphics, getX(), getY(), getWidth(), getHeight(), isActive(), isHoveredOrFocused());
        if(isActive()) {
            guiGraphics.fill(getX(), getY(), (int) (getX()+(getWidth()*value)), getY()+getHeight(), WaterPlayer.player.getAudioPlayer().isPaused() ? CLOWNFISH : track.getInfo().isStream ? GROUPIE : SEADRIVE);
            if(showTime && !track.getInfo().isStream) {
                guiGraphics.drawString(AlinLib.MINECRAFT.font, WaterPlayer.getTimestamp(track.getPosition()), getX(), getY() - 2 - AlinLib.MINECRAFT.font.lineHeight, -1);
                String dur = WaterPlayer.getTimestamp(WaterPlayer.player.getAudioPlayer().getPlayingTrack().getDuration());
                guiGraphics.drawString(AlinLib.MINECRAFT.font, dur, getX() + getWidth() - AlinLib.MINECRAFT.font.width(dur), getY() - 2 - AlinLib.MINECRAFT.font.lineHeight, -1);
            } else if(!showTime && isHovered()){
                String time = track.getInfo().isStream ? WaterPlayer.localization.getLocalization("format.live") : getTimestamp(Music.getPosition(track)) + " / " + getTimestamp(Music.getDuration(track));
                guiGraphics.renderTooltip(AlinLib.MINECRAFT.font, Component.literal(time), i, j);
            }
        }
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
        if(WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null){
            WaterPlayer.player.getAudioPlayer().getPlayingTrack().setPosition((long) (WaterPlayer.player.getAudioPlayer().getPlayingTrack().getDuration()*value));
        }
    }
}
