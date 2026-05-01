package io.github.marios_andr.cys.mixin;

import io.github.marios_andr.cys.ClientZombifiedPlayer;
import io.github.marios_andr.cys.render.ZombifiedPlayerRenderState;
import io.github.marios_andr.cys.render.ZombifiedPlayerRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    @Unique
    private Map<PlayerModelType, ZombifiedPlayerRenderer<ClientZombifiedPlayer>> zp_renderer = Map.of();

    @Inject(at = @At("HEAD"), method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", cancellable = true)
    private <T extends Entity> void getRenderer(T entity, CallbackInfoReturnable<EntityRenderer<? super T, ?>> cir) {
        if (entity instanceof ClientZombifiedPlayer) {
            PlayerModelType type = ((ClientZombifiedPlayer) entity).getSkin().model();
            ZombifiedPlayerRenderer<ClientZombifiedPlayer> renderer = zp_renderer.get(type);
            EntityRenderer<? super T, ?> return_renderer = (EntityRenderer<? super T, ?>) (renderer != null ? renderer : zp_renderer.get(PlayerModelType.WIDE));
            cir.setReturnValue(return_renderer);
        }
    }

    @Inject(at = @At("HEAD"), method = "getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", cancellable = true)
    private <S extends EntityRenderState> void getRenderer2(S entityRenderState, CallbackInfoReturnable<EntityRenderer<?, ? super S>> cir) {
        if (entityRenderState instanceof ZombifiedPlayerRenderState state) {
            PlayerModelType model = state.skin.model();
            EntityRenderer<?, ? super S> playerRenderer = (EntityRenderer<?, ? super S>) zp_renderer.get(model);
            EntityRenderer<?, ? super S> return_renderer = (playerRenderer != null ? playerRenderer : (EntityRenderer<?, ? super S>) zp_renderer.get(PlayerModelType.WIDE));
            cir.setReturnValue(return_renderer);
        }
    }

    @Inject(at = @At("TAIL"), method = "onResourceManagerReload(Lnet/minecraft/server/packs/resources/ResourceManager;)V",
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onResourceManagerReload(ResourceManager resourceManager, CallbackInfo ci, EntityRendererProvider.Context context) {
        this.zp_renderer = Map.of(
                PlayerModelType.WIDE, new ZombifiedPlayerRenderer<>(context, false),
                PlayerModelType.SLIM, new ZombifiedPlayerRenderer<>(context, true));
    }
}
