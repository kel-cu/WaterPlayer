package ru.kelcuprum.waterplayer.frontend.gui.components;

import com.sedmelluq.discord.lavaplayer.container.wav.WavAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.TexturesHelper;
import ru.kelcuprum.waterplayer.frontend.localization.Music;
import ru.kelcuprum.waterplayer.frontend.localization.StarScript;

public class CurrentTrackButton extends Button {
    public CurrentTrackButton(int x, int y, int width, Screen screen) {
        super(x, y, width, 40, InterfaceUtils.DesignType.FLAT, Component.empty(), null);
    }
    @Override
    public void renderText(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if(WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null){
            AudioTrack track = WaterPlayer.player.getAudioPlayer().getPlayingTrack();
            ResourceLocation icon = track.getInfo().artworkUrl != null ? TexturesHelper.getTexture(track.getInfo().artworkUrl, (track.getSourceManager().getSourceName()+"_"+track.getInfo().identifier)) : new ResourceLocation("waterplayer", "textures/no_icon.png");
            guiGraphics.blit(icon, getX()+2, getY()+2, 0.0F, 0.0F, 36, 36, 36, 36);
            StringBuilder builder = new StringBuilder();
            if (!Music.isAuthorNull(track)) builder.append("«").append(Music.getAuthor(track)).append("» ");
            builder.append(Music.getTitle(track));
            if(getWidth()-50 < AlinLib.MINECRAFT.font.width(builder.toString())){
                guiGraphics.drawString(AlinLib.MINECRAFT.font, AlinLib.MINECRAFT.font.substrByWidth(FormattedText.of(builder.toString()), getWidth() - 50 - AlinLib.MINECRAFT.font.width("...")).getString()+"...", getX()+45, getY()+8, -1);
            } else {
                guiGraphics.drawString(AlinLib.MINECRAFT.font, builder.toString(), getX()+45, getY()+8, -1);
            }
            guiGraphics.drawString(AlinLib.MINECRAFT.font, track.getInfo().isStream ? WaterPlayer.localization.getLocalization("format.live") :  StarScript.getTimestamp(Music.getPosition(track)) + " / " + StarScript.getTimestamp(Music.getDuration(track)), getX()+45, getY()+30-AlinLib.MINECRAFT.font.lineHeight, -1);
        } else {
            guiGraphics.drawCenteredString(AlinLib.MINECRAFT.font, Component.translatable("waterplayer.command.now_playing.notPlaying"), getX()+(getWidth()/2),  getY()+20-(AlinLib.MINECRAFT.font.lineHeight/2), -1);
        }

    }
    public static void confirmLinkNow(Screen screen, String string) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmLinkScreen((bl) -> {
            if (bl) {
                Util.getPlatform().openUri(string);
            }

            minecraft.setScreen(screen);
        }, string, true));
    }
}
