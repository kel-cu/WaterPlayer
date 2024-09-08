package ru.kelcuprum.waterplayer.backend.sources.waterplayer.lyrics;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class LRCLyricsFormat implements AudioLyrics {
    final AudioTrack track;
    List<Line> lines = new ArrayList<>();
    String text;

    public static boolean isLrcFormat(String text){
        return text.replaceAll("^\\[(\\d*:\\d*\\.?\\d*)]", "").length() < text.length();
    }

    public LRCLyricsFormat(AudioTrack track, String text){
        this.track = track;
        this.text = text;
        if(text.replaceAll("^\\[(\\d*:\\d*\\.?\\d*)]", "").length() < text.length()){
            lines = new ArrayList<>();
            String[] sLines = text.split("\\n");
            StringBuilder builderText = new StringBuilder();
            String line = "";
            long start = 0;
            long duration = 0;
            for(int i = 0; i < sLines.length; i++){
                String cleanLine = sLines[i].replaceAll("^\\[(\\d*:\\d*\\.?\\d*)]", "");
                if(cleanLine.isBlank()) cleanLine = "🎵";
                if(i != 0) builderText.append("\n");
                String time = sLines[i].replace(cleanLine, "").replaceAll("[\\[\\]]", "");

                if(i>0) {
                    duration = parseLrcTime(time) - start;
                    long finalStart = start;
                    long finalDuration = duration;
                    String finalLine = line;
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
                            return finalLine;
                        }
                    });
                } else if(i == (sLines.length-1)){
                    duration = track.getDuration()-parseLrcTime(time);
                    long finalStart = parseLrcTime(time);
                    long finalDuration = duration;
                    String finalLine = cleanLine;
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
                            return finalLine;
                        }
                    });
                }
                line = cleanLine;
                start = parseLrcTime(time);
            }
            this.text = builderText.toString();
        }
    }
    public long parseLrcTime(String time){
        String[] splitTile = time.split(":");
        long tdl = 0;
        tdl += (long) parseInt(splitTile[0]) * 60 * 1000;
        tdl += (long) parseInt(splitTile[1].split("\\.")[0]) * 1000;
        tdl += (long) ((parseDouble(splitTile[1].split("\\.")[1])/100)*1000);
        return tdl;
    }
    @Override
    public @NotNull String getSourceName() {
        return "lrc-file";
    }

    @Override
    public @Nullable String getProvider() {
        return track.getSourceManager().getSourceName();
    }

    @Override
    public @Nullable String getText() {
        return text;
    }

    @Override
    public @Nullable List<Line> getLines() {
        return lines;
    }
}
