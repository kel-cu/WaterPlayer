package ru.kelcuprum.waterplayer.frontend.gui.screens.playlist;

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
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonWTLBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.exception.WebPlaylistException;
import ru.kelcuprum.waterplayer.backend.playlist.WebPlaylist;
import ru.kelcuprum.waterplayer.frontend.gui.components.TrackButton;

import java.util.ArrayList;
import java.util.List;

public class WebPlaylistScreen extends Screen {
    private final WebPlaylist playlist;
    private final Screen parent;
    private List<AudioTrack> tracks = new ArrayList<>();

    public WebPlaylistScreen(Screen parent, WebPlaylist playlist) {
        super(Component.translatable("waterplayer.playlist"));
        this.parent = parent;
        this.playlist = playlist;
        new Thread(() -> tracks = playlist.getTracks()).start();
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
        addRenderableWidget(new ButtonWTLBuilder(Component.translatable("waterplayer.playlist.title"), Component.literal(playlist.playlist.title), null).setPosition(x, 40).setSize(size, 20).build().setActive(false));
        addRenderableWidget(new ButtonWTLBuilder(Component.translatable("waterplayer.playlist.author"), Component.literal(playlist.playlist.author), null).setPosition(x, 65).setSize(size, 20).build().setActive(false));
        addRenderableWidget(new Button(x, 90, size, 20, Component.translatable("waterplayer.playlist.play"), (s) -> WaterPlayer.player.loadMusic(String.format("wplayer:%s", playlist.url), false)));
        addRenderableWidget(new Button(x, 115, size, 20, Component.translatable("waterplayer.playlist.save"), (s) -> {
            try {
                playlist.save();
                AlinLib.MINECRAFT.setScreen(new PlaylistScreen(parent, playlist.playlist.fileName));
            } catch (WebPlaylistException e) {
                WaterPlayer.log((e.getMessage() == null ? e.getClass().getName() : e.getMessage()), Level.ERROR);
            }
        }));
        addRenderableWidget(new Button(x, height - 30, size, 20, CommonComponents.GUI_BACK, (e) -> onClose()));
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
        for (AudioTrack element : tracks) {
            TrackButton trackButton = new TrackButton(x, -50, width - 200, element, this, false);
            widgets.add(trackButton);
            i += trackButton.getHeight() + 5;
        }
        addRenderableWidgets(widgets);
    }

    protected void addRenderableWidgets(@NotNull List<AbstractWidget> widgets) {
        for (AbstractWidget widget : widgets) {
            this.addRenderableWidget(widget);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        InterfaceUtils.renderLeftPanel(guiGraphics, 190, height);
    }
    List<AudioTrack> lastTracks = new ArrayList<>();
    @Override
    public void tick() {
        if (scroller != null) scroller.onScroll.accept(scroller);
        if (lastTracks != tracks){
            lastTracks = tracks;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scr = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (!scr && scroller != null) {
            scr = scroller.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return scr;
    }

    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
