package ru.kelcuprum.waterplayer.frontend.gui.screens.editor;

import com.google.gson.JsonElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.images.StandardArtwork;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.Colors;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.ImageWidget;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.text.MessageBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.AbstractConfigScreen;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.TextureHelper;
import ru.kelcuprum.waterplayer.frontend.gui.screens.config.PlaylistsScreen;
import ru.kelcuprum.waterplayer.frontend.gui.screens.playlist.ConfirmAddedFiles;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ru.kelcuprum.alinlib.gui.Icons.*;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.NO_ICON;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.NO_PLAYLIST_ICON;

public class TrackEditorScreen extends Screen {
    @NotNull
    protected final File file;
    protected AudioFile audioFile;
    public final Screen parent;

    public TrackEditorScreen(Screen parent, File file) {
        super(Component.empty());
        this.parent = parent;
        this.file = file;
        try{
            this.audioFile = AudioFileIO.read(file);
        } catch (Exception ex){
            ex.printStackTrace();
            WaterPlayer.getToast().setIcon(DONT).setType(ToastBuilder.Type.ERROR).setMessage(ex.getMessage() == null ? Component.translatable("waterplayer.editor.dont_open") : Component.literal(ex.getMessage())).buildAndShow();
            AlinLib.MINECRAFT.setScreen(parent);
        }
    }

    @Override
    protected void init() {
        initPanel();
        initLyrics();
    }

    public Button icon;
    public int panelHeight = 0;
    public void initPanel() {
        int x = 10;
        int size = 200;
        addRenderableWidget(new TextBox(x, 5, size, 20, Component.translatable("waterplayer.editor"), true));
        addRenderableWidget(new TextBox(x, 30, size, 20, Component.translatable("waterplayer.editor.file", file.getName()), true));
        int y = 60;
        icon = (Button) addRenderableWidget(new ButtonBuilder(Component.empty(), (s) -> showOpenFileDialog$icon()).setSprite(getIcon()).setPosition(x, y).setSize(36, 36).build());
        addRenderableWidget(new TextBox(x+41, y, size-41, 18, Component.translatable("waterplayer.playlist.edit.drag_and_drop"), false, (s) -> showOpenFileDialog$icon()));
        addRenderableWidget(new TextBox(x+41, y+18, size-41, 18, Component.translatable("waterplayer.playlist.edit.drag_and_drop.second"), false, (s) -> showOpenFileDialog$icon()));
        y += 41;
        addRenderableWidget(new EditBoxBuilder(Component.translatable("waterplayer.playlist.title"), (s) -> {
            try {
                audioFile.getTag().setField(FieldKey.TITLE, s);
            } catch (Exception ex){
                ex.printStackTrace();
                WaterPlayer.getToast().setIcon(DONT).setType(ToastBuilder.Type.ERROR).setMessage(ex.getMessage() == null ? Component.translatable("waterplayer.editor.error") : Component.literal(ex.getMessage())).buildAndShow();
                AlinLib.MINECRAFT.setScreen(parent);
            }
        }).setSecret(false).setValue(audioFile.getTag().getFirst(FieldKey.TITLE)).setPosition(x, y).setSize(size, 20).build());
        y+=25;
        addRenderableWidget(new EditBoxBuilder(Component.translatable("waterplayer.playlist.author"), (s) -> {
            try {
                audioFile.getTag().setField(FieldKey.ARTIST, s);
            } catch (Exception ex){
                ex.printStackTrace();
                WaterPlayer.getToast().setIcon(DONT).setType(ToastBuilder.Type.ERROR).setMessage(ex.getMessage() == null ? Component.translatable("waterplayer.editor.error") : Component.literal(ex.getMessage())).buildAndShow();
                AlinLib.MINECRAFT.setScreen(parent);
            }
        }).setSecret(false).setValue(audioFile.getTag().getFirst(FieldKey.ARTIST)).setPosition(x, y).setSize(size, 20).build());
        y+=25;
        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.editor.save"), (s) -> {
            try {
                audioFile.commit();
                if(artworkChanged){
                    artworkChanged = false;
                    TextureHelper.removeTexture$File(file);
                }
            } catch (Exception ex){
                ex.printStackTrace();
                WaterPlayer.getToast().setIcon(DONT).setType(ToastBuilder.Type.ERROR).setMessage(ex.getMessage() == null ? Component.translatable("waterplayer.editor.error") : Component.literal(ex.getMessage())).buildAndShow();
                AlinLib.MINECRAFT.setScreen(parent);
            }
        }).setPosition(x, y).setSize(size, 20).build());
        y+=25;
        TextBox msg = addRenderableWidget(new TextBox(x, y, size, 20, Component.translatable("waterplayer.editor.upload_text"), false, (s) -> showOpenFileDialog$lyrics()));
        y+=25;
        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_BACK, (e) -> onClose()).setPosition(x, y).setSize(size, 20).build());
        y+=25;
        panelHeight = y;
    }
    public void initLyrics(){
        widgets = new ArrayList<>();
        this.scroller = addRenderableWidget(new ConfigureScrolWidget(this.width - 8, 0, 4, this.height, Component.empty(), scroller -> {
            scroller.innerHeight = 5;
            for (AbstractWidget widget : widgets) {
                if (widget.visible) {
                    widget.setY((int) (scroller.innerHeight - scroller.scrollAmount()));
                    scroller.innerHeight += (widget.getHeight() + 5);
                } else widget.setY(-widget.getHeight());
            }
        }));
        int x = 220;
        widgets.add(new TextBox(x, 5, width - 225, 20, Component.translatable("waterplayer.editor.lyrics"), true));
        if(audioFile.getTag().getFirst(FieldKey.LYRICS).isBlank()) widgets.add(new MessageBox(x, 5, width - 225, 20, Component.translatable("waterplayer.editor.lyrics.no"), true));
        else widgets.add(new MessageBox(x, 5, width - 225, 20, Component.literal(audioFile.getTag().getFirst(FieldKey.LYRICS)), false));
        addRenderableWidgets(widgets);
    }

    @Override
    public void onFilesDrop(List<Path> list) {
        if(isValidImageType(list.get(0).toFile().getAbsolutePath())){
            File file = list.get(0).toFile();
            try{
                if(file.exists()) {
                    audioFile.getTag().deleteArtworkField();
                    audioFile.getTag().setField(StandardArtwork.createArtworkFromFile(file));
                    artworkChanged = true;
                }
            } catch (Exception ex){
                ex.printStackTrace();
                WaterPlayer.getToast().setIcon(DONT).setType(ToastBuilder.Type.ERROR).setMessage(ex.getMessage() == null ? Component.translatable("waterplayer.editor.error") : Component.literal(ex.getMessage())).buildAndShow();
            }
        } else if(isValidLyricsType(list.get(0).toFile().getAbsolutePath())) {
            File file = list.get(0).toFile();
            try{
                if(file.exists()) {
                    audioFile.getTag().deleteField(FieldKey.LYRICS);
                    audioFile.getTag().setField(FieldKey.LYRICS, Files.readString(file.toPath()));
                    rebuildWidgetsList();
                }
            } catch (Exception ex){
                ex.printStackTrace();
                WaterPlayer.getToast().setIcon(DONT).setType(ToastBuilder.Type.ERROR).setMessage(ex.getMessage() == null ? Component.translatable("waterplayer.editor.dont_open") : Component.literal(ex.getMessage())).buildAndShow();
            }
        }
    }
    public boolean isValidImageType(String name){
        name=name.toLowerCase();
        return (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));
    }
    public boolean isValidLyricsType(String name){
        name=name.toLowerCase();
        return (name.endsWith(".txt") || name.endsWith(".srt") || name.endsWith(".lrc"));
    }
    boolean artworkChanged = false;
    public void showOpenFileDialog$icon(){
        MemoryStack stack = MemoryStack.stackPush();
        PointerBuffer filters = stack.mallocPointer(3);
        filters.put(stack.UTF8("*.png"));
        filters.put(stack.UTF8("*.jpg"));
        filters.put(stack.UTF8("*.jpeg"));

        filters.flip();
        File defaultPath = new File(System.getProperty("user.home")).getAbsoluteFile();
        String defaultString = defaultPath.getAbsolutePath();
        if(defaultPath.isDirectory() && !defaultString.endsWith(File.separator)){
            defaultString += File.separator;
        }

        String result = TinyFileDialogs.tinyfd_openFileDialog(Component.translatable("waterplayer.editor.selector.icon").getString(), defaultString, filters, Component.translatable("waterplayer.editor.selector.icon.filter_description").getString(), false);
        if(result == null) return;
        File file = new File(result);
        WaterPlayer.log(result);
        try{
            if(file.exists()) {
                audioFile.getTag().deleteArtworkField();
                audioFile.getTag().setField(StandardArtwork.createArtworkFromFile(file));
                artworkChanged = true;
            }
        } catch (Exception ex){
            ex.printStackTrace();
            WaterPlayer.getToast().setIcon(DONT).setType(ToastBuilder.Type.ERROR).setMessage(ex.getMessage() == null ? Component.translatable("waterplayer.editor.dont_open") : Component.literal(ex.getMessage())).buildAndShow();
        }
    }

    public void showOpenFileDialog$lyrics(){
        MemoryStack stack = MemoryStack.stackPush();
        PointerBuffer filters = stack.mallocPointer(3);
        filters.put(stack.UTF8("*.txt"));
        filters.put(stack.UTF8("*.srt"));
        filters.put(stack.UTF8("*.lrc"));

        filters.flip();
        File defaultPath = new File(System.getProperty("user.home")).getAbsoluteFile();
        String defaultString = defaultPath.getAbsolutePath();
        if(defaultPath.isDirectory() && !defaultString.endsWith(File.separator)){
            defaultString += File.separator;
        }

        String result = TinyFileDialogs.tinyfd_openFileDialog(Component.translatable("waterplayer.editor.selector.lyrics").getString(), defaultString, filters, Component.translatable("waterplayer.editor.selector.lyrics.filter_description").getString(), false);
        if(result == null) return;
        File file = new File(result);
        WaterPlayer.log(result);
        try{
            if(file.exists()) {
                audioFile.getTag().deleteField(FieldKey.LYRICS);
                audioFile.getTag().setField(FieldKey.LYRICS, Files.readString(file.toPath()));
                rebuildWidgetsList();
            }
        } catch (Exception ex){
            ex.printStackTrace();
            WaterPlayer.getToast().setIcon(DONT).setType(ToastBuilder.Type.ERROR).setMessage(ex.getMessage() == null ? Component.translatable("waterplayer.editor.dont_open") : Component.literal(ex.getMessage())).buildAndShow();
            AlinLib.MINECRAFT.setScreen(parent);
        }
    }

    private ConfigureScrolWidget scroller;
    private List<AbstractWidget> widgets = new ArrayList<>();

    protected void addRenderableWidgets(@NotNull List<AbstractWidget> widgets) {
        for (AbstractWidget widget : widgets) this.addRenderableWidget(widget);
    }

    // - icon

    public ResourceLocation getIcon() {
        if(file == null || !file.exists()) return NO_ICON;
        return TextureHelper.getTexture$File(file, "local_"+file.getAbsolutePath());
    }

    // - Tick
    public ResourceLocation lastIcon = getIcon();

    @Override
    public void tick() {
        if (lastIcon != getIcon()) {
            lastIcon = getIcon();
            removeWidget(icon);
            icon = (Button) addRenderableWidget(new ButtonBuilder(Component.empty(), (s) -> showOpenFileDialog$icon()).setSprite(getIcon()).setPosition(10, 60).setSize(36, 36).build());
        }
        if (scroller != null) scroller.onScroll.accept(scroller);
        super.tick();
    }

    protected void rebuildWidgetsList() {
        removeWidget(scroller);
        scroller = null;
        for (AbstractWidget widget : widgets) {
            removeWidget(widget);
        }
        initLyrics();
    }

    // - RENDER

    //#if MC >= 12002
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        //#elseif MC < 12002
        //$$ public void renderBackground(GuiGraphics guiGraphics) {
        //$$         super.renderBackground(guiGraphics);
        //#endif
        guiGraphics.fill(5, 5, 215, 25, Colors.BLACK_ALPHA);
        guiGraphics.fill(5, 30, 215, 50, Colors.BLACK_ALPHA);
        guiGraphics.fill(5, 55, 215, panelHeight, Colors.BLACK_ALPHA);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
    //#if MC < 12002
    //$$     renderBackground(guiGraphics);
    //#endif
        super.render(guiGraphics, i, j, f);
    }

    //#if MC >=12002
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scr = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (!scr && scroller != null) {
            scr = scroller.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return scr;
    }
    //#elseif MC < 12002
    //$$ public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
    //$$     boolean scr = super.mouseScrolled(mouseX, mouseY, scrollY);
    //$$     if (!scr && scroller != null) {
    //$$         scr = scroller.mouseScrolled(mouseX, mouseY, scrollY);
    //$$     }
    //$$     return scr;
    //$$ }
    //#endif

    // - Close
    public void onClose() {
        if (parent instanceof AbstractConfigScreen) PlaylistsScreen.assetsSize = 0;
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == GLFW.GLFW_KEY_ESCAPE) {
            if (getFocused() != null && getFocused().isFocused()) {
                getFocused().setFocused(false);
                return true;
            }
        }
        return super.keyPressed(i, j, k);
    }
}
