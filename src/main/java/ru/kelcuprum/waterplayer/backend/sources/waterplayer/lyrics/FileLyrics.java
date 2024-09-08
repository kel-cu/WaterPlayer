package ru.kelcuprum.waterplayer.backend.sources.waterplayer.lyrics;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class FileLyrics implements AudioLyrics {
    final AudioTrack track;
    List<Line> lines = null;
    String text;

    public FileLyrics(AudioTrack track, String text){
        this.track = track;
        this.text = text;
        if(LRCLyricsFormat.isLrcFormat(text)){
            LRCLyricsFormat lrc = new LRCLyricsFormat(track, text);
            this.lines = lrc.lines;
            this.text = lrc.text;
        } else if(SRTLyricsFormat.isSrtFormat(text)){
            SRTLyricsFormat srt = new SRTLyricsFormat(track, text);
            this.lines = srt.lines;
            this.text = srt.text.toString();
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
        return "file";
    }

    @Override
    public @Nullable String getProvider() {
        return track.getSourceManager().getSourceName();
    }

    @Override
    public @Nullable String getText() { return text; }

    @Override
    public @Nullable List<Line> getLines() {
        return lines;
    }
}
