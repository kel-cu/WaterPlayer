package ru.kelcuprum.waterplayer.gui.screens.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.buttons.ButtonBoolean;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.localization.Localization;

public class MainConfigsScreen extends Screen {
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
    private ButtonBoolean enableBossBar;
    private static final Component enableBossBarText = Localization.getText("waterplayer.config.enable_bossbar");
    private ButtonBoolean enableOverlay;
    private static final Component enableOverlayText = Localization.getText("waterplayer.config.enable_overlay");
    private ButtonBoolean enableNotice;
    private static final Component enableNoticeText = Localization.getText("waterplayer.config.enable_notice");
    private ButtonBoolean enableChangeTitle;
    private static final Component enableChangeTitleText = Localization.getText("waterplayer.config.enable_change_title");
    //
    private static final Component EXIT = Localization.getText("waterplayer.screen.exit");
    private int scrolled = 0;

    public MainConfigsScreen(Screen parent) {
        super(MainConfigCategory);
        this.parent = parent;
    }

    public void tick() {
        this.titleBox.setYPos(15 - this.scrolled);
        this.enableBossBar.setYPos(40-scrolled);
        this.enableOverlay.setYPos(65-scrolled);
        this.enableNotice.setYPos(90-scrolled);
        this.enableChangeTitle.setYPos(115-scrolled);
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
        this.enableBossBar = new ButtonBoolean(140, 40, x, 20, WaterPlayer.config, "ENABLE_BOSS_BAR", false, enableBossBarText);
        this.addRenderableWidget(enableBossBar);
        this.enableOverlay = new ButtonBoolean(140, 65, x, 20, WaterPlayer.config, "ENABLE_OVERLAY", false, enableOverlayText);
        this.addRenderableWidget(enableOverlay);
        this.enableNotice = new ButtonBoolean(140, 90, x, 20, WaterPlayer.config, "ENABLE_NOTICE", false, enableNoticeText);
        this.addRenderableWidget(enableNotice);
        this.enableChangeTitle = new ButtonBoolean(140, 115, x, 20, WaterPlayer.config, "ENABLE_CHANGE_TITLE", false, enableChangeTitleText);
        this.addRenderableWidget(enableChangeTitle);
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
        this.MainConfigCategoryButton.setActive(false);
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
        int size = 140;
        if (scrolled <= 0 || size <= this.height) {
            this.scrolled = 0;
        } else this.scrolled = Math.min(scrolled, size - this.height);

        return super.mouseScrolled(d, e, f, g);
    }
}
