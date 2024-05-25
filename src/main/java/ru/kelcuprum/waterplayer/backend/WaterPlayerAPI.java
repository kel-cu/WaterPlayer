package ru.kelcuprum.waterplayer.backend;

import ru.kelcuprum.waterplayer.WaterPlayer;

public class WaterPlayerAPI {
    public static String URL = WaterPlayer.config.getString("API.URL", "https://api.waterplayer.ru");
}
