package ru.kelcuprum.waterplayer.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.kelcuprum.waterplayer.Client;
import ru.kelcuprum.waterplayer.config.UserConfig;

@Mixin(value = MinecraftClient.class, priority = -1)

public class TitleManager {
    @Inject(at = @At("HEAD"), method = "updateWindowTitle", cancellable = true)
    private void updateTitle(final CallbackInfo callbackInfo) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(Client.music.getAudioPlayer().getPlayingTrack() != null && (client.world == null && client.player == null) && UserConfig.ENABLE_CHANGE_TITLE) callbackInfo.cancel();
    }
}
