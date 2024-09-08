package ru.kelcuprum.waterplayer.backend.sources.waterplayer.lyrics;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class FileLyrics implements AudioLyrics {
    final AudioTrack track;
    List<Line> lines = null;
    String text ;

    public FileLyrics(AudioTrack track, String text) {
        this.track = track;
        if(text.isBlank()) this.text = null;
        {
            if (LRCLyricsFormat.isLrcFormat(text)) {
                LRCLyricsFormat lrc = new LRCLyricsFormat(track, text);
                this.lines = lrc.lines;
                this.text = lrc.text;
            } else if (SRTLyricsFormat.isSrtFormat(text)) {
                SRTLyricsFormat srt = new SRTLyricsFormat(track, text);
                this.lines = srt.lines;
                this.text = srt.text.toString();
            } else this.text = text;
        }
    }

    @Override
    public @NotNull String getSourceName() {
        return "file";
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
