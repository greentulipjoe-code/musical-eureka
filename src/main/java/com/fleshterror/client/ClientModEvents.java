package com.fleshterror.client;

import com.fleshterror.FleshTerrorMod;
import com.fleshterror.init.ModEntityTypes;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FleshTerrorMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class ClientModEvents {

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FleshMonsterModelLayer.LAYER, FleshMonsterModel::createBodyLayer);
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.FLESH_MONSTER.get(), FleshMonsterRenderer::new);
    }
}
