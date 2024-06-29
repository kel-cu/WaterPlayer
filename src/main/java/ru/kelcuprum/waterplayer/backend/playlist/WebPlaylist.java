package ru.kelcuprum.waterplayer.backend.playlist;

import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.exception.WebPlaylistException;
import ru.kelcuprum.waterplayer.backend.sources.waterplayer.WaterPlayerPlaylist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class WebPlaylist {
    public String id;
    public String url;
    public Playlist playlist;

    public WebPlaylist(JsonObject data) throws WebPlaylistException {
        if (!isValid(data)) throw new WebPlaylistException("Incorrect web playlist format");
        id = data.has("id") ? data.get("id").getAsString() : "Example title";
        url = data.has("url") ? data.get("url").getAsString() : "";
        playlist = new Playlist(data.getAsJsonObject("data"));
    }

    public static boolean isValid(JsonObject data) {
        return data.has("id") && data.has("url") && data.has("data") && Playlist.isValid(data.getAsJsonObject("data"));
    }

    public WebPlaylist save() throws WebPlaylistException {
        Path path = AlinLib.MINECRAFT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists/" + id + ".json");
        if (path.toFile().exists())
            path = AlinLib.MINECRAFT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists/" + url + ".json");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, playlist.toJSON().toString());
            playlist = new Playlist(path);
        } catch (IOException e) {
            WaterPlayer.log(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), Level.ERROR);
            throw new WebPlaylistException("External error: " + (e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
        }
        return this;
    }

    public List<AudioTrack> getTracks(){
        return new WaterPlayerPlaylist(playlist).getTracks();
    }
}
