package ru.kelcuprum.waterplayer.screens;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.json.JSONObject;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.config.PlaylistObject;
import ru.kelcuprum.waterplayer.config.UserConfig;
import ru.kelcuprum.waterplayer.localization.Localization;
import ru.kelcuprum.waterplayer.localization.Music;
import ru.kelcuprum.waterplayer.toasts.ControlToast;

import java.nio.file.Files;
import java.nio.file.Path;

public class MusicScreen {
    public static Screen buildScreen (Screen currentScreen) {
        UserConfig.load();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(currentScreen)
                .setTitle(Localization.getText("waterplayer.name"))
                .setTransparentBackground(true)
                .setSavingRunnable(MusicScreen::save);
        ConfigCategory category = builder.getOrCreateCategory(Localization.getText("waterplayer.load"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.load.url"),
                        "")
                .setDefaultValue("")
                .setSaveConsumer(newValue -> UserConfig.LAST_REQUEST_MUSIC = newValue)
                .build());
        //
        category.addEntry(entryBuilder.startStrField(
                        Localization.getText("waterplayer.load.url.copy"),
                        UserConfig.LAST_REQUEST_MUSIC)
                .setDefaultValue(UserConfig.LAST_REQUEST_MUSIC)
                .build());
        if(!WaterPlayer.music.getTrackManager().queue.isEmpty()){
            category.addEntry(entryBuilder.startTextDescription(Localization.getText("waterplayer.load.queue")).build());
            StringBuilder stringBuilder = new StringBuilder();
            for(AudioTrack track : WaterPlayer.music.getTrackManager().queue){
                stringBuilder.append("«").append(Music.getAuthor(track)).append("» ")
                        .append(Music.getTitle(track)).append(" ")
                        .append(Localization.getTimestamp(Music.getDuration(track))).append("\n");
            }
            category.addEntry(entryBuilder.startTextDescription(Localization.toText(stringBuilder.toString())).build());
        }
        return builder.build();
    }
    private static void save(){
        Minecraft CLIENT = Minecraft.getInstance();
        UserConfig.save();
        if(!UserConfig.LAST_REQUEST_MUSIC.isBlank()){
            if(UserConfig.LAST_REQUEST_MUSIC.startsWith("playlist:")){
                String name = UserConfig.LAST_REQUEST_MUSIC.replace("playlist:", "");
                PlaylistObject playlist;
                JSONObject jsonPlaylist = new JSONObject();

                final Path configFile = CLIENT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists/"+name+".json");
                try {
                    jsonPlaylist = new JSONObject(Files.readString(configFile));
                } catch (Exception ex){
                    ex.printStackTrace();
                }
                playlist = new PlaylistObject(jsonPlaylist);
                for(int i = 0; i<playlist.urls.size(); i++){
                    WaterPlayer.music.getTrackSearch().getTracks(playlist.urls.get(i));
                }
                CLIENT.getToasts().addToast(new ControlToast(Localization.toText(
                        Localization.toString(Localization.getText("waterplayer.load.add.playlist"))
                                .replace("%playlist_name%", playlist.title)
                ), false));
            } else {
                WaterPlayer.music.getTrackSearch().getTracks(UserConfig.LAST_REQUEST_MUSIC);
                CLIENT.getToasts().addToast(new ControlToast(Localization.getText("waterplayer.load.add"), false));
            }
        }else if(CLIENT.player != null) CLIENT.getToasts().addToast(new ControlToast(Localization.getText("waterplayer.load.add.blank"), true));
    }
}
