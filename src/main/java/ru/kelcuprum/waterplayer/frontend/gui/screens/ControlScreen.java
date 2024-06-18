package ru.kelcuprum.waterplayer.frontend.gui.screens;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonSpriteBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.slider.SliderIntegerBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.base.EditBoxString;
import ru.kelcuprum.alinlib.gui.components.text.DescriptionBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.LyricsHelper;
import ru.kelcuprum.waterplayer.frontend.gui.components.CurrentTrackButton;
import ru.kelcuprum.waterplayer.frontend.gui.components.TrackButton;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class ControlScreen extends Screen {
    private final Screen parent;
    private final InterfaceUtils.DesignType designType = InterfaceUtils.DesignType.FLAT;

    public ControlScreen(Screen parent) {
        super(Localization.getText("waterplayer.name"));
        this.parent = parent;
    }

    @Override
    public void init() {
        initPanel();
        initList();
    }

    protected EditBoxString request;
    DescriptionBox lyrics;

    public void initPanel() {
        int x = 5;
        int size = 180;
        addRenderableWidget(new TextBox(x, 15, size, 9, Localization.getText("waterplayer.control"), true));
        request = new EditBoxString(x + 25, 40, size - 25, 20, designType, Localization.getText("waterplayer.load.url"));
        request.setMaxLength(Integer.MAX_VALUE);
        addRenderableWidget(request);
        addRenderableWidget(new ButtonSpriteBuilder(InterfaceUtils.Icons.RESET, (s) -> request.setValue(WaterPlayer.config.getString("LAST_REQUEST_MUSIC", "")))
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x, 40)
                .setDesignType(designType).build());
        addRenderableWidget(new Button(x, 65, size, 20, designType, Localization.getText("waterplayer.load.load"), (OnPress) -> WaterPlayer.player.loadMusic(request.getValue(), true)));
        addRenderableWidget(new SliderIntegerBuilder(Component.translatable("waterplayer.load.volume"), (onPress) -> {
            WaterPlayer.config.setNumber("CURRENT_MUSIC_VOLUME", onPress);
            WaterPlayer.player.getAudioPlayer().setVolume(onPress);
        }).setMax(100).setMin(0).setDefaultValue(WaterPlayer.config.getNumber("CURRENT_MUSIC_VOLUME", 3).intValue())
                .setPosition(x, 90)
                .setSize(size, 20)
                .build().setTypeInteger("%"));
        //

        this.lyrics = new DescriptionBox(x, 115, size, height - 150, Component.empty());
        this.lyrics.visible = false;
        addRenderableWidget(lyrics);

        //
        addRenderableWidget(new ButtonSpriteBuilder(InterfaceUtils.getResourceLocation("waterplayer", "textures/player/" + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause") + ".png"), (s) -> {
            WaterPlayer.player.getAudioPlayer().setPaused(!WaterPlayer.player.getAudioPlayer().isPaused());
            s.setIcon(InterfaceUtils.getResourceLocation("waterplayer", "textures/player/" + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause") + ".png"));
        })
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x, height - 30)
                .setDesignType(designType).build());
        addRenderableWidget(new ButtonSpriteBuilder(WaterPlayer.player.getTrackScheduler().getRepeatIcon(), (s) -> {
            WaterPlayer.player.getTrackScheduler().changeRepeatStatus();
            s.setIcon(WaterPlayer.player.getTrackScheduler().getRepeatIcon());
        })
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x + 100, height - 30)
                .setDesignType(designType).build());

        addRenderableWidget(new ButtonSpriteBuilder(InterfaceUtils.getResourceLocation("waterplayer", "textures/player/skip.png"), (s) -> {
            if (WaterPlayer.player.getTrackScheduler().queue.isEmpty() && WaterPlayer.player.getAudioPlayer().getPlayingTrack() == null)
                return;
            WaterPlayer.player.getTrackScheduler().nextTrack();
        })
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x + 25, height - 30)
                .setDesignType(designType).build());

        addRenderableWidget(new ButtonSpriteBuilder(InterfaceUtils.getResourceLocation("waterplayer", "textures/player/shuffle.png"), (s) -> {
            if (!WaterPlayer.player.getTrackScheduler().queue.isEmpty()) {
                WaterPlayer.player.getTrackScheduler().shuffle();
                rebuildWidgetsList();
            }
        })
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x + 50, height - 30)
                .setDesignType(designType).build());

        addRenderableWidget(new ButtonSpriteBuilder(InterfaceUtils.getResourceLocation("waterplayer", "textures/player/reset_queue.png"), (s) -> WaterPlayer.player.getTrackScheduler().queue.clear())
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x + 75, height - 30)
                .setDesignType(designType).build());
        addRenderableWidget(new Button(x + 125, height - 30, size - 125, 20, designType, CommonComponents.GUI_CANCEL, (OnPress) -> onClose()));
    }

    private ConfigureScrolWidget scroller;
    private List<AbstractWidget> widgets = new ArrayList<>();

    public void initList() {
        int x = 195;
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

        Queue<AudioTrack> queue = WaterPlayer.player.getTrackScheduler().queue;
        widgets.add(new TextBox(x, -20, width - 200, 20, Component.translatable("waterplayer.load.current_track"), true));
        widgets.add(new CurrentTrackButton(x, -42, width - 200, !WaterPlayer.config.getBoolean("SCREEN.QUEUE_COVER_SHOW", true), this));
        widgets.add(new TextBox(x, -20, width - 200, 20, Component.translatable(queue.isEmpty() ? "waterplayer.command.queue.blank" : "waterplayer.command.queue"), true));


        try {
            if (!queue.isEmpty()) {
                for (AudioTrack track : WaterPlayer.player.getTrackScheduler().queue) {
                    widgets.add(new TrackButton(x, -40, width - 200, track, this, !WaterPlayer.config.getBoolean("SCREEN.QUEUE_COVER_SHOW", true)));
                }
            }
        } catch (Exception ex) {
            lastCountQueue = 0;
            WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
        }
        addRenderableWidgets(widgets);
    }

    protected void addRenderableWidgets(@NotNull List<AbstractWidget> widgets) {
        for (AbstractWidget widget : widgets) {
            this.addRenderableWidget(widget);
        }
    }

    protected void rebuildWidgetsList() {
        removeWidget(scroller);
        scroller = null;
        for (AbstractWidget widget : widgets) removeWidget(widget);
        initList();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        InterfaceUtils.renderLeftPanel(guiGraphics, 190, height);
    }

    int lastCountQueue = WaterPlayer.player.getTrackScheduler().queue.size();
    AudioTrack lastTrack = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
    AudioLyrics audioLyrics;
    long lastCheck = System.currentTimeMillis();

    protected boolean isTrackEnable() {
        return WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null;
    }

    @Override
    public void tick() {
        if (scroller != null) scroller.onScroll.accept(scroller);
        if (isTrackEnable() && WaterPlayer.config.getBoolean("CONTROL.ENABLE_LYRICS", true)) {
            AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
            audioLyrics = LyricsHelper.getLyrics(track);
            if (audioLyrics != null) {
                List<AudioLyrics.Line> list = audioLyrics.getLines();
                if(list != null) {
                    StringBuilder builder = new StringBuilder();
                    for (AudioLyrics.Line line : list) {
                        if (((track.getPosition() / 1000) <= line.getTimestamp().getSeconds()) || ((track.getPosition() / 1000) >= line.getTimestamp().getSeconds() && (track.getPosition() / 1000) <= line.getTimestamp().getSeconds() + Objects.requireNonNull(line.getDuration()).getSeconds())) {
//                            AlinLib.log(line.getLine());
                            builder.append(line.getLine()).append("\n");
                        }
                    }
                    this.lyrics.setDescription(Component.literal(builder.toString()));
                    this.lyrics.visible = !builder.toString().isEmpty();
                } else this.lyrics.visible = false;
            } else this.lyrics.visible = false;
        }
        if (lastCountQueue != WaterPlayer.player.getTrackScheduler().queue.size()) {
            if (System.currentTimeMillis() - lastCheck >= 1500) {
                lastCheck = System.currentTimeMillis();
                this.lastCountQueue = WaterPlayer.player.getTrackScheduler().queue.size();
                this.lastTrack = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
                rebuildWidgetsList();
            }
        } else if (lastTrack != WaterPlayer.player.getAudioPlayer().getPlayingTrack()) {
            if (System.currentTimeMillis() - lastCheck >= 1500) {
                lastCheck = System.currentTimeMillis();
                this.lastTrack = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
                this.lastCountQueue = WaterPlayer.player.getTrackScheduler().queue.size();
                rebuildWidgetsList();
            }
        }
        super.tick();
    }

    @Override
    public void onFilesDrop(List<Path> list) {
        if (list.size() == 1) request.setValue(list.get(0).toString());
        else AlinLib.MINECRAFT.setScreen(new ConfirmLoadFiles(list, this));
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
