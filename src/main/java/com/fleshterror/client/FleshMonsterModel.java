package com.fleshterror.client;

import com.fleshterror.entity.FleshMonsterEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * A lumpy, amorphous blob body (several overlapping cubes instead of one clean box, so the
 * silhouette isn't a cube) held up by four leg-tentacles that plant on the ground, plus four
 * arm-tentacles used to reach out and grab blocks. Every tentacle ends in a small tapered
 * "grip" segment. Whole thing is scaled again per growth-stage by FleshMonsterRenderer.
 */
public class FleshMonsterModel extends HierarchicalModel<FleshMonsterEntity> {

    private static final int LEG_COUNT = 4;
    private static final int ARM_COUNT = 4;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart[] legBases = new ModelPart[LEG_COUNT];
    private final ModelPart[] legTips = new ModelPart[LEG_COUNT];
    private final ModelPart[] armBases = new ModelPart[ARM_COUNT];
    private final ModelPart[] armTips = new ModelPart[ARM_COUNT];
    private final ModelPart[] armGrips = new ModelPart[ARM_COUNT];

    public FleshMonsterModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        for (int i = 0; i < LEG_COUNT; i++) {
            ModelPart base = root.getChild("leg_base_" + i);
            legBases[i] = base;
            legTips[i] = base.getChild("leg_tip_" + i);
        }
        for (int i = 0; i < ARM_COUNT; i++) {
            ModelPart base = root.getChild("arm_base_" + i);
            armBases[i] = base;
            ModelPart tip = base.getChild("arm_tip_" + i);
            armTips[i] = tip;
            armGrips[i] = tip.getChild("arm_grip_" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        // ---- Blobby body: several overlapping lumps instead of one clean cube ----
        // Pivot sits above the ground so the leg-tentacles have room to reach down to y=24.
        CubeListBuilder bodyCubes = CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7.0F, -14.0F, -7.0F, 14.0F, 14.0F, 14.0F)   // core mass
                .texOffs(0, 0).addBox(-4.0F, -18.0F, -3.0F, 9.0F, 9.0F, 8.0F)      // upper lump
                .texOffs(0, 0).addBox(-3.0F, -3.0F, -4.0F, 7.0F, 6.0F, 7.0F)       // underbelly bulge
                .texOffs(0, 0).addBox(4.0F, -11.0F, -5.0F, 6.0F, 9.0F, 6.0F)       // right-side lump
                .texOffs(0, 0).addBox(-9.0F, -10.0F, -2.0F, 6.0F, 8.0F, 6.0F)      // left-side lump
                .texOffs(0, 0).addBox(-4.0F, -13.0F, 4.0F, 8.0F, 8.0F, 5.0F);      // rear lump

        parts.addOrReplaceChild("body", bodyCubes, PartPose.offset(0.0F, 18.0F, 0.0F));

        // ---- Legs: plant on the ground (rest pose reaches exactly y=24), used to walk ----
        double[] legX = {-6, 6, -6, 6};
        double[] legZ = {-6, -6, 6, 6};
        for (int i = 0; i < LEG_COUNT; i++) {
            float x = (float) legX[i];
            float z = (float) legZ[i];
            float leanX = z < 0 ? -0.18F : 0.18F; // splay slightly outward, front/back
            float leanZ = x < 0 ? -0.18F : 0.18F; // splay slightly outward, left/right

            int texX = i * 16;

            PartDefinition base = parts.addOrReplaceChild("leg_base_" + i,
                    CubeListBuilder.create()
                            .texOffs(texX, 40)
                            .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                    PartPose.offsetAndRotation(x, 15.0F, z, leanX, 0.0F, leanZ));

            // Tapered foot/grip tip - the "small end to grip things" for legs.
            base.addOrReplaceChild("leg_tip_" + i,
                    CubeListBuilder.create()
                            .texOffs(texX, 52)
                            .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                    PartPose.offset(0.0F, 6.0F, 0.0F));
        }

        // ---- Arms: reach outward/forward to grab structure blocks, end in a pincer grip ----
        double[] armAngleDeg = {50, -50, 130, -130}; // spread around front & back-sides
        for (int i = 0; i < ARM_COUNT; i++) {
            double angle = Math.toRadians(armAngleDeg[i]);
            float x = (float) (Math.sin(angle) * 6.0);
            float z = (float) (Math.cos(angle) * 6.0);
            float yaw = (float) angle;

            int texX = i * 16;

            PartDefinition base = parts.addOrReplaceChild("arm_base_" + i,
                    CubeListBuilder.create()
                            .texOffs(texX, 64)
                            .addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 7.0F),
                    PartPose.offsetAndRotation(x, 6.0F, z, 0.2F, yaw, 0.0F));

            PartDefinition tip = base.addOrReplaceChild("arm_tip_" + i,
                    CubeListBuilder.create()
                            .texOffs(texX, 80)
                            .addBox(-1.25F, -1.25F, 0.0F, 2.5F, 2.5F, 6.0F),
                    PartPose.offsetAndRotation(0.0F, 0.0F, 7.0F, 0.3F, 0.0F, 0.0F));

            // Small pincer/grip end for actually grabbing blocks.
            tip.addOrReplaceChild("arm_grip_" + i,
                    CubeListBuilder.create()
                            .texOffs(texX, 92)
                            .addBox(-0.75F, -0.75F, 0.0F, 1.5F, 1.5F, 2.5F),
                    PartPose.offset(0.0F, 0.0F, 6.0F));
        }

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(FleshMonsterEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Legs: alternating trot gait, driven by actual movement (limbSwing), so it doesn't
        // "moonwalk" - legs stay planted-looking when the monster isn't moving.
        float[] legPhase = {0.0F, (float) Math.PI, (float) Math.PI, 0.0F};
        for (int i = 0; i < LEG_COUNT; i++) {
            float swing = Mth.cos(limbSwing * 0.6662F + legPhase[i]) * 1.1F * limbSwingAmount;
            legBases[i].xRot += swing;
            legTips[i].xRot += Math.abs(swing) * 0.4F;
        }

        // Arms: idle reaching/wiggle animation, plus a subtle grasp-and-release motion.
        float wiggleSpeed = 0.05F;
        for (int i = 0; i < ARM_COUNT; i++) {
            float phase = i * ((float) Math.PI * 2 / ARM_COUNT);
            float reach = Mth.sin(ageInTicks * wiggleSpeed + phase) * 0.25F;
            armBases[i].yRot += reach * 0.5F;
            armTips[i].yRot += reach;
            armGrips[i].xRot += (Mth.sin(ageInTicks * 0.15F + phase) * 0.3F) - 0.15F; // open/close pinch
        }

        // Subtle body breathing pulse.
        float breathe = Mth.sin(ageInTicks * 0.05F) * 0.4F;
        body.y = 18.0F + breathe;
    }
}
