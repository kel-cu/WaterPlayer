package ru.kelcuprum.waterplayer.frontend.gui.components;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.screens.TrackScreen;
import ru.kelcuprum.waterplayer.frontend.localization.MusicHelper;

import static ru.kelcuprum.waterplayer.WaterPlayer.getTimestamp;

public class TrackButton extends Button {
    protected AudioTrack track;
    private final boolean isShort;
    public TrackButton(int x, int y, int width, AudioTrack track, Screen screen, boolean isShort) {
        super(new ButtonBuilder().setOnPress((s) -> AlinLib.MINECRAFT.setScreen(new TrackScreen(screen, track))).setTitle(Component.empty()).setStyle(GuiUtils.getSelected()).setSize(width, isShort ? 20 : 40).setPosition(x, y));
        StringBuilder builder = new StringBuilder();
        if (!MusicHelper.isAuthorNull(track)) builder.append("«").append(MusicHelper.getAuthor(track)).append("» ");
        builder.append(MusicHelper.getTitle(track)).append(" ").append(getTimestamp(MusicHelper.getDuration(track)));
        setMessage(Component.literal(builder.toString()));
        this.isShort = isShort;
        this.track = track;
    }
    @Override
    public void renderText(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (getY() < guiGraphics.guiHeight() && !(getY() <=-getHeight()) ) {
            StringBuilder builder = new StringBuilder();
            if (!MusicHelper.isAuthorNull(track)) builder.append("«").append(MusicHelper.getAuthor(track)).append("» ");
            builder.append(MusicHelper.getTitle(track));
            String time = track.getInfo().isStream ? WaterPlayer.localization.getLocalization("format.live") : getTimestamp(MusicHelper.getDuration(track));
            if (isShort) {
                if(GuiUtils.isDoesNotFit(getMessage(), getWidth(), getHeight())){
                    this.renderScrollingString(guiGraphics, AlinLib.MINECRAFT.font, 2, 0xFFFFFF);
                } else {
                    guiGraphics.drawString(AlinLib.MINECRAFT.font, builder.toString(), getX() + (getHeight() - 8) / 2, getY() + (getHeight() - 8) / 2, 0xffffff);
                    guiGraphics.drawString(AlinLib.MINECRAFT.font, time, getX() + getWidth()-AlinLib.MINECRAFT.font.width(time)-((getHeight() - 8) / 2), getY() + (getHeight() - 8) / 2, 0xffffff);
                }
            } else {
                ResourceLocation icon = MusicHelper.getThumbnail(track);
                guiGraphics.blit(
                        //if MC >= 12102
                        RenderType::guiTextured,
                        //#endif
                        icon, getX() + 2, getY() + 2, 0.0F, 0.0F, 36, 36, 36, 36);
                renderString(guiGraphics, builder.toString(), getX() + 45, getY() + 8);
                renderString(guiGraphics, time+" | "+ MusicHelper.getServiceName(MusicHelper.getService(track)).getString(), getX() + 45, getY() + height - 8 - AlinLib.MINECRAFT.font.lineHeight);
            }
        }
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, Component message, int x, int y, int color) {
        int k = this.getX() + x;
        int l = this.getX() + this.getWidth() - x;
        if(!isShort) k+=40;
        renderScrollingString(guiGraphics, font, message, k, y, l, y+font.lineHeight, color);
    }

    protected void renderString(GuiGraphics guiGraphics, String text, int x, int y) {
        if (getWidth() - 50 < AlinLib.MINECRAFT.font.width(text)) {
            renderScrollingString(guiGraphics, AlinLib.MINECRAFT.font, Component.literal(text), 5, y-1, -1);
        } else {
            guiGraphics.drawString(AlinLib.MINECRAFT.font, text, x, y, -1);
        }
    }
}
