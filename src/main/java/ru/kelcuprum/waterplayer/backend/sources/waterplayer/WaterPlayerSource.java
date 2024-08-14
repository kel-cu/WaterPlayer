package ru.kelcuprum.waterplayer.backend.sources.waterplayer;

import com.github.topi314.lavalyrics.AudioLyricsManager;
import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.WaterPlayerAPI;
import ru.kelcuprum.waterplayer.backend.exception.WebPlaylistException;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;
import ru.kelcuprum.waterplayer.backend.sources.waterplayer.lyrics.SRTLyricsFormat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class WaterPlayerSource implements AudioSourceManager, AudioLyricsManager {

    @Override
    public @NotNull String getSourceName() {
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
                final Path configFile = AlinLib.MINECRAFT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists/" + name + ".json");
                try {
                    JsonObject jsonPlaylist = GsonHelper.parse(Files.readString(configFile));
                    return new WaterPlayerPlaylist(new Playlist(jsonPlaylist));
                } catch (Exception ex) {
                    WaterPlayer.log("ERROR: "+(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage()), Level.DEBUG);
                }
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
    public @Nullable AudioLyrics loadLyrics(AudioTrack track) {
        String id = track.getSourceManager().getSourceName() + "_" + track.getIdentifier();
        WaterPlayer.log("[Lyrics] " + id);
        File srt = new File("./config/WaterPlayer/Lyrics/"+id+".srt");
        if(srt.exists() && srt.isFile()){
            try {
                return new SRTLyricsFormat(track, Files.readString(srt.toPath()));
            } catch (Exception ex){
                WaterPlayer.log(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage(), Level.ERROR);
                return null;
            }
        } else return null;
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
