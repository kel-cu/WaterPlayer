package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.EditBoxConfigString;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;

public class SecretConfigsScreen {
    private static final Component MainConfigCategory = Localization.getText("waterplayer.config");
    private static final Component LocalizationConfigCategory = Localization.getText("waterplayer.config.localization");
    private static final Component SecretConfigCategory = Localization.getText("waterplayer.secret");

    private Component tokensText = Localization.getText("waterplayer.secret.title.tokens");
    private Component yandexMusicText = Localization.getText("waterplayer.config.yandex_music_token");
    private Component deezerText = Localization.getText("waterplayer.config.deezer_decryption_key");
    private Component floweryTTSText = Localization.getText("waterplayer.config.flowery_tts_voice");

    private TextBox spotify;
    private Component spotifyText = Localization.getText("waterplayer.secret.title.spotify");
    private Component spotifyClientIDText = Localization.getText("waterplayer.config.spotify_client_id");
    private Component spotifyClientSecretText = Localization.getText("waterplayer.config.spotify_client_secret");
    private Component spotifyCountryCodeText = Localization.getText("waterplayer.config.spotify_country_code");

    private Component appleMusicText = Localization.getText("waterplayer.secret.title.apple_music");
    private Component appleMusicMediaAPITokenText = Localization.getText("waterplayer.config.apple_music_media_api_token");
    private Component appleMusicCountryCodeText = Localization.getText("waterplayer.config.apple_music_country_code");
    //
    private InterfaceUtils.DesignType designType = InterfaceUtils.DesignType.FLAT;
    public Screen build(Screen parent) {
        return new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"), designType)
                .addPanelWidget(new Button(10, 40, designType, MainConfigCategory, (e) -> {
                    Minecraft.getInstance().setScreen(new MainConfigsScreen().build(parent));
                }))
                .addPanelWidget(new Button(10, 65, designType, LocalizationConfigCategory, (e) -> {
                    Minecraft.getInstance().setScreen(new LocalizationConfigsScreen().build(parent));
                }))
                .addPanelWidget(new Button(10, 90, designType, SecretConfigCategory, (e) -> {
                    Minecraft.getInstance().setScreen(new SecretConfigsScreen().build(parent));
                }))
                ///
                .addWidget(new TextBox(140, 5, SecretConfigCategory, true))
                .addWidget(new CategoryBox(140, 30, tokensText)
                        .addValue(new EditBoxConfigString(140, 55, true, designType, WaterPlayer.config, "YANDEX_MUSIC_TOKEN", "", yandexMusicText))
                        .addValue(new EditBoxConfigString(140, 80, true, designType, WaterPlayer.config, "DEEZER_DECRYPTION_KEY", "", deezerText))
                        .addValue(new EditBoxConfigString(140, 105, false, designType, WaterPlayer.config, "FLOWERY_TTS_VOICE", "", floweryTTSText))
                )
                .addWidget(new CategoryBox(140, 130, spotifyText)
                        .addValue(new EditBoxConfigString(140, 55, true, designType, WaterPlayer.config, "SPOTIFY_CLIENT_ID", "", spotifyClientIDText))
                        .addValue(new EditBoxConfigString(140, 180, true, designType, WaterPlayer.config, "SPOTIFY_CLIENT_SECRET", "", spotifyClientSecretText))
                        .addValue(new EditBoxConfigString(140, 205, true, designType, WaterPlayer.config, "SPOTIFY_COUNTRY_CODE", "US", spotifyCountryCodeText))
                )
                .addWidget(new CategoryBox(140, 230, appleMusicText)
                        .addValue(new EditBoxConfigString(140, 255, true, designType, WaterPlayer.config, "APPLE_MUSIC_MEDIA_API_TOKEN", "", appleMusicMediaAPITokenText))
                        .addValue(new EditBoxConfigString(140, 280, true, designType, WaterPlayer.config, "APPLE_MUSIC_COUNTRY_CODE", "us", appleMusicCountryCodeText))
                )
                .build();
    }
}
