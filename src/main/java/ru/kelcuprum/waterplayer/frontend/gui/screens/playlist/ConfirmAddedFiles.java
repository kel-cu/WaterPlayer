package ru.kelcuprum.waterplayer.frontend.gui.screens.playlist;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.text.MessageBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;

import java.nio.file.Path;
import java.util.List;

public class ConfirmAddedFiles extends Screen {
    protected final List<Path> list;
    protected final Screen parent;
    protected final Playlist playlist;

    protected ConfirmAddedFiles(List<Path> list, Screen parent, Playlist playlist) {
        super(Component.translatable("waterplayer.playlist.add_files"));
        this.list = list;
        this.parent = parent;
        this.playlist = playlist;
    }

    @Override
    protected void init() {
        addRenderableWidget(new TextBox(10, 25, width-20, 20, title, true));
        StringBuilder name = new StringBuilder();
        int i = 0;
        for(Path path : list){
            if(i>5){
                name.append("\n").append(Component.translatable("waterplayer.load.load_files.count_files", list.size()-6).getString());
                break;
            } else if(i == 0) name = new StringBuilder(path.getFileName().toString());
            else name.append("\n").append(path.getFileName().toString());
            i++;
        }
        AbstractWidget msgBx = addRenderableWidget(new MessageBox(10, 55, width-20, height-80, Component.literal(name.toString()), true));
        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_YES, (s) -> {
            for(Path path : list) playlist.addUrl(path.toString());
            onClose();
        }).setPosition(width/2-80, msgBx.getY()+msgBx.getHeight()+10).setSize(75, 20).build());
        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_NO, (s) -> onClose()).setPosition(width/2+5, msgBx.getY()+msgBx.getHeight()+10).setSize(75, 20).build());
    }
    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
