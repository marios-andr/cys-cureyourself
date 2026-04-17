package marios_andr.cys.mixin;

import com.mojang.authlib.GameProfile;
import marios_andr.cys.Config;
import marios_andr.cys.ZombifiedPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
        boolean flag = Config.isEnabled && (!isPeaceful || Config.isPeacefulEnabled);
        if (!flag) {
            this.dropAllDeathLoot(serverLevel, damageSource);
            return;
        }

        ZombifiedPlayer zp = new ZombifiedPlayer(serverLevel, player);
        serverLevel.addFreshEntity(zp);
    }
}