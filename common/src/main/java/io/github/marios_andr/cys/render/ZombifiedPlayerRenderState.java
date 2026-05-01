package io.github.marios_andr.cys.render;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.UndeadRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerSkin;

public class ZombifiedPlayerRenderState extends UndeadRenderState {
    public boolean isAggressive;
    public boolean isConverting;

    public PlayerSkin skin = DefaultPlayerSkin.getDefaultSkin();
    public float capeFlap;
    public float capeLean;
    public float capeLean2;
    public int arrowCount;
    public int stingerCount;
    public boolean showHat = true;
    public boolean showJacket = true;
    public boolean showLeftPants = true;
    public boolean showRightPants = true;
    public boolean showLeftSleeve = true;
    public boolean showRightSleeve = true;
    public boolean showCape = true;
    public float fallFlyingTimeInTicks;
    public boolean shouldApplyFlyingYRot;
    public float flyingYRot;
    public int id;
    public final ItemStackRenderState heldOnHead = new ItemStackRenderState();

    public float fallFlyingScale() {
        return Mth.clamp(this.fallFlyingTimeInTicks * this.fallFlyingTimeInTicks / 100.0F, 0.0F, 1.0F);
    }

    public AvatarRenderState getAvatarRenderState() {
        AvatarRenderState avatarRenderState = new AvatarRenderState();
        return avatarRenderState;
    }

}