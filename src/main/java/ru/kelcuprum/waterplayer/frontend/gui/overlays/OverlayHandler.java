package ru.kelcuprum.waterplayer.frontend.gui.overlays;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
//#if MC >= 12106
import net.minecraft.client.renderer.RenderPipelines;
//#endif
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.api.events.client.ClientTickEvents;
import ru.kelcuprum.alinlib.api.events.client.ScreenEvents;
import ru.kelcuprum.alinlib.api.events.client.GuiRenderEvents;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.TextureHelper;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.ControlScreen;
import ru.kelcuprum.waterplayer.frontend.localization.MusicHelper;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static ru.kelcuprum.alinlib.gui.Colors.*;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.NO_ICON;

public class OverlayHandler implements GuiRenderEvents, ClientTickEvents.StartTick, ScreenEvents.ScreenRender {
    private final List<FormattedCharSequence> texts = new ObjectArrayList<>();

    private boolean isLive = false;
    private boolean isPause = true;
    double v = 0;

    @Override
    public void onStartTick(Minecraft client) {
        updateTexts(true);
    }

    public void updateTexts(boolean bottom) {
        this.texts.clear();
        isLive = false;
        isPause = true;
        try {
            if (WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null && (WaterPlayer.config.getBoolean("ENABLE_OVERLAY", true) || WaterPlayer.config.getBoolean("ENABLE_MENU_OVERLAY", true))) {
                isLive = WaterPlayer.player.getAudioPlayer().getPlayingTrack().getInfo().isStream;
                isPause = WaterPlayer.player.getAudioPlayer().isPaused();
                v = isLive ? 1.0 : (double) WaterPlayer.player.getAudioPlayer().getPlayingTrack().getPosition() / WaterPlayer.player.getAudioPlayer().getPlayingTrack().getDuration();
                //-=-=-=-
                Component author = Component.literal(MusicHelper.getAuthor());
                Component title = Component.literal(MusicHelper.getTitle());
                Component state = Component.literal(WaterPlayer.localization.getParsedText("{waterplayer.player.speaker_icon} {waterplayer.player.volume}% {waterplayer.format.time}{waterplayer.player.repeat_icon}"));
                int pos = WaterPlayer.config.getNumber("OVERLAY.POSITION", 0).intValue();
                int pos1 = WaterPlayer.config.getNumber("OVERLAY.POSITION", 0).intValue();
                int maxWidth = Math.max(AlinLib.MINECRAFT.font.width(state), (bottom ? (pos == 0 || pos == 1) : (pos1 == 0 || pos1 == 1)) ? AlinLib.MINECRAFT.getWindow().getGuiScaledWidth() / 2 : ((AlinLib.MINECRAFT.getWindow().getGuiScaledWidth() - 280) / 2) - (WaterPlayer.player.getAudioPlayer().getPlayingTrack().getInfo().artworkUrl != null || MusicHelper.isFile() ? (AlinLib.MINECRAFT.font.lineHeight + 3) * 3 : 0));
                //-=-=-=-
                texts.addAll(AlinLib.MINECRAFT.font.split(title, maxWidth));
                if (!MusicHelper.isAuthorNull()) texts.addAll(AlinLib.MINECRAFT.font.split(author, maxWidth));
                texts.addAll(AlinLib.MINECRAFT.font.split(state, maxWidth));
            }
        } catch (Exception ex) {
            WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
        }
    }

    public void render(GuiGraphics guiGraphics, int pos) {
        if ((AlinLib.MINECRAFT.options.hideGui ||
                //#if MC >= 12002
                AlinLib.MINECRAFT.gui.getDebugOverlay().showDebugScreen()
                //#elseif MC < 12002
                //$$ AlinLib.MINECRAFT.options.renderDebug
                //#endif
        ) && WaterPlayer.config.getBoolean("ENABLE_OVERLAY.HIDE_IN_DEBUG", true)
        ) return;
        if (WaterPlayer.player.getAudioPlayer().getPlayingTrack() == null) return;
        try {
            if (!texts.isEmpty()) {
                int l = pos == 0 || pos == 1 ? 0 : texts.size() - 1;
                int f = AlinLib.MINECRAFT.font.lineHeight + 3;
                int my = f * texts.size();
                int mx = 0;
                for (FormattedCharSequence text : texts) {
                    mx = Math.max(mx, AlinLib.MINECRAFT.font.width(text));
                }
                boolean caverEnable = false;
                int j = 0;
                if (WaterPlayer.config.getBoolean("OVERLAY.ENABLE_CAVER", true)) {
                    caverEnable = true;
                    j = f * Math.min(texts.size(), 3);
                    mx += j + 10;
                }

                boolean left = pos == 0 || pos == 2;
                boolean top = pos == 0 || pos == 1;
                if (!top) my += 3;
                int i = left ? (mx + 10) : -(mx + 10);
                int i1 = top ? (my + 5) : -(my + 5);
                guiGraphics.fill(
                        left ? 5 : guiGraphics.guiWidth() - 5, (top ? 5 : guiGraphics.guiHeight() - 6 + i1),
                        (left ? 5 : guiGraphics.guiWidth() - 5) + i, (top ? 5 + i1 : guiGraphics.guiHeight() - 9),
                        0x7f000000
                );
                int state = getBarColor();
                guiGraphics.fill(
                        left ? 5 : guiGraphics.guiWidth() - 5, (top ? 5 + i1 : guiGraphics.guiHeight() - 6) + 1,
                        (left ? 5 : guiGraphics.guiWidth() - 5) + i, (top ? 5 + i1 : guiGraphics.guiHeight() - 11) + 3,
                        0x7f000000
                );

                guiGraphics.fill(
                        left ? 5 : guiGraphics.guiWidth() - 15 - mx, (top ? 5 + i1 : guiGraphics.guiHeight() - 6) + 1,
                        (int) ((left ? 5 : guiGraphics.guiWidth() - 15 - mx) + (left ? i * v : (i * -1) * v)), (top ? 5 + i1 : guiGraphics.guiHeight() - 11) + 3,
                        state
                );

                for (FormattedCharSequence text : texts) {
                    int x = (left ? 10 : guiGraphics.guiWidth() - 10 - mx) + (caverEnable ? j + 5 : 0);
                    int y = top ? 10 + (l * f) : guiGraphics.guiHeight() - 11 - AlinLib.MINECRAFT.font.lineHeight - (l * f);
                    guiGraphics.drawString(AlinLib.MINECRAFT.font, text, x, y, -1);
                    if (top) l++;
                    else l--;
                }
                if (caverEnable) {
                    AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
                    guiGraphics.blit(
                            //#if MC >= 12106
                            RenderPipelines.GUI_TEXTURED,
                            //#elseif MC >= 12102
                            //$$ RenderType::guiTextured,
                            //#endif
                            MusicHelper.getThumbnail(track), left ? 6 : guiGraphics.guiWidth() - 14 - mx, (top ? 6 : guiGraphics.guiHeight() - 5 + i1), 0.0F, 0.0F, j + 3, j + 3, j + 3, j + 3);
                }
            }
        } catch (Exception ex) {
            WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
        }
    }

    @Override
    public void onScreenRender(Screen screen, GuiGraphics guiGraphics, int mx130, int my, float tick) {
        if (screen instanceof TitleScreen) {
            if (!WaterPlayer.config.getBoolean("ENABLE_MENU_OVERLAY", true)) return;
            int pos = WaterPlayer.config.getNumber("MENU_OVERLAY.POSITION", 0).intValue();
            render(guiGraphics, pos);
        }
    }

    @Override
    public void onRender(GuiGraphics guiGraphics, float tickDelta) {
        if (!WaterPlayer.config.getBoolean("ENABLE_OVERLAY", true) || (AlinLib.MINECRAFT.screen instanceof ControlScreen)) return;
        int pos = WaterPlayer.config.getNumber("OVERLAY.POSITION", 0).intValue();
        render(guiGraphics, pos);
    }

    public int getBarColor() {
        AudioTrack audio = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
        return isPause ? CLOWNFISH : isLive ? GROUPIE : !WaterPlayer.config.getBoolean("OVERLAY.ACCENT_COLOR", false) || audio == null ? SEADRIVE : switch (MusicHelper.getTitle(audio)) {
            // MiatriSs
            case "HEX OF PINK FF006E" -> 0xFFFF006E;
            default -> {
                int color = getCommonColor(audio);
                if(color == WHITE) color = SEADRIVE;
                yield color;
            }
        };
    }

    public static HashMap<AudioTrack, Integer> commonColors = new HashMap<AudioTrack, Integer>();

    public static int getCommonColor(AudioTrack track) {
        return getCommonColor(track, WHITE);
    }

    public static int getCommonColor(AudioTrack track, int defaultColor) {
        if (track == null) return defaultColor;
        if (MusicHelper.getThumbnail(track) == NO_ICON) return defaultColor;
        if (commonColors.containsKey(track)) return commonColors.get(track);
        BufferedImage image = TextureHelper.dynamicTextures.get(MusicHelper.getThumbnail(track));
        if (image == null) return defaultColor;
        int height = image.getHeight();
        int width = image.getWidth();
        HashMap<Integer, Integer> m = new HashMap();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = image.getRGB(j, i);
                int[] rgbArr = getRGBArr(rgb);
                // Filter out grays....
                if (!notCorrect(rgbArr)) {
                    Integer counter = m.get(rgb);
                    if (counter == null)
                        counter = 0;
                    counter++;
                    m.put(rgb, counter);
                }
            }
        }
        int color = getMostCommonColour(m, defaultColor);
        commonColors.put(track, color);
        return color;
    }

    // Если у вас IDE жалуется на это, то он долбаеб конченый
    public static int getMostCommonColour(HashMap<Integer, Integer> map, int defaultColor) {
        LinkedList list = new LinkedList(map.entrySet());
        if (list.isEmpty()) return defaultColor;
        list.sort((o1, o2) -> ((Comparable) ((Map.Entry<?, ?>) (o1)).getValue())
                .compareTo(((Map.Entry) (o2)).getValue()));
        Map.Entry<Integer, Integer> me = (Map.Entry<Integer, Integer>) list.getLast();
        return me.getKey();
    }

    public static int[] getRGBArr(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        return new int[]{red, green, blue};

    }

    public static boolean notCorrect(int[] rgbArr) {
        int rgDiff = rgbArr[0] - rgbArr[1];
        int rbDiff = rgbArr[0] - rgbArr[2];
        // Filter out black, white and grays...... (tolerance within 10 pixels)
        int tolerance = 10;
        if (rgDiff > tolerance || rgDiff < -tolerance)
            if (rbDiff > tolerance || rbDiff < -tolerance)
                return false;
        double darkness = ((double) (rgbArr[0] + rgbArr[1] + rgbArr[2]) / 3);
        return true;
    }

}
