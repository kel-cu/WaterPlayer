package ru.kelcuprum.waterplayer.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.Level;
import org.json.JSONObject;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.flat.FlatEditBoxString;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.config.PlaylistObject;
import ru.kelcuprum.waterplayer.localization.Localization;
import ru.kelcuprum.waterplayer.gui.toasts.ControlToast;

import java.nio.file.Files;
import java.nio.file.Path;

public class LoadMusicScreen extends Screen {
    private final Screen parent;
    public LoadMusicScreen(Screen parent) {
        super(Localization.getText("waterplayer.name"));
        this.parent = parent;
    }

    public void tick() {
        super.tick();
    }
    @Override
    public void init() {
        int size = 100;
        addRenderableWidget(new TextBox(((width/2)-(310/2)), height/2-(25*3), 310, this.font.lineHeight, Localization.getText("waterplayer.load"), true));
        FlatEditBoxString request = new FlatEditBoxString(this.font,((width/2)-(310/2)), height/2-(25*2), 310,  20, Localization.getText("waterplayer.load.url"));
        request.setMaxLength(Integer.MAX_VALUE);
        addRenderableWidget(request);

        addRenderableWidget(new Button(((width/2)-(size/2))-size-5, height/2-(20), size,  20, 0xB6FF3131, Localization.getText("waterplayer.screen.exit"), (OnPress) -> {
            this.minecraft.setScreen(parent);
        }));
        addRenderableWidget(new Button(((width/2)-(size/2)), height/2-(20), size,  20, Localization.getText("waterplayer.load.load"), (OnPress) -> {
            loadMusic(request.getValue());
            this.minecraft.setScreen(parent);
        }));
        addRenderableWidget(new Button(((width/2)-(size/2))+size+5, height/2-(20), size,  20, Localization.getText("waterplayer.load.url.copy"), (OnPress) -> {
            request.setValue(WaterPlayer.config.getString("LAST_REQUEST_MUSIC", ""));
        }));
    }

    public static void loadMusic(String url){
        WaterPlayer.config.setString("LAST_REQUEST_MUSIC", url);
        WaterPlayer.config.save();
        if(!WaterPlayer.config.getString("LAST_REQUEST_MUSIC", "").isBlank()){
            if(WaterPlayer.config.getString("LAST_REQUEST_MUSIC", "").startsWith("playlist:")){
                String name = WaterPlayer.config.getString("LAST_REQUEST_MUSIC", "").replace("playlist:", "");
                PlaylistObject playlist;
                JSONObject jsonPlaylist = new JSONObject();

                final Path configFile = Minecraft.getInstance().gameDirectory.toPath().resolve("config/WaterPlayer/playlists/"+name+".json");
                try {
                    jsonPlaylist = new JSONObject(Files.readString(configFile));
                } catch (Exception ex){
                    WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
                }
                playlist = new PlaylistObject(jsonPlaylist);
                for(int i = 0; i<playlist.urls.size(); i++){
                    WaterPlayer.music.getTrackSearch().getTracks(playlist.urls.get(i));
                }
                Minecraft.getInstance().getToasts().addToast(new ControlToast(Localization.toText(
                        Localization.toString(Localization.getText("waterplayer.load.add.playlist"))
                                .replace("%playlist_name%", playlist.title)
                ), false));
            } else {
                WaterPlayer.music.getTrackSearch().getTracks(WaterPlayer.config.getString("LAST_REQUEST_MUSIC", ""));
                Minecraft.getInstance().getToasts().addToast(new ControlToast(Localization.getText("waterplayer.load.add"), false));
            }
        }else Minecraft.getInstance().getToasts().addToast(new ControlToast(Localization.getText("waterplayer.load.add.blank"), true));

    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f){
        if(this.minecraft.level != null){
            guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            renderDirtBackground(guiGraphics);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.drawCenteredString(this.font, this.title, width/2, 25, 0xffffff);
        super.render(guiGraphics, i, j, f);
    }
}
