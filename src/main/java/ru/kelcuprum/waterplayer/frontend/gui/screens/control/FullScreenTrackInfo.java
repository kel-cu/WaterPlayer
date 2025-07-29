package ru.kelcuprum.waterplayer.frontend.gui.screens.control;

import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
//#if MC >= 12106
import net.minecraft.client.renderer.RenderPipelines;
//#endif
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.buttons.ButtonBoolean;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.LyricsHelper;
import ru.kelcuprum.waterplayer.frontend.gui.overlays.OverlayHandler;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.components.LyricsBox;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.components.TimelineComponent;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.components.VolumeComponent;
import ru.kelcuprum.waterplayer.frontend.gui.style.AirStyle;
import ru.kelcuprum.waterplayer.frontend.localization.MusicHelper;

import java.util.ArrayList;
import java.util.List;

import static ru.kelcuprum.alinlib.gui.Colors.BLACK_ALPHA;
import static ru.kelcuprum.alinlib.gui.Colors.FORGOT;

public class FullScreenTrackInfo extends Screen {

    public Screen screen;

    public FullScreenTrackInfo(Screen screen) {
        super(Component.empty());
        this.screen = screen;
    }

    int yTimeline = 0;

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
        if(track == null){
            guiGraphics.drawCenteredString(font, Component.translatable("waterplayer.fullscreen.empty"), width/2, height/2-font.lineHeight/2, -1);
        } else {
            AudioLyrics lyrics = LyricsHelper.getLyrics(track);

            int panelSizes = (int) (width*0.475);
            int x = (int) (((width*(lyrics != null ? 0.5 : 1))-panelSizes) / 2);

            ResourceLocation icon =  MusicHelper.getThumbnail(track);


            int iconSize = (int) Math.min(panelSizes*0.75, height*0.45);
            int iconX = x + (panelSizes-iconSize) / 2;
            List<FormattedCharSequence> texts = getTextInfo(lyrics);
            int iconY = height/2 - iconSize/2 - ((font.lineHeight+4)*texts.size()) / 2;
            guiGraphics.blit(
                    //#if MC >= 12106
                    RenderPipelines.GUI_TEXTURED,
                    //#elseif MC >= 12102
                    //$$ RenderType::guiTextured,
                    //#endif
                    icon, iconX, iconY, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
            int y = iconY+iconSize+8;
            for(FormattedCharSequence formattedCharSequence : texts){
                guiGraphics.drawCenteredString(font, formattedCharSequence, x+(panelSizes/2), y, -1);
                y+=font.lineHeight+4;
            }
            yTimeline = y+font.lineHeight+4;
        }
    }

    public List<FormattedCharSequence> getTextInfo(AudioLyrics lyrics){
        int panelSizes = (int) (width*0.475);
        List<FormattedCharSequence> texts = new ArrayList<>();
        int textWidth = lyrics == null ? (int) (width * 0.75) : panelSizes;
        texts.addAll(AlinLib.MINECRAFT.font.split(FormattedText.of(MusicHelper.getTitle()), textWidth));
        texts.addAll(AlinLib.MINECRAFT.font.split(FormattedText.of(MusicHelper.getAuthor()), textWidth));
        return texts;
    }

    public LyricsBox lyricsBox = null;
    public TimelineComponent timeline = null;

    public AbstractWidget close;
    public AbstractWidget volume;
    public boolean view = false;

    @Override
    protected void init() {
        initText();
        initControl();
        close = addRenderableWidget(new ButtonBuilder(Component.literal("x"), (s) -> onClose()).setSize(16, 16).setPosition(width-21, 5).build());
        volume = addRenderableWidget(new VolumeComponent(width - 39 - 70, 11, 70, 4));
        close.active = close.visible = volume.active = volume.visible = view;
    }
    protected void initText() {
        int panelSizes = (int) (width * 0.475);
        int x = (int) ((width * 0.5) + ((width * 0.5) - panelSizes) / 2);
        int iconHeight = (int) Math.min(panelSizes*0.75, height*0.45);
        if(lyricsBox == null) lyricsBox = addRenderableWidget(new LyricsBox(x, 5, panelSizes, height-10, null));
        else {
            lyricsBox.setPosition(x, 5);
            lyricsBox.setSize(panelSizes, height-10);
            addRenderableWidget(lyricsBox);
        }
    }

    public Button back;
    public Button next;
    public Button pause;

    protected void initControl() {
            int panelSizes = (int) (width*0.475);
            int x = (int) (((width*-panelSizes) / 2));

            int iconSize = (int) Math.min(panelSizes*0.75, height*0.45);

            int iconX = x + (panelSizes-iconSize) / 2;
            int iconY = (int) ((width*0.5-panelSizes) / 2) + (panelSizes-iconSize) / 2;

        back = (Button) addRenderableWidget(new ButtonBuilder(Component.literal("◀"), (s) -> {
            if (WaterPlayer.player.getTrackScheduler().queue.getQueue().isEmpty() && WaterPlayer.player.getAudioPlayer().getPlayingTrack() == null)
                return;
            WaterPlayer.player.getTrackScheduler().backTrack();
        }).setSize(26, iconSize).setPosition(iconX-26, iconY).setStyle(new AirStyle()).build());
        next = (Button) addRenderableWidget(new ButtonBuilder(Component.literal("▶"), (s) -> {
            if (WaterPlayer.player.getTrackScheduler().queue.getQueue().isEmpty() && WaterPlayer.player.getAudioPlayer().getPlayingTrack() == null)
                return;
            WaterPlayer.player.getTrackScheduler().nextTrack();
        }).setSize(26, iconSize).setPosition(iconX+iconSize, iconY).setStyle(new AirStyle()).build());
        pause = (Button) addRenderableWidget(new ButtonBuilder(Component.empty(), (s) -> WaterPlayer.player.changePaused()).setSize(iconSize, iconSize).setPosition(iconX, iconY).setStyle(new AirStyle()).build());

            timeline = addRenderableWidget(new TimelineComponent(x, iconY+iconSize+12+(font.lineHeight+4)*2, (int) (panelSizes*0.5), 3, true));
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if(i == GLFW.GLFW_KEY_LEFT){
            if (!WaterPlayer.player.getTrackScheduler().queue.getQueue().isEmpty() && WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null) {
                WaterPlayer.player.getTrackScheduler().backTrack();
                return true;
            }
        } else if(i == GLFW.GLFW_KEY_RIGHT){
            if (!WaterPlayer.player.getTrackScheduler().queue.getQueue().isEmpty() && WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null) {
                WaterPlayer.player.getTrackScheduler().nextTrack();
                return true;
            }
        } else if(i == GLFW.GLFW_KEY_SPACE){
            WaterPlayer.player.changePaused();
            return true;
        }else if(i == GLFW.GLFW_KEY_H){
            view=!view;
            close.active = close.visible = volume.active = volume.visible = view;
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void tick() {
        AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
        if(timeline != null) timeline.setY(yTimeline);
        if(track != null) {
            AudioLyrics lyrics = LyricsHelper.getLyrics(track);
            if(lyricsBox != null){
                if(lyrics == null) lyricsBox.visible = false;
                else {
                    lyricsBox.visible = true;
                    lyricsBox.setLyrics(lyrics);
                    lyricsBox.setPosition(track.getPosition());
                }
            }
            if(timeline != null){
                timeline.active = timeline.visible = true;
                int panelSizes = (int) (width*0.475);
                int x = (int) (((width*(lyrics != null ? 0.5 : 1))-panelSizes) / 2);
                int iconSize = (int) Math.min(panelSizes*0.75, height*0.45);

                int iconX = x + (panelSizes-iconSize) / 2;
                List<FormattedCharSequence> texts = getTextInfo(lyrics);
                int iconY = height/2 - iconSize/2 - ((font.lineHeight+4)*texts.size()) / 2;
                back.setPosition(iconX-26, iconY);
                next.setPosition(iconX+iconSize, iconY);
                pause.setPosition(iconX, iconY);
                timeline.setX((int) (x+panelSizes*0.25));
            }
        } else if(timeline != null) timeline.active = timeline.visible = false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
        super.renderBackground(guiGraphics, i, j, f);
        if(track != null) {
            AudioLyrics lyrics = LyricsHelper.getLyrics(track);
            guiGraphics.fill(0, 0, width, height, OverlayHandler.getCommonColor(track, FORGOT));
            if(lyrics != null) guiGraphics.fill(width/2, 0, width, height, 0x25000000);
        }
        guiGraphics.fill(0, 0, width, height, BLACK_ALPHA);
    }

    @Override
    public void onClose() {
        AlinLib.MINECRAFT.setScreen(screen);
    }
}
