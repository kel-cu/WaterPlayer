package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBooleanBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.selector.SelectorBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.slider.SliderBuilder;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.ControlScreen;

import static ru.kelcuprum.alinlib.gui.Icons.*;

public class MainConfigsScreen {
    public static Screen build(Screen parent) {
        String[] type = {
                Component.translatable("waterplayer.config.overlay.position.top_left").getString(),
                Component.translatable("waterplayer.config.overlay.position.top_right").getString(),
                Component.translatable("waterplayer.config.overlay.position.bottom_left").getString(),
                Component.translatable("waterplayer.config.overlay.position.bottom_right").getString()
        };
        return new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"))
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.config"), (e) -> AlinLib.MINECRAFT.setScreen(MainConfigsScreen.build(parent))).setIcon(OPTIONS).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.config.localization"), (e) -> AlinLib.MINECRAFT.setScreen(LocalizationConfigsScreen.build(parent))).setIcon(LIST).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.secret"), (e) -> AlinLib.MINECRAFT.setScreen(SecretConfigsScreen.build(parent))).setIcon(WARNING).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.playlists"), (e) -> AlinLib.MINECRAFT.setScreen(PlaylistsScreen.build(parent))).setIcon(LIST).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.play"), (e) -> AlinLib.MINECRAFT.setScreen(new ControlScreen(SecretConfigsScreen.build(parent)))).setIcon(GuiUtils.getResourceLocation("waterplayer", "textures/player/play.png")).setCentered(false).build())
                //
                .addWidget(new TextBox(Component.translatable("waterplayer.config"), true))
                .addWidget(new CategoryBox(Component.translatable("waterplayer.config.overlay"))
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.enable_overlay"), true).setConfig(WaterPlayer.config, "ENABLE_OVERLAY").build())
                        .addValue(new SelectorBuilder(Component.translatable("waterplayer.config.overlay.position")).setList(type).setConfig(WaterPlayer.config, "OVERLAY.POSITION").setValue(0).build())
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.overlay.enable_caver"), true).setConfig(WaterPlayer.config, "OVERLAY.ENABLE_CAVER").build())
                )
                .addWidget(new CategoryBox(Component.translatable("waterplayer.config.menu"))
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.enable_menu_overlay"), true).setConfig(WaterPlayer.config, "ENABLE_MENU_OVERLAY").build())
                        .addValue(new SelectorBuilder(Component.translatable("waterplayer.config.menu_overlay.position")).setList(type).setConfig(WaterPlayer.config, "MENU_OVERLAY.POSITION").setValue(0).build())
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.enable_keybind"), false).setConfig(WaterPlayer.config, "ENABLE_KEYBINDS").build())
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.screem.queue_cover_show"), true).setConfig(WaterPlayer.config, "SCREEN.QUEUE_COVER_SHOW").build())
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.control.enable_lyrics"), true).setConfig(WaterPlayer.config, "CONTROL.ENABLE_LYRICS").build())
                )
                .addWidget(new CategoryBox(Component.translatable("waterplayer.config.notice"))
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.enable_notice"), true).setConfig(WaterPlayer.config, "ENABLE_NOTICE").build())
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.enable_notice.start_track"), true).setConfig(WaterPlayer.config, "ENABLE_NOTICE.START_TRACK").build())
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.enable_notice.start_track.clear"), false).setConfig(WaterPlayer.config, "ENABLE_NOTICE.START_TRACK.CLEAR").build())
                )
                .addWidget(new CategoryBox(Component.translatable("waterplayer.config.subtitles"))
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.subtitles"), false).setConfig(WaterPlayer.config, "SUBTITLES").build())
                        .addValue(new SliderBuilder(Component.translatable("waterplayer.config.subtitles.indent_y")).setDefaultValue(85).setConfig(WaterPlayer.config, "SUBTITLES.INDENT_Y").setMin(5).setMax(150).build())
                        .addValue(new SliderBuilder(Component.translatable("waterplayer.config.subtitles.back_alpha")).setDefaultValue(0.5, true).setConfig(WaterPlayer.config, "SUBTITLES.BACK_ALPHA").setMin(0).setMax(1).build())
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.subtitles.text_color")).setColor(-1).setConfig(WaterPlayer.config, "SUBTITLES.TEXT_COLOR").build())
                )
                .addWidget(new CategoryBox(Component.translatable("waterplayer.config.services"))
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.services.youtube"), true).setConfig(WaterPlayer.config, "ENABLE_YOUTUBE").build())
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.services.soundcloud"), true).setConfig(WaterPlayer.config, "ENABLE_SOUNDCLOUD").build())
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.services.bandcamp"), true).setConfig(WaterPlayer.config, "ENABLE_BANDCAMP").build())
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.services.vimeo"), true).setConfig(WaterPlayer.config, "ENABLE_VIMEO").build())
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.services.twitch"), false).setConfig(WaterPlayer.config, "ENABLE_TWITCH").build())
                        .addValue(new ButtonBooleanBuilder(Component.translatable("waterplayer.config.services.beam"), true).setConfig(WaterPlayer.config, "ENABLE_BEAM").build())
                )
                .build();
    }
}
