package ru.kelcuprum.waterplayer.frontend.gui.screens.search;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.builder.selector.SelectorBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.ButtonSprite;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.base.EditBoxString;
import ru.kelcuprum.alinlib.gui.components.text.MessageBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.frontend.gui.components.TrackButton;

import java.util.ArrayList;
import java.util.List;

import static ru.kelcuprum.alinlib.gui.InterfaceUtils.Icons.RESET;

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

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        try {
            super.render(guiGraphics, i, j, f);
        } catch (Exception e) {
            WaterPlayer.log("Mojang, иди нахуй со своим "+ (e.getLocalizedMessage() == null ? e.getClass().getName() : e.getLocalizedMessage()));
        }
    }

    String[] services = {
            "ytsearch:",
            "ytmsearch:",
            "spsearch:",
            "ymsearch:",
            "scsearch:",
            "amsearch:",
            "dzsearch:"
    };
    protected EditBoxString request;
    protected Button search;
    protected String requestValue = "";
    protected int searchService = WaterPlayer.config.getNumber("SEARCH.LAST_SERVICE", 1).intValue();
    @Override
    protected void init() {
        int x = 5;
        int size = 180;
        addRenderableWidget(new TextBox(x, 15, size, 9, title, true));

        request = new EditBoxString(x + 25, 40, size - 25, 20, Component.translatable("waterplayer.search.query"));
        request.setValue(requestValue);
        request.setResponder((s) -> requestValue = s);
        request.setMaxLength(Integer.MAX_VALUE);
        addRenderableWidget(request);
        addRenderableWidget(new ButtonSprite(x, 40, 20, 20, RESET, Component.translatable("waterplayer.search.last_query"), (e) -> request.setValue(WaterPlayer.config.getString("SEARCH.LAST", ""))));
        this.search = addRenderableWidget(new Button(x, 65, size, 20, Component.translatable("waterplayer.search.button"), (e) -> {
            if (requestValue.isBlank()) {
                WaterPlayer.getToast().setMessage(Localization.getText("waterplayer.load.add.blank")).show(AlinLib.MINECRAFT.getToasts());
                return;
            }
            WaterPlayer.config.setString("SEARCH.LAST", requestValue);
            String value = services[searchService] + requestValue;
            AlinLib.log(value);
            load(value, this);
        }));
        addRenderableWidget(new SelectorBuilder(Component.translatable("waterplayer.search.service")).setValue(searchService).setList(new String[]{
                Component.translatable("waterplayer.config.services.youtube").getString(),
                Component.translatable("waterplayer.config.services.youtube_music").getString(),
                Component.translatable("waterplayer.config.services.spotify").getString(),
                Component.translatable("waterplayer.config.services.yandex").getString(),
                Component.translatable("waterplayer.config.services.soundcloud").getString(),
                Component.translatable("waterplayer.config.services.apple").getString(),
                Component.translatable("waterplayer.config.services.deezer").getString()
        }).setOnPress((e) -> {
            searchService = e.getPosition();
            WaterPlayer.config.setNumber("SEARCH.LAST_SERVICE", searchService);
        }).setPosition(x, 90).setSize(size, 20).build());

        addRenderableWidget(new Button(x, height-25, size, 20, CommonComponents.GUI_BACK, (e) -> onClose()));
        initList();
    }
    private ConfigureScrolWidget scroller;
    private List<AbstractWidget> widgets = new ArrayList<>();
    List<AudioTrack> list = new ArrayList<>();
    public void initList(){
        widgets = new ArrayList<>();
        int x = 195;
        this.scroller = addRenderableWidget(new ConfigureScrolWidget(this.width - 8, 0, 4, this.height, Component.empty(), scroller -> {
            scroller.innerHeight = 5;
            for(AbstractWidget widget : widgets){
                if(widget.visible){
                    widget.setWidth(width-200);
                    widget.setPosition(x, ((int) (scroller.innerHeight - scroller.scrollAmount())));
                    scroller.innerHeight += (widget.getHeight()+5);
                } else widget.setY(-widget.getHeight());
            }
        }));
        widgets.add(new TextBox(x, 5, width-200, 20, Component.translatable("waterplayer.search.result"), true));
        if(list.isEmpty()) widgets.add(new MessageBox(x, 20, width - 200, 20, Component.translatable("waterplayer.search.not_found"), true));
        else for (AudioTrack track : list) widgets.add(new TrackButton(x, 20, width - 200, track, this, false, true));

        int i = 0;
        addRenderableWidgets(widgets);
    }

    protected void rebuildWidgetsList() {
        removeWidget(scroller);
        scroller = null;
        for (AbstractWidget widget : widgets) {
            removeWidget(widget);
        }
        initList();
    }

    protected void addRenderableWidgets(@NotNull List<AbstractWidget> widgets) {
        for (AbstractWidget widget : widgets) {
            this.addRenderableWidget(widget);
        }
    }


    public void load(String url, Screen screen){
        WaterPlayer.player.getAudioPlayerManager().loadItemOrdered(WaterPlayer.player.getAudioPlayerManager(), url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                list = new ArrayList<>();
                list.add(track);
                rebuildWidgetsList();
                WaterPlayer.log("Adding Track: " + track.getInfo().title);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                list = playlist.getTracks();
                rebuildWidgetsList();
            }

            @Override
            public void noMatches() {
                list = new ArrayList<>();
                rebuildWidgetsList();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                list = new ArrayList<>();
                rebuildWidgetsList();
                WaterPlayer.log(exception.getMessage(), Level.ERROR);
            }
        });
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scr = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (!scr && scroller != null) {
            scr = scroller.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return scr;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if(getFocused() != null && getFocused() instanceof EditBox && i == GLFW.GLFW_KEY_ENTER){
            search.onPress();
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void tick(){
        if(scroller != null) scroller.onScroll.accept(scroller);
        super.tick();
    }

    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(parent);
    }
}
