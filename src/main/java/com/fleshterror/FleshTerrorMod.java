package com.fleshterror;

import com.fleshterror.entity.FleshMonsterEntity;
import com.fleshterror.init.ModBlocks;
import com.fleshterror.init.ModEntityTypes;
import com.fleshterror.init.ModItems;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(FleshTerrorMod.MOD_ID)
public class FleshTerrorMod {

    public static final String MOD_ID = "fleshterror";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FleshTerrorMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerAttributes);

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SpawnPlacements.register(ModEntityTypes.FLESH_MONSTER.get(),
                    SpawnPlacements.Type.ON_GROUND,
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Monster::checkMonsterSpawnRules);
        });
    }

    private void registerAttributes(final EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.FLESH_MONSTER.get(), FleshMonsterEntity.createAttributes().build());
    }
}
