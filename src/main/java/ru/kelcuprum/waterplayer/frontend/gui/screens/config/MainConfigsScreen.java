package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.ButtonConfigBoolean;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.alinlib.gui.components.selector.SelectorIntegerButton;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.screens.LoadMusicScreen;

public class MainConfigsScreen{
    private static final Component MainConfigCategory = Localization.getText("waterplayer.config");
    private static final Component LocalizationConfigCategory = Localization.getText("waterplayer.config.localization");
    private static final Component SecretConfigCategory = Localization.getText("waterplayer.secret");
    private static final Component PlaylistsCategory = Localization.getText("waterplayer.playlists");
    private static final Component PlayCategory = Localization.getText("waterplayer.play");
    // CATEGORY CONTENT
    private static final Component enableOverlayText = Localization.getText("waterplayer.config.enable_overlay");
    private static final Component overlayPositionText = Localization.getText("waterplayer.config.overlay.position");
    private static final Component enableNoticeText = Localization.getText("waterplayer.config.enable_notice");
    private static final Component enableChangeTitleText = Localization.getText("waterplayer.config.enable_change_title");

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
                .addWidget(new ButtonConfigBoolean(140, 55, designType, WaterPlayer.config, "ENABLE_OVERLAY", false, enableOverlayText))
                .addWidget(new SelectorIntegerButton(140, 80, designType, type, WaterPlayer.config, "OVERLAY.POSITION", 0, overlayPositionText))
                .addWidget(new ButtonConfigBoolean(140, 105, designType, WaterPlayer.config, "ENABLE_NOTICE", false, enableNoticeText))
                .addWidget(new ButtonConfigBoolean(140, 130, designType, WaterPlayer.config, "ENABLE_CHANGE_TITLE", false, enableChangeTitleText))
                .addWidget(new CategoryBox(Component.translatable("waterplayer.config.services"))
                    .addValue(new ButtonConfigBoolean(140, 205, WaterPlayer.config, "ENABLE_YOUTUBE", true, Component.translatable("waterplayer.config.services.youtube")))
                    .addValue(new ButtonConfigBoolean(140, 230, WaterPlayer.config, "ENABLE_SOUNDCLOUD", true, Component.translatable("waterplayer.config.services.soundcloud")))
                    .addValue(new ButtonConfigBoolean(140, 255, WaterPlayer.config, "ENABLE_BANDCAMP", true, Component.translatable("waterplayer.config.services.bandcamp")))
                    .addValue(new ButtonConfigBoolean(140, 280, WaterPlayer.config, "ENABLE_VIMEO", true, Component.translatable("waterplayer.config.services.vimeo")))
                    .addValue(new ButtonConfigBoolean(140, 305, WaterPlayer.config, "ENABLE_TWITCH", false, Component.translatable("waterplayer.config.services.twitch")))
                    .addValue(new ButtonConfigBoolean(140, 330, WaterPlayer.config, "ENABLE_BEAM", true, Component.translatable("waterplayer.config.services.beam")))
                    )
                .build();
    }
}
