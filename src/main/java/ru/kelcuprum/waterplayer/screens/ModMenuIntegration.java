package ru.kelcuprum.waterplayer.screens;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;=
import ru.kelcuprum.waterplayer.screens.config.MainConfigsScreen;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
//        if (WaterPlayer.clothConfig) {
//            return ConfigScreen::buildScreen;
//        } else {
//            return null;
//        }
        return MainConfigsScreen::new;
    }
}
