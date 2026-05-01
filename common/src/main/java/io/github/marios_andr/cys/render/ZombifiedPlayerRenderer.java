package io.github.marios_andr.cys.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.marios_andr.cys.CysClient;
import io.github.marios_andr.cys.ZombifiedPlayer;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ZombifiedPlayerRenderer<E extends ZombifiedPlayer & ClientAvatarEntity> extends HumanoidMobRenderer<E, ZombifiedPlayerRenderState, ZombifiedPlayerModel> {
    public ZombifiedPlayerRenderer(EntityRendererProvider.Context context, boolean slimSteve) {
        super(context, new ZombifiedPlayerModel(context.bakeLayer(slimSteve ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slimSteve), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(
                        this, ArmorModelSet.bake(
                        slimSteve ? ModelLayers.PLAYER_SLIM_ARMOR : ModelLayers.PLAYER_ARMOR, context.getModelSet(), part -> new ZombifiedPlayerModel(part, slimSteve)
                ),
                        context.getEquipmentRenderer()
                )
        );
        this.addLayer(new ZombieCapeLayer(this, context.getModelSet(), context.getEquipmentAssets()));
    }

    @Override
    public ZombifiedPlayerRenderState createRenderState() {
        return new ZombifiedPlayerRenderState();
    }

    @Override
    public void extractRenderState(E entity, ZombifiedPlayerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAggressive = entity.isAggressive();
        state.isConverting = entity.isConverting();

        state.skin = entity.getSkin();
        state.arrowCount = entity.getArrowCount();
        state.stingerCount = entity.getStingerCount();
        state.showHat = entity.isModelPartShown(PlayerModelPart.HAT);
        state.showJacket = entity.isModelPartShown(PlayerModelPart.JACKET);
        state.showLeftPants = entity.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        state.showRightPants = entity.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        state.showLeftSleeve = entity.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        state.showRightSleeve = entity.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        state.showCape = entity.isModelPartShown(PlayerModelPart.CAPE);
        this.extractFlightData(entity, state, partialTicks);
        this.extractCapeState(entity, state, partialTicks);
        state.id = entity.getId();
        state.heldOnHead.clear();
    }

    @Override
    protected void setupRotations(ZombifiedPlayerRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);

        float xRot = state.xRot;
        if (state.isFallFlying) {
            float scale = state.fallFlyingScale();
            if (!state.isAutoSpinAttack) {
                poseStack.mulPose(Axis.XP.rotationDegrees(scale * (-90.0F - xRot)));
            }

            if (state.shouldApplyFlyingYRot) {
                poseStack.mulPose(Axis.YP.rotation(state.flyingYRot));
            }
        }
    }

    @Override
    public Identifier getTextureLocation(ZombifiedPlayerRenderState state) {
        return state.skin.body().texturePath();
    }

    @Override
    protected @Nullable RenderType getRenderType(ZombifiedPlayerRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        Identifier texture = this.getTextureLocation(state);
        if (isBodyVisible) {
            return CysClient.ZOMBIFY_ENTITY_CUTOUT.apply(texture, true);
        }
        return super.getRenderType(state, isBodyVisible, forceTransparent, appearGlowing);
    }

    @Override
    protected boolean isShaking(ZombifiedPlayerRenderState state) {
        return super.isShaking(state) || state.isConverting;
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(E mob, HumanoidArm arm) {
        SwingAnimation otherAnim = mob.getItemHeldByArm(arm.getOpposite()).get(DataComponents.SWING_ANIMATION);
        return otherAnim != null && otherAnim.type() == SwingAnimationType.STAB ? HumanoidModel.ArmPose.SPEAR : super.getArmPose(mob, arm);
    }

    @Override
    protected boolean shouldShowName(E entity, double distanceToCameraSq) {
        return true;
    }

    private void extractFlightData(E entity, ZombifiedPlayerRenderState state, float partialTicks) {
        state.fallFlyingTimeInTicks = entity.getFallFlyingTicks() + partialTicks;
        Vec3 lookAngle = entity.getViewVector(partialTicks);
        Vec3 movement = entity.avatarState().deltaMovementOnPreviousTick().lerp(entity.getDeltaMovement(), partialTicks);
        if (movement.horizontalDistanceSqr() > 1.0E-5F && lookAngle.horizontalDistanceSqr() > 1.0E-5F) {
            state.shouldApplyFlyingYRot = true;
            double dot = movement.horizontal().normalize().dot(lookAngle.horizontal().normalize());
            double sign = movement.x * lookAngle.z - movement.z * lookAngle.x;
            state.flyingYRot = (float)(Math.signum(sign) * Math.acos(Math.min(1.0, Math.abs(dot))));
        } else {
            state.shouldApplyFlyingYRot = false;
            state.flyingYRot = 0.0F;
        }
    }

    private void extractCapeState(E entity, ZombifiedPlayerRenderState state, float partialTicks) {
        ClientAvatarState clientState = entity.avatarState();
        double deltaX = clientState.getInterpolatedCloakX(partialTicks) - Mth.lerp((double)partialTicks, entity.xo, entity.getX());
        double deltaY = clientState.getInterpolatedCloakY(partialTicks) - Mth.lerp((double)partialTicks, entity.yo, entity.getY());
        double deltaZ = clientState.getInterpolatedCloakZ(partialTicks) - Mth.lerp((double)partialTicks, entity.zo, entity.getZ());
        float yBodyRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        double forwardX = Mth.sin(yBodyRot * (float) (Math.PI / 180.0));
        double forwardZ = -Mth.cos(yBodyRot * (float) (Math.PI / 180.0));
        state.capeFlap = (float)deltaY * 10.0F;
        state.capeFlap = Mth.clamp(state.capeFlap, -6.0F, 32.0F);
        state.capeLean = (float)(deltaX * forwardX + deltaZ * forwardZ) * 100.0F;
        state.capeLean = state.capeLean * (1.0F - state.fallFlyingScale());
        state.capeLean = Mth.clamp(state.capeLean, 0.0F, 150.0F);
        state.capeLean2 = (float)(deltaX * forwardZ - deltaZ * forwardX) * 100.0F;
        state.capeLean2 = Mth.clamp(state.capeLean2, -20.0F, 20.0F);
        float pow = clientState.getInterpolatedBob(partialTicks);
        float walkDistance = clientState.getInterpolatedWalkDistance(partialTicks);
        state.capeFlap = state.capeFlap + Mth.sin(walkDistance * 6.0F) * 32.0F * pow;
    }

}