package ru.kelcuprum.waterplayer.backend.sources.waterplayer;

import com.github.topi314.lavalyrics.AudioLyricsManager;
import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.kelcuprum.waterplayer.WaterPlayer;

public class LyricsWithoutException implements AudioLyricsManager {
    private final AudioLyricsManager audioLyricsManager;
    public LyricsWithoutException(AudioLyricsManager audioLyricsManager){
        this.audioLyricsManager = audioLyricsManager;
    }
    @Override
    public @NotNull String getSourceName() {
        return audioLyricsManager.getSourceName();
    }

    @Override
    public @Nullable AudioLyrics loadLyrics(@NotNull AudioTrack audioTrack) {
        AudioLyrics lyrics = null;
        try{
            lyrics = audioLyricsManager.loadLyrics(audioTrack);
        } catch (Exception ex){
            WaterPlayer.log("[Lyrics] " + ex.getLocalizedMessage(), Level.ERROR);
        }
        return lyrics;
    }

    @Override
    public void shutdown() {

    }
}
