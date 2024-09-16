package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.CommonUtils;
import ru.kelcuprum.alinlib.config.Config;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static ru.kelcuprum.alinlib.gui.Icons.*;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.*;

public class AdditionalScreen {
    public static int assetsSize = 0;
    public static boolean isLoaded = false;
    public static Screen build(Screen parent) {
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
        builder.addWidget(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.discord"), false).setConfig(WaterPlayer.config, "DISCORD"));
        builder.addWidget(new CategoryBox(Component.translatable("waterplayer.cache"))
                .addValue(new ButtonBuilder(Component.translatable("waterplayer.cache.icons.size"), Component.literal(CommonUtils.getParsedFileSize(new File(WaterPlayer.getPath()+"/textures")))).setActive(false))
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
                    WaterPlayer.getToast().setMessage(Component.translatable("waterplayer.api.config_updated")).buildAndShow();
                }).setIcon(RESET).build())
        );
        builder.addWidget(new TextBox(Component.translatable("waterplayer.config.experiments")));
        builder.addWidget(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.experiment.filters"), false).setConfig(WaterPlayer.config, "EXPERIMENT.FILTERS"));
        builder.addWidget(new CategoryBox(Component.translatable("waterplayer.config.data"))
                .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.data.use_global"), false).setConfig(WaterPlayer.pathConfig, "USE_GLOBAL"))
                .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.data.path")).setValue("{HOME}/WaterPlayer").setConfig(WaterPlayer.pathConfig, "PATH"))
                .addValue(new ButtonBuilder(Component.translatable("waterplayer.config.data.move")).setOnPress((s) -> {
                    try {
                        if(!new File(WaterPlayer.getPath()).exists()) Files.createDirectory(Path.of(WaterPlayer.getPath()));
                        if(new File("config/WaterPlayer/config.json").exists()) Files.copy(Path.of("config/WaterPlayer/config.json"), Path.of(WaterPlayer.getPath()+"/config.json"), REPLACE_EXISTING);
                        if(new File("config/WaterPlayer/playlists").exists()) {
                            if(!new File(WaterPlayer.getPath()+"/playlists").exists()) Files.copy(Path.of("config/WaterPlayer/playlists"), Path.of(WaterPlayer.getPath()+"/playlists"), REPLACE_EXISTING);
                            for(File file : new File("config/WaterPlayer/playlists").listFiles()) Files.copy(file.toPath(), Path.of(WaterPlayer.getPath()+"/playlists/"+file.getName()), REPLACE_EXISTING);
                        }
                        if(new File("config/WaterPlayer/lyrics").exists()) {
                            if(!new File(WaterPlayer.getPath()+"/lyrics").exists()) Files.copy(Path.of("config/WaterPlayer/lyrics"), Path.of(WaterPlayer.getPath()+"/lyrics"), REPLACE_EXISTING);
                            for(File file : new File("config/WaterPlayer/lyrics").listFiles()) Files.copy(file.toPath(), Path.of(WaterPlayer.getPath()+"/lyrics/"+file.getName()), REPLACE_EXISTING);
                        }
                        if(new File("config/WaterPlayer/textures").exists()) {
                            if(!new File(WaterPlayer.getPath()+"/textures").exists()) Files.copy(Path.of("config/WaterPlayer/textures"), Path.of(WaterPlayer.getPath()+"/textures"), REPLACE_EXISTING);
                            for(File file : new File("config/WaterPlayer/textures").listFiles()) Files.copy(file.toPath(), Path.of(WaterPlayer.getPath()+"/textures/"+file.getName()), REPLACE_EXISTING);
                        }
                        if(new File("config/WaterPlayer/lang").exists()) {
                            if(!new File(WaterPlayer.getPath()+"/lang").exists()) Files.copy(Path.of("config/WaterPlayer/lang"), Path.of(WaterPlayer.getPath()+"/lang"), REPLACE_EXISTING);
                            for(File file : new File("config/WaterPlayer/lang").listFiles()) Files.copy(file.toPath(), Path.of(WaterPlayer.getPath()+"/lang/"+file.getName()), REPLACE_EXISTING);
                        }

                        WaterPlayer.config = new Config(WaterPlayer.getPath()+"/config.json");
                        AlinLib.MINECRAFT.setScreen(build(parent));
                    } catch (IOException e) {
                        WaterPlayer.log(e.getMessage(), Level.ERROR);
                        e.printStackTrace();
                    }
                }))
                .addValue(new ButtonBuilder(Component.translatable("waterplayer.config.data.update_config")).setOnPress((s) -> {
                    WaterPlayer.config = new Config(WaterPlayer.getPath()+"/config.json");
                    AlinLib.MINECRAFT.setScreen(build(parent));
                }))
        );
        return builder.build();
    }
}
