package ru.kelcuprum.waterplayer.backend.playlist;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.waterplayer.WaterPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Playlist {
    public String title;
    public String author;
    public JsonArray urlsJSON;
    public List<String> urls = new ArrayList<>();
    public Path path;
    public String fileName = "Unknown";

    public Playlist(String name) throws IOException {
        this(Path.of(AlinLib.MINECRAFT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists/"+name+".json").toUri()));
    }

    public Playlist(Path path) throws IOException {
        this(path.toFile().exists() ? GsonHelper.parse(Files.readString(path)) : new JsonObject());
        this.path = path;
        this.fileName = path.getFileName().toString();
        this.fileName = fileName.substring(0, fileName.length()-5);
    }

    public Playlist(JsonObject data){
        title = data.has("title") ? data.get("title").getAsString() : "Example title";
        author = data.has("author") ? data.get("author").getAsString() : AlinLib.MINECRAFT.getUser().getName();
        urlsJSON = data.has("urls") ? data.get("urls").getAsJsonArray() : GsonHelper.parseArray("[\"https://c418.bandcamp.com/track/strad\"]");
        for(int i = 0; i < urlsJSON.size(); i++){
            urls.add(urlsJSON.get(i).getAsString());
        }
    }

    public void save(){
        if(this.path == null) return;
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, toJSON().toString());
        } catch (IOException e) {
            WaterPlayer.log(e.getLocalizedMessage(), Level.ERROR);
        }
    }



    public Playlist addUrl(String url){
        urls.add(url);
        save();
        return this;
    }
    public Playlist setUrl(String url, int position){
        urls.set(position, url);
        save();
        return this;
    }

    public JsonObject toJSON(){
        JsonObject data = new JsonObject();
        data.addProperty("title", title);
        data.addProperty("author", author);
        data.add("urls", getUrlsJSON());
        return data;
    }
    public JsonArray getUrlsJSON(){
        JsonArray array = new JsonArray();
        for(String url : urls){
            if(!url.isBlank()) array.add(url);
        }
        return array;
    }
}
