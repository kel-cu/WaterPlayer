package ru.kelcuprum.waterplayer.frontend.gui;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Async;
import ru.kelcuprum.waterplayer.WaterPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Objects;

public class LyricsHelper {
    public static HashMap<String, AudioLyrics> lyricsHashMap = new HashMap<>();
    public static HashMap<AudioTrack, Boolean> urls = new HashMap<>();

    public static void clear(){
        lyricsHashMap.clear();
        urls.clear();
    }

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
            WaterPlayer.log("[Lyrics] " + ex.getLocalizedMessage(), Level.ERROR);
            audioLyrics = null;
        }
        lyricsHashMap.put(track.getInfo().uri, audioLyrics);
    }

    public static String saveSRT(AudioTrack track, String text) throws IOException {
        String id = WaterPlayer.parseFileSystem(track.getSourceManager().getSourceName() + "_" + track.getIdentifier());
        File srt = new File("./config/WaterPlayer/Lyrics/" + id + ".srt");
        AudioLyrics lyrics = getLyrics(track);
        StringBuilder builder = new StringBuilder();
        if (lyrics == null || lyrics.getLines() == null || lyrics.getLines().isEmpty()) {
            String[] lines = text.split("\n");
            String start = formatTimeToSRT(0);
            String end = formatTimeToSRT(track.getDuration());
            int i = 1;
            for (String line : lines) {
                builder.append(i).append("\n").append(start).append(" --> ").append(end).append("\n").append(line).append("\n\n");
                i++;
            }
        } else {
            int i = 1;
            for (AudioLyrics.Line line : lyrics.getLines()) {
                builder.append(i).append("\n").append(formatTimeToSRT(line.getTimestamp().toMillis())).append(" --> ").append(formatTimeToSRT(line.getTimestamp().toMillis() + Objects.requireNonNull(line.getDuration()).toMillis())).append("\n").append(line.getLine()).append("\n\n");
                i++;
            }
        }
        Files.createDirectories(srt.toPath().getParent());
        Files.writeString(srt.toPath(), builder.toString());
        return srt.getPath();
    }

    public static String formatTimeToSRT(long milliseconds) {
        int ms = (int) milliseconds % 1000;
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, ms);
    }

}
