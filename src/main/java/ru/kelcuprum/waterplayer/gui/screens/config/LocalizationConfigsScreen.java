package ru.kelcuprum.waterplayer.gui.screens.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.EditBoxString;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.localization.Localization;

public class LocalizationConfigsScreen extends Screen {
    private final Screen parent;
    private static final Component TITLE = Localization.getText("waterplayer.name");
    // CATEGORYES
    private static final Component MainConfigCategory = Localization.getText("waterplayer.config");
    private Button MainConfigCategoryButton;
    private static final Component LocalizationConfigCategory = Localization.getText("waterplayer.config.localization");
    private Button LocalizationConfigCategoryButton;
    private static final Component SecretConfigCategory = Localization.getText("waterplayer.secret");
    private Button SecretConfigCategoryButton;
    // CATEGORY CONTENT
    private TextBox titleBox;

    private TextBox titleBossBar;
    private Component titleBossBarText = Localization.getText("waterplayer.config.localization.title.bossbar");
    private EditBoxString bossBar;
    private Component bossBarText = Localization.getText("waterplayer.config.localization.bossbar");
    private EditBoxString bossBarWithoutAuthor;
    private Component bossBarWithoutAuthorText = Localization.getText("waterplayer.config.localization.bossbar.withoutAuthor");
    private EditBoxString bossBarLive;
    private Component bossBarLiveText = Localization.getText("waterplayer.config.localization.bossbar.live");
    private EditBoxString bossBarLiveWithoutAuthor;
    private Component bossBarLiveWithoutAuthorText = Localization.getText("waterplayer.config.localization.bossbar.live.withoutAuthor");
    private EditBoxString bossBarPause;
    private Component bossBarPauseText = Localization.getText("waterplayer.config.localization.bossbar.pause");

    private TextBox titleOverlay;
    private Component titleOverlayText = Localization.getText("waterplayer.config.localization.title.title");
    private EditBoxString overlay;
    private Component overlayText = Localization.getText("waterplayer.config.localization.title");
    private EditBoxString overlayWithoutAuthor;
    private Component overlayWithoutAuthorText = Localization.getText("waterplayer.config.localization.title.withoutAuthor");
    private EditBoxString overlayLive;
    private Component overlayLiveText = Localization.getText("waterplayer.config.localization.title.live");
    private EditBoxString overlayLiveWithoutAuthor;
    private Component overlayLiveWithoutAuthorText = Localization.getText("waterplayer.config.localization.title.live.withoutAuthor");
    private EditBoxString overlayPause;
    private Component overlayPauseText = Localization.getText("waterplayer.config.localization.title.pause");

    private TextBox titleFormats;
    private Component titleFormatsText = Localization.getText("waterplayer.config.localization.title.formats");
    private EditBoxString author;
    private Component authorText = Localization.getText("waterplayer.config.localization.format.author");
    private EditBoxString titleTrack;
    private Component titleText = Localization.getText("waterplayer.config.localization.format.title");
    private EditBoxString time;
    private Component timeText = Localization.getText("waterplayer.config.localization.format.time");
    private EditBoxString live;
    private Component liveText = Localization.getText("waterplayer.config.localization.format.live");
    //
    private static final Component EXIT = Localization.getText("waterplayer.screen.exit");
    private int scrolled = 0;

    public LocalizationConfigsScreen(Screen parent) {
        super(LocalizationConfigCategory);
        this.parent = parent;
    }

    public void tick() {
        this.titleBox.setYPos(15 - this.scrolled);

        this.titleBossBar.setYPos(40 - this.scrolled);
        this.bossBar.setYPos(65 - this.scrolled);
        this.bossBarWithoutAuthor.setYPos(90 - this.scrolled);
        this.bossBarLive.setYPos(115 - this.scrolled);
        this.bossBarLiveWithoutAuthor.setYPos(140 - this.scrolled);
        this.bossBarPause.setYPos(165 - this.scrolled);

        this.titleOverlay.setYPos(190 - this.scrolled);
        this.overlay.setYPos(215 - this.scrolled);
        this.overlayWithoutAuthor.setYPos(240 - this.scrolled);
        this.overlayLive.setYPos(265 - this.scrolled);
        this.overlayLiveWithoutAuthor.setYPos(290 - this.scrolled);
        this.overlayPause.setYPos(315 - this.scrolled);

        this.titleFormats.setYPos(340 - this.scrolled);
        this.author.setYPos(365 - this.scrolled);
        this.titleTrack.setYPos(390 - this.scrolled);
        this.time.setYPos(415 - this.scrolled);
        this.live.setYPos(440 - this.scrolled);
        super.tick();
    }

    public void init() {
        this.scrolled = 0;
        this.initButton();
        this.initButtonsCategory();
    }

    private void initButtonsCategory() {
        int x = this.width - 150;
        this.titleBox = this.addRenderableWidget(new TextBox(140, 15, x, 9, this.title, true));
        //

        this.titleBossBar = this.addRenderableWidget(new TextBox(140, 40, x, 20, this.titleBossBarText, true));

        this.bossBar = new EditBoxString(140, 65, x, 20, this.bossBarText);
        this.bossBar.setContent(Localization.getLocalization("bossbar", false));
        this.bossBar.setResponse(s->{
            Localization.setLocalization("bossbar", s);
        });
        this.addRenderableWidget(bossBar);

        this.bossBarWithoutAuthor = new EditBoxString(140, 90, x, 20, this.bossBarWithoutAuthorText);
        this.bossBarWithoutAuthor.setContent(Localization.getLocalization("bossbar.withoutAuthor", false));
        this.bossBarWithoutAuthor.setResponse(s->{
            Localization.setLocalization("bossbar.withoutAuthor", s);
        });
        this.addRenderableWidget(bossBarWithoutAuthor);

        this.bossBarLive = new EditBoxString(140, 115, x, 20, this.bossBarLiveText);
        this.bossBarLive.setContent(Localization.getLocalization("bossbar.live", false));
        this.bossBarLive.setResponse(s->{
            Localization.setLocalization("bossbar.live", s);
        });
        this.addRenderableWidget(bossBarLive);

        this.bossBarLiveWithoutAuthor = new EditBoxString(140, 140, x, 20, this.bossBarLiveWithoutAuthorText);
        this.bossBarLiveWithoutAuthor.setContent(Localization.getLocalization("bossbar.live.withoutAuthor", false));
        this.bossBarLiveWithoutAuthor.setResponse(s->{
            Localization.setLocalization("bossbar.live.withoutAuthor", s);
        });
        this.addRenderableWidget(bossBarLiveWithoutAuthor);

        this.bossBarPause = new EditBoxString(140, 165, x, 20, this.bossBarPauseText);
        this.bossBarPause.setContent(Localization.getLocalization("bossbar.pause", false));
        this.bossBarPause.setResponse(s->{
            Localization.setLocalization("bossbar.pause", s);
        });
        this.addRenderableWidget(bossBarPause);
        //
        this.titleOverlay = this.addRenderableWidget(new TextBox(140, 190, x, 20, this.titleOverlayText, true));

        this.overlay = new EditBoxString(140, 215, x, 20, this.overlayText);
        this.overlay.setContent(Localization.getLocalization("title", false));
        this.overlay.setResponse(s->{
            Localization.setLocalization("title", s);
        });
        this.addRenderableWidget(overlay);

        this.overlayWithoutAuthor = new EditBoxString(140, 240, x, 20, this.overlayWithoutAuthorText);
        this.overlayWithoutAuthor.setContent(Localization.getLocalization("title.withoutAuthor", false));
        this.overlayWithoutAuthor.setResponse(s->{
            Localization.setLocalization("title.withoutAuthor", s);
        });
        this.addRenderableWidget(overlayWithoutAuthor);

        this.overlayLive = new EditBoxString(140, 265, x, 20, this.overlayLiveText);
        this.overlayLive.setContent(Localization.getLocalization("title.live", false));
        this.overlayLive.setResponse(s->{
            Localization.setLocalization("title.live", s);
        });
        this.addRenderableWidget(overlayLive);

        this.overlayLiveWithoutAuthor = new EditBoxString(140, 290, x, 20, this.overlayLiveWithoutAuthorText);
        this.overlayLiveWithoutAuthor.setContent(Localization.getLocalization("title.live.withoutAuthor", false));
        this.overlayLiveWithoutAuthor.setResponse(s->{
            Localization.setLocalization("title.live.withoutAuthor", s);
        });
        this.addRenderableWidget(overlayLiveWithoutAuthor);

        this.overlayPause = new EditBoxString(140, 315, x, 20, this.overlayPauseText);
        this.overlayPause.setContent(Localization.getLocalization("title.pause", false));
        this.overlayPause.setResponse(s->{
            Localization.setLocalization("title.pause", s);
        });
        this.addRenderableWidget(overlayPause);
        //
        this.titleFormats = this.addRenderableWidget(new TextBox(140, 340, x, 20, this.titleFormatsText, true));

        this.author = new EditBoxString(140, 365, x, 20, this.authorText);
        this.author.setContent(Localization.getLocalization("format.author", false));
        this.author.setResponse(s->{
            Localization.setLocalization("format.author", s);
        });
        this.addRenderableWidget(author);

        this.titleTrack = new EditBoxString(140, 390, x, 20, this.titleText);
        this.titleTrack.setContent(Localization.getLocalization("format.title", false));
        this.titleTrack.setResponse(s->{
            Localization.setLocalization("format.title", s);
        });
        this.addRenderableWidget(titleTrack);

        this.time = new EditBoxString(140, 415, x, 20, this.timeText);
        this.time.setContent(Localization.getLocalization("format.time", false));
        this.time.setResponse(s->{
            Localization.setLocalization("format.time", s);
        });
        this.addRenderableWidget(time);

        this.live = new EditBoxString(140, 440, x, 20, this.liveText);
        this.live.setContent(Localization.getLocalization("format.live", false));
        this.live.setResponse(s->{
            Localization.setLocalization("format.live", s);
        });
        this.addRenderableWidget(live);

    }

    private void initButton() {
        this.MainConfigCategoryButton = this.addRenderableWidget(new Button(10, 40, 110, 20, MainConfigCategory, (OnPress) -> {
            this.minecraft.setScreen(new MainConfigsScreen(this.parent));
        }));
        this.LocalizationConfigCategoryButton = this.addRenderableWidget(new Button(10, 65, 110, 20, LocalizationConfigCategory, (OnPress) -> {
            this.minecraft.setScreen(new LocalizationConfigsScreen(this.parent));
        }));
        this.SecretConfigCategoryButton = this.addRenderableWidget(new Button(10, 90, 110, 20, SecretConfigCategory, (OnPress) -> {
            this.minecraft.setScreen(new SecretConfigsScreen(this.parent));
        }));
        this.LocalizationConfigCategoryButton.setActive(false);
        //
        this.addRenderableWidget(new Button(10, this.height - 30, 110, 20, -1224789711, EXIT, (OnPress) -> {
            WaterPlayer.config.save();
            this.minecraft.setScreen(this.parent);
        }));
    }

    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        if (this.minecraft.level != null) {
            guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.renderDirtBackground(guiGraphics);
        }

        InterfaceUtils.renderLeftPanel(guiGraphics, 130, this.height);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.minecraft.font, TITLE, 65, 15, -1);
    }

    public boolean mouseScrolled(double d, double e, double f, double g) {
        int scrolled = (int)((double)this.scrolled + g * 10.0 * -1.0);
        int size = 490;
        if (scrolled <= 0 || size <= this.height) {
            this.scrolled = 0;
        } else this.scrolled = Math.min(scrolled, size - this.height);

        return super.mouseScrolled(d, e, f, g);
    }
}
