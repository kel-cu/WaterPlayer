package ru.kelcuprum.waterplayer.backend;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Async;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.config.Config;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.exception.AuthException;
import ru.kelcuprum.waterplayer.backend.exception.WebPlaylistException;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;
import ru.kelcuprum.waterplayer.backend.playlist.WebPlaylist;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
                    WaterPlayer.getToast().setMessage(Component.literal(msg)).setType(ToastBuilder.Type.ERROR).setIcon(DONT).show(AlinLib.MINECRAFT.getToasts());
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
            WaterPlayer.getToast().setMessage(Component.literal(e.getMessage() == null ? e.getClass().getName() : e.getMessage())).setType(ToastBuilder.Type.ERROR).setIcon(DONT).show(AlinLib.MINECRAFT.getToasts());
        }
    }

    public static Playlist getPlaylist(String url, boolean save) throws WebPlaylistException {
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
            throw new WebPlaylistException("External error: "+(e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
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

    // -=-=-=-=-=-=-

    public static HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static String getString(HttpRequest request) throws IOException, InterruptedException {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        }
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//        WaterPlayer.log(response.body());
        return response.body();
    }
    public static String getString(HttpRequest.Builder url) throws IOException, InterruptedException {
        return getString(url.build());
    }
    public static String getString(String url) throws IOException, InterruptedException {
        return getString(HttpRequest.newBuilder().uri(URI.create(url)).build());
    }

    public static JsonObject getJsonObject(String url) throws IOException, InterruptedException {
        return GsonHelper.parse(getString(HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").build()));
    }

    public static JsonArray getJsonArray(String url) throws IOException, InterruptedException {
        return GsonHelper.parseArray(getString(HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").build()));
    }
    // HttpRequest
    public static JsonObject getJsonObject(HttpRequest url) throws IOException, InterruptedException {
        return GsonHelper.parse(getString(url));
    }

    public static JsonArray getJsonArray(HttpRequest url) throws IOException, InterruptedException {
        return GsonHelper.parseArray(getString(url));
    }
    // HttpRequest.Builder
    public static JsonObject getJsonObject(HttpRequest.Builder url) throws IOException, InterruptedException {
        return getJsonObject(url.header("Content-Type", "application/json").build());
    }

    public static JsonArray getJsonArray(HttpRequest.Builder url) throws IOException, InterruptedException {
        return getJsonArray(url.header("Content-Type", "application/json").build());
    }

}
