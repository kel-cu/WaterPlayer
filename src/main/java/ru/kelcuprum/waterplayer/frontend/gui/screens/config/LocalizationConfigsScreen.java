package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
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

    public static Screen build(Screen parent) {
        return new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"))
                .addPanelWidget(new ButtonWithIconBuilder(Component.translatable("waterplayer.config"), OPTIONS, (e) -> AlinLib.MINECRAFT.setScreen(MainConfigsScreen.build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(Component.translatable("waterplayer.config.localization"), LIST, (e) -> AlinLib.MINECRAFT.setScreen(LocalizationConfigsScreen.build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(Component.translatable("waterplayer.secret"), WARNING, (e) -> AlinLib.MINECRAFT.setScreen(SecretConfigsScreen.build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(Component.translatable("waterplayer.playlists"), LIST, (e) -> AlinLib.MINECRAFT.setScreen(PlaylistsScreen.build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(Component.translatable("waterplayer.play"), InterfaceUtils.getResourceLocation("waterplayer", "textures/player/play.png"), (e) -> AlinLib.MINECRAFT.setScreen(new ControlScreen(SecretConfigsScreen.build(parent)))).setCentered(false).build())
                //
                .addWidget(new TextBox(Component.translatable("waterplayer.config.localization"), true))
                .addWidget(new CategoryBox(Component.translatable("waterplayer.config.localization.title.formats"))
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.localization.format.time")).setLocalization(WaterPlayer.localization, "format.time").build())
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.localization.format.live")).setLocalization(WaterPlayer.localization, "format.live").build())
                )
                .build();
    }
}
