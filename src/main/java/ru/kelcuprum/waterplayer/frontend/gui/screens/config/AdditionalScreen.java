package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.Icons;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBooleanBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.selector.SelectorBuilder;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.WaterPlayerAPI;
import ru.kelcuprum.waterplayer.frontend.gui.LyricsHelper;
import ru.kelcuprum.waterplayer.frontend.gui.TextureHelper;

import java.io.File;
import static ru.kelcuprum.alinlib.gui.Icons.*;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.*;

public class AdditionalScreen {
    public static int assetsSize = 0;
    public static boolean isLoaded = false;
    public static Screen build(Screen parent) {
        assetsSize = 0;
        File playlists = AlinLib.MINECRAFT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists").toFile();

        ConfigScreenBuilder builder = new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"))
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.config"), (e) -> AlinLib.MINECRAFT.setScreen(MainConfigsScreen.build(parent))).setIcon(OPTIONS).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.config.localization"), (e) -> AlinLib.MINECRAFT.setScreen(LocalizationConfigsScreen.build(parent))).setIcon(Icons.LIST).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.secret"), (e) -> AlinLib.MINECRAFT.setScreen(SecretConfigsScreen.build(parent))).setIcon(WARNING).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.playlists"), (e) -> AlinLib.MINECRAFT.setScreen(PlaylistsScreen.build(parent))).setIcon(Icons.LIST).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.additional"), (e) -> AlinLib.MINECRAFT.setScreen(AdditionalScreen.build(parent))).setIcon(OPTIONS).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.play"), (e) -> AlinLib.MINECRAFT.setScreen(WaterPlayer.getControlScreen(AdditionalScreen.build(parent)))).setIcon(getPlayOrPause(WaterPlayer.player.getAudioPlayer().isPaused())).setCentered(false).build())
                //
                .addWidget(new TextBox(140, 5, Component.translatable("waterplayer.additional"), true));
        String[] devices = WaterPlayer.player.getAudioOutput().getAudioDevices();
        String selectedDevice = WaterPlayer.config.getString("SPEAKER", "");
        if(devices.length > 0) {
            selectedDevice = selectedDevice.isBlank() ? devices[0] : selectedDevice;
            int pos = WaterPlayer.player.getAudioOutput().getAudioDevicesList().indexOf(selectedDevice);
            if(pos < 0) {
                pos = 0;
                WaterPlayer.player.getAudioOutput().setMixer(devices[0]);
            }
            builder.addWidget(
                    new SelectorBuilder(Component.translatable("waterplayer.config.speaker"))
                            .setList(devices)
                            .setValue(pos)
                            .setOnPress((selectorButton) -> {
                                WaterPlayer.config.setString("SPEAKER", devices[selectorButton.getPosition()]);
                                WaterPlayer.player.getAudioOutput().setMixer(devices[selectorButton.getPosition()]);
                            })
            );
        }
        builder.addWidget(new CategoryBox(Component.translatable("waterplayer.cache"))
                .addValue(new ButtonBuilder(Component.translatable("waterplayer.cache.icons.size"), Component.literal(TextureHelper.getParsedSize(TextureHelper.getSize()))).setActive(false))
                .addValue(new ButtonBuilder(Component.translatable("waterplayer.cache.icons.reset.tracks")).setOnPress((s) -> {
                    TextureHelper.removeTracksCache();
                    AlinLib.MINECRAFT.setScreen(AdditionalScreen.build(parent));
                }))
                .addValue(new ButtonBuilder(Component.translatable("waterplayer.cache.icons.reset.playlists")).setOnPress((s) -> {
                    TextureHelper.removePlaylistsIconCache();
                    AlinLib.MINECRAFT.setScreen(AdditionalScreen.build(parent));
                }))
                .addValue(new ButtonBuilder(Component.translatable("waterplayer.cache.lyrics.reset")).setOnPress((s) -> LyricsHelper.clear()))
        );

        builder.addWidget(new CategoryBox(Component.translatable("waterplayer.api"))
                .addValue(new ButtonBuilder(Component.translatable("waterplayer.web.what_data_is_sent"), (e) -> WaterPlayer.confirmLinkNow(SecretConfigsScreen.build(parent), AlinLib.MINECRAFT.options.languageCode.equals("ru_ru") ? "https://waterplayer.ru/data" : "https://waterplayer.ru/data/en_us")).setIcon(THINK).build())
                .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.api.enable"), true).setConfig(WaterPlayer.config, "API.ENABLE").build())
                .addValue(new EditBoxBuilder(Component.translatable("waterplayer.api.url")).setValue("https://api.waterplayer.ru").setConfig(WaterPlayer.config, "API.URL").build())
                .addValue(new ButtonBuilder(Component.translatable("waterplayer.api.update_configs"), (e) -> {
                    WaterPlayerAPI.loadConfig();
                    WaterPlayer.getToast().setMessage(Component.translatable("waterplayer.api.config_updated")).show(AlinLib.MINECRAFT.getToasts());
                }).setIcon(RESET).build())
        );
        return builder.build();
    }
}
