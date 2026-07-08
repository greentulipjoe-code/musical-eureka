package com.fleshterror.init;

import com.fleshterror.FleshTerrorMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    // Blocks in this tag CANNOT be eaten (natural stone terrain). Everything else is fair game.
    // See src/main/resources/data/fleshterror/tags/blocks/inedible.json
    public static final TagKey<Block> INEDIBLE = TagKey.create(
            Registries.BLOCK, new ResourceLocation(FleshTerrorMod.MOD_ID, "inedible"));
}
