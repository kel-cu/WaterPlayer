package ru.kelcuprum.waterplayer.screens.config.category;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import ru.kelcuprum.waterplayer.localization.Localization;
import ru.kelcuprum.waterplayer.config.UserConfig;

public class SecretConfigs {
    public ConfigCategory getCategory(ConfigBuilder builder){
        ConfigCategory category = builder.getOrCreateCategory(Localization.getText("waterplayer.secret"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        category.addEntry(entryBuilder.startTextDescription(Localization.getText("waterplayer.secret.description")).build());

        category.addEntry(entryBuilder.startTextDescription(Localization.getText("waterplayer.secret.title.tokens")).build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.yandex_music_token"),
                        UserConfig.YANDEX_MUSIC_TOKEN)
                .setDefaultValue("")
                .setSaveConsumer(newValue -> UserConfig.YANDEX_MUSIC_TOKEN = newValue)
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.deezer_decryption_key"),
                        UserConfig.DEEZER_DECRYPTION_KEY)
                .setDefaultValue("")
                .setSaveConsumer(newValue -> UserConfig.DEEZER_DECRYPTION_KEY = newValue)
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.flowery_tts_voice"),
                        UserConfig.FLOWERY_TTS_VOICE)
                .setDefaultValue("Alena")
                .setSaveConsumer(newValue -> UserConfig.FLOWERY_TTS_VOICE = newValue)
                .build());
        category.addEntry(entryBuilder.startTextDescription(Localization.getText("waterplayer.secret.title.spotify")).build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.spotify_client_id"),
                        UserConfig.SPOTIFY_CLIENT_ID)
                .setDefaultValue("")
                .setSaveConsumer(newValue -> UserConfig.SPOTIFY_CLIENT_ID = newValue)
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.spotify_client_secret"),
                        UserConfig.SPOTIFY_CLIENT_SECRET)
                .setDefaultValue("")
                .setSaveConsumer(newValue -> UserConfig.SPOTIFY_CLIENT_SECRET = newValue)
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.spotify_country_code"),
                        UserConfig.SPOTIFY_COUNTRY_CODE)
                .setDefaultValue("US")
                .setSaveConsumer(newValue -> UserConfig.SPOTIFY_COUNTRY_CODE = newValue)
                .build());
        category.addEntry(entryBuilder.startTextDescription(Localization.getText("waterplayer.secret.title.apple_music")).build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.apple_music_media_api_token"),
                        UserConfig.APPLE_MUSIC_MEDIA_API_TOKEN)
                .setDefaultValue("")
                .setSaveConsumer(newValue -> UserConfig.APPLE_MUSIC_MEDIA_API_TOKEN = newValue)
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.config.apple_music_country_code"),
                        UserConfig.APPLE_MUSIC_COUNTRY_CODE)
                .setDefaultValue("us")
                .setSaveConsumer(newValue -> UserConfig.APPLE_MUSIC_COUNTRY_CODE = newValue)
                .build());
        return category;
    }
}
