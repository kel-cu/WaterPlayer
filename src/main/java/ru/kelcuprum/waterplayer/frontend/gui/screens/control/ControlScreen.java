package ru.kelcuprum.waterplayer.frontend.gui.screens.control;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.selector.SelectorBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.EditBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.queue.AbstractQueue;
import ru.kelcuprum.waterplayer.frontend.gui.LyricsHelper;
import ru.kelcuprum.waterplayer.frontend.gui.components.LyricsBox;
import ru.kelcuprum.waterplayer.frontend.gui.components.TrackButton;
import ru.kelcuprum.waterplayer.frontend.gui.screens.TrackScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.config.PlaylistsScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.components.*;
import ru.kelcuprum.waterplayer.frontend.gui.screens.search.SearchScreen;
import ru.kelcuprum.waterplayer.frontend.gui.style.AirStyle;
import ru.kelcuprum.waterplayer.frontend.localization.MusicHelper;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.kelcuprum.alinlib.gui.Colors.BLACK_ALPHA;
import static ru.kelcuprum.alinlib.gui.Icons.*;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.*;

public class ControlScreen extends Screen {
    private final Screen parent;

    public ControlScreen(Screen parent) {
        super(Component.translatable("waterplayer.control"));
        this.parent = parent;
    }

    public boolean showControlPanel = true;

    // Init

    @Override
    protected void init() {
        if (showControlPanel) initControlPanel();
        initPlayerPanel();
        initQueue();
    }

    // - Control panel
    public final int controlPanelWidth = 180;
    public String value = "";
    public LyricsBox lyricsBox;
    public EditBox editBox;
    public Button load;

    public void initControlPanel() {
        int cWidth = controlPanelWidth - 10;
        int x = 10;
        int y = 35;
        addRenderableWidget(new TextBox(5, 5, cWidth, 20, title, true));
        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.load.url.copy"), (s) -> editBox.setValue(WaterPlayer.config.getString("LAST_REQUEST_MUSIC", "")))
                .setSprite(RESET)
                .setSize(20, 20)
                .setPosition(x, y).build());
        editBox = (EditBox) addRenderableWidget(new EditBoxBuilder(Component.translatable("waterplayer.load.url"))
                .setValue(value).setResponder((s) -> value = s)
                .setWidth(cWidth - 22).setPosition(x + 22, y)
                .build());
        y += 22;
        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.search"), (s) -> AlinLib.MINECRAFT.setScreen(new SearchScreen(this)))
                .setCentered(false)
                .setSprite(SEARCH).
                setSize(20, 20).setPosition(x, y)
                .build());
        load = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.load.load"), (e) -> WaterPlayer.player.loadMusic(value, true))
                .setSize(cWidth - 22, 20).setPosition(x + 22, y)
                .build());
        y += 22;
        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.playlists"), (s) -> AlinLib.MINECRAFT.setScreen(PlaylistsScreen.build(this)))
                .setCentered(false)
                .setIcon(LIST)
                .setSize(cWidth, 20).setPosition(x, y)
                .build());
        y += 22;
        if (WaterPlayer.config.getBoolean("EXPERIMENT.FILTERS", false)) {
            String[] speed = {"0.25x", "0.5x", "0.75x", "1x", "1.5x", "2x", "2.5x", "3x", "3.5x", "4x", "4.5x", "5x"};
            List<Double> speedD = List.of(0.25, 0.5, 0.75, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0);
            int pos = speedD.indexOf(WaterPlayer.player.speed);
            if (pos < 0) {
                pos = 3;
                WaterPlayer.player.speed = speedD.get(pos);
                WaterPlayer.config.setNumber("CURRENT_MUSIC_SPEED", WaterPlayer.player.speed);
                WaterPlayer.player.updateFilter();
            }
            addRenderableWidget(new SelectorBuilder(Component.translatable("waterplayer.control.speed")).setList(speed).setValue(pos)
                    .setOnPress((s) -> {
                        WaterPlayer.player.speed = speedD.get(s.getPosition());
                        WaterPlayer.config.setNumber("CURRENT_MUSIC_SPEED", WaterPlayer.player.speed);
                        WaterPlayer.player.updateFilter();
                    }).setPosition(x, y).setWidth(cWidth).build());

            y += 22;

            String[] pitch = {"0.25", "0.5", "0.75", "1", "1.25", "1.5", "1.75", "2"};
            List<Double> pitchD = List.of(0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0);
            int ppos = pitchD.indexOf(WaterPlayer.player.pitch);
            if (ppos < 0) {
                ppos = 3;
                WaterPlayer.player.pitch = speedD.get(ppos);
                WaterPlayer.config.setNumber("CURRENT_MUSIC_PITCH", WaterPlayer.player.pitch);
                WaterPlayer.player.updateFilter();
            }
            addRenderableWidget(new SelectorBuilder(Component.translatable("waterplayer.control.pitch")).setList(pitch).setValue(ppos)
                    .setOnPress((s) -> {
                        WaterPlayer.player.pitch = pitchD.get(s.getPosition());
                        WaterPlayer.config.setNumber("CURRENT_MUSIC_SPEED", WaterPlayer.player.speed);
                        WaterPlayer.player.updateFilter();
                    }).setPosition(x, y).setWidth(cWidth).build());

            y += 22;
        }

        lyricsBox = addRenderableWidget(new LyricsBox(x, y, cWidth, height - y - 55, Component.empty()));
        this.lyricsBox.visible = false;
    }

    // - Player panel
    public Button clear;
    public Button shuffle;
    public Button play;
    public Button skip;
    public Button repeat;
    public AirStyle nothingStyle = new AirStyle();
    public Button trackIcon;

    public void initPlayerPanel() {
        int x = 5;
        int y = height - 45;
        int size = width - 10;
        // - Left
        trackIcon = addRenderableWidget(new TrackIconButton(new ButtonBuilder(Component.empty(), (s) -> {
            AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
            if (track != null) AlinLib.MINECRAFT.setScreen(new TrackScreen(this, track));
        }).setSize(34, 34).setPosition(x + 3, y + 3)));

        trackIcon.visible = trackIcon.active = WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null;
        boolean isModern = WaterPlayer.config.getBoolean("CONTROL.MODERN_BUTTONS", true);
        // - Center
        int x$Buttons = x + (size / 2) - ((25 * 5 - 5) / 2);
        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.shuffle"), (e) -> {
            WaterPlayer.player.getTrackScheduler().shuffle();
            for (AbstractWidget widget : queue.widgets) removeWidget(widget);
            queue.resetWidgets();
            addQueue();
        })
                .setSprite(SHUFFLE)
                .setSize((isModern ? 16 : 20), (isModern ? 16 : 20))
                .setPosition(x$Buttons+(isModern ? 2 : 0), y + (isModern ? 5 : 3))
                .setStyle(isModern ? nothingStyle : GuiUtils.getSelected())
                .build());
        x$Buttons += 25;


        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.back"), (e) -> WaterPlayer.player.getTrackScheduler().backTrack())
                .setSprite(BACK)
                .setSize((isModern ? 16 : 20), (isModern ? 16 : 20))
                .setPosition(x$Buttons+(isModern ? 2 : 0), y + (isModern ? 5 : 3))
                .setStyle(isModern ? nothingStyle : GuiUtils.getSelected())
                .build());
        x$Buttons += 25;

        play = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control." + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause")), (e) -> {
            WaterPlayer.player.changePaused();
            e.builder.setTitle(Component.translatable("waterplayer.control." + (WaterPlayer.player.isPaused() ? "play" : "pause")));
            ((ButtonBuilder) e.builder).setSprite(getPlayOrPause(WaterPlayer.player.isPaused()));
        })
                .setSprite(getPlayOrPause(WaterPlayer.player.isPaused()))
                .setSize(20, 20)
                .setPosition(x$Buttons, y + 3)
                .build());
        x$Buttons += 25;
        skip = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.skip"), (e) -> {
            if (WaterPlayer.player.getTrackScheduler().queue.getQueue().isEmpty() && WaterPlayer.player.getAudioPlayer().getPlayingTrack() == null)
                return;
            WaterPlayer.player.getTrackScheduler().nextTrack();
        })
                .setSprite(SKIP)
                .setSize((isModern ? 16 : 20), (isModern ? 16 : 20))
                .setPosition(x$Buttons+(isModern ? 2 : 0), y + (isModern ? 5 : 3))
                .setStyle(isModern ? nothingStyle : GuiUtils.getSelected())
                .build());
        x$Buttons += 25;
        repeat = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.repeat"), (e) -> {
            WaterPlayer.player.getTrackScheduler().changeRepeatStatus();
            ((ButtonBuilder) e.builder).setSprite(WaterPlayer.player.getTrackScheduler().getRepeatIcon());
        })
                .setSprite(WaterPlayer.player.getTrackScheduler().getRepeatIcon())
                .setSize((isModern ? 16 : 20), (isModern ? 16 : 20))
                .setPosition(x$Buttons+(isModern ? 2 : 0), y + (isModern ? 5 : 3))
                .setStyle(isModern ? nothingStyle : GuiUtils.getSelected())
                .build());
        x$Buttons += 25;

        int timelineSize = Math.min(size / 3, 230);//190;
        addRenderableWidget(new TimelineComponent(x + (size / 2) - (timelineSize / 2), y + 30, timelineSize, 3, timelineSize >= 190));

        // - Left
        int clearPos = x + size - 12 - 70 - 26 - AlinLib.MINECRAFT.font.width("100%");
        if(clearPos > x$Buttons) clear = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.reset_queue"), (e) -> WaterPlayer.player.getTrackScheduler().reset())
                .setSprite(DONT)
                .setStyle(nothingStyle)
                .setSize(14, 14)
                .setPosition(clearPos, y + 13)
                .build());
        addRenderableWidget(new VolumeComponent(x + size - 12 - 70, y + 18, 70, 4));
    }

    // - You're just like pop music
    ConfigureScrolWidget queue;
    int lastCountQueue = WaterPlayer.player.getTrackScheduler().queue.getQueue().size();

    public void initQueue() {
        addRenderableWidget(new ButtonBuilder(Component.literal("x"), (s) -> onClose()).setPosition(width - 25, 5).setSize(20, 20).build());
        int queueWidth = width - 15 - controlPanelWidth;
        this.queue = addRenderableWidget(new ConfigureScrolWidget(10 + controlPanelWidth + queueWidth - 4, 30, 5, this.height - 80, Component.empty(), scroller -> {
            scroller.innerHeight = 0;
            for (AbstractWidget widget : scroller.widgets) {
                if (widget.visible) {
                    widget.setWidth(queueWidth);
                    widget.setY((int) (scroller.getY() + scroller.innerHeight - scroller.scrollAmount()));
                    scroller.innerHeight += (widget.getHeight() + 5);
                } else widget.setY(-widget.getHeight());
            }
            scroller.innerHeight -= 13;
        }));
        addQueue();
    }

    public AbstractQueue queueTracks = WaterPlayer.player.getTrackScheduler().queue;

    public void addQueue() {
        int queueWidth = width - 15 - controlPanelWidth;
        List<AbstractWidget> widgets = new ArrayList<>();
        int y = queue.getY();
        try {
            if (!queueTracks.getQueue().isEmpty()) {
                for (AudioTrack track : queueTracks.getQueue()) {
                    TrackButton tbutton = new TrackButton(10 + controlPanelWidth, y, queueWidth, track, this, !WaterPlayer.config.getBoolean("SCREEN.QUEUE_COVER_SHOW", true));
                    widgets.add(tbutton);
                    y += tbutton.getHeight() + 5;
                }
            }
        } catch (Exception ex) {
            lastCountQueue = 0;
            WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
        }
        addWidgetsToQueue(widgets);
    }
//        add
    // Render

    @Override
    //#if MC >=12002
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        //#elseif MC < 12002
        //$$ public void renderBackground(GuiGraphics guiGraphics) {
        //$$         super.renderBackground(guiGraphics);
        //#endif
        if (showControlPanel) renderControlPanel(guiGraphics);
        renderPlayerPanel(guiGraphics);
        renderQueueTitle(guiGraphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        //#if MC < 12002
        //$$     renderBackground(guiGraphics);
        //#endif
        super.render(guiGraphics, i, j, f);
        guiGraphics.enableScissor(10 + controlPanelWidth, 30, 10 + controlPanelWidth + (width - 15 - controlPanelWidth), this.height - 50);
        if (queue != null) for (AbstractWidget widget : queue.widgets) widget.render(guiGraphics, i, j, f);
        guiGraphics.disableScissor();
    }

    public void renderControlPanel(GuiGraphics guiGraphics) {
        int x = 5;
        int y = 5;

        guiGraphics.fill(x, y+25, x + controlPanelWidth, (lyricsBox.visible ? height - 50 : (WaterPlayer.config.getBoolean("EXPERIMENT.FILTERS", false) ? 148 : 104)), BLACK_ALPHA);
        guiGraphics.fill(x, y, x + controlPanelWidth, 25, BLACK_ALPHA);
    }

    public void renderPlayerPanel(GuiGraphics guiGraphics) {
        int x = 5;
        int y = height - 45;
        guiGraphics.fill(x, y, x + width - 10, y + 40, BLACK_ALPHA);

        if (isTrackEnable()) {
            guiGraphics.blit(
                    //#if MC >= 12102
                    RenderType::guiTextured,
                    //#endif
                    MusicHelper.getThumbnail(), x + 3, y + 3, 0f, 0f, 34, 34, 34, 34);
            int size = width - 10;
            int x$Buttons = x + (size / 2) - ((25 * 5 - 5) / 2) - 6;
            int x$Timeline = x + (size / 2) - (Math.min(size / 3, 230) / 2) - 6;
            int maxX = Math.min(x$Buttons, x$Timeline);
            renderString(guiGraphics, MusicHelper.getTitle(), x + 43, y + 8, maxX);
            renderString(guiGraphics, MusicHelper.getAuthor(), x + 43, y + 40 - 8 - AlinLib.MINECRAFT.font.lineHeight, maxX);
        }
    }

    public void renderQueueTitle(GuiGraphics guiGraphics) {
        guiGraphics.fill(10 + controlPanelWidth, 5, width - 30, 25, BLACK_ALPHA);
        int titleWidth = width - 40 - controlPanelWidth;
        Component component = Component.literal(queueTracks.getName());
        if(AlinLib.MINECRAFT.font.width(component) > titleWidth) renderScrollingString(guiGraphics, AlinLib.MINECRAFT.font, component, 10 + controlPanelWidth, width-30, 11, -1);
        else guiGraphics.drawCenteredString(AlinLib.MINECRAFT.font, component, 10 + controlPanelWidth + (titleWidth / 2), 11, -1);
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, Component message, int x, int maxX, int y, int color) {
        renderScrollingString(guiGraphics, font, message, x, y, maxX, y + font.lineHeight, color);
    }

    protected static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component component, int i, int j, int k, int l, int m) {
        renderScrollingString(guiGraphics, font, component, (i + k) / 2, i, j, k, l, m);
    }

    protected static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component component, int i, int j, int k, int l, int m, int n) {
        int o = font.width(component);
        int var10000 = k + m;
        Objects.requireNonNull(font);
        int p = (var10000 - 9) / 2 + 1;
        int q = l - j;
        int r;
        if (o > q) {
            r = o - q;
            double d = (double) Util.getMillis() / 1000.0;
            double e = Math.max((double) r * 0.5, 3.0);
            double f = Math.sin(1.5707963267948966 * Math.cos(6.283185307179586 * d / e)) / 2.0 + 0.5;
            double g = Mth.lerp(f, 0.0, (double) r);
            guiGraphics.enableScissor(j, k, l, m);
            guiGraphics.drawString(font, component, j - (int) g, p, n);
            guiGraphics.disableScissor();
        } else {
            r = Mth.clamp(i, j + o / 2, l - o / 2);
            guiGraphics.drawCenteredString(font, component, r, p, n);
        }

    }

    protected void renderString(GuiGraphics guiGraphics, String text, int x, int y, int maxX) {
        if (maxX - x < AlinLib.MINECRAFT.font.width(text)) {
            renderScrollingString(guiGraphics, AlinLib.MINECRAFT.font, Component.literal(text), x, maxX, y - 1, -1);
        } else {
            guiGraphics.drawString(AlinLib.MINECRAFT.font, text, x, y, -1);
        }
    }

    //

    public void addWidgetsToQueue(List<AbstractWidget> widgets) {
        for (AbstractWidget widget : widgets) addWidgetToQueue(widget);
    }

    public AbstractWidget addWidgetToQueue(AbstractWidget widget) {
        this.queue.addWidget(widget);
        return this.addWidget(widget);
    }

    //

    protected boolean isTrackEnable() {
        return WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null;
    }

    AudioTrack lastTrack = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
    AudioLyrics audioLyrics;
    long lastCheck = System.currentTimeMillis();

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        boolean st = true;
        GuiEventListener selected = null;
        for (GuiEventListener guiEventListener : this.children()) {
            if (queue != null && queue.widgets.contains(guiEventListener)) {
                if ((d >= 10 + controlPanelWidth && d <= width - 5) && (e >= 30 && e <= height - 50)) {
                    if (guiEventListener.mouseClicked(d, e, i)) {
                        st = false;
                        selected = guiEventListener;
                        break;
                    }
                }
            } else if (guiEventListener.mouseClicked(d, e, i)) {
                st = false;
                selected = guiEventListener;
                break;
            }
        }

        this.setFocused(selected);
        if (i == 0) {
            this.setDragging(true);
        }

        return st;
    }

    @Override
    public void tick() {
        if (queue != null) queue.onScroll.accept(queue);
        trackIcon.visible = trackIcon.active = WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null;
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
                            int type = WaterPlayer.config.getNumber("CONTROL.LYRICS.TYPE", 0).intValue();
                            Duration pos = Duration.ofMillis(track.getPosition());
                            if (type == 0) {
                                if (pos.toMillis() >= line.getTimestamp().toMillis() && pos.toMillis() <= line.getTimestamp().toMillis() + line.getDuration().toMillis())
                                    builder.append(line.getLine().replace("\r", "")).append("\n");
                                else builder.append("§7").append(line.getLine().replace("\r", "")).append("§r\n");
                            } else if (type == 1) {
                                if ((pos.toMillis() <= line.getTimestamp().toMillis()) || (pos.toMillis() >= line.getTimestamp().toMillis() && pos.toMillis() <= line.getTimestamp().toMillis() + line.getDuration().toMillis()))
                                    builder.append(line.getLine().replace("\r", "")).append("\n");
                            } else {
                                builder.append(line.getLine().replace("\r", "")).append("\n");
                            }
                        }
                    }
                    this.lyricsBox.setLyrics(Component.literal(builder.toString()));
                    this.lyricsBox.visible = !builder.toString().isEmpty();
                } else if (text != null) {
                    this.lyricsBox.setLyrics(Component.literal(text.replace("\r", "")));
                    this.lyricsBox.visible = !text.isBlank();
                } else this.lyricsBox.visible = false;
            } else this.lyricsBox.visible = false;
        } else this.lyricsBox.visible = false;
        if (queueTracks != WaterPlayer.player.getTrackScheduler().queue) queueTracks = WaterPlayer.player.getTrackScheduler().queue;
        if (lastCountQueue != WaterPlayer.player.getTrackScheduler().queue.getQueue().size()) {
            this.lastCountQueue = WaterPlayer.player.getTrackScheduler().queue.getQueue().size();
            this.lastTrack = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
            for (AbstractWidget widget : queue.widgets) removeWidget(widget);
            queue.resetWidgets();
            addQueue();
        } else if (lastTrack != WaterPlayer.player.getAudioPlayer().getPlayingTrack()) {
            this.lastTrack = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
            this.lastCountQueue = WaterPlayer.player.getTrackScheduler().queue.getQueue().size();
            for (AbstractWidget widget : queue.widgets) removeWidget(widget);
            queue.resetWidgets();
            addQueue();
        }
//        load.setActive(WaterPlayer.player.getTrackScheduler().queue.addTrackAvailable());
//        editBox.active = (WaterPlayer.player.getTrackScheduler().queue.addTrackAvailable());
        super.tick();
    }

    //#if MC >=12002
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scr = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        int queueWidth = width - 15 - controlPanelWidth;
        if ((mouseX >= lyricsBox.getX() && mouseX <= lyricsBox.getX() + lyricsBox.getWidth()) && (mouseY >= lyricsBox.getY() && mouseY <= lyricsBox.getY() + lyricsBox.getHeight())) {
            scr = lyricsBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        } else if (!scr && queue != null && (mouseX >= queue.getX() - queueWidth && mouseX <= queue.getX() + queue.getWidth()) && (mouseY >= queue.getY() && mouseY <= queue.getY() + queue.getHeight())) {
            scr = queue.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return scr;
    }
    //#elseif MC < 12002
    //$$ public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
    //$$     boolean scr = super.mouseScrolled(mouseX, mouseY, scrollY);
    //$$     int queueWidth = width - 15 - controlPanelWidth;
    //$$     if ((mouseX >= lyricsBox.getX() && mouseX <= lyricsBox.getX()+lyricsBox.getWidth()) && (mouseY >= lyricsBox.getY() && mouseY <= lyricsBox.getY()+lyricsBox.getHeight())) {
    //$$         scr = lyricsBox.mouseScrolled(mouseX, mouseY, scrollY);
    //$$     } else if (!scr && queue != null && (mouseX >= queue.getX()-queueWidth && mouseX <= queue.getX()+queue.getWidth()) && (mouseY >= queue.getY() && mouseY <= queue.getY()+queue.getHeight())) {
    //$$         scr = queue.mouseScrolled(mouseX, mouseY, scrollY);
    //$$     }
    //$$     return scr;
    //$$ }
    //#endif

    @Override
    public void onFilesDrop(List<Path> list) {
        if (list.size() == 1) editBox.setValue(list.get(0).toString());
        else AlinLib.MINECRAFT.setScreen(new ConfirmLoadFiles(list, this));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_P && (modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
            WaterPlayer.config.setBoolean("CONTROL.MODERN_BUTTONS", !WaterPlayer.config.getBoolean("CONTROL.MODERN_BUTTONS", true));
            rebuildWidgets();
            return true;
        }
        if (getFocused() != null && getFocused() instanceof EditBox && keyCode == GLFW.GLFW_KEY_ENTER) {
            load.onPress();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        AlinLib.MINECRAFT.setScreen(parent);
    }
}
