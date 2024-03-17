package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.screens.LoadMusicScreen;

public class SecretConfigsScreen {
    private static final Component MainConfigCategory = Localization.getText("waterplayer.config");
    private static final Component LocalizationConfigCategory = Localization.getText("waterplayer.config.localization");
    private static final Component SecretConfigCategory = Localization.getText("waterplayer.secret");
    private static final Component PlaylistsCategory = Localization.getText("waterplayer.playlists");
    private static final Component PlayCategory = Localization.getText("waterplayer.play");

    private final Component tokensText = Localization.getText("waterplayer.secret.title.tokens");
    private final Component yandexMusicText = Localization.getText("waterplayer.config.yandex_music_token");
    private final Component deezerText = Localization.getText("waterplayer.config.deezer_decryption_key");
    private final Component floweryTTSText = Localization.getText("waterplayer.config.flowery_tts_voice");

    private final Component spotifyText = Localization.getText("waterplayer.secret.title.spotify");
    private final Component spotifyClientIDText = Localization.getText("waterplayer.config.spotify_client_id");
    private final Component spotifyClientSecretText = Localization.getText("waterplayer.config.spotify_client_secret");
    private final Component spotifyCountryCodeText = Localization.getText("waterplayer.config.spotify_country_code");

    private final Component appleMusicText = Localization.getText("waterplayer.secret.title.apple_music");
    private final Component appleMusicMediaAPITokenText = Localization.getText("waterplayer.config.apple_music_media_api_token");
    private final Component appleMusicCountryCodeText = Localization.getText("waterplayer.config.apple_music_country_code");
    //
    private final InterfaceUtils.DesignType designType = InterfaceUtils.DesignType.FLAT;
    public Screen build(Screen parent) {
        return new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"), designType)
                .addPanelWidget(new ButtonBuilder(MainConfigCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new MainConfigsScreen().build(parent))).build())
                .addPanelWidget(new ButtonBuilder(LocalizationConfigCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new LocalizationConfigsScreen().build(parent))).build())
                .addPanelWidget(new ButtonBuilder(SecretConfigCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new SecretConfigsScreen().build(parent))).build())
                .addPanelWidget(new ButtonBuilder(PlaylistsCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new PlaylistsScreen().build(parent))).build())
                .addPanelWidget(new ButtonBuilder(PlayCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new LoadMusicScreen(this.build(parent)))).build())
                //
                ///
                .addWidget(new TextBox(SecretConfigCategory, true))
                .addWidget(new CategoryBox(tokensText)
                        .addValue(new EditBoxBuilder(yandexMusicText).setValue("").setConfig(WaterPlayer.config, "YANDEX_MUSIC_TOKEN").setSecret(true).build())
                        .addValue(new EditBoxBuilder(deezerText).setValue("").setConfig(WaterPlayer.config, "DEEZER_DECRYPTION_KEY").setSecret(true).build())
                        .addValue(new EditBoxBuilder(floweryTTSText).setValue("").setConfig(WaterPlayer.config, "FLOWERY_TTS_VOICE").build())
                )
                .addWidget(new CategoryBox(spotifyText)
                        .addValue(new EditBoxBuilder(spotifyClientIDText).setValue("").setConfig(WaterPlayer.config, "SPOTIFY_CLIENT_ID").setSecret(true).build())
                        .addValue(new EditBoxBuilder(spotifyClientSecretText).setValue("").setConfig(WaterPlayer.config, "SPOTIFY_CLIENT_SECRET").setSecret(true).build())
                        .addValue(new EditBoxBuilder(spotifyCountryCodeText).setValue("US").setConfig(WaterPlayer.config, "SPOTIFY_COUNTRY_CODE").build())
                )
                .addWidget(new CategoryBox(appleMusicText)
                        .addValue(new EditBoxBuilder(appleMusicMediaAPITokenText).setValue("").setConfig(WaterPlayer.config, "APPLE_MUSIC_MEDIA_API_TOKEN").setSecret(true).build())
                        .addValue(new EditBoxBuilder(appleMusicCountryCodeText).setValue("US").setConfig(WaterPlayer.config, "APPLE_MUSIC_COUNTRY_CODE").build())
                )
                .build();
    }
}
