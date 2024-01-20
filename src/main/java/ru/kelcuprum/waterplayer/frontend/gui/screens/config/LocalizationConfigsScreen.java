package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.EditBoxLocalization;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;

public class LocalizationConfigsScreen {
    private static final Component MainConfigCategory = Localization.getText("waterplayer.config");
    private static final Component LocalizationConfigCategory = Localization.getText("waterplayer.config.localization");
    private static final Component SecretConfigCategory = Localization.getText("waterplayer.secret");

    private final Component titleBossBarText = Localization.getText("waterplayer.config.localization.title.bossbar");
    private final Component bossBarText = Localization.getText("waterplayer.config.localization.bossbar");
    private final Component bossBarWithoutAuthorText = Localization.getText("waterplayer.config.localization.bossbar.withoutAuthor");
    private final Component bossBarLiveText = Localization.getText("waterplayer.config.localization.bossbar.live");
    private final Component bossBarLiveWithoutAuthorText = Localization.getText("waterplayer.config.localization.bossbar.live.withoutAuthor");
    private final Component bossBarPauseText = Localization.getText("waterplayer.config.localization.bossbar.pause");

    private final Component titleOverlayText = Localization.getText("waterplayer.config.localization.title.title");
    private final Component overlayText = Localization.getText("waterplayer.config.localization.title");
    private final Component overlayWithoutAuthorText = Localization.getText("waterplayer.config.localization.title.withoutAuthor");
    private final Component overlayLiveText = Localization.getText("waterplayer.config.localization.title.live");
    private final Component overlayLiveWithoutAuthorText = Localization.getText("waterplayer.config.localization.title.live.withoutAuthor");
    private final Component overlayPauseText = Localization.getText("waterplayer.config.localization.title.pause");

    private final Component titleFormatsText = Localization.getText("waterplayer.config.localization.title.formats");
    private final Component authorText = Localization.getText("waterplayer.config.localization.format.author");
    private final Component titleText = Localization.getText("waterplayer.config.localization.format.title");
    private final Component timeText = Localization.getText("waterplayer.config.localization.format.time");
    private final Component liveText = Localization.getText("waterplayer.config.localization.format.live");

    private final InterfaceUtils.DesignType designType = InterfaceUtils.DesignType.FLAT;
    public Screen build(Screen parent){
        return new ConfigScreenBuilder(parent, Component.translatable("waterplayer.name"), designType)
                .addPanelWidget(new Button(10, 40, designType, MainConfigCategory, (e) -> {
                    Minecraft.getInstance().setScreen(new MainConfigsScreen().build(parent));
                }))
                .addPanelWidget(new Button(10, 65, designType, LocalizationConfigCategory, (e) -> {
                    Minecraft.getInstance().setScreen(new LocalizationConfigsScreen().build(parent));
                }))
                .addPanelWidget(new Button(10, 90, designType, SecretConfigCategory, (e) -> {
                    Minecraft.getInstance().setScreen(new SecretConfigsScreen().build(parent));
                }))
                //
                .addWidget(new TextBox(140, 5, LocalizationConfigCategory, true))
                .addWidget(new CategoryBox(140, 30, titleBossBarText)
                        .addValue(new EditBoxLocalization(140, 55, designType, WaterPlayer.localization, "bossbar", bossBarText))
                        .addValue(new EditBoxLocalization(140, 80, designType, WaterPlayer.localization, "bossbar.withoutAuthor", bossBarWithoutAuthorText))
                        .addValue(new EditBoxLocalization(140, 105, designType, WaterPlayer.localization, "bossbar.live", bossBarLiveText))
                        .addValue(new EditBoxLocalization(140, 130, designType, WaterPlayer.localization, "bossbar.live.withoutAuthor", bossBarLiveWithoutAuthorText))
                        .addValue(new EditBoxLocalization(140, 155, designType, WaterPlayer.localization, "bossbar.pause", bossBarPauseText))
                )
                .addWidget(new CategoryBox(140, 180, titleOverlayText)
                        .addValue(new EditBoxLocalization(140, 205, designType, WaterPlayer.localization, "title", overlayText))
                        .addValue(new EditBoxLocalization(140, 230, designType, WaterPlayer.localization, "title.withoutAuthor", overlayWithoutAuthorText))
                        .addValue(new EditBoxLocalization(140, 255, designType, WaterPlayer.localization, "title.live", overlayLiveText))
                        .addValue(new EditBoxLocalization(140, 280, designType, WaterPlayer.localization, "title.live.withoutAuthor", overlayLiveWithoutAuthorText))
                        .addValue(new EditBoxLocalization(140, 305, designType, WaterPlayer.localization, "title.pause", overlayPauseText))
                )
                .addWidget(new CategoryBox(140, 330, titleFormatsText)
                        .addValue(new EditBoxLocalization(140, 355, designType, WaterPlayer.localization, "format.author", authorText))
                        .addValue(new EditBoxLocalization(140, 380, designType, WaterPlayer.localization, "format.title", titleText))
                        .addValue(new EditBoxLocalization(140, 405, designType, WaterPlayer.localization, "format.time", timeText))
                        .addValue(new EditBoxLocalization(140, 430, designType, WaterPlayer.localization, "format.live", liveText))
                )
                .build();
    }
}
