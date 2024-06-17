package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonWithIconBuilder;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;
import ru.kelcuprum.waterplayer.frontend.gui.screens.CreatePlaylistScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.ControlScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.PlaylistScreen;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Icons.*;
import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Icons.LIST;

public class PlaylistsScreen {
    private static final Component MainConfigCategory = Localization.getText("waterplayer.config");
    private static final Component LocalizationConfigCategory = Localization.getText("waterplayer.config.localization");
    private static final Component SecretConfigCategory = Localization.getText("waterplayer.secret");
    private static final Component PlaylistsCategory = Localization.getText("waterplayer.playlists");
    private static final Component PlayCategory = Localization.getText("waterplayer.play");
    //
    int assetsSize = 0;
    boolean isLoaded = false;
    public Screen build(Screen parent) {
        File playlists = WaterPlayer.MINECRAFT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists").toFile();

        ConfigScreenBuilder builder = new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"))
                .setOnTick((s) -> {
                    int size = 0;
                    if(playlists.exists() && playlists.isDirectory()){
                        for(File playlist : Objects.requireNonNull(playlists.listFiles())){
                            if(playlist.isFile() && playlist.getName().endsWith(".json")){
                                try {
                                    JsonObject jsonPlaylist = GsonHelper.parse(Files.readString(playlist.toPath()));
                                    new Playlist(jsonPlaylist);
                                    size++;
                                } catch (Exception e){
                                    WaterPlayer.log(e.getLocalizedMessage(), Level.ERROR);
                                }
                            }
                        }
                        if(isLoaded && (assetsSize != size)) AlinLib.MINECRAFT.setScreen(this.build(parent));
                    }
                })
                .addPanelWidget(new ButtonWithIconBuilder(MainConfigCategory, OPTIONS, (e) -> WaterPlayer.MINECRAFT.setScreen(new MainConfigsScreen().build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(LocalizationConfigCategory, LIST, (e) -> WaterPlayer.MINECRAFT.setScreen(new LocalizationConfigsScreen().build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(SecretConfigCategory, WARNING, (e) -> WaterPlayer.MINECRAFT.setScreen(new SecretConfigsScreen().build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(PlaylistsCategory, LIST, (e) -> WaterPlayer.MINECRAFT.setScreen(new PlaylistsScreen().build(parent))).setCentered(false).build())
                .addPanelWidget(new ButtonWithIconBuilder(PlayCategory, InterfaceUtils.getResourceLocation("waterplayer", "textures/player/play.png"), (e) -> WaterPlayer.MINECRAFT.setScreen(new ControlScreen(this.build(parent)))).setCentered(false).build())
                //
                .addWidget(new TextBox(140, 5, PlaylistsCategory, true));
        if(playlists.exists() && playlists.isDirectory()){
            for(File playlist : Objects.requireNonNull(playlists.listFiles())){
                if(playlist.isFile() && playlist.getName().endsWith(".json")){
                    try {
                        Playlist playlistObject = new Playlist(playlist.toPath());
                        assetsSize++;
                        builder.addWidget(new ButtonBuilder(Component.literal(String.format("%s by %s (%s)", playlistObject.title, playlistObject.author, playlistObject.fileName)), (s) -> WaterPlayer.MINECRAFT.setScreen(new PlaylistScreen(new PlaylistsScreen().build(parent), playlistObject.fileName))).build());
                    } catch (Exception e){
                        WaterPlayer.log(e.getLocalizedMessage(), Level.ERROR);
                    }
                }
            }
        }
        isLoaded = true;
        builder.addWidget(new ButtonBuilder(Component.translatable("waterplayer.playlist.create"), (s) -> WaterPlayer.MINECRAFT.setScreen(new CreatePlaylistScreen(new PlaylistsScreen().build(parent)))).build());
        return builder.build();
    }
}
