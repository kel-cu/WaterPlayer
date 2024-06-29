package ru.kelcuprum.waterplayer.frontend.gui.components;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.screens.TrackScreen;
import ru.kelcuprum.waterplayer.frontend.localization.Music;
import ru.kelcuprum.waterplayer.frontend.localization.StarScript;

public class CurrentTrackButton extends Button {
    protected Screen screen;
    private final boolean isShort;

    public CurrentTrackButton(int x, int y, int width, boolean isShort, Screen screen) {
        super(x, y, width, isShort ? 20 : 40, InterfaceUtils.DesignType.FLAT, Component.empty(), null);
        this.isShort = isShort;
        this.screen = screen;
    }

    @Override
    public void onPress() {
        if (isTrackEnable()) AlinLib.MINECRAFT.setScreen(new TrackScreen(screen, WaterPlayer.player.getAudioPlayer().getPlayingTrack()));
    }

    public int getHeight() {
        int i = isShort ? 20 : 40;
        if (isTrackEnable()) i += getTimelineSize();
        this.height = i;
        return i;
    }

    protected boolean isTrackEnable() {
        return WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null;
    }

    @Override
    public @NotNull Component getMessage() {
        if (isTrackEnable()) {
            AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
            StringBuilder builder = new StringBuilder();
            if (!Music.isAuthorNull(track)) builder.append("«").append(Music.getAuthor(track)).append("» ");
            builder.append(Music.getTitle(track)).append(" ").append(track.getInfo().isStream ? WaterPlayer.localization.getLocalization("format.live") : StarScript.getTimestamp(Music.getPosition(track)) + " / " + StarScript.getTimestamp(Music.getDuration(track)));
            return Component.literal(builder.toString());
        } else return Component.translatable("waterplayer.command.now_playing.notPlaying");
    }

    public int getTimelineSize() {
        return isTrackEnable() ? 4 : 0;
    }

    @Override
    public void renderText(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (isTrackEnable()) {
            this.active = true;
            AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();

            StringBuilder builder = new StringBuilder();
            if (!Music.isAuthorNull(track)) builder.append("«").append(Music.getAuthor(track)).append("» ");
            builder.append(Music.getTitle(track));

            String time = track.getInfo().isStream ? WaterPlayer.localization.getLocalization("format.live") : StarScript.getTimestamp(Music.getPosition(track)) + " / " + StarScript.getTimestamp(Music.getDuration(track));

            int color = WaterPlayer.player.getAudioPlayer().isPaused() ? InterfaceUtils.Colors.CLOWNFISH : track.getInfo().isStream ? InterfaceUtils.Colors.GROUPIE : InterfaceUtils.Colors.SEADRIVE;
            //
            if (isShort) {
                if (InterfaceUtils.isDoesNotFit(getMessage(), getWidth(), getHeight())) {
                    this.renderScrollingString(guiGraphics, AlinLib.MINECRAFT.font, 2, 0xFFFFFF);
                } else {
                    guiGraphics.drawString(AlinLib.MINECRAFT.font, builder.toString(), getX() + (getHeight() - 8) / 2, getY() + (getHeight() - 8) / 2, 0xffffff);
                    guiGraphics.drawString(AlinLib.MINECRAFT.font, time, getX() + getWidth() - AlinLib.MINECRAFT.font.width(time) - ((getHeight() - 8) / 2), getY() + (getHeight() - 8) / 2, 0xffffff);
                }
            } else {
                ResourceLocation icon = Music.getThumbnail(track);
                guiGraphics.blit(icon, getX() + 2, getY() + 2, 0.0F, 0.0F, 36, 36, 36, 36);
                renderString(guiGraphics, Music.getTitle(track), getX() + 45, getY() + 8);
                renderString(guiGraphics, Music.getAuthor(track), getX() + 45, getY() + height - getTimelineSize() - 8 - AlinLib.MINECRAFT.font.lineHeight);
                if(isHovered() && mouseY > getY() + getHeight() - getTimelineSize() - 2) guiGraphics.renderTooltip(AlinLib.MINECRAFT.font, Component.literal(time), mouseX, mouseY);
            }
            double state = track.getInfo().isStream ? 1 : ((double) track.getPosition() / track.getDuration());
            guiGraphics.fill(getX(), getY() + getHeight() - getTimelineSize(), getX() + getWidth(), getY() + getHeight(), color - 0xb2000000);
            guiGraphics.fill(getX(), getY() + getHeight() - getTimelineSize(), (int) (getX() + (getWidth() * state)), getY() + getHeight(), color - 1291845632);
        } else {
            this.active = false;
            guiGraphics.drawCenteredString(AlinLib.MINECRAFT.font, Component.translatable("waterplayer.command.now_playing.notPlaying"), getX() + (getWidth() / 2), getY() + (getHeight() / 2) - (AlinLib.MINECRAFT.font.lineHeight / 2), -1);
        }
    }

    protected void renderString(GuiGraphics guiGraphics, String text, int x, int y) {
        if (getWidth() - 50 < AlinLib.MINECRAFT.font.width(text)) {
            renderScrollingString(guiGraphics, AlinLib.MINECRAFT.font, Component.literal(text), 5, y-1, -1);
        } else {
            guiGraphics.drawString(AlinLib.MINECRAFT.font, text, x, y, -1);
        }
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, Component message, int x, int y, int color) {
        int k = this.getX() + x;
        int l = this.getX() + this.getWidth() - x;
        if(!isShort) k+=40;
        renderScrollingString(guiGraphics, font, message, k, y, l, y+font.lineHeight, color);
    }

    private void setValueFromMouse(double d) {
        if (isDragble()) {
            double pos = ((d - (double) (this.getX() + 2)) / (double) (this.getWidth() - 4));
            long dur = WaterPlayer.player.getAudioPlayer().getPlayingTrack().getDuration();
            WaterPlayer.player.getAudioPlayer().getPlayingTrack().setPosition((long) (dur * pos));
        }
    }

    protected boolean isDragble() {
        return isTrackEnable() && !WaterPlayer.player.getAudioPlayer().getPlayingTrack().getInfo().isStream;
    }

    @Override
    public void onClick(double d, double e) {
        if (isDragble() && e > getY() + getHeight() - getTimelineSize() - 2) setValueFromMouse(d);
        else super.onClick(d, e);
    }

    @Override
    protected void onDrag(double d, double e, double f, double g) {
        if (isDragble() && e > getY() + getHeight() - getTimelineSize() - 2) setValueFromMouse(d);
        else super.onDrag(d, e, f, g);
    }
}
