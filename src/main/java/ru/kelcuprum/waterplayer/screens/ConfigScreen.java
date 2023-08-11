package ru.kelcuprum.waterplayer.screens;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screen.Screen;
import ru.kelcuprum.waterplayer.Client;
import ru.kelcuprum.waterplayer.config.Localization;
import ru.kelcuprum.waterplayer.config.UserConfig;
import ru.kelcuprum.waterplayer.screens.category.MainConfigs;
import ru.kelcuprum.waterplayer.screens.category.SecretConfigs;

public class ConfigScreen {
    public static Screen buildScreen(Screen currentScreen) {
        UserConfig.load();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(currentScreen)
                .setTitle(Localization.getText("waterplayer.name.config"))
                .setTransparentBackground(true)
                .setSavingRunnable(ConfigScreen::save);
        new MainConfigs().getCategory(builder);
        new SecretConfigs().getCategory(builder);
        return builder.build();
    }
    private static void save(){
        Client.log("Save configurations & Localization");
        UserConfig.save();
    }
}
