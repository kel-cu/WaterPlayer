package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.editbox.EditBoxLocalization;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.screens.LoadMusicScreen;

public class LocalizationConfigsScreen {
    private static final Component MainConfigCategory = Localization.getText("waterplayer.config");
    private static final Component LocalizationConfigCategory = Localization.getText("waterplayer.config.localization");
    private static final Component SecretConfigCategory = Localization.getText("waterplayer.secret");
    private static final Component PlaylistsCategory = Localization.getText("waterplayer.playlists");
    private static final Component PlayCategory = Localization.getText("waterplayer.play");
    private final Component titleFormatsText = Localization.getText("waterplayer.config.localization.title.formats");
    private final Component authorText = Localization.getText("waterplayer.config.localization.format.author");
    private final Component titleText = Localization.getText("waterplayer.config.localization.format.title");
    private final Component timeText = Localization.getText("waterplayer.config.localization.format.time");
    private final Component liveText = Localization.getText("waterplayer.config.localization.format.live");

    private final InterfaceUtils.DesignType designType = InterfaceUtils.DesignType.FLAT;

    public Screen build(Screen parent) {
        return new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"), designType)
                .addPanelWidget(new ButtonBuilder(MainConfigCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new MainConfigsScreen().build(parent))).build())
                .addPanelWidget(new ButtonBuilder(LocalizationConfigCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new LocalizationConfigsScreen().build(parent))).build())
                .addPanelWidget(new ButtonBuilder(SecretConfigCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new SecretConfigsScreen().build(parent))).build())
                .addPanelWidget(new ButtonBuilder(PlaylistsCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new PlaylistsScreen().build(parent))).build())
                .addPanelWidget(new ButtonBuilder(PlayCategory, (e) -> WaterPlayer.MINECRAFT.setScreen(new LoadMusicScreen(this.build(parent)))).build())
                //
                .addWidget(new TextBox(LocalizationConfigCategory, true))
                .addWidget(new CategoryBox(titleFormatsText)
                        .addValue(new EditBoxLocalization(140, 355, designType, WaterPlayer.localization, "format.author", authorText))
                        .addValue(new EditBoxLocalization(140, 380, designType, WaterPlayer.localization, "format.title", titleText))
                        .addValue(new EditBoxLocalization(140, 405, designType, WaterPlayer.localization, "format.time", timeText))
                        .addValue(new EditBoxLocalization(140, 430, designType, WaterPlayer.localization, "format.live", liveText))
                )
                .build();
    }
}
