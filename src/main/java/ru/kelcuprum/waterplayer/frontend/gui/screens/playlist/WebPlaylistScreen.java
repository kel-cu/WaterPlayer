package ru.kelcuprum.waterplayer.frontend.gui.screens.playlist;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.Colors;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.exception.WebPlaylistException;
import ru.kelcuprum.waterplayer.backend.playlist.WebPlaylist;
import ru.kelcuprum.waterplayer.frontend.gui.components.TrackButton;

import java.util.ArrayList;
import java.util.List;

import static ru.kelcuprum.alinlib.gui.Icons.MUSIC;

public class WebPlaylistScreen extends Screen {
    private final WebPlaylist playlist;
    private final Screen parent;
    private List<AbstractWidget> trackWidgets = new ArrayList<>();

    public WebPlaylistScreen(Screen parent, WebPlaylist playlist) {
        super(Component.translatable("waterplayer.playlist"));
        this.parent = parent;
        this.playlist = playlist;
        new Thread(() -> trackWidgets = getTrackWidgets(playlist)).start();
    }

    public List<AbstractWidget> getTrackWidgets(WebPlaylist webplaylist){
        List<AbstractWidget> trackWidgets = new ArrayList<>();
        for (String url : webplaylist.playlist.urls) {
            final Screen thisScreen = this;
            WaterPlayer.player.getAudioPlayerManager().loadItemSync(url, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    trackWidgets.add(new TrackButton(195, -50, width - 200, track, thisScreen, false));
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    for(AudioTrack track : playlist.getTracks())
                        trackWidgets.add(new TrackButton(195, -50, width - 200, track, thisScreen, false));
                }

                @Override
                public void noMatches() {
                    trackWidgets.add(new ButtonBuilder(Component.literal(url)).setOnPress((s) -> WaterPlayer.confirmLinkNow(thisScreen, url)).setCentered(false).setIcon(MUSIC).setPosition(195, -50).setWidth(width-200).build());
                    WaterPlayer.log("Nothing Found by " + url, Level.WARN);
                }

                @Override
                public void loadFailed(FriendlyException ex) {
                    trackWidgets.add(new ButtonBuilder(Component.literal(url)).setOnPress((s) -> WaterPlayer.confirmLinkNow(thisScreen, url)).setCentered(false).setIcon(MUSIC).setPosition(195, -50).setWidth(width-200).build());
                    WaterPlayer.log("ERROR: "+(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage()), Level.DEBUG);
                }
            });
        }
        return trackWidgets;
    }

    @Override
    protected void init() {
        initPanel();
        initList();
    }

    public Button upload;
    public TextBox desc;

    public void initPanel() {
        int x = 5;
        int size = 180;
        addRenderableWidget(new TextBox(x, 15, size, 9, title, true));
        //x, 40, size, 20,
        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.playlist.title"), Component.literal(playlist.playlist.title), null).setPosition(x, 40).setSize(size, 20).setActive(false).build());
        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.playlist.author"), Component.literal(playlist.playlist.author), null).setPosition(x, 65).setSize(size, 20).setActive(false).build());
        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.playlist.play"), (s) -> WaterPlayer.player.loadMusic(String.format("wplayer:%s", playlist.url), false)).setPosition(x, 90).setSize(size, 20).build());
        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.playlist.save"), (s) -> {
            try {
                playlist.save();
                AlinLib.MINECRAFT.setScreen(new PlaylistScreen(parent, playlist.playlist.fileName));
            } catch (WebPlaylistException e) {
                WaterPlayer.log((e.getMessage() == null ? e.getClass().getName() : e.getMessage()), Level.ERROR);
            }
        }).setPosition(x, 115).setSize(size, 20).build());
        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_BACK, (e) -> onClose()).setPosition(x, height - 30).setSize(size, 20).build());
    }

    private ConfigureScrolWidget scroller;
    private List<AbstractWidget> widgets = new ArrayList<>();

    public void initList() {
        widgets = new ArrayList<>();
        this.scroller = addRenderableWidget(new ConfigureScrolWidget(this.width - 8, 0, 4, this.height, Component.empty(), scroller -> {
            scroller.innerHeight = 5;
            for (AbstractWidget widget : widgets) {
                if (widget.visible) {
                    widget.setY((int) (scroller.innerHeight - scroller.scrollAmount()));
                    scroller.innerHeight += (widget.getHeight() + 5);
                } else widget.setY(-widget.getHeight());
            }
        }));
        int x = 195;
        widgets.add(new TextBox(x, 5, width - 200, 20, Component.translatable("waterplayer.playlist.tracks"), true));
        int i = 30;
        for (AbstractWidget element : trackWidgets) {
            element.setPosition(x, i);
            element.setWidth(width-200);
            widgets.add(element);
            i += element.getHeight() + 5;
        }
        addRenderableWidgets(widgets);
    }

    protected void addRenderableWidgets(@NotNull List<AbstractWidget> widgets) {
        for (AbstractWidget widget : widgets) {
            this.addRenderableWidget(widget);
        }
    }

    @Override
    //#if MC >= 12002
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        //#elseif MC < 12002
        //$$ public void renderBackground(GuiGraphics guiGraphics) {
        //$$         super.renderBackground(guiGraphics);
        //#endif
        guiGraphics.fill(0, 0, 190, height, Colors.BLACK_ALPHA);
    }

    //#if MC < 12002
    //$$ @Override
    //$$ public void render(GuiGraphics guiGraphics, int i, int j, float f) {
    //$$     renderBackground(guiGraphics);
    //$$     super.render(guiGraphics, i, j, f);
    //$$ }
    //#endif

    List<AbstractWidget> lastTracks = new ArrayList<>();
    @Override
    public void tick() {
        if (scroller != null) scroller.onScroll.accept(scroller);
        if (lastTracks != trackWidgets){
            lastTracks = trackWidgets;
            rebuildWidgetsList();
        }
        super.tick();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == GLFW.GLFW_KEY_ESCAPE) {
            if (getFocused() != null && getFocused().isFocused()) {
                getFocused().setFocused(false);
                return true;
            }
        }
        return super.keyPressed(i, j, k);
    }

    protected void rebuildWidgetsList() {
        removeWidget(scroller);
        scroller = null;
        for (AbstractWidget widget : widgets) {
            removeWidget(widget);
        }
        initList();
    }

    @Override
    //#if MC >= 12002
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scr = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (!scr && scroller != null) {
            scr = scroller.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return scr;
    }
    //#elseif MC < 12002
    //$$ public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
    //$$     boolean scr = super.mouseScrolled(mouseX, mouseY, scrollY);
    //$$     if (!scr && scroller != null) {
    //$$         scr = scroller.mouseScrolled(mouseX, mouseY, scrollY);
    //$$     }
    //$$     return scr;
    //$$ }
    //#endif

    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
