package io.github.marios_andr.cys;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ClientZombifiedPlayer extends ZombifiedPlayer implements ClientAvatarEntity {
    public static final PlayerSkin DEFAULT_SKIN = DefaultPlayerSkin.get(Mannequin.DEFAULT_PROFILE.partialProfile());

    private final ClientAvatarState avatarState = new ClientAvatarState();
    private @Nullable CompletableFuture<Optional<PlayerSkin>> skinLookup;
    private PlayerSkin skin = DEFAULT_SKIN;
    private final PlayerSkinRenderCache skinRenderCache;

    public static void registerOverrides(PlayerSkinRenderCache cache) {
        ZombifiedPlayer.constructor = (type, level) -> (ZombifiedPlayer)(level instanceof ClientLevel ? new ClientZombifiedPlayer(level, cache) : new ZombifiedPlayer(type, level));
    }

    public ClientZombifiedPlayer(Level level, PlayerSkinRenderCache skinRenderCache) {
        super(level);
        this.skinRenderCache = skinRenderCache;
    }

    @Override
    public void tick() {
        super.tick();
        this.avatarState.tick(this.position(), this.getDeltaMovement());
        if (this.skinLookup != null && this.skinLookup.isDone()) {
            try {
                this.skinLookup.get().ifPresent(this::setSkin);
                this.skinLookup = null;
            } catch (Exception var2) {
                Constants.LOG.error("Error when trying to look up skin for zombified player", var2);
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (accessor.equals(DATA_PROFILE)) {
            this.updateSkin();
        }
    }

    private void updateSkin() {
        if (this.skinLookup != null) {
            CompletableFuture<Optional<PlayerSkin>> future = this.skinLookup;
            this.skinLookup = null;
            future.cancel(false);
        }

        this.skinLookup = this.skinRenderCache.lookup(this.getProfile()).thenApply(info -> info.map(PlayerSkinRenderCache.RenderInfo::playerSkin));
    }

    public ClientAvatarState avatarState() {
        return this.avatarState;
    }

    public PlayerSkin getSkin() {
        return this.skin;
    }

    @Override
    public Parrot.@Nullable Variant getParrotVariantOnShoulder(boolean left) {
        return null;
    }

    @Override
    public boolean showExtraEars() {
        return false;
    }

    private void setSkin(PlayerSkin skin) {
        this.skin = skin;
    }
}