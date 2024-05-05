package ru.kelcuprum.waterplayer.frontend.gui.overlays;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.api.events.client.ScreenEvents;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.TexturesHelper;
import ru.kelcuprum.waterplayer.frontend.gui.components.CurrentTrackButton;
import ru.kelcuprum.waterplayer.frontend.localization.Music;

import java.util.List;

import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Colors.*;

public class OverlayHandler implements HudRenderCallback, ClientTickEvents.StartTick, ScreenEvents.ScreenRender {
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
                Component author = Component.literal(Music.getAuthor());
                Component title = Component.literal(Music.getTitle());
                Component state = Component.literal(WaterPlayer.localization.getParsedText("{player.speaker_icon} {player.volume}% {format.time}{player.repeat_icon}"));
                int pos = WaterPlayer.config.getNumber("OVERLAY.POSITION", 0).intValue();
                int pos1 = WaterPlayer.config.getNumber("OVERLAY.POSITION", 0).intValue();
                int maxWidth = Math.max(WaterPlayer.MINECRAFT.font.width(state), (bottom ? (pos == 0 || pos == 1) : (pos1 == 0 || pos1 == 1)) ? AlinLib.MINECRAFT.getWindow().getGuiScaledWidth() / 2 : ((AlinLib.MINECRAFT.getWindow().getGuiScaledWidth() - 280) / 2)-(WaterPlayer.player.getAudioPlayer().getPlayingTrack().getInfo().artworkUrl != null ? ( WaterPlayer.MINECRAFT.font.lineHeight + 3)*3 : 0));
                //-=-=-=-
                if (!Music.isAuthorNull()) texts.addAll(WaterPlayer.MINECRAFT.font.split(author, maxWidth));
                texts.addAll(WaterPlayer.MINECRAFT.font.split(title, maxWidth));
                texts.addAll(WaterPlayer.MINECRAFT.font.split(state, maxWidth));
            }
        } catch (Exception ex) {
            WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
        }
    }
    public void render(GuiGraphics guiGraphics, int pos){
        if(WaterPlayer.player.getAudioPlayer().getPlayingTrack() == null) return;;
        if (!texts.isEmpty()) {
            int l = pos == 0 || pos == 1 ? 0 : texts.size() - 1;
            int f = WaterPlayer.MINECRAFT.font.lineHeight + 3;
            int my = f * texts.size();
            int mx = 0;
            for (FormattedCharSequence text : texts) {
                mx = Math.max(mx, WaterPlayer.MINECRAFT.font.width(text));
            }
            boolean caverEnable = false;
            int j = 0;
            if(WaterPlayer.config.getBoolean("OVERLAY.ENABLE_CAVER", true)){
                if(WaterPlayer.player.getAudioPlayer().getPlayingTrack().getInfo().artworkUrl != null) {
                    caverEnable = true;
                    j = f * 3;
                    mx += j + 10;
                }
            }

            boolean left = pos == 0 || pos == 2;
            boolean top = pos == 0 || pos == 1;
            if (!top) my += 3;
            int i = left ? (mx + 10) : -(mx + 10);
            int i1 = top ? (my + 5) : -(my + 5);
            guiGraphics.fill(
                    left ? 5 : guiGraphics.guiWidth() - 5, (top ? 5 : guiGraphics.guiHeight() - 6 + i1),
                    (left ? 5 : guiGraphics.guiWidth() - 5) + i, (top ? 5+i1 : guiGraphics.guiHeight() - 9),
                    0x7f000000
            );
            int state = isPause ? CLOWNFISH : isLive ? GROUPIE : SEADRIVE;
            guiGraphics.fill(
                    left ? 5 : guiGraphics.guiWidth() - 5, (top ? 5 + i1 : guiGraphics.guiHeight() - 6) + 1,
                    (left ? 5 : guiGraphics.guiWidth() - 5) + i, (top ? 5 + i1 : guiGraphics.guiHeight() - 11) + 3,
                    state - 0x7f000000
            );

            guiGraphics.fill(
                    left ? 5 : guiGraphics.guiWidth() - 15 - mx, (top ? 5 + i1 : guiGraphics.guiHeight() - 6) + 1,
                    (int) ((left ? 5 : guiGraphics.guiWidth() - 15 - mx) + (left ? i * v : (i * -1) * v)), (top ? 5 + i1 : guiGraphics.guiHeight() - 11) + 3,
                    state - 0x7f000000
            );

            for (FormattedCharSequence text : texts) {
                int x = (left ? 10 : guiGraphics.guiWidth() - 10 - mx)+(caverEnable ? j+5 : 0);
                int y = top ? 10 + (l * f) : guiGraphics.guiHeight() - 11 - WaterPlayer.MINECRAFT.font.lineHeight - (l * f);
                guiGraphics.drawString(WaterPlayer.MINECRAFT.font, text, x, y, -1);
                if (top) l++;
                else l--;
            }
            if(caverEnable){
                int x = left ? 6 : guiGraphics.guiWidth() - 14 - mx;
                int y = top ? 6 : guiGraphics.guiHeight() - 14 + ((l * f));
                AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
                guiGraphics.blit(TexturesHelper.getTexture(track.getInfo().artworkUrl, (track.getSourceManager().getSourceName()+"_"+track.getInfo().identifier)), left ? 6 : guiGraphics.guiWidth() - 14 - mx, (top ? 6 : guiGraphics.guiHeight() - 5 + i1), 0.0F, 0.0F, j+3, j+3, j+3, j+3);
            }
        }
    }

    @Override
    public void onScreenRender(Screen screen, GuiGraphics guiGraphics, int mx130, int my, float tick) {
        if (!(screen instanceof TitleScreen)) return;
        if(!WaterPlayer.config.getBoolean("ENABLE_MENU_OVERLAY", true)) return;
        int pos = WaterPlayer.config.getNumber("MENU_OVERLAY.POSITION", 0).intValue();
        render(guiGraphics, pos);
    }

    @Override
    public void onHudRender(GuiGraphics guiGraphics, float tickDelta) {
        if(!WaterPlayer.config.getBoolean("ENABLE_OVERLAY", true)) return;
        int pos = WaterPlayer.config.getNumber("OVERLAY.POSITION", 0).intValue();
        render(guiGraphics, pos);
    }
}
