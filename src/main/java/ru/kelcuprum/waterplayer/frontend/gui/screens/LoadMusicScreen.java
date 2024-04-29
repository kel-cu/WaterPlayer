package ru.kelcuprum.waterplayer.frontend.gui.screens;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonSpriteBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.base.EditBoxString;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.components.CurrentTrackButton;
import ru.kelcuprum.waterplayer.frontend.gui.components.TrackButton;
import ru.kelcuprum.waterplayer.frontend.localization.Music;
import ru.kelcuprum.waterplayer.frontend.localization.StarScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class LoadMusicScreen extends Screen {
    private final Screen parent;
    private final InterfaceUtils.DesignType designType = InterfaceUtils.DesignType.FLAT;

    public LoadMusicScreen(Screen parent) {
        super(Localization.getText("waterplayer.name"));
        this.parent = parent;
    }

    @Override
    public void init() {
        initPanel();
        initList();
    }

    public void initPanel() {
        int x = 5;
        int size = 180;
        addRenderableWidget(new TextBox(x, 15, size, 9, Localization.getText("waterplayer.load"), true));
        EditBoxString request = new EditBoxString(x + 25, 60, size - 25, 20, designType, Localization.getText("waterplayer.load.url"));
        request.setMaxLength(Integer.MAX_VALUE);
        addRenderableWidget(request);
        addRenderableWidget(new ButtonSpriteBuilder(InterfaceUtils.Icons.RESET, (s) -> request.setValue(WaterPlayer.config.getString("LAST_REQUEST_MUSIC", "")))
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x, 60)
                .setDesignType(designType).build());
        addRenderableWidget(new Button(x, 85, size, 20, designType, Localization.getText("waterplayer.load.load"), (OnPress) -> WaterPlayer.player.loadMusic(request.getValue(), true)));

        //
        addRenderableWidget(new ButtonSpriteBuilder(new ResourceLocation("waterplayer", "textures/player/" + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause") + ".png"), (s) -> {
            WaterPlayer.player.getAudioPlayer().setPaused(!WaterPlayer.player.getAudioPlayer().isPaused());
            rebuildWidgets();
        })
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x, height - 30)
                .setDesignType(designType).build());

        addRenderableWidget(new ButtonSpriteBuilder(new ResourceLocation("waterplayer", "textures/player/skip.png"), (s) -> {
            if(WaterPlayer.player.getTrackScheduler().queue.isEmpty() && WaterPlayer.player.getAudioPlayer().getPlayingTrack() == null) return;
            WaterPlayer.player.getTrackScheduler().nextTrack();
        })
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x + 25, height - 30)
                .setDesignType(designType).build());

        addRenderableWidget(new ButtonSpriteBuilder(new ResourceLocation("waterplayer", "textures/player/shuffle.png"), (s) -> {
            if (!WaterPlayer.player.getTrackScheduler().queue.isEmpty()) {
                WaterPlayer.player.getTrackScheduler().shuffle();
                rebuildWidgets();
            }
        })
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x+50, height - 30)
                .setDesignType(designType).build());

        addRenderableWidget(new ButtonSpriteBuilder(new ResourceLocation("waterplayer", "textures/player/reset_queue.png"), (s) -> {
            WaterPlayer.player.getTrackScheduler().queue.clear();
        })
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x + 75, height - 30)
                .setDesignType(designType).build());
        //
//        addRenderableWidget(new Button(x, height - 55, size, 20, designType, Localization.getText("waterplayer.key.shuffle"), (OnPress) -> {
//        }));
        addRenderableWidget(new Button(x+100, height - 30, size-100, 20, designType, CommonComponents.GUI_CANCEL, (OnPress) -> onClose()));
    }

    private ConfigureScrolWidget scroller;
    private List<AbstractWidget> widgets = new ArrayList<>();

    public void initList() {
        widgets = new ArrayList<>();
        int x = 195;
        this.scroller = addRenderableWidget(new ConfigureScrolWidget(this.width - 8, 0, 4, this.height, Component.empty(), scroller -> {
            scroller.innerHeight = 5;
            for (AbstractWidget widget : widgets) {
                if (widget.visible) {
                    widget.setY((int) (scroller.innerHeight - scroller.scrollAmount()));
                    scroller.innerHeight += (widget.getHeight() + 5);
                } else widget.setY(-widget.getHeight());
            }
        }));
        addRenderableWidget(scroller);
        Queue<AudioTrack> queue = WaterPlayer.player.getTrackScheduler().queue;
        widgets.add(new TextBox(x, -20, width - 200, 20, Component.translatable("waterplayer.command.now_playing"), true));
        widgets.add(new CurrentTrackButton(x, -40, width - 200, this));
        widgets.add(new TextBox(x, -20, width - 200, 20, Component.translatable(queue.isEmpty() ? "waterplayer.command.queue.blank" : "waterplayer.command.queue"), true));
        int pos = 1;
        if (!queue.isEmpty()) {
            for (AudioTrack track : WaterPlayer.player.getTrackScheduler().queue) {
                if(WaterPlayer.config.getBoolean("SCREEN.QUEUE_COVER_SHOW", false)){
                    widgets.add(new TrackButton(x, -40, width - 200, track, this));
                } else {
                    StringBuilder builder = new StringBuilder();
                    if (!Music.isAuthorNull(track)) builder.append("«").append(Music.getAuthor(track)).append("» ");
                    builder.append(Music.getTitle(track)).append(" ").append(StarScript.getTimestamp(Music.getDuration(track)));
                    widgets.add(new TextBox(x, -10, width - 200, 10, Component.literal(builder.toString()), false, (s) -> {
                        if (track.getInfo().uri != null) TrackButton.confirmLinkNow(this, track.getInfo().uri);
                    }));
                }
                pos++;
            }
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

    int lastCountQueue = WaterPlayer.player.getTrackScheduler().queue.size();

    @Override
    public void tick() {
        if (scroller != null) scroller.onScroll.accept(scroller);
        if (lastCountQueue != WaterPlayer.player.getTrackScheduler().queue.size()) {
            this.lastCountQueue = WaterPlayer.player.getTrackScheduler().queue.size();
            rebuildWidgets();
        }
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
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
