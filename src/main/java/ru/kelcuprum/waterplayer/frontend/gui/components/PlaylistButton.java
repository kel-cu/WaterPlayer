package ru.kelcuprum.waterplayer.frontend.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.screens.AbstractConfigScreen;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;
import ru.kelcuprum.waterplayer.backend.playlist.WebPlaylist;
import ru.kelcuprum.waterplayer.frontend.gui.TextureHelper;
import ru.kelcuprum.waterplayer.frontend.gui.screens.config.PlaylistsScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.ControlScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.control.ModernControlScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.playlist.ViewPlaylistScreen;

import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.NO_PLAYLIST_ICON;

public class PlaylistButton extends Button {
    protected final Playlist playlist;
    protected WebPlaylist webPlaylist = null;

    public PlaylistButton(int x, int y, int width, WebPlaylist webPlaylist, Screen screen) {
        this(x, y, width, webPlaylist.playlist, screen);
        this.webPlaylist = webPlaylist;
    }
    public PlaylistButton(int x, int y, int width, Playlist playlist, Screen screen) {
        super(new ButtonBuilder().setTitle(Component.empty()).setStyle(GuiUtils.getSelected()).setSize(width, playlist.icon == null ? 20 : 40).setPosition(x, y));
        ((ButtonBuilder)this.builder).setOnPress((s) -> {
            Screen parent = screen;
            if(screen instanceof ControlScreen || screen instanceof ModernControlScreen || screen instanceof AbstractConfigScreen) parent = PlaylistsScreen.build(screen);
            if(webPlaylist == null) AlinLib.MINECRAFT.setScreen(new ViewPlaylistScreen(parent, playlist));
            else AlinLib.MINECRAFT.setScreen(new ViewPlaylistScreen(parent, webPlaylist));
        });
        this.playlist = playlist;
        StringBuilder builder = new StringBuilder();
        builder.append(Component.translatable("waterplayer.playlists.value", playlist.title, playlist.author).getString()).append(" ")
                .append(webPlaylist == null ? String.format("playlist:%s", playlist.fileName) : String.format("wplayer:%s", webPlaylist.url));
        setMessage(Component.literal(builder.toString()));
    }
    @Override
    public void renderText(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (getY() < guiGraphics.guiHeight() && !(getY() <=-getHeight()) ) {
            StringBuilder builder = new StringBuilder();
            builder.append(Component.translatable("waterplayer.playlists.value", playlist.title, playlist.author).getString());
            String type = webPlaylist == null ? String.format("playlist:%s", playlist.fileName) : String.format("wplayer:%s", webPlaylist.url);
            if (playlist.icon == null) {
                if(GuiUtils.isDoesNotFit(getMessage(), getWidth(), getHeight())){
                    this.renderScrollingString(guiGraphics, AlinLib.MINECRAFT.font, 2, 0xFFFFFF);
                } else {
                    guiGraphics.drawString(AlinLib.MINECRAFT.font, builder.toString(), getX() + (getHeight() - 8) / 2, getY() + (getHeight() - 8) / 2, 0xffffff);
                    guiGraphics.drawString(AlinLib.MINECRAFT.font, type, getX() + getWidth()-AlinLib.MINECRAFT.font.width(type)-((getHeight() - 8) / 2), getY() + (getHeight() - 8) / 2, 0xffffff);
                }
            } else {
                guiGraphics.blit(getIcon(), getX() + 2, getY() + 2, 0.0F, 0.0F, 36, 36, 36, 36);
                renderString(guiGraphics, builder.toString(), getX() + 45, getY() + 8);
                renderString(guiGraphics, type, getX() + 45, getY() + height - 8 - AlinLib.MINECRAFT.font.lineHeight);
            }
        }
    }
    public ResourceLocation getIcon(){
        if(playlist == null) return NO_PLAYLIST_ICON;
        return playlist.icon == null ? NO_PLAYLIST_ICON : TextureHelper.getTexture$Base64(playlist.icon, webPlaylist == null ? String.format("playlist-%s", playlist.fileName) : String.format("webplaylist-%s", webPlaylist.url));
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, Component message, int x, int y, int color) {
        int k = this.getX() + x;
        int l = this.getX() + this.getWidth() - x;
        if(playlist.icon != null) k+=40;
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
