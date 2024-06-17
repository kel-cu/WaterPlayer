package ru.kelcuprum.waterplayer.backend;

import net.minecraft.client.KeyMapping;

public record KeyBind(KeyMapping key, ru.kelcuprum.waterplayer.backend.KeyBind.Execute onExecute) {
    public interface Execute {
        boolean onExecute();
    }
}
