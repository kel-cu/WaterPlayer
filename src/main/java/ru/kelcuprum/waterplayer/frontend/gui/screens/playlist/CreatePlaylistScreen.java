package ru.kelcuprum.waterplayer.frontend.gui.screens.playlist;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.WaterPlayerAPI;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;

import java.io.IOException;

public class CreatePlaylistScreen extends Screen {
    private final Screen parent;
    private String fileName;
    public CreatePlaylistScreen(Screen parent) {
        super(Component.translatable("waterplayer.playlist.create"));
        this.parent = parent;
    }
    //#if MC < 12002
    //$$ @Override
    //$$ public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, int i, int j, float f) {
    //$$     renderBackground(guiGraphics);
    //$$     super.render(guiGraphics, i, j, f);
    //$$ }
    //#endif

    @Override
    protected void init() {
        int x = width/2;
        int y = height/2;

        addRenderableWidget(new TextBox(x-150, 20, 300, 20, title, true));
        addRenderableWidget(new EditBoxBuilder(Component.translatable("waterplayer.playlist.create.filename"), (s) -> fileName = s)
                .setSecret(false)
                .setPosition(x-150, y-10)
                .setSize(300, 20).build());
        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_CANCEL, (s) -> onClose())
                .setPosition(x-150, y+15).setSize(145, 20).build());
        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_CONTINUE, (s) -> {
            if(fileName.startsWith("http://") || fileName.startsWith("https://")){
                try {
                    fileName = WaterPlayerAPI.getPlaylist(fileName, true).fileName;
                } catch (Exception e){
                    WaterPlayer.log(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), Level.ERROR);
                    WaterPlayer.getToast().setMessage(Component.literal(e.getMessage() == null ? e.getClass().getName() : e.getMessage())).setType(ToastBuilder.Type.ERROR).buildAndShow();
                    return;
                }
            }
            assert this.minecraft != null;
            if(naturalSelectionSocieties(fileName)) {
                try {
                    this.minecraft.setScreen(new ViewPlaylistScreen(parent, new Playlist(fileName)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else WaterPlayer.getToast().setMessage(Component.translatable("waterplayer.playlist.create.illegal_characters"))
                    .setType(ToastBuilder.Type.ERROR).buildAndShow();
        })
                .setPosition(x+5, y+15).setSize(145, 20).build());
    }

    public boolean naturalSelectionSocieties(String string){
        String refactor = string.replaceAll("[<>:\"?|\\\\/*]", "");
        return refactor.length() == string.length();
    }

    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
