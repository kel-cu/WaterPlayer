package ru.kelcuprum.waterplayer.screens;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.Level;
import org.json.JSONObject;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.localization.Localization;
import ru.kelcuprum.waterplayer.config.PlaylistObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlaylistScreen {
    private PlaylistObject playlist;
    private String plName = "its-normal";
    JSONObject jsonPlaylist = new JSONObject();
    public Screen buildScreen(Screen currentScreen, String playlistName) {
        Minecraft CLIENT = Minecraft.getInstance();
        //
        plName = playlistName;
        final Path configFile = CLIENT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists/"+playlistName+".json");
        try {
            jsonPlaylist = new JSONObject(Files.readString(configFile));
        } catch (Exception ex){
            WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
        }
        //
        playlist = new PlaylistObject(jsonPlaylist);
        //
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(currentScreen)
                .setTitle(Localization.getText("waterplayer.name.playlist"))
                .setTransparentBackground(true)
                .setSavingRunnable(this::save);
        ConfigCategory category = builder.getOrCreateCategory(Localization.getText("waterplayer.playlist"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.playlist.title"),
                        playlist.title)
                .setDefaultValue("Example title")
                .setSaveConsumer(newValue -> playlist.title = newValue)
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.playlist.author"),
                        playlist.author)
                .setDefaultValue("Example author")
                .setSaveConsumer(newValue -> playlist.author = newValue)
                .build());
        category.addEntry(entryBuilder.startStrList(
                Localization.getText("waterplayer.playlist.urls"),
                        playlist.urls
        ).setSaveConsumer(newValue -> playlist.urls = newValue)
                .build());
        return builder.build();
    }
    private void save(){
        Minecraft CLIENT = Minecraft.getInstance();
        final Path configFile = CLIENT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists/"+plName+".json");
        try {
            Files.createDirectories(configFile.getParent());
            Files.writeString(configFile, playlist.toJSON().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
