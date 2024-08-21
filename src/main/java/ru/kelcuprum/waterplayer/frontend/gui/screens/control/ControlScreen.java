package ru.kelcuprum.waterplayer.frontend.gui.screens.control;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
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
import ru.kelcuprum.alinlib.gui.Icons;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.slider.SliderBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.EditBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.LyricsHelper;
import ru.kelcuprum.waterplayer.frontend.gui.components.CurrentTrackButton;
import ru.kelcuprum.waterplayer.frontend.gui.components.LyricsBox;
import ru.kelcuprum.waterplayer.frontend.gui.components.TrackButton;
import ru.kelcuprum.waterplayer.frontend.gui.screens.config.PlaylistsScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.search.SearchScreen;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static ru.kelcuprum.alinlib.gui.Icons.LIST;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.*;

public class ControlScreen extends Screen {
    private final Screen parent;

    public ControlScreen(Screen parent) {
        super(Component.translatable("waterplayer.control"));
        this.parent = parent;
    }

    @Override
    public void init() {
        initPanel();
        initList();
    }

    protected EditBox request;
    LyricsBox lyrics;

    public Button play;
    public Button repeat;
    public Button load;
    public Button shuffle;
    public String query = "";
    public void initPanel() {
        int x = 5;
        int size = 180;
        addRenderableWidget(new TextBox(x, 15, size, 9, title, true));
        request = (EditBox) new EditBoxBuilder(Component.translatable("waterplayer.load.url")).setSize(size-25, 20).setPosition(x+25, 40).build();
        request.setMaxLength(Integer.MAX_VALUE);
        request.setResponder((s) ->  query = s);
        request.setValue(query);
        addRenderableWidget(request);
        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.load.url.copy"), (buttonSprite) -> request.setValue(WaterPlayer.config.getString("LAST_REQUEST_MUSIC", "")))
                .setSprite(Icons.RESET)
                .setTextureSize(20, 20)
                .setSize(20, 20)
                .setPosition(x, 40)
                .build());
        Button bws = (Button) new ButtonBuilder(Component.translatable("waterplayer.control.search"), (e) -> AlinLib.MINECRAFT.setScreen(new SearchScreen(this)))
                .setSprite(SEARCH)
                .setTextureSize(20, 20)
                .setPosition(x, 65).setSize(20, 20)
                .build();
        addRenderableWidget(bws);
        this.load = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.load.load"), (e) -> WaterPlayer.player.loadMusic(query, true))
                .setPosition(x+25, 65)
                .setSize(size-25, 20).build());
        addRenderableWidget(new SliderBuilder(Component.translatable("waterplayer.load.volume"), (onPress) -> {
            WaterPlayer.config.setNumber("CURRENT_MUSIC_VOLUME", (int) (100*onPress));
            WaterPlayer.player.getAudioPlayer().setVolume((int) (100*onPress));
        }).setMax(100).setMin(0).setDefaultValue(WaterPlayer.config.getNumber("CURRENT_MUSIC_VOLUME", 3).intValue())
                .setValueType("%")
                .setPosition(x, 90)
                .setSize(size, 20)
                .build());

        //

        this.lyrics = new LyricsBox(x, 115, size, height - 170, Component.empty());
        this.lyrics.visible = false;
        addRenderableWidget(lyrics);

        //
        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.playlists"), (e) -> AlinLib.MINECRAFT.setScreen(PlaylistsScreen.build(this)))
                .setIcon(LIST)
                .setCentered(false)
                .setSize(size, 20).setPosition(x, height-50)
                .build());
        this.play = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control." + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause")), (e) -> {
            WaterPlayer.player.getAudioPlayer().setPaused(!WaterPlayer.player.getAudioPlayer().isPaused());
            e.builder.setTitle(Component.translatable("waterplayer.control." + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause")));
            ((ButtonBuilder)e.builder).setSprite(getPlayOrPause(WaterPlayer.player.getAudioPlayer().isPaused()));
        })
                .setSprite(getPlayOrPause(WaterPlayer.player.getAudioPlayer().isPaused()))
                .setTextureSize(20, 20)
                .setSize(20, 20)
                .setPosition(x, height - 25).build());
        this.repeat = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.repeat"), (e) -> {
            WaterPlayer.player.getTrackScheduler().changeRepeatStatus();
            ((ButtonBuilder)e.builder).setSprite(WaterPlayer.player.getTrackScheduler().getRepeatIcon());
        })
                .setSprite(WaterPlayer.player.getTrackScheduler().getRepeatIcon())
                .setTextureSize(20, 20)
                .setSize(20, 20)
                .setPosition(x + 100, height - 25).build());

        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.skip"), (e) -> {
            if (WaterPlayer.player.getTrackScheduler().queue.isEmpty() && WaterPlayer.player.getAudioPlayer().getPlayingTrack() == null)
                return;
            WaterPlayer.player.getTrackScheduler().nextTrack();
        })
                .setSprite(SKIP)
                .setTextureSize(20, 20)
                .setSize(20, 20)
                .setPosition(x + 25, height - 25)
                .build());

        this.shuffle = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.shuffle"), (e) -> {
            if (!WaterPlayer.player.getTrackScheduler().queue.isEmpty()) {
                WaterPlayer.player.getTrackScheduler().shuffle();
                rebuildWidgetsList();
            }
        })
                .setSprite(SHUFFLE)
                .setTextureSize(20, 20)
                .setSize(20, 20)
                .setPosition(x + 50, height - 25)
                .build());

        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.reset_queue"), (e) -> WaterPlayer.player.getTrackScheduler().queue.clear())
                .setSprite(RESET_QUEUE)
                .setTextureSize(20, 20)
                .setSize(20, 20)
                .setPosition(x + 75, height - 25)
                .build());
        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_CANCEL, (e) -> onClose())
                .setPosition(x+125, height-25).setSize(size-125, 20).build());
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
    //#if MC >=12002
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

    int lastCountQueue = WaterPlayer.player.getTrackScheduler().queue.size();
    AudioTrack lastTrack = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
    AudioLyrics audioLyrics;
    long lastCheck = System.currentTimeMillis();

    protected boolean isTrackEnable() {
        return WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_P && (modifiers & GLFW.GLFW_MOD_SHIFT) != 0){
            AlinLib.MINECRAFT.setScreen(new ModernControlScreen(parent));
            WaterPlayer.config.setBoolean("CONTROL.MODERN", true);
            return true;
        }
        if(getFocused() != null && getFocused() instanceof EditBox && keyCode == GLFW.GLFW_KEY_ENTER){
            load.onPress();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
                            Duration pos = Duration.ofMillis(track.getPosition());
                            if ((pos.toMillis() <= line.getTimestamp().toMillis()) || (pos.toMillis() >= line.getTimestamp().toMillis() && pos.toMillis() <= line.getTimestamp().toMillis() + line.getDuration().toMillis())) {
                                builder.append(line.getLine().replace("\r", "")).append("\n");
                            }
                        }
                    }
                    this.lyrics.setLyrics(Component.literal(builder.toString()));
                    this.lyrics.visible = !builder.toString().isEmpty();
                } else if (text != null) {
                    this.lyrics.setLyrics(Component.literal(text.replace("\r", "")));
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

    //#if MC >=12002
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scr = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if ((mouseX >= 5 && mouseX <= 185) && (mouseY >= 115 && mouseY <= height - 30)) {
            scr = lyrics.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }else if (!scr && scroller != null && mouseX > 190) {
            scr = scroller.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return scr;
    }
    //#elseif MC < 12002
    //$$ public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
    //$$     boolean scr = super.mouseScrolled(mouseX, mouseY, scrollY);
    //$$     if ((mouseX >= 5 && mouseX <= 185) && (mouseY >= 115 && mouseY <= height - 30)) {
    //$$         scr = lyrics.mouseScrolled(mouseX, mouseY, scrollY);
    //$$     }else if (!scr && scroller != null && mouseX > 190) {
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
