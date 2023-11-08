package ru.kelcuprum.waterplayer.screens.config_old;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.localization.Localization;
import ru.kelcuprum.waterplayer.screens.config_old.category.MainConfigs;
import ru.kelcuprum.waterplayer.screens.config_old.category.SecretConfigs;

public class ConfigScreen {
    public static Screen buildScreen(Screen currentScreen) {
        WaterPlayer.config.load();

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
        WaterPlayer.config.save();
    }
}
