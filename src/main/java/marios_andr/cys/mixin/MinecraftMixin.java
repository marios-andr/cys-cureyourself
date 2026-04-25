package marios_andr.cys.mixin;

import marios_andr.cys.ClientZombifiedPlayer;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/client/Minecraft")
public class MinecraftMixin {

    @Shadow
    @Final
    private PlayerSkinRenderCache playerSkinRenderCache;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/ClientMannequin;registerOverrides(Lnet/minecraft/client/renderer/PlayerSkinRenderCache;)V"), method = "Lnet/minecraft/client/Minecraft;<init>(Lnet/minecraft/client/main/GameConfig;)V")
    private void registerOverrides(GameConfig gameConfig, CallbackInfo ci) {
        ClientZombifiedPlayer.registerOverrides(this.playerSkinRenderCache);
    }
}
