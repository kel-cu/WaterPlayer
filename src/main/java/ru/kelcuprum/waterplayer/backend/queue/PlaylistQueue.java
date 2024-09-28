package ru.kelcuprum.waterplayer.backend.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;

public class PlaylistQueue extends AbstractQueue {
    public final Playlist playlist;
    public PlaylistQueue(Playlist playlist) {
        super(playlist.getLavaplayerPlaylist().getTracks());
        this.playlist = playlist;
    }

    @Override
    public Boolean addTrackAvailable() {
        return false;
    }

    @Override
    public String getName() {
        return Component.translatable("waterplayer.control.playing_playlist", playlist.title).getString();
    }

    @Override
    public void addTrack(AudioTrack audioTrack) {
        WaterPlayer.log("PlaylistQueue not support addTrack");
    }
}

