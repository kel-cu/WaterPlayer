package ru.kelcuprum.waterplayer.frontend.gui.screens.playlist;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.base.EditBoxString;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.WaterPlayerAPI;

public class CreatePlaylistScreen extends Screen {
    private final Screen parent;
    private String fileName;
    public CreatePlaylistScreen(Screen parent) {
        super(Component.translatable("waterplayer.playlist.create"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = width/2;
        int y = height/2;

        addRenderableWidget(new TextBox(x-150, 20, 300, 20, title, true));
        addRenderableWidget(new EditBoxString(x-150, y-10, 300, 20, false, "", InterfaceUtils.DesignType.FLAT, Component.translatable("waterplayer.playlist.create.filename"), (s) -> fileName = s));
        addRenderableWidget(new Button(x-150, y+15, 145, 20, InterfaceUtils.DesignType.FLAT, CommonComponents.GUI_CANCEL, (s) -> onClose()));
        addRenderableWidget(new Button(x+5, y+15, 145, 20, InterfaceUtils.DesignType.FLAT, CommonComponents.GUI_CONTINUE, (s) -> {
            if(fileName.startsWith("http://") || fileName.startsWith("https://")){
                try {
                    fileName = WaterPlayerAPI.getPlaylist(fileName, true).fileName;
                } catch (Exception e){
                    WaterPlayer.log(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), Level.ERROR);
                    WaterPlayer.getToast().setMessage(Component.literal(e.getMessage() == null ? e.getClass().getName() : e.getMessage())).setType(ToastBuilder.Type.ERROR).show(AlinLib.MINECRAFT.getToasts());
                    return;
                }
            }
            assert this.minecraft != null;
            this.minecraft.setScreen(new PlaylistScreen(parent, fileName));
        }));
    }

    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
