package ru.kelcuprum.waterplayer.backend.sources.directory;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.waterplayer.WaterPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DirectoryPlaylist implements AudioPlaylist {
    protected final File file;
    List<AudioTrack> tracks;

    public DirectoryPlaylist(File file) {
        tracks = new ArrayList<>();
        if(!file.isDirectory()) throw new IllegalArgumentException("File is not folder");
        if(file.exists() && file.listFiles() != null) {
            for (File url : Objects.requireNonNull(file.listFiles())) {
                if(url.isFile()) WaterPlayer.player.getAudioPlayerManager().loadItemSync(url.getPath(), new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        tracks.add(track);
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        tracks.addAll(playlist.getTracks());
                    }

                    @Override
                    public void noMatches() {
                        WaterPlayer.log("Nothing Found by " + url, Level.WARN);
                    }

                    @Override
                    public void loadFailed(FriendlyException exception) {
                        if(!exception.getMessage().toLowerCase().startsWith("unknown file format")) WaterPlayer.log(exception.getMessage(), Level.ERROR);
                    }
                });
            }
        }
        this.file = file;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public List<AudioTrack> getTracks() {
        return tracks;
    }

    @Override
    public AudioTrack getSelectedTrack() {
        return getTracks().isEmpty() ? null : getTracks().get(0);
    }

    @Override
    public boolean isSearchResult() {
        return false;
    }
}
