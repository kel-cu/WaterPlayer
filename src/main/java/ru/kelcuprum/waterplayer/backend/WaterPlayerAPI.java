package ru.kelcuprum.waterplayer.backend;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import org.apache.commons.codec.binary.Base64;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Icons.DONT;

public class WaterPlayerAPI {
    public static Config config = new Config(new JsonObject());
    public static String getURL(){
        return getURL("/");
    }
    public static String getURL(String route){
        String url = WaterPlayer.config.getString("API.URL", "https://api.waterplayer.ru") + route;
        url = url+(url.contains("?") ? "&" : "?")+"version=2.0";
        return url;
    }

    @Async.Execute
    public static boolean serverEnable(){
        try{
            JsonObject object = WebAPI.getJsonObject(getURL("/ping"));
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
            JsonObject json = WebAPI.getJsonObject(String.format(getURL("/verify?access=%s"), AlinLib.MINECRAFT.getUser().getAccessToken()));
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
            JsonObject object = WebAPI.getJsonObject(getURL("/public_config"));
            config = new Config(object);
        } catch (Exception e){
            WaterPlayer.log(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), Level.ERROR);
            WaterPlayer.getToast().setMessage(Component.literal(e.getMessage() == null ? e.getClass().getName() : e.getMessage())).setType(ToastBuilder.Type.ERROR).setIcon(DONT).show(AlinLib.MINECRAFT.getToasts());
        }
    }

    public static Playlist getPlaylist(String url, boolean save) throws WebPlaylistException {
        try {
            WebPlaylist data = new WebPlaylist(WebAPI.getJsonObject(url));
            if(save) data.save();
            return data.playlist;
        } catch (Exception e){
            WaterPlayer.log(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), Level.ERROR);
            throw new WebPlaylistException("External error: "+(e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
        }
    }

    @Async.Execute
    public static String uploadPlaylist(Playlist playlist, String id) throws AuthException {
        if(!isVerified()) throw new AuthException("Your account is not authorized!");
        String base64 = new String(Base64.encodeBase64(playlist.toJSON().toString().getBytes(StandardCharsets.UTF_8)) );
        try {
            String url = config.getBoolean("ENABLE_VERIFY", true) ? String.format("/upload?playlist_data=%1$s&id=%2$s&access=%3$s", base64, id, AlinLib.MINECRAFT.getUser().getAccessToken()) : String.format("/upload?playlist_data=%1$s&id=%2$s", base64, id);
            JsonObject data = WebAPI.getJsonObject(getURL(url));
            return data.has("url") ? String.format(config.getString("PLAYLIST_URL", getURL("/playlist/%s")), data.get("url").getAsString()) : "";
        } catch (Exception e){
            WaterPlayer.log(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), Level.ERROR);
            return "";
        }
    }

    public static List<WebPlaylist> searchPlaylists(String query){
        List<WebPlaylist> results = new ArrayList<>();
        if(!config.getBoolean("SEARCH", true)) return results;
        try {
            JsonObject data = WebAPI.getJsonObject(getURL(String.format("/search?query=%s", query)));
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
}
