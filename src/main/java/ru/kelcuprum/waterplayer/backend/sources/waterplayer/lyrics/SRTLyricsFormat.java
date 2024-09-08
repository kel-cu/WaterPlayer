package ru.kelcuprum.waterplayer.backend.sources.waterplayer.lyrics;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.kelcuprum.waterplayer.WaterPlayer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

public class SRTLyricsFormat implements AudioLyrics {
    final AudioTrack track;
    List<Line> lines = new ArrayList<>();
    StringBuilder text = new StringBuilder();

    public static boolean isSrtFormat(String text){
        return text.replaceAll("\\d*:\\d*:\\d*,?\\d* --> \\d*:\\d*:\\d*,?\\d*", "").length() < text.length();
    }

    public SRTLyricsFormat(AudioTrack track, String data){
        this.track = track;
        String[] pepe = data.split("\n");
        boolean isLine;
        int ll = 0;
        StringBuilder lineText = new StringBuilder();
        long start = 0;
        long duration = 0;
        for(String line : pepe) {
            line = line.replace("\r", "");
            isLine = !line.isBlank();
            if(isLine){
                if(ll == 1){
                    String[] times = line.split("-->");
                    //
                    String[] ts = times[0].replace(" ", "").split(":");
                    start+= (long) parseInt(ts[0]) *60*60*1000;
                    start+= (long) parseInt(ts[1]) *60*1000;
                    start+= parseInt(ts[2].split(",")[0])*1000L;
                    start+= parseInt(ts[2].split(",")[1]);
                    //
                    String[] td = times[1].replace(" ", "").split(":");
                    long tdl = 0;
                    tdl+= (long) parseInt(td[0]) *60*60*1000;
                    tdl+= (long) parseInt(td[1]) *60*1000;
                    tdl+= parseInt(td[2].split(",")[0])*1000L;
                    tdl+= parseInt(td[2].split(",")[1]);
                    duration = tdl - start;
                } else if(ll > 1){
                    if(ll>2) lineText.append("\n");
                    lineText.append(line);
                    text.append(line).append("\n");
                }
                    ll++;
            } else {
                long finalStart = start;
                long finalDuration = duration;
                StringBuilder finalLineText = lineText;
                WaterPlayer.log(String.format("Start: %s, Duration: %s, Text: %s", WaterPlayer.getTimestamp(start), WaterPlayer.getTimestamp(duration), lineText), Level.DEBUG);
                WaterPlayer.log("--- new line ---", Level.DEBUG);
                lines.add(new Line() {
                    @Override
                    public @NotNull Duration getTimestamp() {
                        return Duration.ofMillis(finalStart);
                    }

                    @Override
                    public @Nullable Duration getDuration() {
                        return Duration.ofMillis(finalDuration);
                    }

                    @Override
                    public @NotNull String getLine() {
                        return finalLineText.toString();
                    }
                });
                ll = 0;
                start = 0;
                duration = 0;
                lineText = new StringBuilder();
            }
        }
    }
    @Override
    public @NotNull String getSourceName() {
        return "srt-file";
    }

    @Override
    public @Nullable String getProvider() {
        return track.getSourceManager().getSourceName();
    }

    @Override
    public @Nullable String getText() {
        return text.toString();
    }

    @Override
    public @Nullable List<Line> getLines() {
        return lines;
    }
}
