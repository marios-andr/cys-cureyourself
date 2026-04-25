package marios_andr.cys.mixin;

import com.mojang.authlib.GameProfile;
import marios_andr.cys.Config;
import marios_andr.cys.ZombifiedPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/server/level/ServerPlayer")
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V"),
            method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V")
    private void die(ServerPlayer player, ServerLevel serverLevel, DamageSource damageSource) {
        boolean isPeaceful = serverLevel.getDifficulty().equals(Difficulty.PEACEFUL);
        boolean isKeepInventory = serverLevel.getGameRules().get(GameRules.KEEP_INVENTORY);
        boolean shouldSpawn = Config.isEnabled && (!isPeaceful || Config.isPeacefulEnabled) && !isKeepInventory;

        if (!shouldSpawn) {
            this.dropAllDeathLoot(serverLevel, damageSource);
        } else {
            if (Config.zombieExperienceOptions.equals(Config.ExperienceOptions.NONE)) {
                this.dropExperience(serverLevel, damageSource.getEntity());
            }

            ZombifiedPlayer zp = new ZombifiedPlayer(serverLevel, player);
            serverLevel.addFreshEntity(zp);
        }
    }
}