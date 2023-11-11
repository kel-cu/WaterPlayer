package ru.kelcuprum.waterplayer.gui.screens;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.localization.Localization;
import ru.kelcuprum.waterplayer.localization.Music;

import java.awt.*;
import java.util.List;

public class OverlayHandler implements HudRenderCallback, ClientTickEvents.StartTick {
    private final List<Component> textList = new ObjectArrayList<>();

    private final Minecraft client = Minecraft.getInstance();
    private boolean isLive = false;
    private boolean isPause = true;

    @Override
    public void onStartTick(Minecraft client) {
        this.textList.clear();
        isLive = false;
        isPause = true;
        if (WaterPlayer.music.getAudioPlayer().getPlayingTrack() != null && WaterPlayer.config.getBoolean("ENABLE_OVERLAY", true)) {
            isLive = WaterPlayer.music.getAudioPlayer().getPlayingTrack().getInfo().isStream;
            isPause = WaterPlayer.music.getAudioPlayer().isPaused();
            if(!Music.isAuthorNull()) this.textList.add(Localization.toText(Music.getAuthor()));
            this.textList.add(Localization.toText(Music.getTitle()));
            this.textList.add(Localization.toText(Localization.getParsedText("{player.speaker_icon} {player.volume}% {format.time}", false)));
        }

    }
    private int maxX = 10;
    private int maxY = 10;
    @Override
    public void onHudRender(GuiGraphics drawContext, float tickDelta) {
        boolean isDebugOverlay = this.client.gui.getDebugOverlay().showDebugScreen();
        if(isDebugOverlay) return;
        if (WaterPlayer.music.getAudioPlayer().getPlayingTrack() != null  && WaterPlayer.config.getBoolean("ENABLE_OVERLAY", true)) {
            maxX = 10;
            maxY = 10;
            int l = 0;
            int k = 0;
            int x;
            int y;
            for (Component text : this.textList) {
                y = 10 + (l*this.client.font.lineHeight);
                if(maxY < y) maxY = y;
                int width = this.client.font.width(Localization.toString(text));
                if(maxX < (width)) maxX = width;
                l++;
            }
            drawBackground(drawContext);
            for (Component text : this.textList) {
                x = 10;
                y = 10 + (k*this.client.font.lineHeight);
                this.drawString(drawContext, text, x, y);
                k++;
            }
        }
    }

    private void drawString(GuiGraphics guiGraphics, Component text, int x, int y) {
        guiGraphics.drawString(this.client.font, text, x, y, 16777215);
    }
    private void drawBackground(GuiGraphics guiGraphics) {
//        int rgba = new Color(0x75020E15, true).getRGB();
        final float fc = 1.5F * 0.9F + 0.1F;
        final int colorBackground = (int) (255.0F * fc);

        // old - 0x7602131E
        // - - - - - - - - - -
        // red - 0xB6861919
        // green - 0xB6197C40
        // yellow - 0xB6C29D25
        int rgbaTime = new Color(isPause ? 0xB6C29D25 : isLive ? 0xB6861919 : 0xB6197C40, true).getRGB();

        // red - 0xB6FF3131
        // green - 0xB631FF83
        // yellow - 0xB6FFCF31
        int rgbaTimeLine = new Color(isPause ? 0xB6FFCF31 : isLive ? 0xB6FF3131 : 0xB631FF83, true).getRGB();

        guiGraphics.fill(RenderType.guiOverlay(), 4, 4, maxX+(this.client.font.lineHeight)+6, maxY+(this.client.font.lineHeight)+4, colorBackground / 2 << 24);

        // Timeline
        guiGraphics.fill(RenderType.guiOverlay(), 4, maxY+(this.client.font.lineHeight)+4+1, maxX+(this.client.font.lineHeight)+6, maxY+(this.client.font.lineHeight)+4+2, rgbaTime);
        guiGraphics.fill(RenderType.guiOverlay(), 4, maxY+(this.client.font.lineHeight)+4+1, getTimelineSize(), maxY+(this.client.font.lineHeight)+4+2, rgbaTimeLine);
    }

    private int getTimelineSize() {
        int timelineSize = maxX+this.client.font.lineHeight+6;
        if(!isLive){
            int max = maxX+this.client.font.lineHeight+2;
            double onePercent = max / 100.0;
            double percentTime = ((double) WaterPlayer.music.getAudioPlayer().getPlayingTrack().getPosition() / WaterPlayer.music.getAudioPlayer().getPlayingTrack().getDuration())*100.0;
            timelineSize = (int) ((max+4)-(max-(onePercent*percentTime)));
        }
        return timelineSize;
    }
}
