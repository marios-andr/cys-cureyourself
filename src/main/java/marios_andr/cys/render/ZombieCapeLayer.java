package marios_andr.cys.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class ZombieCapeLayer extends RenderLayer<ZombifiedPlayerRenderState, ZombifiedPlayerModel> {
    private final HumanoidModel<AvatarRenderState> model;
    private final EquipmentAssetManager equipmentAssets;
    private final AvatarRenderState capeState = new AvatarRenderState();

    public ZombieCapeLayer(RenderLayerParent<ZombifiedPlayerRenderState, ZombifiedPlayerModel> renderer, EntityModelSet modelSet, EquipmentAssetManager equipmentAssets) {
        super(renderer);
        this.model = new PlayerCapeModel(modelSet.bakeLayer(ModelLayers.PLAYER_CAPE));
        this.equipmentAssets = equipmentAssets;
    }

    private boolean hasLayer(ItemStack itemStack, EquipmentClientInfo.LayerType layerType) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && !equippable.assetId().isEmpty()) {
            EquipmentClientInfo equipmentClientInfo = this.equipmentAssets.get(equippable.assetId().get());
            return !equipmentClientInfo.getLayers(layerType).isEmpty();
        } else {
            return false;
        }
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, ZombifiedPlayerRenderState state, float yRot, float xRot) {
        if (!state.isInvisible && state.showCape) {
            PlayerSkin skin = state.skin;
            if (skin.cape() != null) {
                if (!this.hasLayer(state.chestEquipment, EquipmentClientInfo.LayerType.WINGS)) {
                    poseStack.pushPose();
                    if (this.hasLayer(state.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID)) {
                        poseStack.translate(0.0F, -0.053125F, 0.06875F);
                    }

                    this.capeState.capeFlap = state.capeFlap;
                    this.capeState.capeLean = state.capeLean;
                    this.capeState.capeLean2 = state.capeLean2;

                    submitNodeCollector.submitModel(
                            this.model,
                            capeState,
                            poseStack,
                            RenderTypes.entitySolid(skin.cape().texturePath()),
                            lightCoords,
                            OverlayTexture.NO_OVERLAY,
                            state.outlineColor,
                            null
                    );
                    poseStack.popPose();
                }
            }
        }
    }
}
