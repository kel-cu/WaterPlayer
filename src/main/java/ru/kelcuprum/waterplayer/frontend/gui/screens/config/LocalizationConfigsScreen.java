package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.Icons;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;

import static ru.kelcuprum.alinlib.gui.Icons.*;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.VOLUME_MAX;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.getPlayOrPause;

public class LocalizationConfigsScreen {

    public static Screen build(Screen parent) {
        return new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"))
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.config"), (e) -> AlinLib.MINECRAFT.setScreen(MainConfigsScreen.build(parent))).setIcon(OPTIONS).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.config.localization"), (e) -> AlinLib.MINECRAFT.setScreen(LocalizationConfigsScreen.build(parent))).setIcon(LIST).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.secret"), (e) -> AlinLib.MINECRAFT.setScreen(SecretConfigsScreen.build(parent))).setIcon(WARNING).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.playlists"), (e) -> AlinLib.MINECRAFT.setScreen(PlaylistsScreen.build(parent))).setIcon(LIST).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.additional"), (e) -> AlinLib.MINECRAFT.setScreen(AdditionalScreen.build(parent))).setIcon(OPTIONS).setCentered(false).build())
                .addPanelWidget(new ButtonBuilder(Component.translatable("waterplayer.play"), (e) -> AlinLib.MINECRAFT.setScreen(WaterPlayer.getControlScreen(LocalizationConfigsScreen.build(parent)))).setIcon(getPlayOrPause(WaterPlayer.player.getAudioPlayer().isPaused())).setCentered(false).build())
                //
                .addWidget(new TextBox(Component.translatable("waterplayer.config.localization"), true))
                .addWidget(new CategoryBox(Component.translatable("waterplayer.config.localization.title.formats"))
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.localization.format.time")).setLocalization(WaterPlayer.localization, "format.time").build())
                        .addValue(new EditBoxBuilder(Component.translatable("waterplayer.config.localization.format.live")).setLocalization(WaterPlayer.localization, "format.live").build())
                )
                .build();
    }
}
