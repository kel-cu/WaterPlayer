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
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.WaterPlayerAPI;
import ru.kelcuprum.waterplayer.backend.exception.WebPlaylistException;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;
import ru.kelcuprum.waterplayer.backend.sources.waterplayer.lyrics.FileLyrics;
import ru.kelcuprum.waterplayer.backend.sources.waterplayer.lyrics.SRTLyricsFormat;
import ru.kelcuprum.waterplayer.frontend.localization.MusicHelper;

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
                final Path configFile = AlinLib.MINECRAFT.gameDirectory.toPath().resolve(WaterPlayer.getPath()+"/playlists/" + name + ".json");
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
            } catch (Exception ex) {
                WaterPlayer.log("ERROR: "+(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage()), Level.DEBUG);
            }
        }
        return null;
    }

    @Override
    public @Nullable AudioLyrics loadLyrics(AudioTrack track) {
        String id = WaterPlayer.parseFileSystem(track.getSourceManager().getSourceName() + "_" + track.getIdentifier());
        File srt = new File(WaterPlayer.getPath()+"/Lyrics/"+id+".srt");
        File lrc = new File(WaterPlayer.getPath()+"/Lyrics/"+id+".lrc");
        if(srt.exists() && srt.isFile()){
            try {
                return new SRTLyricsFormat(track, Files.readString(srt.toPath()));
            } catch (Exception ex){
                WaterPlayer.log(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage(), Level.ERROR);
            }
        } else if(lrc.exists() && lrc.isFile()){
            try {
                return new SRTLyricsFormat(track, Files.readString(srt.toPath()));
            } catch (Exception ex){
                WaterPlayer.log(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage(), Level.ERROR);
            }
        } else if(MusicHelper.isFile(track)){
            try {
                AudioFile f = AudioFileIO.read(new File(track.getInfo().uri));
                String text = f.getTag().getFirst(FieldKey.LYRICS);
                if(!text.isBlank()) return new FileLyrics(track, text);
            } catch (Exception e){
                e.printStackTrace();
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
