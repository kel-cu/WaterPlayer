package ru.kelcuprum.waterplayer.backend.sources.waterplayer;

import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.WaterPlayerAPI;
import ru.kelcuprum.waterplayer.backend.exception.WebPlaylistException;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class WaterPlayerSource implements AudioSourceManager {

    @Override
    public String getSourceName() {
        return "wplayer";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        var identifier = reference.identifier;
        File file = new File(identifier);
        if(identifier.startsWith("playlist:") || (file.exists() && file.isFile() && file.getName().endsWith(".json"))){
            String name = identifier.replace("playlist:", "");
            if (file.exists()) {
                try {
                    return new WaterPlayerPlaylist(new Playlist(file.toPath()));
                } catch (Exception ex) {
                    WaterPlayer.log("ERROR: "+(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage()), Level.DEBUG);
                }
            } else {
                JsonObject jsonPlaylist = new JsonObject();

                final Path configFile = AlinLib.MINECRAFT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists/" + name + ".json");
                try {
                    jsonPlaylist = GsonHelper.parse(Files.readString(configFile));
                } catch (Exception ex) {
                    WaterPlayer.log("ERROR: "+(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage()), Level.DEBUG);
                }
                return new WaterPlayerPlaylist(new Playlist(jsonPlaylist));
            }

        }
        if(identifier.startsWith("http://") || identifier.startsWith("https://")){
            try{
                Playlist playlist = WaterPlayerAPI.getPlaylist(identifier, false);
                return new WaterPlayerPlaylist(playlist);
            } catch (Exception ex){
                WaterPlayer.log("ERROR: "+(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage()), Level.DEBUG);
            }
        }
        if(identifier.startsWith(getSourceName()+":")){
            String id = identifier.replace(getSourceName()+":", "");
            String url = String.format(WaterPlayerAPI.config.getString("PLAYLIST_URL", WaterPlayerAPI.getURL("/playlist/%s")), id);
            try {
                return new WaterPlayerPlaylist(WaterPlayerAPI.getPlaylist(url, false));
            } catch (WebPlaylistException ex) {
                WaterPlayer.log("ERROR: "+(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage()), Level.DEBUG);
            }
        }
        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {

    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return null;
    }

    @Override
    public void shutdown() {

    }
}
