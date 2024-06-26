package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonWithIconBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.ControlScreen;

import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Icons.*;

public class LocalizationConfigsScreen {
    private static final Component MainConfigCategory = Localization.getText("waterplayer.config");
    private static final Component LocalizationConfigCategory = Localization.getText("waterplayer.config.localization");
    private static final Component SecretConfigCategory = Localization.getText("waterplayer.secret");
    private static final Component PlaylistsCategory = Localization.getText("waterplayer.playlists");
    private static final Component PlayCategory = Localization.getText("waterplayer.play");
    private final Component titleFormatsText = Localization.getText("waterplayer.config.localization.title.formats");
    private final Component timeText = Localization.getText("waterplayer.config.localization.format.time");
    private final Component liveText = Localization.getText("waterplayer.config.localization.format.live");

    public Screen build(Screen parent) {
        return new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"))
                .addPanelWidget(new ButtonWithIconBuilder(MainConfigCategory, OPTIONS, (e) -> AlinLib.MINECRAFT.setScreen(new MainConfigsScreen().build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(LocalizationConfigCategory, LIST, (e) -> AlinLib.MINECRAFT.setScreen(new LocalizationConfigsScreen().build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(SecretConfigCategory, WARNING, (e) -> AlinLib.MINECRAFT.setScreen(new SecretConfigsScreen().build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(PlaylistsCategory, LIST, (e) -> AlinLib.MINECRAFT.setScreen(new PlaylistsScreen().build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(PlayCategory, InterfaceUtils.getResourceLocation("waterplayer", "textures/player/play.png"), (e) -> AlinLib.MINECRAFT.setScreen(new ControlScreen(this.build(parent)))).setCentered(false).build())
                //
                .addWidget(new TextBox(LocalizationConfigCategory, true))
                .addWidget(new CategoryBox(titleFormatsText)
                        .addValue(new EditBoxBuilder(timeText).setLocalization(WaterPlayer.localization, "format.time").build())
                        .addValue(new EditBoxBuilder(liveText).setLocalization(WaterPlayer.localization, "format.live").build())
                )
                .build();
    }
}
