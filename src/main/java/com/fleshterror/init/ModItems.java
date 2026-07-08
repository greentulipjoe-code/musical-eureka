package com.fleshterror.init;

import com.fleshterror.FleshTerrorMod;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FleshTerrorMod.MOD_ID);

    // Base color is the sickly flesh tone, highlight is the vein/blood red spot color.
    public static final RegistryObject<Item> FLESH_MONSTER_SPAWN_EGG = ITEMS.register("flesh_monster_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.FLESH_MONSTER, 0xc79489, 0x7a1414,
                    new Item.Properties()));

    @Mod.EventBusSubscriber(modid = FleshTerrorMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class TabContent {
        @SubscribeEvent
        public static void buildContents(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
                event.accept(FLESH_MONSTER_SPAWN_EGG.get());
            }
            if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
                event.accept(ModBlocks.FLESH_HEART_ITEM.get());
            }
        }
    }
}
