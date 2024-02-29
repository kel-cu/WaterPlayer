package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.config.PlaylistObject;
import ru.kelcuprum.waterplayer.frontend.gui.screens.CreatePlaylistScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.LoadMusicScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.PlaylistScreen;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

public class PlaylistsScreen {
    private static final Component MainConfigCategory = Localization.getText("waterplayer.config");
    private static final Component LocalizationConfigCategory = Localization.getText("waterplayer.config.localization");
    private static final Component SecretConfigCategory = Localization.getText("waterplayer.secret");
    private static final Component PlaylistsCategory = Localization.getText("waterplayer.playlists");
    private static final Component PlayCategory = Localization.getText("waterplayer.play");
    //
    private final InterfaceUtils.DesignType designType = InterfaceUtils.DesignType.FLAT;
    public Screen build(Screen parent) {
        ConfigScreenBuilder builder = new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"), designType)
                .addPanelWidget(new Button(10, 40, designType, MainConfigCategory, (e) -> {
                    WaterPlayer.MINECRAFT.setScreen(new MainConfigsScreen().build(parent));
                }))
                .addPanelWidget(new Button(10, 65, designType, LocalizationConfigCategory, (e) -> {
                    WaterPlayer.MINECRAFT.setScreen(new LocalizationConfigsScreen().build(parent));
                }))
                .addPanelWidget(new Button(10, 90, designType, SecretConfigCategory, (e) -> {
                    WaterPlayer.MINECRAFT.setScreen(new SecretConfigsScreen().build(parent));
                }))
                .addPanelWidget(new Button(10, 115, designType, PlaylistsCategory, (e) -> {
                    WaterPlayer.MINECRAFT.setScreen(new PlaylistsScreen().build(parent));
                }))
                .addPanelWidget(new Button(10, 140, designType, PlayCategory, (e) -> {
                    WaterPlayer.MINECRAFT.setScreen(new LoadMusicScreen(this.build(parent)));
                }))
                ///
                .addWidget(new TextBox(140, 5, PlaylistsCategory, true));
        File playlists = WaterPlayer.MINECRAFT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists").toFile();
        if(playlists.exists() && playlists.isDirectory()){
            for(File playlist : Objects.requireNonNull(playlists.listFiles())){
                if(playlist.isFile() && playlist.getName().endsWith(".json")){
                    try {
                        JsonObject jsonPlaylist = GsonHelper.parse(Files.readString(playlist.toPath()));
                        PlaylistObject playlistObject = new PlaylistObject(jsonPlaylist);
                        String fileName = playlist.getName().replace(".json", "");
                        builder.addWidget(new Button(140, -20, designType, Component.literal(String.format("%s by %s (%s)", playlistObject.title, playlistObject.author, fileName)), (s) ->{
                            WaterPlayer.MINECRAFT.setScreen(new PlaylistScreen(new PlaylistsScreen().build(parent), fileName));
                        }));
                    } catch (Exception e){
                        WaterPlayer.log(e.getLocalizedMessage(), Level.ERROR);
                    }
                }
            }
        }
        builder.addWidget(new Button(140, -20, designType, Component.translatable("waterplayer.playlist.create"), (s) -> {
            WaterPlayer.MINECRAFT.setScreen(new CreatePlaylistScreen(new PlaylistsScreen().build(parent)));
        }));
        return builder.build();
    }
}
