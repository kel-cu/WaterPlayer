package ru.kelcuprum.waterplayer.frontend.gui.screens;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.buttons.ButtonSprite;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.base.EditBoxString;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Icons.REMOVE;
import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Icons.RESET;

public class PlaylistScreen extends Screen {
    private Playlist playlist;
    private final String playlistName;
    JsonObject jsonPlaylist = new JsonObject();
    private final Screen parent;
    public PlaylistScreen(Screen parent, String playlistName) {
        super(Component.translatable("waterplayer.playlist"));
        this.parent = parent;
        this.playlistName = playlistName;
    }
    private final InterfaceUtils.DesignType designType = InterfaceUtils.DesignType.FLAT;
    Path playlistFile;
    boolean isDeleted = false;
    @Override
    protected void init() {
        assert this.minecraft != null;
        playlistFile = this.minecraft.gameDirectory.toPath().resolve("config/WaterPlayer/playlists/"+ playlistName +".json");
        try {
            jsonPlaylist = GsonHelper.parse(Files.readString(playlistFile));
        } catch (Exception ex){
            WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
        }
        playlist = new Playlist(jsonPlaylist);
        initPanel();
        initContent();
    }
    public void initPanel(){
        int x = 5;
        int size = 180;
        addRenderableWidget(new TextBox(x, 15, size, 9, title, true));

        addRenderableWidget(new EditBoxString(x, 40, size, 20, false, playlist.title, designType, Component.translatable("waterplayer.playlist.title"), (s) -> {
            playlist.title = s;
            save();
        }));

        addRenderableWidget(new EditBoxString(x, 65, size, 20, false, playlist.author, designType, Component.translatable("waterplayer.playlist.author"), (s) -> {
            playlist.author = s;
            save();
        }));
        addRenderableWidget(new TextBox(x, 90, size, 20, Localization.toText(String.format(WaterPlayer.localization.getLocalization("playlist.description"), playlistName)), true));
        addRenderableWidget(new Button(x, height-30, size-75, 20, designType, CommonComponents.GUI_BACK, (s) -> onClose()));
        addRenderableWidget(new ButtonSprite(x+size-70, height-30, 20, 20, designType, new InterfaceUtils().getResourceLocation("waterplayer", "textures/player/play.png"), Localization.getText("waterplayer.playlist.play"), (OnPress) -> {
            save();
            WaterPlayer.player.loadMusic(String.format("playlist:%s", playlistName), true);
            onClose();
        }));
        addRenderableWidget(new ButtonSprite(x+size-45, height-30, 20, 20, designType, new InterfaceUtils().getResourceLocation("waterplayer", "textures/player/reset_queue.png"), Localization.getText("waterplayer.playlist.remove"), (OnPress) -> {
            isDeleted = true;
            playlistFile.toFile().delete();
            onClose();
        }));
        addRenderableWidget(new ButtonSprite(x+size-20, height-30, 20, 20, designType, RESET, Localization.getText("waterplayer.playlist.reload"), (OnPress) -> {
           save();
           rebuildWidgets();
        }));
    }
    private ConfigureScrolWidget scroller;
    private List<AbstractWidget> widgets = new ArrayList<>();
    public void initContent(){
        widgets = new ArrayList<>();
        this.scroller = addRenderableWidget(new ConfigureScrolWidget(this.width - 8, 0, 4, this.height, Component.empty(), scroller -> {
            scroller.innerHeight = 5;
            for(AbstractWidget widget : widgets){
                if(widget.visible){
                    widget.setY((int) (scroller.innerHeight - scroller.scrollAmount()));
                    scroller.innerHeight += (widget.getHeight()+5);
                } else widget.setY(-widget.getHeight());
            }
        }));
        int x = 195;
        widgets.add(new TextBox(x, 5, width-200, 20, Component.translatable("waterplayer.playlist.urls"), true));
        int i = 0;
        for(String url : playlist.urls){
            int finalI = i;
            widgets.add(new EditBoxString(x, -20, width-200, 20, false, url, designType, Component.literal(String.format("%s. ", i)), (s) -> {
                playlist.urls.set(finalI, s);
                save();
            }));
            i++;
        }
        widgets.add(new Button(x, -20, width-200, 20, designType, Component.translatable("waterplayer.playlist.add"), (s) -> {
           playlist.urls.add("https://c418.bandcamp.com/track/strad");
           save();
           rebuildWidgets();
        }));
        addRenderableWidgets(widgets);
    }
    protected void addRenderableWidgets(@NotNull List<AbstractWidget> widgets) {

        for (AbstractWidget widget : widgets) {
            this.addRenderableWidget(widget);
        }

    }
    private void save(){
        Minecraft CLIENT = Minecraft.getInstance();
        final Path configFile = CLIENT.gameDirectory.toPath().resolve("config/WaterPlayer/playlists/"+ playlistName +".json");
        try {
            Files.createDirectories(configFile.getParent());
            Files.writeString(configFile, playlist.toJSON().toString());
        } catch (IOException e) {
            WaterPlayer.log(e.getLocalizedMessage(), Level.ERROR);
        }
    }
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f){
        super.renderBackground(guiGraphics, i, j, f);
        InterfaceUtils.renderLeftPanel(guiGraphics, 190, height);
    }
    @Override
    public void tick(){
        if(scroller != null) scroller.onScroll.accept(scroller);
        super.tick();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scr = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (!scr && scroller != null) {
            scr = scroller.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return scr;
    }
    public void onClose() {
        if(!isDeleted) save();
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
