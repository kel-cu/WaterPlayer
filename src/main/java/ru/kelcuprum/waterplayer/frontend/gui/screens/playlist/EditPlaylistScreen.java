package ru.kelcuprum.waterplayer.frontend.gui.screens.playlist;

import com.google.gson.JsonElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBooleanBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.text.TextBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.playlist.Playlist;
import ru.kelcuprum.waterplayer.frontend.gui.TextureHelper;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ru.kelcuprum.alinlib.gui.Icons.*;
import static ru.kelcuprum.waterplayer.WaterPlayer.Icons.*;

public class EditPlaylistScreen extends Screen {
    @NotNull
    protected final Playlist playlist;
    private Screen parent;
    //
    boolean isEnable = false;
    boolean isInit = false;
    public EditPlaylistScreen(Screen parent, @NotNull Playlist playlist) {
        super(Component.empty());
        this.playlist = playlist;
        this.parent = parent;
    }

    // Ахуеть

    public void showOpenFileDialog(){
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

        String result = TinyFileDialogs.tinyfd_openFileDialog(Component.translatable("waterplayer.playlist.edit.filedialog.title").getString(), defaultString, filters, Component.translatable("waterplayer.playlist.edit.filedialog.filter_description").getString(), false);
        if(result == null) return;
        File file = new File(result);
        WaterPlayer.log(result);
        if(file.exists()) playlist.setIcon(file);
    }

    @Override
    public void onFilesDrop(List<Path> list) {
        if(isValidImageType(list.get(0).toFile().getAbsolutePath())){
            File file = list.get(0).toFile();
            WaterPlayer.log(file.getName());
            if(file.exists()) playlist.setIcon(file);
        } else {
            if (list.size() == 1) playlist.addUrl(list.get(0).toString());
            else AlinLib.MINECRAFT.setScreen(new ConfirmAddedFiles(list, this, playlist));
        }
    }
    public boolean isValidImageType(String name){
        name=name.toLowerCase();
        return (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));
    }
    //

    @Override
    protected void init() {
        initPanel();
        initTracks();
    }
    public Button icon;
    public boolean isDeleted = false;
    public int maxY = 35;
    public void initPanel(){
        int x = 10;
        int size = 200;
        addRenderableWidget(new TextBuilder(Component.translatable("waterplayer.playlist.edit")).setPosition(x, 5).setSize(size, 20).build());
        int y = 35;
        // x, y, 36, 36, getIcon(), 36, 36,
        icon = (Button) addRenderableWidget(new ButtonBuilder(Component.empty(), (s) -> showOpenFileDialog()).setSprite(getIcon()).setPosition(x, y).setSize(36, 36).build());

        addRenderableWidget(new TextBuilder(Component.translatable("waterplayer.playlist.edit.drag_and_drop"), (s) -> showOpenFileDialog()).setStyle(GuiUtils.getStyleByID("flat")).setPosition(x+41, y).setSize(size-41, 18).build());
        addRenderableWidget(new TextBuilder(Component.translatable("waterplayer.playlist.edit.drag_and_drop.second"), (s) -> showOpenFileDialog()).setStyle(GuiUtils.getStyleByID("flat")).setPosition(x+41, y+18).setSize(size-41, 18).build());
        y+=41;
        addRenderableWidget(new EditBoxBuilder(Component.translatable("waterplayer.playlist.title"), (s) -> {
            playlist.title = s;
            playlist.save();
        }).setSecret(false).setValue(playlist.title).setPosition(x, y).setSize(size, 20).build());
        y+=25;
        addRenderableWidget(new EditBoxBuilder(Component.translatable("waterplayer.playlist.author"), (s) -> {
            playlist.author = s;
            playlist.save();
        }).setSecret(false).setValue(playlist.author).setPosition(x, y).setSize(size, 20).build());
        y+=25;
        addRenderableWidget(new ButtonBooleanBuilder(Component.translatable("waterplayer.playlist.public"), playlist.isPublic, (s) -> {
            playlist.isPublic = s;
            playlist.save();
        }).setSize(size, 20).setPosition(x, y).build());
        y+=25;

            addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.playlist.save"), (s) -> {
                playlist.save();
                onClose();
            }).setPosition(x, y).setSize(size, 20).build());
        y+=25;
            addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_BACK, (e) -> onClose()).setPosition(x, y).setSize(size-25, 20).build());
        addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.playlist.remove"), (s) -> {
            playlist.path.toFile().delete();
            isDeleted = true;
            if(parent instanceof ViewPlaylistScreen){
                this.parent = ((ViewPlaylistScreen) parent).parent;
                onClose();
            }
        }).setSprite(RECYCLE_BIN).setPosition(x+size-20, y).setSize(20, 20).build());
        y+=25;
        maxY = y;
    }

    private ConfigureScrolWidget scroller;
    private List<AbstractWidget> widgets = new ArrayList<>();
    public void initTracks(){
        widgets = new ArrayList<>();
        this.scroller = addRenderableWidget(new ConfigureScrolWidget(this.width - 4, 0, 4, this.height, Component.empty(), scroller -> {
            scroller.innerHeight = 5;
            for (AbstractWidget widget : widgets) {
                if (widget.visible) {
                    widget.setY((int) (scroller.innerHeight - scroller.scrollAmount()));
                    scroller.innerHeight += (widget.getHeight() + 5);
                } else widget.setY(-widget.getHeight());
            }
        }));
        int x = 220;
        widgets.add(new TextBuilder(Component.translatable("waterplayer.playlist.urls")).setPosition(x, 5).setSize(width-225, 20).build());
        int i = 0;
        for (JsonElement element : playlist.getUrlsJSON()) {
            String url = element.getAsString();
            int finalI = i;
            widgets.add(new EditBoxBuilder(Component.literal(String.format("%s. ", i + 1)), (s) -> {
                playlist.setUrl(s, finalI);
                playlist.save();
            })
                    .setSecret(false)
                    .setValue(url)
                    .setPosition(x, -20)
                    .setSize(width - 225, 20)
                    .build());
            i++;
        }
        widgets.add(new ButtonBuilder(Component.translatable("waterplayer.playlist.add"), (e) -> {
            playlist.urls.add("https://youtube.com/");
            playlist.save();
        }).setIcon(ADD).setPosition(x, -20).setSize(width - 225, 20).build());
        addRenderableWidgets(widgets);
    }

    protected void addRenderableWidgets(@NotNull List<AbstractWidget> widgets) {
        for (AbstractWidget widget : widgets) {
            this.addRenderableWidget(widget);
        }
    }

    // - icon

    public ResourceLocation getIcon(){
        if(playlist == null) return NO_PLAYLIST_ICON;
        return playlist.icon == null ? NO_PLAYLIST_ICON : TextureHelper.getTexture$Base64(playlist.icon, String.format("playlist-%s", playlist.fileName));
    }

    // - Tick
    public ResourceLocation lastIcon = getIcon();
    private int lastSize = 0;
    @Override
    public void tick() {
        if(lastIcon != getIcon()){
            lastIcon = getIcon();
            removeWidget(icon);
            icon = (Button) addRenderableWidget(new ButtonBuilder(Component.empty(), (s) -> showOpenFileDialog()).setSprite(getIcon()).setPosition(10, 35).setSize(36, 36).build());
        }
        if (scroller != null) scroller.onScroll.accept(scroller);
        if (lastSize != playlist.getUrlsJSON().size()) {
            if (getFocused() == null || !(getFocused().isFocused() && (getFocused() instanceof EditBox))) {
                lastSize = playlist.getUrlsJSON().size();
                rebuildWidgetsList();
            }
        }
        super.tick();
    }

    protected void rebuildWidgetsList() {
        removeWidget(scroller);
        scroller = null;
        for (AbstractWidget widget : widgets) {
            removeWidget(widget);
        }
        initTracks();
    }

    // - RENDER

    //#if MC >= 12002
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        //#elseif MC < 12002
        //$$ public void renderBackground(GuiGraphics guiGraphics) {
        //$$         super.renderBackground(guiGraphics);
        //#endif
        GuiUtils.getSelected().renderTitleBackground(guiGraphics, 5, 5, 215, 25);
        GuiUtils.getSelected().renderBackground(guiGraphics, 5, 30, 215, maxY);
    }

    //#if MC < 12002
    //$$ @Override
    //$$ public void render(GuiGraphics guiGraphics, int i, int j, float f) {
    //$$     renderBackground(guiGraphics);
    //$$     super.render(guiGraphics, i, j, f);
    //$$ }
    //#endif

    //#if MC >=12002
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scr = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (!scr && scroller != null) scr = scroller.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        return scr;
    }
    //#elseif MC < 12002
    //$$ public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
    //$$     boolean scr = super.mouseScrolled(mouseX, mouseY, scrollY);
    //$$     if (!scr && scroller != null) scr = scroller.mouseScrolled(mouseX, mouseY, scrollY);
    //$$     return scr;
    //$$ }
    //#endif

    // - Close
    public void onClose() {
        if(!isDeleted) playlist.save();
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
