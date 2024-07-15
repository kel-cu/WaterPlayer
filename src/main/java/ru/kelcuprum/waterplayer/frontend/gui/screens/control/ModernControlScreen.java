package ru.kelcuprum.waterplayer.frontend.gui.screens.control;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.EditBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.LyricsHelper;
import ru.kelcuprum.waterplayer.frontend.gui.components.LyricsBox;
import ru.kelcuprum.waterplayer.frontend.gui.components.TrackButton;
import ru.kelcuprum.waterplayer.frontend.gui.screens.TrackScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.config.PlaylistsScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.components.ConfigureScrolWidget;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.components.TimelineComponent;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.components.TrackIconButton;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.components.VolumeComponent;
import ru.kelcuprum.waterplayer.frontend.gui.screens.search.SearchScreen;
import ru.kelcuprum.waterplayer.frontend.localization.Music;

import java.time.Duration;
import java.util.*;

import static ru.kelcuprum.alinlib.gui.Colors.BLACK_ALPHA;
import static ru.kelcuprum.alinlib.gui.Icons.*;

public class ModernControlScreen extends Screen {
    private final Screen parent;

    public ModernControlScreen(Screen parent) {
        super(Component.translatable("waterplayer.control"));
        this.parent = parent;
    }

    public boolean showControlPanel = true;

    // Init

    @Override
    protected void init() {
        if(WaterPlayer.config.getBoolean("CONTROL.MODERN.FIRST_RUN", true)) {
            WaterPlayer.getToast().setMessage(Component.translatable("waterplayer.control.modern.notice")).setDisplayTime(15000).setIcon(Items.NETHER_STAR).show(AlinLib.MINECRAFT.getToasts());
            WaterPlayer.config.setBoolean("CONTROL.MODERN.FIRST_RUN", false);
        }
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
        int y = 10;
        addRenderableWidget(new TextBox(x, y, cWidth, 20, title, true));
        y += 22;
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
                .setSprite(GuiUtils.getResourceLocation("waterplayer", "textures/search.png")).
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
        lyricsBox = addRenderableWidget(new LyricsBox(x, y, cWidth, height - y - 55, Component.empty()));
        this.lyricsBox.visible = false;
    }

    // - Player panel
    public Button clear;
    public Button shuffle;
    public Button playOrPause;
    public Button skip;
    public Button repeat;

    public Button trackIcon;

    public void initPlayerPanel() {
        int x = 5;
        int y = height - 45;
        int size = width - 10;
        // - Left
        trackIcon = addRenderableWidget(new TrackIconButton(new ButtonBuilder(Component.empty(), (s)-> {
            AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
            if(track != null) AlinLib.MINECRAFT.setScreen(new TrackScreen(this, track));
        }).setSize(34, 34).setPosition(x+3, y+3)));

        trackIcon.visible = trackIcon.active = WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null;

        // - Center
        int x$Buttons = x + (size / 2) - ((25 * 5 - 5) / 2);
        clear = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.reset_queue"), (e) -> WaterPlayer.player.getTrackScheduler().queue.clear())
                .setSprite(DONT)
                .setSize(20, 20)
                .setPosition(x$Buttons, y + 3)
                .build());
        x$Buttons += 25;
        shuffle = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.shuffle"), (e) -> {
            if (!WaterPlayer.player.getTrackScheduler().queue.isEmpty())
                WaterPlayer.player.getTrackScheduler().shuffle();
            for (AbstractWidget widget : queue.widgets) removeWidget(widget);
            queue.resetWidgets();
            addQueue();
        })
                .setSprite(GuiUtils.getResourceLocation("waterplayer", "textures/player/shuffle.png"))
                .setSize(20, 20)
                .setPosition(x$Buttons, y + 3)
                .build());
        x$Buttons += 25;
        playOrPause = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control." + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause")), (e) -> {
            WaterPlayer.player.getAudioPlayer().setPaused(!WaterPlayer.player.getAudioPlayer().isPaused());
            e.builder.setTitle(Component.translatable("waterplayer.control." + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause")));
            ((ButtonBuilder) e.builder).setSprite(GuiUtils.getResourceLocation("waterplayer", "textures/player/" + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause") + ".png"));
        })
                .setSprite(GuiUtils.getResourceLocation("waterplayer", "textures/player/" + (WaterPlayer.player.getAudioPlayer().isPaused() ? "play" : "pause") + ".png"))
                .setSize(20, 20)
                .setPosition(x$Buttons, y + 3)
                .build());
        x$Buttons += 25;
        skip = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.skip"), (e) -> {
            if (WaterPlayer.player.getTrackScheduler().queue.isEmpty() && WaterPlayer.player.getAudioPlayer().getPlayingTrack() == null)
                return;
            WaterPlayer.player.getTrackScheduler().nextTrack();
        })
                .setSprite(GuiUtils.getResourceLocation("waterplayer", "textures/player/skip.png"))
                .setSize(20, 20)
                .setPosition(x$Buttons, y + 3)
                .build());
        x$Buttons += 25;
        repeat = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.control.repeat"), (e) -> {
            WaterPlayer.player.getTrackScheduler().changeRepeatStatus();
            ((ButtonBuilder) e.builder).setSprite(WaterPlayer.player.getTrackScheduler().getRepeatIcon());
        })
                .setSprite(WaterPlayer.player.getTrackScheduler().getRepeatIcon())
                .setSize(20, 20)
                .setPosition(x$Buttons, y + 3)
                .build());

        int timelineSize = Math.min(size / 3, 230);//190;
        addRenderableWidget(new TimelineComponent(x + (size / 2) - (timelineSize / 2), y + 30, timelineSize, 3, timelineSize >= 190));

        // - Left

        addRenderableWidget(new VolumeComponent(x + size - 12 - 70, y + 18, 70, 4));
    }

    // - You're just like pop music
    ConfigureScrolWidget queue;
    int lastCountQueue = WaterPlayer.player.getTrackScheduler().queue.size();

    public void initQueue() {
        addRenderableWidget(new ButtonBuilder(Component.literal("x"), (s) -> onClose()).setPosition(width - 25, 5).setSize(20, 20).build());
        int queueWidth = width - 15 - controlPanelWidth;
        this.queue = addRenderableWidget(new ConfigureScrolWidget(10 + controlPanelWidth + queueWidth - 4, 30, 5, this.height - 80, Component.empty(), scroller -> {
            scroller.innerHeight = 0;
            for (AbstractWidget widget : scroller.widgets) {
                if (widget.visible) {
                    widget.setWidth(queueWidth);
                    widget.setY((int) (scroller.getY()+scroller.innerHeight - scroller.scrollAmount()));
                    scroller.innerHeight += (widget.getHeight() + 5);
                } else widget.setY(-widget.getHeight());
            }
            scroller.innerHeight-=13;
        }));
        addQueue();
    }

    public Queue<AudioTrack> queueTracks = WaterPlayer.player.getTrackScheduler().queue;
    public void addQueue() {
        int queueWidth = width - 15 - controlPanelWidth;
        List<AbstractWidget> widgets = new ArrayList<>();
        int y = queue.getY();
        try {
            if (!queueTracks.isEmpty()) {
                for (AudioTrack track : queueTracks) {
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
        guiGraphics.enableScissor(10 + controlPanelWidth, 30, 10 + controlPanelWidth+ (width - 15 - controlPanelWidth), this.height - 50);
        if(queue != null) for (AbstractWidget widget : queue.widgets) widget.render(guiGraphics, i, j, f);
        guiGraphics.disableScissor();
    }

    public void renderControlPanel(GuiGraphics guiGraphics) {
        int x = 5;
        int y = 5;
        guiGraphics.fill(x, y, x + controlPanelWidth, height - 50, BLACK_ALPHA);
    }

    public void renderPlayerPanel(GuiGraphics guiGraphics) {
        int x = 5;
        int y = height - 45;
        guiGraphics.fill(x, y, x + width - 10, y + 40, BLACK_ALPHA);

        if (isTrackEnable()) {
            guiGraphics.blit(Music.getThumbnail(), x + 3, y + 3, 0f, 0f, 34, 34, 34, 34);
            int size = width - 10;
            int x$Buttons = x + (size / 2) - ((25 * 5 - 5) / 2) - 6;
            int x$Timeline = x + (size / 2) - (Math.min(size / 3, 230) / 2) - 6;
            int maxX = Math.min(x$Buttons, x$Timeline);
            renderString(guiGraphics, Music.getTitle(), x + 43, y + 8, maxX);
            renderString(guiGraphics, Music.getAuthor(), x + 43, y + 40 - 8 - AlinLib.MINECRAFT.font.lineHeight, maxX);
        }
    }

    public void renderQueueTitle(GuiGraphics guiGraphics) {
        guiGraphics.fill(10 + controlPanelWidth, 5, width - 30, 25, BLACK_ALPHA);
        int titleWidth = width - 40 - controlPanelWidth;
        guiGraphics.drawCenteredString(AlinLib.MINECRAFT.font, Component.translatable(queueTracks.isEmpty() ? "waterplayer.command.queue.blank" : "waterplayer.command.queue"), 10 + controlPanelWidth + (titleWidth / 2), 11, -1);
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
        for(GuiEventListener guiEventListener : this.children()){
            if(queue != null && queue.widgets.contains(guiEventListener)){
                if((d >= 10+controlPanelWidth && d <= width-5) && (e >= 30 && e <= height-50)){
                    if(guiEventListener.mouseClicked(d, e, i)){
                        st = false;
                        selected = guiEventListener;
                        break;
                    }
                }
            }else if(guiEventListener.mouseClicked(d, e, i)){
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
                            Duration pos = Duration.ofMillis(track.getPosition());
                            if ((pos.toMillis() <= line.getTimestamp().toMillis()) || (pos.toMillis() >= line.getTimestamp().toMillis() && pos.toMillis() <= line.getTimestamp().toMillis() + line.getDuration().toMillis())) {
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
        if (lastCountQueue != WaterPlayer.player.getTrackScheduler().queue.size()) {
            this.lastCountQueue = WaterPlayer.player.getTrackScheduler().queue.size();
            this.lastTrack = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
            for (AbstractWidget widget : queue.widgets) removeWidget(widget);
            queue.resetWidgets();
            addQueue();
        } else if (lastTrack != WaterPlayer.player.getAudioPlayer().getPlayingTrack()) {
            this.lastTrack = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
            this.lastCountQueue = WaterPlayer.player.getTrackScheduler().queue.size();
            for (AbstractWidget widget : queue.widgets) removeWidget(widget);
            queue.resetWidgets();
            addQueue();
        }
        super.tick();
    }
    //#if MC >=12002
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scr = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        int queueWidth = width - 15 - controlPanelWidth;
        if ((mouseX >= lyricsBox.getX() && mouseX <= lyricsBox.getX()+lyricsBox.getWidth()) && (mouseY >= lyricsBox.getY() && mouseY <= lyricsBox.getY()+lyricsBox.getHeight())) {
            scr = lyricsBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        } else if (!scr && queue != null && (mouseX >= queue.getX()-queueWidth && mouseX <= queue.getX()+queue.getWidth()) && (mouseY >= queue.getY() && mouseY <= queue.getY()+queue.getHeight())) {
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_P && (modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
            AlinLib.MINECRAFT.setScreen(new ControlScreen(parent));
            WaterPlayer.config.setBoolean("CONTROL.MODERN", false);
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
