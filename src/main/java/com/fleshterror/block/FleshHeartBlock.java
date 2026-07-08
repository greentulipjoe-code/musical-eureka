package com.fleshterror.block;

import com.fleshterror.entity.FleshMonsterEntity;
import com.fleshterror.init.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The heart of the beast. Craft it, place it, then punch/right-click it to make it awaken.
 * On activation it consumes itself in a burst of particles and spawns a small (stage 0)
 * FleshMonsterEntity that will grow the more structure blocks it tears apart.
 */
public class FleshHeartBlock extends Block {

    public FleshHeartBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        awaken((ServerLevel) level, pos);
        return InteractionResult.CONSUME;
    }

    public void awaken(ServerLevel level, BlockPos pos) {
        level.removeBlock(pos, false);

        level.playSound(null, pos, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 2.0f, 0.6f);
        level.playSound(null, pos, SoundEvents.ZOMBIE_AMBIENT, SoundSource.HOSTILE, 1.5f, 0.4f);

        for (int i = 0; i < 60; i++) {
            double dx = (level.random.nextDouble() - 0.5) * 1.5;
            double dy = level.random.nextDouble() * 1.5;
            double dz = (level.random.nextDouble() - 0.5) * 1.5;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, dx, dy, dz, 0.02);
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, dx, dy, dz, 0.02);
        }

        FleshMonsterEntity monster = ModEntityTypes.FLESH_MONSTER.get().create(level);
        if (monster != null) {
            monster.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
            monster.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), net.minecraft.world.entity.MobSpawnType.TRIGGERED, null, null);
            monster.setGrowthStage(0);
            level.addFreshEntity(monster);
        }
    }
}
