package ru.kelcuprum.waterplayer.frontend.gui.screens.control.components;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
//#if MC >= 12106
import net.minecraft.client.renderer.RenderPipelines;
//#endif
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.TrackScheduler;
import ru.kelcuprum.waterplayer.frontend.gui.overlays.OverlayHandler;
import ru.kelcuprum.waterplayer.frontend.localization.MusicHelper;

import static ru.kelcuprum.alinlib.gui.Colors.*;
import static ru.kelcuprum.waterplayer.WaterPlayer.getTimestamp;

public class TimelineComponent extends AbstractSliderButton {
    public boolean showTime;
    public TimelineComponent(int x, int y, int width, int height, boolean showTime) {
        super(x, y, width, height, Component.empty(),  WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null ? (double) TrackScheduler.trackPosition /WaterPlayer.player.getAudioPlayer().getPlayingTrack().getDuration() : 0);
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
            value = WaterPlayer.player.getAudioPlayer().getPlayingTrack ().getInfo().isStream ? 1 : (double) TrackScheduler.trackPosition / track.getDuration();
        }
        GuiUtils.getSelected().renderBackground$widget(guiGraphics, getX(), getY(), getWidth(), getHeight(), isActive(), isHoveredOrFocused());
        if(isActive()) {
            guiGraphics.fill(getX(), getY(), (int) (getX()+(getWidth()*value)), getY()+getHeight(), WaterPlayer.player.getAudioPlayer().isPaused() ? CLOWNFISH : track.getInfo().isStream ? GROUPIE : 0xFFFFFFFF);
            if(isHoveredOrFocused()) {
                int pos = (int) (getX()+(getWidth()*value));
                guiGraphics.fill(pos-1, getY()-2, pos+2, getY()+getHeight()+2, -1);

                if(showTime && !track.getInfo().isStream) {
                    float scale = 0.7f;
                    int color = 0xA9FFFFFF;
                    //#if MC >= 12106
                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().scale(scale, scale);
                    //#else
                    //$$ guiGraphics.pose().pushPose();
                    //$$ guiGraphics.pose().scale(scale, scale, scale);
                    //#endif
                    int y = (int) ((getY() - 2)  / scale) - AlinLib.MINECRAFT.font.lineHeight;
                    guiGraphics.drawString(AlinLib.MINECRAFT.font, WaterPlayer.getTimestamp(MusicHelper.getPosition(track)), (int) (getX()/scale), y, color);
                    String dur = WaterPlayer.getTimestamp(track.getDuration());
                    guiGraphics.drawString(AlinLib.MINECRAFT.font, dur, (int) ((getX() + getWidth())/scale) - AlinLib.MINECRAFT.font.width(dur), y, color);
                    //#if MC >= 12106
                    guiGraphics.pose().popMatrix();
                    //#else
                    //$$ guiGraphics.pose().popPose();
                    //#endif
                } else if(!showTime){
                    String time = track.getInfo().isStream ? WaterPlayer.localization.getLocalization("format.live") : getTimestamp(MusicHelper.getPosition(track)) + " / " + getTimestamp(MusicHelper.getDuration(track));
                    //#if MC >= 12106
                    guiGraphics.setTooltipForNextFrame(Component.literal(time), i, j);
                    //#else
                    //$$ guiGraphics.renderTooltip(AlinLib.MINECRAFT.font, Component.literal(time), i, j);
                    //#endif
                }
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
            WaterPlayer.player.setPosition((long) (WaterPlayer.player.getAudioPlayer().getPlayingTrack().getDuration()*value));
        }
    }
}
