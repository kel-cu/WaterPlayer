package ru.kelcuprum.waterplayer.mixin;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.kelcuprum.waterplayer.frontend.gui.TexturesHelper;

@Mixin(TextureManager.class)
public class TextureManagerMixin {
    @Inject(method = "<init>", at=@At("RETURN"))
    public void init(ResourceManager resourceManager, CallbackInfo ci){
        TexturesHelper.loadTextures((TextureManager) (Object) this);
    }
}
