package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBooleanBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonWithIconBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.components.text.MessageBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.WaterPlayerAPI;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.ControlScreen;

import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Icons.*;
import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Icons.LIST;

public class SecretConfigsScreen {
    public static Screen build(Screen parent) {
        return new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"))
                .addPanelWidget(new ButtonWithIconBuilder(Component.translatable("waterplayer.config"), OPTIONS, (e) -> AlinLib.MINECRAFT.setScreen(MainConfigsScreen.build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(Component.translatable("waterplayer.config.localization"), LIST, (e) -> AlinLib.MINECRAFT.setScreen(LocalizationConfigsScreen.build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(Component.translatable("waterplayer.secret"), WARNING, (e) -> AlinLib.MINECRAFT.setScreen(SecretConfigsScreen.build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(Component.translatable("waterplayer.playlists"), LIST, (e) -> AlinLib.MINECRAFT.setScreen(PlaylistsScreen.build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(Component.translatable("waterplayer.play"), InterfaceUtils.getResourceLocation("waterplayer", "textures/player/play.png"), (e) -> AlinLib.MINECRAFT.setScreen(new ControlScreen(SecretConfigsScreen.build(parent)))).setCentered(false).build())

                .addWidget(new TextBox(Component.translatable("waterplayer.secret"), true))
                .addWidget(new ButtonWithIconBuilder(Component.translatable("waterplayer.secret.how_to_get_tokens"), InterfaceUtils.getResourceLocation("waterplayer", "textures/think.png"), (e)-> WaterPlayer.confirmLinkNow(SecretConfigsScreen.build(parent), "https://github.com/topi314/LavaSrc?tab=readme-ov-file#usage")).build())
                .addWidget(new MessageBox(Component.translatable("waterplayer.secret.description")))
                .addWidget(new CategoryBox(Component.translatable("waterplayer.secret.title.tokens"))
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.yandex_music_token")).setValue("").setConfig(WaterPlayer.config, "YANDEX_MUSIC_TOKEN").setSecret(true).build())
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.deezer_decryption_key")).setValue("").setConfig(WaterPlayer.config, "DEEZER_DECRYPTION_KEY").setSecret(true).build())
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.flowery_tts_voice")).setValue("").setConfig(WaterPlayer.config, "FLOWERY_TTS_VOICE").build())
                )
                .addWidget(new CategoryBox(Component.translatable("waterplayer.secret.title.spotify"))
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.spotify_client_id")).setValue("").setConfig(WaterPlayer.config, "SPOTIFY_CLIENT_ID").setSecret(true).build())
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.spotify_client_secret")).setValue("").setConfig(WaterPlayer.config, "SPOTIFY_CLIENT_SECRET").setSecret(true).build())
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.spotify_sp_dc")).setValue("").setConfig(WaterPlayer.config, "SPOTIFY_SP_DC").setSecret(true).build())
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.spotify_country_code")).setValue("US").setConfig(WaterPlayer.config, "SPOTIFY_COUNTRY_CODE").build())
                )
                .addWidget(new CategoryBox(Component.translatable("waterplayer.secret.title.apple_music"))
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.apple_music_media_api_token")).setValue("").setConfig(WaterPlayer.config, "APPLE_MUSIC_MEDIA_API_TOKEN").setSecret(true).build())
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.apple_music_country_code")).setValue("US").setConfig(WaterPlayer.config, "APPLE_MUSIC_COUNTRY_CODE").build())
                )
                .addWidget(new CategoryBox(Component.translatable("waterplayer.api"))
                        .addValue(new ButtonWithIconBuilder(Component.translatable("waterplayer.web.what_data_is_sent"), InterfaceUtils.getResourceLocation("waterplayer", "textures/think.png"), (e) -> WaterPlayer.confirmLinkNow(SecretConfigsScreen.build(parent), AlinLib.MINECRAFT.options.languageCode.equals("ru_ru") ? "https://waterplayer.ru/data" : "https://waterplayer.ru/data_en")).build())
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.api.enable"), true).setConfig(WaterPlayer.config, "API.ENABLE").build())
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.api.url")).setValue("https://api.waterplayer.ru").setConfig(WaterPlayer.config, "API.URL").build())
                        .addValue(new ButtonWithIconBuilder(Component.translatable("waterplayer.api.update_configs"), RESET, (e) -> {
                            WaterPlayerAPI.loadConfig();
                            WaterPlayer.getToast().setMessage(Component.translatable("waterplayer.api.config_updated")).show(AlinLib.MINECRAFT.getToasts());
                        }).build())
                )
                .build();
    }
}
