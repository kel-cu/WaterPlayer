package ru.kelcuprum.waterplayer.frontend.gui.overlays;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.api.events.client.ClientTickEvents;
import ru.kelcuprum.alinlib.api.events.client.GuiRenderEvents;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.LyricsHelper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SubtitlesHandler implements GuiRenderEvents {
    private final List<FormattedCharSequence> texts = new ArrayList<>();
//    @Override
//    public void onStartTick(Minecraft client) {
//        updateTexts();
//    }

    public void updateTexts() {
        this.texts.clear();
        AudioLyrics audioLyrics;
        try {
            if (isTrackEnable()) {
                AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
                audioLyrics = LyricsHelper.getLyrics(track);
                if (audioLyrics != null) {
                    List<AudioLyrics.Line> list = audioLyrics.getLines();
                    if (list != null) {
                        StringBuilder builder = new StringBuilder();
                        for (AudioLyrics.Line line : list) {
                            Duration pos = Duration.ofMillis(track.getPosition());
                            if (line.getDuration() != null) {
                                if (pos.toMillis() >= line.getTimestamp().toMillis() && pos.toMillis() <= line.getTimestamp().toMillis() + line.getDuration().toMillis()) {
                                    builder.append(line.getLine()).append("\n");
                                }
                            }
                        }
                        texts.addAll(AlinLib.MINECRAFT.font.split(FormattedText.of(builder.toString()), AlinLib.MINECRAFT.getWindow().getGuiScaledWidth() / 2));
                    }
                }
            }
        } catch (Exception ex) {
            WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
        }
    }

    protected boolean isTrackEnable() {
        return WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null;
    }

    @Override
    public void onRender(GuiGraphics guiGraphics, float tickDelta) {
        if (!WaterPlayer.config.getBoolean("SUBTITLES", false)) return;
        updateTexts();
        int l = texts.size() - 1;
        int iay = WaterPlayer.config.getNumber("SUBTITLES.INDENT_Y", 85).intValue();
        int f = AlinLib.MINECRAFT.font.lineHeight + 6;
        for (FormattedCharSequence text : texts) {
            if(AlinLib.MINECRAFT.font.width(text) > 0){
                int back = (int) (255.0F * WaterPlayer.config.getNumber("SUBTITLES.BACK_ALPHA", 0.5).doubleValue()) << 24;
                guiGraphics.fill((guiGraphics.guiWidth() / 2) - (AlinLib.MINECRAFT.font.width(text) / 2) - 3, (guiGraphics.guiHeight() - iay - (l * f)) - 3
                        , (guiGraphics.guiWidth() / 2) + (AlinLib.MINECRAFT.font.width(text) / 2) + 3, (guiGraphics.guiHeight() - iay - (l * f)) - 3 + f, back);
                guiGraphics.drawCenteredString(AlinLib.MINECRAFT.font, text, guiGraphics.guiWidth() / 2, guiGraphics.guiHeight() - iay - (l * f), WaterPlayer.config.getNumber("SUBTITLES.TEXT_COLOR", -1).intValue());
                l--;
            }
        }
    }
}
