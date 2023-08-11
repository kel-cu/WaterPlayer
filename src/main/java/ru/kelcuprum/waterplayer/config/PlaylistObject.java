package ru.kelcuprum.waterplayer.config;

import net.minecraft.client.MinecraftClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlaylistObject {
    public String title;
    public String author;
    public JSONArray urlsJSON;
    public List<String> urls = new ArrayList<String>();;

    public PlaylistObject(JSONObject data){
        title = data.isNull("title") ? "Example title" :
                data.getString("title");
        author = data.isNull("author") ? MinecraftClient.getInstance().getSession().getUsername() :
                data.getString("author");
        urlsJSON = data.isNull("urls") ? new JSONArray().put("https://c418.bandcamp.com/track/strad") :
                data.getJSONArray("urls");
        for(int i = 0; i < urlsJSON.length(); i++){
            urls.add(urlsJSON.getString(i));
        }
    }
    public JSONObject toJSON(){
        JSONObject data = new JSONObject();
        data.put("title", title)
                .put("author", author)
                .put("urls", urls);
        return data;
    }
}
