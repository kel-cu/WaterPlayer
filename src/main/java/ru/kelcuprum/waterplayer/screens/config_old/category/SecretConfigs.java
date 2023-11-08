package ru.kelcuprum.waterplayer.screens.config_old.category;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.localization.Localization;

public class SecretConfigs {
    public ConfigCategory getCategory(ConfigBuilder builder){
        ConfigCategory category = builder.getOrCreateCategory(Localization.getText("waterplayer.secret"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        category.addEntry(entryBuilder.startTextDescription(Localization.getText("waterplayer.secret.description")).build());

        category.addEntry(entryBuilder.startTextDescription(Localization.getText("waterplayer.secret.title.tokens")).build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.yandex_music_token"),
                        WaterPlayer.config.getString("YANDEX_MUSIC_TOKEN", ""))
                .setDefaultValue("")
                .setSaveConsumer(newValue -> WaterPlayer.config.setString("YANDEX_MUSIC_TOKEN", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.deezer_decryption_key"),
                        WaterPlayer.config.getString("DEEZER_DECRYPTION_KEY", ""))
                .setDefaultValue("")
                .setSaveConsumer(newValue -> WaterPlayer.config.setString("DEEZER_DECRYPTION_KEY", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.flowery_tts_voice"),
                        WaterPlayer.config.getString("FLOWERY_TTS_VOICE", "Alena"))
                .setDefaultValue("Alena")
                .setSaveConsumer(newValue -> WaterPlayer.config.setString("FLOWERY_TTS_VOICE", newValue))
                .build());
        category.addEntry(entryBuilder.startTextDescription(Localization.getText("waterplayer.secret.title.spotify")).build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.spotify_client_id"),
                        WaterPlayer.config.getString("SPOTIFY_CLIENT_ID", ""))
                .setDefaultValue("")
                .setSaveConsumer(newValue -> WaterPlayer.config.setString("SPOTIFY_CLIENT_ID", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.spotify_client_secret"),
                        WaterPlayer.config.getString("SPOTIFY_CLIENT_SECRET", ""))
                .setDefaultValue("")
                .setSaveConsumer(newValue -> WaterPlayer.config.setString("SPOTIFY_CLIENT_SECRET", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.spotify_country_code"),
                        WaterPlayer.config.getString("SPOTIFY_COUNTRY_CODE", "US"))
                .setDefaultValue("US")
                .setSaveConsumer(newValue -> WaterPlayer.config.setString("SPOTIFY_COUNTRY_CODE", newValue))
                .build());
        category.addEntry(entryBuilder.startTextDescription(Localization.getText("waterplayer.secret.title.apple_music")).build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.apple_music_media_api_token"),
                        WaterPlayer.config.getString("APPLE_MUSIC_MEDIA_API_TOKEN", ""))
                .setDefaultValue("")
                .setSaveConsumer(newValue -> WaterPlayer.config.setString("APPLE_MUSIC_MEDIA_API_TOKEN", newValue))
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.apple_music_country_code"),
                        WaterPlayer.config.getString("APPLE_MUSIC_COUNTRY_CODE", "us"))
                .setDefaultValue("us")
                .setSaveConsumer(newValue -> WaterPlayer.config.setString("APPLE_MUSIC_COUNTRY_CODE", newValue))
                .build());
        return category;
    }
}
