package ru.kelcuprum.waterplayer.frontend.gui.screens.playlist;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.Colors;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.ImageWidget;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.AbstractConfigScreen;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.MusicPlayer;
import ru.kelcuprum.waterplayer.backend.WaterPlayerAPI;
import ru.kelcuprum.waterplayer.backend.exception.WebPlaylistException;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;
import ru.kelcuprum.waterplayer.backend.playlist.WebPlaylist;
import ru.kelcuprum.waterplayer.backend.queue.PlaylistQueue;
import ru.kelcuprum.waterplayer.frontend.gui.TextureHelper;
import ru.kelcuprum.waterplayer.frontend.gui.components.TrackButton;
import ru.kelcuprum.waterplayer.frontend.gui.screens.config.PlaylistsScreen;

import java.util.ArrayList;
import java.util.List;

import static ru.kelcuprum.alinlib.gui.Icons.*;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.NO_PLAYLIST_ICON;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.PLAY;

public class ViewPlaylistScreen extends Screen {
    @NotNull
    protected final Playlist playlist;
    protected WebPlaylist webPlaylist;
    private List<AbstractWidget> trackWidgets = new ArrayList<>();
    public final Screen parent;

    // Share
    boolean isCreatedLink = false;
    String link = "";
    //
    boolean isEnable = false;
    boolean isInit = false;

    public ViewPlaylistScreen(Screen parent, WebPlaylist webPlaylist) {
        this(parent, webPlaylist.playlist);
        this.webPlaylist = webPlaylist;
        this.isCreatedLink = true;
        this.link = String.format(WaterPlayerAPI.config.getString("PLAYLIST_URL", WaterPlayerAPI.getURL("/playlist/%s")), webPlaylist.url);
    }

    public ViewPlaylistScreen(Screen parent, @NotNull Playlist playlist) {
        super(Component.empty());
        this.playlist = playlist;
        this.parent = parent;
    }

    @Override
    protected void init() {
        initPanel();
        initTracks();
        if (!isInit) {
            isInit = true;
            if (isCreatedLink) isEnable = true;
            else new Thread(() -> isEnable = WaterPlayerAPI.isPlaylistUploadEnable()).start();
            new Thread(() -> trackWidgets = getTrackWidgets(playlist)).start();
        }
    }

    public Button upload;
    public ImageWidget icon;

    public void initPanel() {
        int x = 10;
        int size = 200;
        addRenderableWidget(new TextBox(x, 5, size, 20, Component.translatable(webPlaylist == null ? "waterplayer.playlist" : "waterplayer.playlist.web"), true));
        int y = 35;
        icon = addRenderableWidget(new ImageWidget(x, y, 36, 36, getIcon(), 36, 36, Component.empty()));
        addRenderableWidget(new TextBox(x + 41, y, size - 41, 18, Component.literal(playlist.title), false));
        addRenderableWidget(new TextBox(x + 41, y + 18, size - 41, 18, Component.literal(playlist.author), false));
        y += 41;
        if (webPlaylist == null) {
            addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.playlist.edit_button"), (e) -> {
                isInit = isEnable = false;
                trackWidgets = new ArrayList<>();
                AlinLib.MINECRAFT.setScreen(new EditPlaylistScreen(this, playlist));
            }).setPosition(x, y).setSize(size, 20).build());
        } else {
            addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.playlist.save"), (s) -> {
                try {
                    webPlaylist.save();
                } catch (WebPlaylistException e) {
                    e.printStackTrace();
                    WaterPlayer.getToast().setMessage(Component.literal(e.getMessage() == null ? e.getClass().getName() : e.getMessage()))
                            .setType(ToastBuilder.Type.ERROR).buildAndShow();
                }
                AlinLib.MINECRAFT.setScreen(new ViewPlaylistScreen(parent, playlist));
            }).setPosition(x, y).setSize(size, 20).build());
        }
        y += 25;
        upload = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable(isCreatedLink ? "waterplayer.playlist.copy_link" : "waterplayer.playlist.upload"), (e) -> {
            if (isCreatedLink) {
                AlinLib.MINECRAFT.keyboardHandler.setClipboard(link);
                WaterPlayer.getToast().setMessage(Component.translatable("waterplayer.playlist.link_copied")).buildAndShow();
            } else {
                try {
                    link = WaterPlayerAPI.uploadPlaylist(playlist, playlist.fileName);
                    AlinLib.MINECRAFT.keyboardHandler.setClipboard(link);
                    isCreatedLink = true;
                    WaterPlayer.getToast().setMessage(Component.translatable("waterplayer.playlist.uploaded")).buildAndShow();
                    e.builder.setTitle(Component.translatable("waterplayer.playlist.copy_link"));
                } catch (Exception ex) {
                    isEnable = false;
                    e.builder.setTitle(Component.translatable("waterplayer.playlist.upload.unavailable"));
                    String msg = ex.getMessage() == null ? e.getClass().getName() : ex.getMessage();
                    WaterPlayer.getToast().setMessage(Component.literal(msg)).setType(ToastBuilder.Type.ERROR).setIcon(DONT).buildAndShow();
                    WaterPlayer.log(msg, Level.ERROR);
                }
            }
        }).setPosition(x, y).setSize(size, 20).setActive(false).build());
        y += 25;

        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_BACK, (e) -> onClose()).setPosition(x, y).setSize(size - 25, 20).build());
        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.playlist.play"), (s) -> {
//            WaterPlayer.player.loadMusic(webPlaylist == null ? String.format("playlist:%s", playlist.fileName) : String.format("wplayer:%s", webPlaylist.url), false);
            WaterPlayer.player.getTrackScheduler().changeQueue(new PlaylistQueue(this.playlist));
        })
                .setSprite(PLAY).setPosition(x + size - 20, y).setSize(20, 20).build());
    }

    private ConfigureScrolWidget scroller;
    private List<AbstractWidget> widgets = new ArrayList<>();
    private TextBox emptyTracks;
    public void initTracks() {
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
        int x = 220;
        widgets.add(new TextBox(x, 5, width - 225, 20, Component.translatable("waterplayer.playlist.tracks"), true));
        int i = 30;
        for (AbstractWidget element : trackWidgets) {
            element.setPosition(x, i);
            element.setWidth(width - 225);
            widgets.add(element);
            i += element.getHeight() + 5;
        }
        emptyTracks = addWidget(new TextBox(x, 76, width-225, 20, Component.translatable("waterplayer.playlist.is_empty"), false));
        emptyTracks.setActive(false);
        emptyTracks.visible = isClownfishMoment();
        addRenderableWidgets(widgets);
    }

    protected void addRenderableWidgets(@NotNull List<AbstractWidget> widgets) {
        for (AbstractWidget widget : widgets) {
            this.addRenderableWidget(widget);
        }
    }
    public boolean isClownfishMoment(){
        return trackWidgets.isEmpty() && webPlaylist == null && isInit;
    }

    public static MusicPlayer searchPlayer = new MusicPlayer();

    public List<AbstractWidget> getTrackWidgets(Playlist playlist) {
        List<AbstractWidget> trackWidgets = new ArrayList<>();
        for (String url : playlist.urls) {
            final Screen thisScreen = this;
            if (!url.isBlank()) searchPlayer.getAudioPlayerManager().loadItemSync(url, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    trackWidgets.add(new TrackButton(195, -50, width - 200, track, thisScreen, false));
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    for (AudioTrack track : playlist.getTracks())
                        trackWidgets.add(new TrackButton(195, -50, width - 200, track, thisScreen, false));
                }

                @Override
                public void noMatches() {
                    trackWidgets.add(new ButtonBuilder(Component.literal(url)).setOnPress((s) -> WaterPlayer.confirmLinkNow(thisScreen, url)).setCentered(false).setIcon(MUSIC).setPosition(195, -50).setWidth(width - 200).build());
                    WaterPlayer.log("Nothing Found by " + url, Level.WARN);
                }

                @Override
                public void loadFailed(FriendlyException ex) {
                    trackWidgets.add(new ButtonBuilder(Component.literal(url)).setOnPress((s) -> WaterPlayer.confirmLinkNow(thisScreen, url)).setCentered(false).setIcon(MUSIC).setPosition(195, -50).setWidth(width - 200).build());
                    WaterPlayer.log("ERROR: " + (ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage()), Level.DEBUG);
                }
            });
        }
        return trackWidgets;
    }

    // - icon

    public ResourceLocation getIcon() {
        if (playlist == null) return NO_PLAYLIST_ICON;
        return playlist.icon == null ? NO_PLAYLIST_ICON : TextureHelper.getTexture$Base64(playlist.icon, webPlaylist == null ? String.format("playlist-%s", playlist.fileName) : String.format("webplaylist-%s", webPlaylist.url));
    }

    // - Tick
    public ResourceLocation lastIcon = getIcon();
    List<AbstractWidget> lastTracks = new ArrayList<>();
    private int lastSize = 0;

    @Override
    public void tick() {
        emptyTracks.setActive(isClownfishMoment());
        if (lastIcon != getIcon()) {
            lastIcon = getIcon();
            removeWidget(icon);
            icon = addRenderableWidget(new ImageWidget(10, 35, 36, 36, getIcon(), 36, 36, Component.empty()));
        }
        if (scroller != null) scroller.onScroll.accept(scroller);
        if (isEnable) {
            upload.setActive(true);
            upload.setMessage(Component.translatable(isCreatedLink ? "waterplayer.playlist.copy_link" : "waterplayer.playlist.upload"));
        } else {
            upload.setActive(false);
            upload.setMessage(Component.translatable("waterplayer.playlist.upload.unavailable"));
        }
        if (lastTracks != trackWidgets) {
            lastTracks = trackWidgets;
            rebuildWidgetsList();
        }
        if (lastSize != playlist.getUrlsJSON().size()) {
            if (getFocused() == null || !(getFocused().isFocused() && (getFocused() instanceof EditBox))) {
                new Thread(() -> trackWidgets = getTrackWidgets(playlist)).start();
                lastSize = playlist.getUrlsJSON().size();
                rebuildWidgetsList();
            }
        }
        super.tick();
    }

    protected void rebuildWidgetsList() {
        removeWidget(scroller);
        scroller = null;
        for (AbstractWidget widget : widgets) {
            removeWidget(widget);
        }
        initTracks();
    }

    // - RENDER

    //#if MC >= 12002
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        //#elseif MC < 12002
        //$$ public void renderBackground(GuiGraphics guiGraphics) {
        //$$         super.renderBackground(guiGraphics);
        //#endif
        if(isClownfishMoment()){
            int clownfishSize = Math.min(height-96, width-230);
            guiGraphics.blit(
                    //#if MC >= 12102
                    RenderType::guiTextured,
                    //#endif
                    CLOWNFISH, 220, height-clownfishSize, 0.0f, 0.0f, clownfishSize, clownfishSize, clownfishSize, clownfishSize
            );
        }
        guiGraphics.fill(5, 5, 215, 25, Colors.BLACK_ALPHA);
        guiGraphics.fill(5, 30, 215, 151, Colors.BLACK_ALPHA);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
    //#if MC < 12002
    //$$     renderBackground(guiGraphics);
    //#endif
        super.render(guiGraphics, i, j, f);
        if(isClownfishMoment()) emptyTracks.render(guiGraphics, i, j, f);
    }

    //#if MC >=12002
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

    // - Close
    public void onClose() {
        if (parent instanceof AbstractConfigScreen) PlaylistsScreen.assetsSize = 0;
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
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
}
