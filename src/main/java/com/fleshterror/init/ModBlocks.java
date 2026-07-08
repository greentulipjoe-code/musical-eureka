package com.fleshterror.init;

import com.fleshterror.FleshTerrorMod;
import com.fleshterror.block.FleshHeartBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, FleshTerrorMod.MOD_ID);

    public static final RegistryObject<Block> FLESH_HEART = BLOCKS.register("flesh_heart",
            () -> new FleshHeartBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.SLIME_BLOCK)
                    .lightLevel(state -> 7)
                    .noOcclusion()));

    // Registered directly (not via DeferredRegister<Item>) so it lives alongside the block,
    // but it still must be added to ModItems.ITEMS to actually be registered to the game.
    public static final RegistryObject<Item> FLESH_HEART_ITEM = ModItems.ITEMS.register("flesh_heart",
            () -> new BlockItem(FLESH_HEART.get(), new Item.Properties()));
}
