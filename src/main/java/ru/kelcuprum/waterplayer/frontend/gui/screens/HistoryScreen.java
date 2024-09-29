package ru.kelcuprum.waterplayer.frontend.gui.screens;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.Colors;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.text.MessageBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.components.TrackButton;
import ru.kelcuprum.waterplayer.frontend.localization.MusicHelper;

import java.util.ArrayList;
import java.util.List;

public class HistoryScreen extends Screen {
    public final Screen parent;
    public HistoryScreen(Screen parent) {
        super(Component.translatable("waterplayer.history"));
        this.parent = parent;
    }

    @Override
    public void init() {
        initPanel();
        initList();
    }

    protected EditBox request;
    protected Button search;
    protected String requestValue = "";

    public void initPanel() {
        int x = 10;
        int size = 200;
        addRenderableWidget(new TextBox(x, 5, size, 20, title, true));

        request = (EditBox) new EditBoxBuilder(Component.translatable("waterplayer.search.query")).setPosition(x, 35).setSize(size, 20).build();
        request.setValue(requestValue);
        request.setResponder((s) -> requestValue = s);
        request.setMaxLength(Integer.MAX_VALUE);
        addRenderableWidget(request);
        this.search = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("waterplayer.search.button"), (e) -> {
            rebuildWidgets();
        }).setPosition(x, 60).setSize(size / 2 - 1, 20).build());

        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_BACK, (e) -> onClose()).setPosition(size / 2 + 1 + x, 60).setSize(size / 2 - 1, 20).build());
    }

    private List<AbstractWidget> widgets = new ArrayList<>();

    private ConfigureScrolWidget scroller;
    public List<AudioTrack> getTracks(){
        List<AudioTrack> tracks = new ArrayList<>();
        String rv = requestValue.toLowerCase();
        for(AudioTrack track : WaterPlayer.history.getTracks()){
            if(requestValue.isBlank() ||
                    (track.getInfo().uri.toLowerCase().contains(rv) || track.getInfo().title.toLowerCase().contains(rv) || track.getInfo().author.toLowerCase().contains(rv)
                            || MusicHelper.getServiceName(MusicHelper.getService(track)).getString().toLowerCase().contains(rv))
                    || track.getSourceManager().getSourceName().toLowerCase().contains(rv)) tracks.add(track);
        }
        return tracks;
    }
    public void initList() {
        widgets = new ArrayList<>();
        int x = 220;
        this.scroller = addRenderableWidget(new ConfigureScrolWidget(this.width - 8, 0, 4, this.height, Component.empty(), scroller -> {
            scroller.innerHeight = 5;
            for (AbstractWidget widget : widgets) {
                if (widget.visible) {
                    widget.setWidth(width - 225);
                    widget.setPosition(x, ((int) (scroller.innerHeight - scroller.scrollAmount())));
                    scroller.innerHeight += (widget.getHeight() + 5);
                } else widget.setY(-widget.getHeight());
            }
        }));
        widgets.add(new TextBox(x, 5, width - 220, 20, Component.translatable("waterplayer.search.result"), true));
        List<AudioTrack> list = getTracks();
        if (list.isEmpty())
            widgets.add(new MessageBox(x, 20, width - 220, 20, Component.translatable("waterplayer.search.not_found"), true));
        else for (AudioTrack track : list) widgets.add(new TrackButton(x, 20, width - 220, track, this, false));

        int i = 0;
        addRenderableWidgets(widgets);
    }
    protected void addRenderableWidgets(@NotNull List<AbstractWidget> widgets) {
        for (AbstractWidget widget : widgets) this.addRenderableWidget(widget);
    }

    @Override
    //#if MC >=12002
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scr = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (!scr && scroller != null) scr = scroller.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        return scr;
    }
    //#elseif MC < 12002
    //$$ public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
    //$$     boolean scr = super.mouseScrolled(mouseX, mouseY, scrollY);
    //$$     if (!scr && scroller != null)  scr = scroller.mouseScrolled(mouseX, mouseY, scrollY);
    //$$     return scr;
    //$$ }
    //#endif

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if(getFocused() != null && getFocused() instanceof EditBox && i == GLFW.GLFW_KEY_ENTER){
            search.onPress();
            return true;
        }
        return super.keyPressed(i, j, k);
    }
    public List<AudioTrack> lastTracks;
    protected void rebuildWidgetsList() {
        removeWidget(scroller);
        scroller = null;
        for (AbstractWidget widget : widgets) {
            removeWidget(widget);
        }
        initList();
    }
    @Override
    public void tick(){
        if(scroller != null) scroller.onScroll.accept(scroller);
        if (lastTracks != WaterPlayer.history.getTracks()) {
            lastTracks = WaterPlayer.history.getTracks();
            rebuildWidgetsList();
        }
        super.tick();
    }

    @Override
    //#if MC >=12002
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        //#elseif MC < 12002
        //$$ public void renderBackground(GuiGraphics guiGraphics) {
        //$$         super.renderBackground(guiGraphics);
        //#endif
        guiGraphics.fill(5, 5, 215, 25, Colors.BLACK_ALPHA);
        guiGraphics.fill(5, 30, 215, 85, Colors.BLACK_ALPHA);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        //#if MC < 12002
        //$$     renderBackground(guiGraphics);
        //#endif
        super.render(guiGraphics, i, j, f);
    }

    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
