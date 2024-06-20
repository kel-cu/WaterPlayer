package ru.kelcuprum.waterplayer.frontend.gui.screens.search;

import com.github.topi314.lavasearch.result.AudioSearchResult;
import com.github.topi314.lavasearch.result.AudioText;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.editbox.base.EditBoxString;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.components.ButtonWithSprite;
import ru.kelcuprum.waterplayer.frontend.localization.Music;

import java.util.Set;

public class SearchScreen extends Screen {
    protected final Screen parent;
    public SearchScreen(Screen parent) {
        super(Component.translatable("waterplayer.search"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        InterfaceUtils.renderLeftPanel(guiGraphics, 190, height);
    }

    protected EditBoxString request;
    @Override
    protected void init() {
        int x = 5;
        int size = 180;
        addRenderableWidget(new TextBox(x, 15, size, 9, title, true));

        request = new EditBoxString(x + 25, 40, size - 25, 20, Component.translatable("waterplayer.search.query"));
        request.setMaxLength(Integer.MAX_VALUE);
        addRenderableWidget(request);
        addRenderableWidget(new ButtonWithSprite(x, 40, 20, 20, InterfaceUtils.getResourceLocation("waterplayer", "search"), Component.translatable("waterplayer.control.search"), (s) -> {
            AlinLib.log(request.getValue());
            AudioSearchResult result = WaterPlayer.player.getSearchManager().loadSearch(request.getValue(), Set.of(AudioSearchResult.Type.TRACK, AudioSearchResult.Type.ALBUM, AudioSearchResult.Type.ARTIST, AudioSearchResult.Type.PLAYLIST, AudioSearchResult.Type.TEXT));
            if(result != null){
                for(AudioTrack track : result.getTracks()) AlinLib.log("M" + Music.getAuthor(track) + " "+ Music.getTitle(track));
                for(AudioPlaylist track : result.getAlbums()) AlinLib.log("Al" + track.getName());
                for(AudioPlaylist track : result.getArtists()) AlinLib.log("Ar" + track.getName());
                for(AudioPlaylist track : result.getPlaylists()) AlinLib.log("P" + track.getName());
                for(AudioText track : result.getTexts()) AlinLib.log("T" + track.getText());
            } else AlinLib.log("нихуя нет");
        }));
    }

    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
