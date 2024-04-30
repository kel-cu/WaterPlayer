package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBooleanBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.selector.SelectorBuilder;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.screens.LoadMusicScreen;

public class MainConfigsScreen {
    private static final Component MainConfigCategory = Localization.getText("waterplayer.config");
    private static final Component LocalizationConfigCategory = Localization.getText("waterplayer.config.localization");
    private static final Component SecretConfigCategory = Localization.getText("waterplayer.secret");
    private static final Component PlaylistsCategory = Localization.getText("waterplayer.playlists");
    private static final Component PlayCategory = Localization.getText("waterplayer.play");
    // CATEGORY CONTENT
    private static final Component enableOverlayText = Localization.getText("waterplayer.config.enable_overlay");
    private static final Component overlayPositionText = Localization.getText("waterplayer.config.overlay.position");
    private static final Component enableNoticeText = Localization.getText("waterplayer.config.enable_notice");
    private static final Component screenQueueCoverShow = Localization.getText("waterplayer.config.screem.queue_cover_show");

    private final InterfaceUtils.DesignType designType = InterfaceUtils.DesignType.FLAT;

    public Screen build(Screen parent) {
        String[] type = {
                Component.translatable("waterplayer.config.overlay.position.top_left").getString(),
                Component.translatable("waterplayer.config.overlay.position.top_right").getString(),
                Component.translatable("waterplayer.config.overlay.position.bottom_left").getString(),
                Component.translatable("waterplayer.config.overlay.position.bottom_right").getString()
        };
        return new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"), designType)
                .addPanelWidget(new ButtonBuilder(MainConfigCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new MainConfigsScreen().build(parent))).build())
                .addPanelWidget(new ButtonBuilder(LocalizationConfigCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new LocalizationConfigsScreen().build(parent))).build())
                .addPanelWidget(new ButtonBuilder(SecretConfigCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new SecretConfigsScreen().build(parent))).build())
                .addPanelWidget(new ButtonBuilder(PlaylistsCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new PlaylistsScreen().build(parent))).build())
                .addPanelWidget(new ButtonBuilder(PlayCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new LoadMusicScreen(this.build(parent)))).build())
                //
                .addWidget(new TextBox(MainConfigCategory, true))
                .addWidget(new ButtonBooleanBuilder(enableOverlayText, true).setConfig(WaterPlayer.config, "ENABLE_OVERLAY").build())
                .addWidget(new SelectorBuilder(overlayPositionText).setList(type).setConfig(WaterPlayer.config, "OVERLAY.POSITION").setValue(0).build())
                .addWidget(new ButtonBooleanBuilder(enableNoticeText, true).setConfig(WaterPlayer.config, "ENABLE_NOTICE").build())
                .addWidget(new ButtonBooleanBuilder(screenQueueCoverShow, false).setConfig(WaterPlayer.config, "SCREEN.QUEUE_COVER_SHOW").build())
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
