package ru.kelcuprum.waterplayer.screens.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.localization.Localization;
import ru.kelcuprum.waterplayer.config.UserConfig;
import ru.kelcuprum.waterplayer.screens.config.category.MainConfigs;
import ru.kelcuprum.waterplayer.screens.config.category.SecretConfigs;

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
        WaterPlayer.log("Save configurations & Localization");
        UserConfig.save();
    }
}
