package ru.kelcuprum.waterplayer.frontend.gui;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Async;
import ru.kelcuprum.waterplayer.WaterPlayer;

import java.util.HashMap;

public class LyricsHelper {
    public static HashMap<String, AudioLyrics> lyricsHashMap = new HashMap<>();
    public static HashMap<AudioTrack, Boolean> urls = new HashMap<>();

    public static AudioLyrics getLyrics(AudioTrack track) {
        if (lyricsHashMap.containsKey(track.getInfo().uri)) return lyricsHashMap.get(track.getInfo().uri);
        else {
            if (!urls.getOrDefault(track, false)) {
                urls.put(track, true);
                new Thread(() -> registerLyrics(track)).start();
            }
            return new SafeLyrics(track);
        }
    }

    @Async.Execute
    public static void registerLyrics(AudioTrack track) {
        AudioLyrics audioLyrics;
        try {
            audioLyrics = WaterPlayer.player.getLyricsManager().loadLyrics(track);
        } catch (Exception ex) {
            WaterPlayer.log("[Lyrics] "+ex.getLocalizedMessage(), Level.ERROR);
            audioLyrics = null;
        }
        lyricsHashMap.put(track.getInfo().uri, audioLyrics);
    }

}
