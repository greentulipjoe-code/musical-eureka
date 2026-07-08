package com.fleshterror.client;

import com.fleshterror.FleshTerrorMod;
import com.fleshterror.entity.FleshMonsterEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FleshMonsterRenderer extends MobRenderer<FleshMonsterEntity, FleshMonsterModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FleshTerrorMod.MOD_ID, "textures/entity/flesh_monster.png");

    public FleshMonsterRenderer(EntityRendererProvider.Context context) {
        super(context, new FleshMonsterModel(context.bakeLayer(FleshMonsterModelLayer.LAYER)), 1.1f);
    }

    @Override
    public ResourceLocation getTextureLocation(FleshMonsterEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(FleshMonsterEntity entity, PoseStack poseStack, float partialTick) {
        float scale = entity.getStageScale();
        poseStack.scale(scale, scale, scale);
    }
}
