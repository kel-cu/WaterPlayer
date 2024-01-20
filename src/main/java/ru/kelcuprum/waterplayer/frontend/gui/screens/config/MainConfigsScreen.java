package ru.kelcuprum.waterplayer.frontend.gui.screens.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.config.Localization;
import ru.kelcuprum.alinlib.gui.InterfaceUtils;
import ru.kelcuprum.alinlib.gui.components.buttons.ButtonConfigBoolean;
import ru.kelcuprum.alinlib.gui.components.buttons.base.Button;
import ru.kelcuprum.alinlib.gui.components.text.TextBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.waterplayer.WaterPlayer;

public class MainConfigsScreen{
    private static final Component MainConfigCategory = Localization.getText("waterplayer.config");
    private static final Component LocalizationConfigCategory = Localization.getText("waterplayer.config.localization");
    private static final Component SecretConfigCategory = Localization.getText("waterplayer.secret");
    // CATEGORY CONTENT
    private static final Component enableBossBarText = Localization.getText("waterplayer.config.enable_bossbar");
    private static final Component enableOverlayText = Localization.getText("waterplayer.config.enable_overlay");
    private static final Component enableNoticeText = Localization.getText("waterplayer.config.enable_notice");
    private static final Component enableChangeTitleText = Localization.getText("waterplayer.config.enable_change_title");

    private InterfaceUtils.DesignType designType = InterfaceUtils.DesignType.FLAT;
    public Screen build(Screen parent) {
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
                ///
                .addWidget(new TextBox(140, 5, MainConfigCategory, true))
                .addWidget(new ButtonConfigBoolean(140, 30, designType, WaterPlayer.config, "ENABLE_BOSS_BAR", false, enableBossBarText))
                .addWidget(new ButtonConfigBoolean(140, 55, designType, WaterPlayer.config, "ENABLE_OVERLAY", false, enableOverlayText))
                .addWidget(new ButtonConfigBoolean(140, 80, designType, WaterPlayer.config, "ENABLE_NOTICE", false, enableNoticeText))
                .addWidget(new ButtonConfigBoolean(140, 105, designType, WaterPlayer.config, "ENABLE_CHANGE_TITLE", false, enableChangeTitleText))
                .addWidget(new ButtonConfigBoolean(140, 130, designType, WaterPlayer.config, "ENABLE_DISCORD_RPC", false, Component.translatable("waterplayer.config.enable_discord_rpc")))
                .build();
    }
}
