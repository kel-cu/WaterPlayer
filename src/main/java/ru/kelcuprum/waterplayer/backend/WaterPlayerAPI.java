package ru.kelcuprum.waterplayer.backend;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Async;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.WebAPI;
import ru.kelcuprum.alinlib.config.Config;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.exception.AuthException;
import ru.kelcuprum.waterplayer.backend.exception.WebPlaylistException;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;
import ru.kelcuprum.waterplayer.backend.playlist.WebPlaylist;
import ru.kelcuprum.waterplayer.frontend.localization.MusicHelper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static ru.kelcuprum.alinlib.WebAPI.getJsonObject;
import static ru.kelcuprum.alinlib.gui.Icons.DONT;

public class WaterPlayerAPI {
    public static Config config = new Config(new JsonObject());
    public static String getURL(){
        return getURL("/");
    }
    public static String getURL(String route){
        String url = WaterPlayer.config.getString("API.URL", "https://api.waterplayer.ru") + route;
        url = url+(url.contains("?") ? "&" : "?")+"version=2.1";
        return url;
    }

    @Async.Execute
    public static boolean serverEnable(){
        try{
            JsonObject object = getJsonObject(getURL("/ping"));
            if(object.has("error")){
                throw new AuthException(object.get("error").getAsJsonObject().get("message").getAsString());
            }
            return true;
        } catch (Exception e){
            WaterPlayer.log(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), Level.ERROR);
            return false;
        }
    }
    public static boolean isPlaylistUploadEnable(){
        return serverEnable() && isVerified();
    }
    @Async.Execute
    public static boolean isVerified(){
        if(!config.getBoolean("ENABLE_VERIFY", true)) return true;
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(getURL("/verify")))
                    .header("Authorization", "Bearer "+AlinLib.MINECRAFT.getUser().getAccessToken());
            JsonObject json = getJsonObject(builder);
            if(json.has("error")){
                if (json.getAsJsonObject("error").get("code").getAsNumber().intValue() != 401) {
                    String msg = json.getAsJsonObject("error").has("message") ? json.getAsJsonObject("error").get("message").getAsString() : json.getAsJsonObject("error").get("codename").getAsString();
                    WaterPlayer.getToast().setTitle(Component.literal("WaterPlayer API")).setMessage(Component.literal(msg)).setType(ToastBuilder.Type.ERROR).setIcon(DONT).buildAndShow();
                }
                return false;
            }
            return true;
        } catch (Exception e){
            WaterPlayer.log(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), Level.ERROR);
            return false;
        }
    }
    @Async.Execute
    public static void loadConfig(){
        try{
            JsonObject object = getJsonObject(getURL("/public_config"));
            if(object.has("error")){
                throw new AuthException(object.get("error").getAsJsonObject().get("message").getAsString());
            }
            config = new Config(object);
        } catch (Exception e){
            WaterPlayer.log(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), Level.ERROR);
            if(e instanceof AuthException) WaterPlayer.getToast().setMessage(Component.literal(e.getMessage() == null ? e.getClass().getName() : e.getMessage())).setType(ToastBuilder.Type.ERROR).setIcon(DONT).buildAndShow();
            else WaterPlayer.getToast().setMessage(Component.literal("Failed connect to API")).setType(ToastBuilder.Type.ERROR).setIcon(DONT).buildAndShow();
        }
    }

    public static Playlist getPlaylist(String url, boolean save) throws WebPlaylistException, IOException, InterruptedException {
        try {
            JsonObject data = getJsonObject(url);
            if(data.has("error")){
                throw new WebPlaylistException(data.getAsJsonObject("error").get("message").getAsString());
            }
            WebPlaylist playlist = new WebPlaylist(data);
            if(save) playlist.save();
            return playlist.playlist;
        } catch (Exception e){
            WaterPlayer.log(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), Level.ERROR);
            if(e instanceof WebPlaylistException) throw new WebPlaylistException("External error: "+(e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
            else throw e;
        }
    }

    @Async.Execute
    public static String uploadPlaylist(Playlist playlist, String id) throws AuthException, WebPlaylistException {
        if(!isVerified()) throw new AuthException("Your account is not authorized!");
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(getURL("/upload")));
            if(config.getBoolean("ENABLE_VERIFY", true)) builder.header("Authorization", "Bearer "+AlinLib.MINECRAFT.getUser().getAccessToken());
            JsonObject request = new JsonObject();
            request.addProperty("id", id);
            request.add("data", playlist.toJSON());
            builder.POST(HttpRequest.BodyPublishers.ofString(request.toString()));
            JsonObject data = getJsonObject(builder);
            if(data.has("error")){
                throw new WebPlaylistException(data.getAsJsonObject("error").get("message").getAsString());
            }
            return data.has("url") ? String.format(config.getString("PLAYLIST_URL", getURL("/playlist/%s")), data.get("url").getAsString()) : "";
        } catch (Exception e){
            if(e instanceof WebPlaylistException) throw new WebPlaylistException(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
            else throw new RuntimeException((e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
        }
    }

    public static List<WebPlaylist> searchPlaylists(String query){
        List<WebPlaylist> results = new ArrayList<>();
        if(!config.getBoolean("SEARCH", true)) return results;
        try {
            JsonObject data = getJsonObject(getURL(String.format("/search?query=%s", query)));
            if(data.has("error")){
                throw new WebPlaylistException(data.getAsJsonObject("error").get("message").getAsString());
            }
            if(data.has("results") && data.get("results").isJsonArray()){
                for(JsonElement j : data.getAsJsonArray("results")){
                    try {
                        WebPlaylist wp = new WebPlaylist(j.getAsJsonObject());
                        results.add(wp);
                    } catch (Exception e){
                        WaterPlayer.log((e.getMessage() == null ? e.getClass().getName() : e.getMessage()), Level.ERROR);
                    }
                }
            }
            return results;
        } catch (Exception e){
            WaterPlayer.log((e.getMessage() == null ? e.getClass().getName() : e.getMessage()), Level.ERROR);
            return results;
        }
    }



    public static HashMap<String, JsonObject> urlsArtworks = new HashMap<>();
    public static JsonObject getTrackInfo(AudioTrack track, boolean onlyAuthor){
        String author = MusicHelper.getAuthor(track);
        if(author.split(",").length > 1) author = author.split(",")[0];
        else if(author.split(";").length > 1) author = author.split(";")[0];
        else if(author.split("/").length > 1) author = author.split("/")[0];
        try{
            JsonObject authorInfo;
            String url = getURL(onlyAuthor ? String.format("/info?author=%1$s", uriEncode(author)) : String.format("/info?author=%1$s&album=%2$s", uriEncode(author), uriEncode(MusicHelper.getTitle(track))));
            if(urlsArtworks.containsKey(url)) authorInfo = urlsArtworks.get(url);
            else {
                authorInfo = WebAPI.getJsonObject(url);
                urlsArtworks.put(url, authorInfo);
            }
            if(!onlyAuthor){
                String aUrl = String.format("/info?author=%1$s", uriEncode(author));
                if(!urlsArtworks.containsKey(aUrl)){
                    JsonObject authorObject = new JsonObject();
                    authorObject.add("author", authorInfo.get("author"));
                    urlsArtworks.put(aUrl, authorObject);
                }
            }
            return authorInfo;
        } catch (Exception ex){
            WaterPlayer.log(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage(), Level.DEBUG);
            JsonObject rep = new JsonObject();
            JsonObject error = new JsonObject();
            error.addProperty("code", 500);
            error.addProperty("codename", "Internal Server Error");
            error.addProperty("message", ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
            rep.add("error", error);
            return rep;
        }
    }
    public static String getAuthorAvatar(AudioTrack track){
        String author = MusicHelper.getAuthor(track);
        try{
            JsonObject authorInfo = getTrackInfo(track, true);
            if(authorInfo.has("error")) throw new RuntimeException(authorInfo.getAsJsonObject("error").get("message").getAsString());
            else if(authorInfo.getAsJsonObject("author").has("artwork"))
                return authorInfo.getAsJsonObject("author").get("artwork").getAsString();
            else return "";
        } catch (Exception ex){
            WaterPlayer.log(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage(), Level.DEBUG);
            return "";
        }
    }
    public static String getArtwork(AudioTrack track){
        try{
            JsonObject authorInfo = getTrackInfo(track, false);
            if(authorInfo.has("error")) throw new RuntimeException(authorInfo.getAsJsonObject("error").get("message").getAsString());
            else if(authorInfo.getAsJsonObject("track").has("artwork"))
                return authorInfo.getAsJsonObject("track").get("artwork").getAsString();
            else return "";
        } catch (Exception ex){
            WaterPlayer.log(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage(), Level.DEBUG);
            return "";
        }
    }
    protected static String uriEncode(String uri){
        return URLEncoder.encode(uri, StandardCharsets.UTF_8);
    }
}
