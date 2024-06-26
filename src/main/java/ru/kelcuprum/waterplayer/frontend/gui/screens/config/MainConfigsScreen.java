package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBooleanBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonWithIconBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.selector.SelectorBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.slider.SliderDoubleBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.slider.SliderIntegerBuilder;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.ControlScreen;

import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Icons.*;
import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Icons.LIST;

public class MainConfigsScreen {
    private static final Component MainConfigCategory = Localization.getText("waterplayer.config");
    private static final Component LocalizationConfigCategory = Localization.getText("waterplayer.config.localization");
    private static final Component SecretConfigCategory = Localization.getText("waterplayer.secret");
    private static final Component PlaylistsCategory = Localization.getText("waterplayer.playlists");
    private static final Component PlayCategory = Localization.getText("waterplayer.play");
    // CATEGORY CONTENT

    private static final Component overlay = Localization.getText("waterplayer.config.overlay");
    private static final Component enableOverlayText = Localization.getText("waterplayer.config.enable_overlay");
    private static final Component overlayPositionText = Localization.getText("waterplayer.config.overlay.position");
    private static final Component overlayCaverText = Localization.getText("waterplayer.config.overlay.enable_caver");

    private static final Component menu = Localization.getText("waterplayer.config.menu");
    private static final Component enableMenuOverlayText = Localization.getText("waterplayer.config.enable_menu_overlay");
    private static final Component menuOverlayPositionText = Localization.getText("waterplayer.config.menu_overlay.position");
    private static final Component enableKeyBind = Localization.getText("waterplayer.config.enable_keybind");
    private static final Component screenQueueCoverShow = Localization.getText("waterplayer.config.screem.queue_cover_show");
    private static final Component controlEnableLyrics = Localization.getText("waterplayer.config.control.enable_lyrics");

    private static final Component notice = Localization.getText("waterplayer.config.notice");
    private static final Component enableNoticeText = Localization.getText("waterplayer.config.enable_notice");
    private static final Component enableNoticeStartTrackText = Localization.getText("waterplayer.config.enable_notice.start_track");
    private static final Component enableNoticeStartTrackClearText = Localization.getText("waterplayer.config.enable_notice.start_track.clear");

    private static final Component subtitles = Localization.getText("waterplayer.config.subtitles");
    private static final Component subtitlesIndentY = Localization.getText("waterplayer.config.subtitles.indent_y");
    private static final Component subtitlesBackAlpha = Localization.getText("waterplayer.config.subtitles.back_alpha");
    private static final Component subtitlesTextColor = Localization.getText("waterplayer.config.subtitles.text_color");

    public Screen build(Screen parent) {
        String[] type = {
                Component.translatable("waterplayer.config.overlay.position.top_left").getString(),
                Component.translatable("waterplayer.config.overlay.position.top_right").getString(),
                Component.translatable("waterplayer.config.overlay.position.bottom_left").getString(),
                Component.translatable("waterplayer.config.overlay.position.bottom_right").getString()
        };
        return new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"))
                .addPanelWidget(new ButtonWithIconBuilder(MainConfigCategory, OPTIONS, (e) -> AlinLib.MINECRAFT.setScreen(new MainConfigsScreen().build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(LocalizationConfigCategory, LIST, (e) -> AlinLib.MINECRAFT.setScreen(new LocalizationConfigsScreen().build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(SecretConfigCategory, WARNING, (e) -> AlinLib.MINECRAFT.setScreen(new SecretConfigsScreen().build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(PlaylistsCategory, LIST, (e) -> AlinLib.MINECRAFT.setScreen(new PlaylistsScreen().build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(PlayCategory, InterfaceUtils.getResourceLocation("waterplayer", "textures/player/play.png"), (e) -> AlinLib.MINECRAFT.setScreen(new ControlScreen(this.build(parent)))).setCentered(false).build())
                //
                .addWidget(new TextBox(MainConfigCategory, true))
                .addWidget(new CategoryBox(overlay)
                        .addValue(new ButtonBooleanBuilder(enableOverlayText, true).setConfig(WaterPlayer.config, "ENABLE_OVERLAY").build())
                        .addValue(new SelectorBuilder(overlayPositionText).setList(type).setConfig(WaterPlayer.config, "OVERLAY.POSITION").setValue(0).build())
                        .addValue(new ButtonBooleanBuilder(overlayCaverText, true).setConfig(WaterPlayer.config, "OVERLAY.ENABLE_CAVER").build())
                )
                .addWidget(new CategoryBox(menu)
                        .addValue(new ButtonBooleanBuilder(enableMenuOverlayText, true).setConfig(WaterPlayer.config, "ENABLE_MENU_OVERLAY").build())
                        .addValue(new SelectorBuilder(menuOverlayPositionText).setList(type).setConfig(WaterPlayer.config, "MENU_OVERLAY.POSITION").setValue(0).build())
                        .addValue(new ButtonBooleanBuilder(enableKeyBind, false).setConfig(WaterPlayer.config, "ENABLE_KEYBINDS").build())
                        .addValue(new ButtonBooleanBuilder(screenQueueCoverShow, true).setConfig(WaterPlayer.config, "SCREEN.QUEUE_COVER_SHOW").build())
                        .addValue(new ButtonBooleanBuilder(controlEnableLyrics, true).setConfig(WaterPlayer.config, "CONTROL.ENABLE_LYRICS").build())
                )
                .addWidget(new CategoryBox(notice)
                        .addValue(new ButtonBooleanBuilder(enableNoticeText, true).setConfig(WaterPlayer.config, "ENABLE_NOTICE").build())
                        .addValue(new ButtonBooleanBuilder(enableNoticeStartTrackText, true).setConfig(WaterPlayer.config, "ENABLE_NOTICE.START_TRACK").build())
                        .addValue(new ButtonBooleanBuilder(enableNoticeStartTrackClearText, false).setConfig(WaterPlayer.config, "ENABLE_NOTICE.START_TRACK.CLEAR").build())
                )
                .addWidget(new CategoryBox(Component.translatable("waterplayer.config.subtitles"))
                        .addValue(new ButtonBooleanBuilder(subtitles, false).setConfig(WaterPlayer.config, "SUBTITLES").build())
                        .addValue(new SliderIntegerBuilder(subtitlesIndentY).setDefaultValue(85).setConfig(WaterPlayer.config, "SUBTITLES.INDENT_Y").setMin(5).setMax(150).build())
                        .addValue(new SliderDoubleBuilder(subtitlesBackAlpha).setDefaultValue(0.5).setConfig(WaterPlayer.config, "SUBTITLES.BACK_ALPHA").setMin(0).setMax(1).build())
                        .addValue(new EditBoxBuilder(subtitlesTextColor).setColor(-1).setConfig(WaterPlayer.config, "SUBTITLES.TEXT_COLOR").build())
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
