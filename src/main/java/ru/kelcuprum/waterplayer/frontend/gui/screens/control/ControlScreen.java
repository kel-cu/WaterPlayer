package ru.kelcuprum.waterplayer.frontend.gui.screens.control;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonSpriteBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonWithIconBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.slider.SliderIntegerBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.ButtonSprite;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.base.EditBoxString;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.LyricsHelper;
import ru.kelcuprum.waterplayer.frontend.gui.components.CurrentTrackButton;
import ru.kelcuprum.waterplayer.frontend.gui.components.LyricsBox;
import ru.kelcuprum.waterplayer.frontend.gui.components.TrackButton;
import ru.kelcuprum.waterplayer.frontend.gui.screens.config.PlaylistsScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.search.SearchScreen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Icons.LIST;

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
    LyricsBox lyrics;
    public Button play;
    public Button repeat;
    public Button load;
    public String query = "";
    public void initPanel() {
        int x = 5;
        int size = 180;
        addRenderableWidget(new TextBox(x, 15, size, 9, Localization.getText("waterplayer.control"), true));
        request = new EditBoxString(x + 25, 40, size - 25, 20, designType, Localization.getText("waterplayer.load.url"));
        request.setMaxLength(Integer.MAX_VALUE);
        request.setResponder((s) ->  query = s);
        request.setValue(query);
        addRenderableWidget(request);
        addRenderableWidget(new ButtonSpriteBuilder(InterfaceUtils.Icons.RESET, (buttonSprite) -> request.setValue(WaterPlayer.config.getString("LAST_REQUEST_MUSIC", "")))
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x, 40)
                .setDesignType(designType).build());
        ButtonSprite bws = new ButtonSprite(x, 65, 20, 20, InterfaceUtils.getResourceLocation("waterplayer", "textures/search.png"), Component.translatable("waterplayer.control.search"), (e) -> AlinLib.MINECRAFT.setScreen(new SearchScreen(this)));
//        bws.active = false;
        addRenderableWidget(bws);
        this.load = addRenderableWidget(new Button(x+25, 65, size-25, 20, designType, Localization.getText("waterplayer.load.load"), (e) -> WaterPlayer.player.loadMusic(query, true)));
        addRenderableWidget(new SliderIntegerBuilder(Component.translatable("waterplayer.load.volume"), (onPress) -> {
            WaterPlayer.config.setNumber("CURRENT_MUSIC_VOLUME", onPress);
            WaterPlayer.player.getAudioPlayer().setVolume(onPress);
        }).setMax(100).setMin(0).setDefaultValue(WaterPlayer.config.getNumber("CURRENT_MUSIC_VOLUME", 3).intValue())
                .setPosition(x, 90)
                .setSize(size, 20)
                .build().setTypeInteger("%"));

        //

        this.lyrics = new LyricsBox(x, 115, size, height - 170, Component.empty());
        this.lyrics.visible = false;
        addRenderableWidget(lyrics);

        //
        addRenderableWidget(new ButtonWithIconBuilder(Component.translatable("waterplayer.playlists"), LIST, (e) -> AlinLib.MINECRAFT.setScreen(new PlaylistsScreen().build(this)))
                .setSize(size, 20).setPosition(x, height-50)
                .setCentered(false).build());
        this.play = addRenderableWidget(new ButtonSpriteBuilder(InterfaceUtils.getResourceLocation("waterplayer", "textures/player/" + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause") + ".png"), (e) -> {
            WaterPlayer.player.getAudioPlayer().setPaused(!WaterPlayer.player.getAudioPlayer().isPaused());
            e.setMessage(Component.translatable("waterplayer.control." + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause")));
            e.setIcon(InterfaceUtils.getResourceLocation("waterplayer", "textures/player/" + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause") + ".png"));
        })
                .setTitle(Component.translatable("waterplayer.control." + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause")))
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x, height - 25)
                .setDesignType(designType).build());
        this.repeat = addRenderableWidget(new ButtonSpriteBuilder(WaterPlayer.player.getTrackScheduler().getRepeatIcon(), (e) -> {
            WaterPlayer.player.getTrackScheduler().changeRepeatStatus();
            e.setMessage(Component.translatable("waterplayer.control."+(WaterPlayer.player.getTrackScheduler().getRepeatStatus() == 0 ? "non_repeat" : WaterPlayer.player.getTrackScheduler().getRepeatStatus() == 1 ? "repeat" : "one_repeat" )));
            e.setIcon(WaterPlayer.player.getTrackScheduler().getRepeatIcon());
        })
                .setTitle(Component.translatable("waterplayer.control."+(WaterPlayer.player.getTrackScheduler().getRepeatStatus() == 0 ? "non_repeat" : WaterPlayer.player.getTrackScheduler().getRepeatStatus() == 1 ? "repeat" : "one_repeat" )))
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x + 100, height - 25)
                .setDesignType(designType).build());

        addRenderableWidget(new ButtonSpriteBuilder(InterfaceUtils.getResourceLocation("waterplayer", "textures/player/skip.png"), (e) -> {
            if (WaterPlayer.player.getTrackScheduler().queue.isEmpty() && WaterPlayer.player.getAudioPlayer().getPlayingTrack() == null)
                return;
            WaterPlayer.player.getTrackScheduler().nextTrack();
        })
                .setTitle(Component.translatable("waterplayer.control.skip"))
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x + 25, height - 25)
                .setDesignType(designType).build());

        addRenderableWidget(new ButtonSpriteBuilder(InterfaceUtils.getResourceLocation("waterplayer", "textures/player/shuffle.png"), (e) -> {
            if (!WaterPlayer.player.getTrackScheduler().queue.isEmpty()) {
                WaterPlayer.player.getTrackScheduler().shuffle();
                rebuildWidgetsList();
            }
        })
                .setTitle(Component.translatable("waterplayer.control.shuffle"))
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x + 50, height - 25)
                .setDesignType(designType).build());

        addRenderableWidget(new ButtonSpriteBuilder(InterfaceUtils.getResourceLocation("waterplayer", "textures/player/reset_queue.png"), (e) -> WaterPlayer.player.getTrackScheduler().queue.clear())
                .setTitle(Component.translatable("waterplayer.control.reset_queue"))
                .setSize(20, 20)
                .setTextureSize(20, 20)
                .setPosition(x + 75, height - 25)
                .setDesignType(designType).build());
        addRenderableWidget(new Button(x + 125, height - 25, size - 125, 20, designType, CommonComponents.GUI_CANCEL, (e) -> onClose()));
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
    public boolean keyPressed(int i, int j, int k) {
        if(getFocused() != null && getFocused() instanceof EditBox && i == GLFW.GLFW_KEY_ENTER){
            load.onPress();
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void tick() {
        if (scroller != null) scroller.onScroll.accept(scroller);
        if (isTrackEnable() && WaterPlayer.config.getBoolean("CONTROL.ENABLE_LYRICS", true)) {
            AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
            audioLyrics = LyricsHelper.getLyrics(track);
            if (audioLyrics != null) {
                List<AudioLyrics.Line> list = audioLyrics.getLines();
                String text = audioLyrics.getText();
                if (list != null) {
                    StringBuilder builder = new StringBuilder();
                    for (AudioLyrics.Line line : list) {
                        if (!(line.getDuration() == null)) {
                            if (((track.getPosition() / 1000) <= line.getTimestamp().getSeconds()) || ((track.getPosition() / 1000) >= line.getTimestamp().getSeconds() && (track.getPosition() / 1000) <= line.getTimestamp().getSeconds() + line.getDuration().getSeconds())) {
                                builder.append(line.getLine()).append("\n");
                            }
                        }
                    }
                    this.lyrics.setLyrics(Component.literal(builder.toString()));
                    this.lyrics.visible = !builder.toString().isEmpty();
                } else if (text != null) {
                    this.lyrics.setLyrics(Component.literal(text));
                    this.lyrics.visible = !text.isBlank();
                } else this.lyrics.visible = false;
            } else this.lyrics.visible = false;
        } else this.lyrics.visible = false;
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
        if ((mouseX >= 5 && mouseX <= 185) && (mouseY >= 115 && mouseY <= height - 30)) {
            scr = lyrics.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }else if (!scr && scroller != null && mouseX > 190) {
            scr = scroller.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return scr;
    }

    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
