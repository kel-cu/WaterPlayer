package ru.kelcuprum.waterplayer.backend.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class PlaylistObject {
    public String title;
    public String author;
    public JsonArray urlsJSON;
    public List<String> urls = new ArrayList<String>();;

    public PlaylistObject(JsonObject data){
        title = data.has("title") ? "Example title" :
                data.get("title").getAsString();
        author = data.has("author") ? Minecraft.getInstance().getUser().getName() :
                data.get("author").getAsString();
        urlsJSON = data.has("urls") ? GsonHelper.parseArray("[\"https://c418.bandcamp.com/track/strad\"]") :
                data.get("urls").getAsJsonArray();
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
            array.add(url);
        }
        return array;
    }
}
