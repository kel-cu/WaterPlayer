package ru.kelcuprum.waterplayer.frontend.gui;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class SafeLyrics implements AudioLyrics {
    AudioTrack track;
    public SafeLyrics(AudioTrack track){
        this.track = track;
    }
    @Override
    public @NotNull String getSourceName() {
        return this.track.getSourceManager().getSourceName();
    }

    @Override
    public @Nullable String getProvider() {
        return this.track.getSourceManager().getSourceName();
    }

    @Override
    public @Nullable String getText() {
        return "Loading lyrics...\nPlease wait...";
    }

    @Override
    public @Nullable List<Line> getLines() {
        return List.of(new Line() {
            @Override
            public @NotNull Duration getTimestamp() {
                return Duration.ofSeconds(0);
            }

            @Override
            public @Nullable Duration getDuration() {
                return Duration.ofSeconds(track.getDuration()/1000);
            }

            @Override
            public @NotNull String getLine() {
                return "Loading lyrics...";
            }
        }, new Line() {
            @Override
            public @NotNull Duration getTimestamp() {
                return Duration.ofSeconds(0);
            }

            @Override
            public @Nullable Duration getDuration() {
                return Duration.ofSeconds(track.getDuration()/1000);
            }

            @Override
            public @NotNull String getLine() {
                return "Please wait...";
            }
        });
    }
}
