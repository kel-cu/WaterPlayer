package ru.kelcuprum.waterplayer.backend.sources.waterplayer.lyrics;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class WPLyricsFormat implements AudioLyrics {
    protected final AudioTrack track;
    protected final JsonObject data;

    public WPLyricsFormat(AudioTrack track, JsonObject data){
        this.track = track;
        this.data = data;
    }
    @Override
    public @NotNull String getSourceName() {
        return "wp-lyrics-format";
    }

    @Override
    public @Nullable String getProvider() {
        return track.getSourceManager().getSourceName();
    }

    @Override
    public @Nullable String getText() {
        return data.has("text") ? data.get("text").getAsString() : "";
    }

    @Override
    public @Nullable List<Line> getLines() {
        JsonArray array = data.has("lines") ? data.getAsJsonArray("lines") : new JsonArray();
        if(array.isEmpty()) return null;
        else {
            List<Line> lines = new ArrayList<>();
            for(JsonElement data : array){
                JsonObject json = data.getAsJsonObject();
                Line line = new Line() {
                    @Override
                    public @NotNull Duration getTimestamp() {
                        return Duration.ofMillis(json.get("millisecond").getAsNumber().longValue());
                    }

                    @Override
                    public @Nullable Duration getDuration() {
                        return Duration.ofMillis(json.get("duration").getAsNumber().longValue());
                    }

                    @Override
                    public @NotNull String getLine() {
                        return json.get("line").getAsString();
                    }
                };
                lines.add(line);
            }
            return lines;
        }
    }
}
