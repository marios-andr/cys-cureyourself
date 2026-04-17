package marios_andr.cys;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;

public class ZombifiedPlayerModel extends HumanoidModel<ZombifiedPlayerRenderer.ZombifiedPlayerRenderState> {
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;
    private final boolean slim;

    public ZombifiedPlayerModel(ModelPart root, boolean slimSteve) {
        super(root);
        this.slim = slimSteve;
        this.leftSleeve = this.leftArm.getChild("left_sleeve");
        this.rightSleeve = this.rightArm.getChild("right_sleeve");
        this.leftPants = this.leftLeg.getChild("left_pants");
        this.rightPants = this.rightLeg.getChild("right_pants");
        this.jacket = this.body.getChild("jacket");
    }

    public void setupAnim(ZombifiedPlayerRenderer.ZombifiedPlayerRenderState state) {
        this.hat.visible = state.showHat;
        this.jacket.visible = state.showJacket;
        this.leftPants.visible = state.showLeftPants;
        this.rightPants.visible = state.showRightPants;
        this.leftSleeve.visible = state.showLeftSleeve;
        this.rightSleeve.visible = state.showRightSleeve;
        super.setupAnim(state);
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, state.isAggressive, state);
    }

    public void translateToHand(ZombifiedPlayerRenderer.ZombifiedPlayerRenderState state, HumanoidArm arm, PoseStack poseStack) {
        this.root().translateAndRotate(poseStack);
        ModelPart part = this.getArm(arm);
        if (this.slim) {
            float offset = 0.5F * (arm == HumanoidArm.RIGHT ? 1 : -1);
            part.x += offset;
            part.translateAndRotate(poseStack);
            part.x -= offset;
        } else {
            part.translateAndRotate(poseStack);
        }
    }
}
