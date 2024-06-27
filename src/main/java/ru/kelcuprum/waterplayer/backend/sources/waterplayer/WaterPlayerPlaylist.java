package ru.kelcuprum.waterplayer.backend.sources.waterplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;

import java.util.ArrayList;
import java.util.List;

public class WaterPlayerPlaylist implements AudioPlaylist {
    protected final Playlist playlist;
    List<AudioTrack> tracks;

    public WaterPlayerPlaylist(Playlist playlist) {
        tracks = new ArrayList<>();
        for (String url : playlist.urls) {
            WaterPlayer.player.getAudioPlayerManager().loadItemSync(url, new AudioLoadResultHandler() {
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
                    WaterPlayer.log(exception.getMessage(), Level.ERROR);
                }
            });
        }
        this.playlist = playlist;
    }

    @Override
    public String getName() {
        return playlist.title;
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
