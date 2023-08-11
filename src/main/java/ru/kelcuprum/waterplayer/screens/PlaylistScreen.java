package ru.kelcuprum.waterplayer.screens;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.json.JSONObject;
import ru.kelcuprum.waterplayer.config.Localization;
import ru.kelcuprum.waterplayer.config.PlaylistObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlaylistScreen {
    private PlaylistObject playlist;
    private String plName = "its-normal";
    JSONObject jsonPlaylist = new JSONObject();
    public Screen buildScreen(Screen currentScreen, String playlistName) {
        MinecraftClient CLIENT = MinecraftClient.getInstance();
        //
        plName = playlistName;
        final Path configFile = CLIENT.runDirectory.toPath().resolve("config/WaterPlayer/playlists/"+playlistName+".json");
        try {
            jsonPlaylist = new JSONObject(Files.readString(configFile));
        } catch (Exception ex){
            ex.printStackTrace();
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
        MinecraftClient CLIENT = MinecraftClient.getInstance();
        final Path configFile = CLIENT.runDirectory.toPath().resolve("config/WaterPlayer/playlists/"+plName+".json");
        try {
            Files.createDirectories(configFile.getParent());
            Files.writeString(configFile, playlist.toJSON().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
