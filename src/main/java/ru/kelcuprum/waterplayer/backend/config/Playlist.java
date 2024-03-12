package ru.kelcuprum.waterplayer.backend.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    public String title;
    public String author;
    public JsonArray urlsJSON;
    public List<String> urls = new ArrayList<String>();;

    public Playlist(JsonObject data){
        title = data.has("title") ? data.get("title").getAsString() : "Example title";
        author = data.has("author") ? data.get("author").getAsString() : Minecraft.getInstance().getUser().getName();
        urlsJSON = data.has("urls") ? data.get("urls").getAsJsonArray() : GsonHelper.parseArray("[\"https://c418.bandcamp.com/track/strad\"]");
        for(int i = 0; i < urlsJSON.size(); i++){
            urls.add(urlsJSON.get(i).getAsString());
        }
    }
    public JsonObject toJSON(){
        JsonObject data = new JsonObject();
        data.addProperty("title", title);
        data.addProperty("author", author);
        data.add("urls", getUrlsJSON());
        return data;
    }
    private JsonArray getUrlsJSON(){
        JsonArray array = new JsonArray();
        for(String url : urls){
            if(!url.isEmpty()) array.add(url);
        }
        return array;
    }
}
