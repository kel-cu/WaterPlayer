package ru.kelcuprum.waterplayer.frontend.gui.overlays;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import org.apache.logging.log4j.Level;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.localization.Music;

import java.util.List;

import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Colors.*;

public class OverlayHandler implements HudRenderCallback, ClientTickEvents.StartTick {
    private final List<FormattedCharSequence> texts = new ObjectArrayList<>();

    private boolean isLive = false;
    private boolean isPause = true;
    double v = 0;

    @Override
    public void onStartTick(Minecraft client) {
        this.texts.clear();
        isLive = false;
        isPause = true;
        try {
            if (WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null && WaterPlayer.config.getBoolean("ENABLE_OVERLAY", true)) {
                isLive = WaterPlayer.player.getAudioPlayer().getPlayingTrack().getInfo().isStream;
                isPause = WaterPlayer.player.getAudioPlayer().isPaused();
                v = isLive ? 1.0 : (double) WaterPlayer.player.getAudioPlayer().getPlayingTrack().getPosition() / WaterPlayer.player.getAudioPlayer().getPlayingTrack().getDuration();
                //-=-=-=-
                Component author = Component.literal(Music.getAuthor());
                Component title = Component.literal(Music.getTitle());
                Component state = Component.literal(WaterPlayer.localization.getParsedText("{player.speaker_icon} {player.volume}% {format.time}"));
                int pos = WaterPlayer.config.getNumber("OVERLAY.POSITION", 0).intValue();
                int maxWidth = Math.max(WaterPlayer.MINECRAFT.font.width(state), pos == 0 || pos == 1 ? client.getWindow().getGuiScaledWidth()/2 : (client.getWindow().getGuiScaledWidth()-280)/2);
                //-=-=-=-
                if(!Music.isAuthorNull()) texts.addAll(WaterPlayer.MINECRAFT.font.split(author, maxWidth));
                texts.addAll(WaterPlayer.MINECRAFT.font.split(title, maxWidth));
                texts.addAll(WaterPlayer.MINECRAFT.font.split(state, maxWidth));
            }
        } catch (Exception ex){
            WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
        }

    }
    @Override
    public void onHudRender(GuiGraphics guiGraphics, float tickDelta) {
        int pos = WaterPlayer.config.getNumber("OVERLAY.POSITION", 0).intValue();
        if(!texts.isEmpty()){
            int l = pos == 0 || pos == 1 ? 0 : texts.size()-1;
            int f = WaterPlayer.MINECRAFT.font.lineHeight+3;
            int my = f*texts.size();
            int mx = 0;
            for(FormattedCharSequence text : texts){
                mx = Math.max(mx, WaterPlayer.MINECRAFT.font.width(text));
            }
            boolean left = pos == 0 || pos == 2;
            boolean top  = pos == 0 || pos == 1;
            if(!top) my+=3;
            int i = left ? (mx + 10) : -(mx + 10);
            int i1 = top ? (my + 5) : -(my + 5);
            guiGraphics.fill(
                    left ? 5 : guiGraphics.guiWidth() - 5, top ? 5 : guiGraphics.guiHeight() - 5,
                    (left ? 5 : guiGraphics.guiWidth() - 5) + i, (top ? 5 : guiGraphics.guiHeight() - 5) + i1,
                    0x7f000000
            );
            int state = isPause ? CLOWNFISH : isLive ? GROUPIE : SEADRIVE;
            guiGraphics.fill(
                    left ? 5 : guiGraphics.guiWidth() - 5, (top ? 5 + i1 : guiGraphics.guiHeight() - 5)+1,
                    (left ? 5 : guiGraphics.guiWidth() - 5) + i, (top ? 5 + i1 : guiGraphics.guiHeight() - 5)+3,
                    state-0x7f000000
            );

            guiGraphics.fill(
                    left ? 5 : guiGraphics.guiWidth() - 15 - mx, (top ? 5 + i1 : guiGraphics.guiHeight() - 5)+1,
                    (int) ((left ? 5 : guiGraphics.guiWidth() - 15 - mx) + (left ? i*v : (i*-1)*v)), (top ? 5 + i1 : guiGraphics.guiHeight() - 5)+3,
                    state-0x7f000000
            );

            for(FormattedCharSequence text : texts){
                int x = left ? 10 : guiGraphics.guiWidth() - 10 - mx;
                int y = top ? 10+(l*f) : guiGraphics.guiHeight() - 10 - WaterPlayer.MINECRAFT.font.lineHeight - (l*f);
                guiGraphics.drawString(WaterPlayer.MINECRAFT.font, text, x, y, -1);
                if(top) l++; else l--;
            }
        }
    }
}
