package ru.kelcuprum.waterplayer.backend;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.waterplayer.WaterPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class History {
    public MusicPlayer historyPlayer = new MusicPlayer();
    public JsonArray huy = new JsonArray();
    public static File file = new File(WaterPlayer.getPath()+"/history.json");
    public History(){
        if(file.exists()){
            try {
                huy = GsonHelper.parseArray(Files.readString(file.toPath()));
            } catch (Exception ex){
                WaterPlayer.log(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
            }
        } else save();
        updateList();
    }

    public void addHistory(AudioTrack track){
        String url = track.getInfo().uri;
        int i = 0;
        int pos = -1;
        for(JsonElement element : huy){
            if(element.getAsJsonPrimitive().isString()){
                if(element.getAsString().equals(url)) {
                    pos = i;
                    break;
                }
            }
            i++;
        }
        if(pos >= 0) huy.remove(pos);
        JsonArray newHuy = new JsonArray();
        newHuy.add(url);
        newHuy.addAll(huy);
        huy = newHuy;
        updateList();
        save();
    }
    List<AudioTrack> list = new ArrayList<>();
    public List<AudioTrack> getTracks(){
        return list;
    }

    public HashMap<String, AudioTrack> cache = new HashMap<>();

    public void updateList(){
        list = new ArrayList<>();
        for(JsonElement element : huy){
            if(element.getAsJsonPrimitive().isString()){
                String url = element.getAsString();
                if(cache.containsKey(url)) list.add(cache.get(url));
                else {
                    try {
                        historyPlayer.getAudioPlayerManager().loadItemOrdered(WaterPlayer.player.getAudioPlayerManager(), url, new AudioLoadResultHandler() {
                            @Override
                            public void trackLoaded(AudioTrack track) {
                                list.add(track);
                                cache.put(url, track);
                            }

                            @Override
                            public void playlistLoaded(AudioPlaylist playlist) {
                                list.addAll(playlist.getTracks());
                            }

                            @Override
                            public void noMatches() {}
                            @Override
                            public void loadFailed(FriendlyException exception) {}
                        });
                    } catch (Exception ex){
                        WaterPlayer.log(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
                    }
                }
            }
        }
    }

    public void update(){
        try {
            file = new File(WaterPlayer.getPath()+"/history.json");
            huy = GsonHelper.parseArray(Files.readString(file.toPath()));
        } catch (Exception ex){
            WaterPlayer.log(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage(), Level.DEBUG);
        }
    }

    public void save(){
        try {
            Files.createDirectories(file.toPath().getParent());
            Files.writeString(file.toPath(), this.huy.toString());
        } catch (IOException var3) {
            IOException e = var3;
            AlinLib.log(e.getLocalizedMessage(), Level.ERROR);
        }
    }
}
