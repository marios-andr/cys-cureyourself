package io.github.marios_andr.cys.mixin;

import io.github.marios_andr.cys.ClientZombifiedPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/ClientMannequin;registerOverrides(Lnet/minecraft/client/renderer/PlayerSkinRenderCache;)V"), method = "<init>(Lnet/minecraft/client/main/GameConfig;)V")
    private void registerOverrides(PlayerSkinRenderCache cache) {
        ClientMannequin.registerOverrides(cache);
        ClientZombifiedPlayer.registerOverrides(cache);
    }
}