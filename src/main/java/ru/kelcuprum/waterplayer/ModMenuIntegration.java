package ru.kelcuprum.waterplayer;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.kelcuprum.waterplayer.screens.ConfigScreen;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (Client.clothConfig) {
            return ConfigScreen::buildScreen;
        } else {
            return null;
        }
    }
}
