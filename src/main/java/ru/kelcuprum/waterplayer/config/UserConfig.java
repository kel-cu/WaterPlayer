package ru.kelcuprum.waterplayer.config;

import net.minecraft.client.MinecraftClient;
import org.json.JSONObject;
import ru.kelcuprum.waterplayer.Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UserConfig {
    public static boolean ENABLE_OVERLAY = true;
    public static boolean ENABLE_NOTICE = true;
    public static boolean ENABLE_CHANGE_TITLE = true;
    public static int CURRENT_MUSIC_VOLUME = 2;
    public static int SELECT_MUSIC_VOLUME = 1;
    public static String LAST_REQUEST_MUSIC = "";
    // Yandex Music
    public static String YANDEX_MUSIC_TOKEN = "";
    // YouTube
//    public static String YOUTUBE_EMAIL = "";
//    public static String YOUTUBE_PASSWORD = "";
    // Spotify
    public static String SPOTIFY_CLIENT_ID = "";
    public static String SPOTIFY_CLIENT_SECRET = "";
    public static String SPOTIFY_COUNTRY_CODE = "us";
    // Apple Music
    public static String APPLE_MUSIC_MEDIA_API_TOKEN = "";
    public static String APPLE_MUSIC_COUNTRY_CODE = "us";
    // Deezer
    public static String DEEZER_DECRYPTION_KEY = "";
    // Flowery TTS
    public static String FLOWERY_TTS_VOICE = "Alena";

    /**
     * Сохранение конфигурации
     */
    public static void save(){
        MinecraftClient mc = MinecraftClient.getInstance();
        final Path configFile = mc.runDirectory.toPath().resolve("config/WaterPlayer/config.json");
        JSONObject jsonConfig = new JSONObject();
        jsonConfig.put("ENABLE_OVERLAY", ENABLE_OVERLAY)
                .put("ENABLE_NOTICE", ENABLE_NOTICE)
                .put("ENABLE_CHANGE_TITLE", ENABLE_CHANGE_TITLE)
                .put("SELECT_MUSIC_VOLUME", SELECT_MUSIC_VOLUME)
                .put("CURRENT_MUSIC_VOLUME", CURRENT_MUSIC_VOLUME)
                .put("LAST_REQUEST_MUSIC", LAST_REQUEST_MUSIC)

                .put("YANDEX_MUSIC_TOKEN", YANDEX_MUSIC_TOKEN)
                .put("DEEZER_DECRYPTION_KEY", DEEZER_DECRYPTION_KEY)
                .put("FLOWERY_TTS_VOICE", FLOWERY_TTS_VOICE)

//                .put("YOUTUBE", new JSONObject()
//                        .put("EMAIL", YOUTUBE_EMAIL)
//                        .put("PASSWORD", YOUTUBE_PASSWORD))
                .put("SPOTIFY", new JSONObject()
                        .put("CLIENT_ID", SPOTIFY_CLIENT_ID)
                        .put("CLIENT_SECRET", SPOTIFY_CLIENT_SECRET)
                        .put("COUNTRY_CODE", SPOTIFY_COUNTRY_CODE))
                .put("APPLE_MUSIC", new JSONObject()
                        .put("MEDIA_API_TOKEN", APPLE_MUSIC_MEDIA_API_TOKEN)
                        .put("COUNTRY_CODE", APPLE_MUSIC_COUNTRY_CODE));
        try {
            Files.createDirectories(configFile.getParent());
            Files.writeString(configFile, jsonConfig.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Загрузка файла конфигов
     */
    public static void load(){
        MinecraftClient mc = MinecraftClient.getInstance();
        final Path configFile = mc.runDirectory.toPath().resolve("config/WaterPlayer/config.json");
        try{
            JSONObject jsonConfig = new JSONObject(Files.readString(configFile));
            for (String key : jsonConfig.keySet()) {
                switch (key.toUpperCase()) {
                    case "ENABLE_OVERLAY" -> ENABLE_OVERLAY = jsonConfig.getBoolean(key);
                    case "ENABLE_NOTICE" -> ENABLE_NOTICE = jsonConfig.getBoolean(key);
                    case "ENABLE_CHANGE_TITLE" -> ENABLE_CHANGE_TITLE = jsonConfig.getBoolean(key);
                    case "CURRENT_MUSIC_VOLUME" -> CURRENT_MUSIC_VOLUME = jsonConfig.getInt(key);
                    case "SELECT_MUSIC_VOLUME" -> SELECT_MUSIC_VOLUME = jsonConfig.getInt(key);
                    case "LAST_REQUEST_MUSIC" -> LAST_REQUEST_MUSIC = jsonConfig.getString(key);
                    case "YANDEX_MUSIC_TOKEN" -> YANDEX_MUSIC_TOKEN = jsonConfig.getString(key);
                    case "DEEZER_DECRYPTION_KEY" -> DEEZER_DECRYPTION_KEY = jsonConfig.getString(key);
                    case "FLOWERY_TTS_VOICE" -> FLOWERY_TTS_VOICE = jsonConfig.getString(key);
//                    case "YOUTUBE" -> {
//                        JSONObject jsonConfigYouTube = jsonConfig.getJSONObject(key);
//                        for (String keyY : jsonConfigYouTube.keySet()) {
//                            switch (keyY.toUpperCase()) {
//                                case "EMAIL" -> YOUTUBE_EMAIL = jsonConfigYouTube.getString(keyY);
//                                case "PASSWORD" -> YOUTUBE_PASSWORD = jsonConfigYouTube.getString(keyY);
//                            }
//                        }
//                    }
                    case "SPOTIFY" -> {
                        JSONObject jsonConfigSpotify = jsonConfig.getJSONObject(key);
                        for (String keyS : jsonConfigSpotify.keySet()) {
                            switch (keyS.toUpperCase()) {
                                case "CLIENT_ID" -> SPOTIFY_CLIENT_ID = jsonConfigSpotify.getString(keyS);
                                case "CLIENT_SECRET" -> SPOTIFY_CLIENT_SECRET = jsonConfigSpotify.getString(keyS);
                                case "COUNTRY_CODE" -> SPOTIFY_COUNTRY_CODE = jsonConfigSpotify.getString(keyS);
                            }
                        }
                    }
                    case "APPLE_MUSIC" -> {
                        JSONObject jsonConfigAppleMusic = jsonConfig.getJSONObject(key);
                        for (String keyAM : jsonConfigAppleMusic.keySet()) {
                            switch (keyAM.toUpperCase()) {
                                case "MEDIA_API_TOKEN" -> APPLE_MUSIC_MEDIA_API_TOKEN = jsonConfigAppleMusic.getString(keyAM);
                                case "COUNTRY_CODE" -> APPLE_MUSIC_COUNTRY_CODE = jsonConfigAppleMusic.getString(keyAM);
                            }
                        }
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            save();
        }

    }
}
