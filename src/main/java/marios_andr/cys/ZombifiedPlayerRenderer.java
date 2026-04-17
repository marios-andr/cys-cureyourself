package marios_andr.cys;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.SwingAnimation;
import org.jspecify.annotations.Nullable;

public class ZombifiedPlayerRenderer<E extends ZombifiedPlayer & ClientAvatarEntity> extends HumanoidMobRenderer<E, ZombifiedPlayerRenderer.ZombifiedPlayerRenderState, ZombifiedPlayerModel> {
    public ZombifiedPlayerRenderer(EntityRendererProvider.Context context, boolean slimSteve) {
        super(context, new ZombifiedPlayerModel(context.bakeLayer(slimSteve ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slimSteve), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(
                this, ArmorModelSet.bake(
                                slimSteve ? ModelLayers.PLAYER_SLIM_ARMOR : ModelLayers.PLAYER_ARMOR, context.getModelSet(), part -> new ZombifiedPlayerModel(part, slimSteve)
                        ),
                        context.getEquipmentRenderer()
                )
        );
        //this.addLayer(new CapeLayer(this, context.getModelSet(), context.getEquipmentAssets())); TODO
    }

    @Override
    public ZombifiedPlayerRenderState createRenderState() {
        return new ZombifiedPlayerRenderState();
    }

    @Override
    public void extractRenderState(E entity, ZombifiedPlayerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAggressive = entity.isAggressive();
        state.isConverting = entity.isUnderWaterConverting();

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
        //this.extractCapeState(entity, state, partialTicks); TODO
        state.id = entity.getId();
        state.heldOnHead.clear();
        if (state.isUsingItem) {
            ItemStack useItem = entity.getItemInHand(state.useItemHand);
            if (useItem.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SPYGLASS_SCOPE)) {
                this.itemModelResolver.updateForLiving(state.heldOnHead, useItem, ItemDisplayContext.HEAD, entity);
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
            return CysModClient.ZOMBIFY_ENTITY_CUTOUT.apply(texture, true);
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

    public static class ZombifiedPlayerRenderState extends ZombieRenderState {
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
        public int id;
        public final ItemStackRenderState heldOnHead = new ItemStackRenderState();
    }
}
