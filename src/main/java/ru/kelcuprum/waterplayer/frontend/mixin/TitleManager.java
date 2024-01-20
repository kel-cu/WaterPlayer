package ru.kelcuprum.waterplayer.frontend.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.kelcuprum.waterplayer.WaterPlayer;

@Mixin(value = Minecraft.class, priority = -1)

public class TitleManager {
    @Inject(at = @At("HEAD"), method = "updateTitle", cancellable = true)
    private void updateTitle(final CallbackInfo callbackInfo) {
        Minecraft client = Minecraft.getInstance();
        if(WaterPlayer.player != null && WaterPlayer.player.getAudioPlayer().getPlayingTrack() != null && (client.level == null && client.player == null) && WaterPlayer.config.getBoolean("ENABLE_CHANGE_TITLE", true)) callbackInfo.cancel();
    }
}
