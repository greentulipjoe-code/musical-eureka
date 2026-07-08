package com.fleshterror.init;

import com.fleshterror.FleshTerrorMod;
import com.fleshterror.entity.FleshMonsterEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FleshTerrorMod.MOD_ID);

    public static final RegistryObject<EntityType<FleshMonsterEntity>> FLESH_MONSTER =
            ENTITY_TYPES.register("flesh_monster", () -> EntityType.Builder.of(FleshMonsterEntity::new, MobCategory.MONSTER)
                    .sized(1.2f, 1.6f) // base size at stage 0, entity scales this dynamically
                    .clientTrackingRange(16)
                    .fireImmune()
                    .build("flesh_monster"));
}
