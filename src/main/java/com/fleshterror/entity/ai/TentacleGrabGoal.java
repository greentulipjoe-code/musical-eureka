package com.fleshterror.entity.ai;

import com.fleshterror.entity.FleshMonsterEntity;
import com.fleshterror.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reaches tentacles out into nearby structures and tears blocks free to feed on.
 * It will eat almost anything (buildings, furniture, ores, wood, wool...) except
 * natural stone (see the fleshterror:inedible block tag).
 *
 * How much it eats scales with growth stage:
 *  - Stage 0 (Spawnling): only nibbles half-blocks (slabs).
 *  - Stage 1 (Juvenile):  eats one full block at a time.
 *  - Stage 2 (Adult):     eats a small 2-block cluster at once.
 *  - Stage 3 (Elder):     eats a 4-block cluster at once.
 *  - Stage 4 (Colossus):  eats a 7-block cluster at once.
 */
public class TentacleGrabGoal extends Goal {

    private final FleshMonsterEntity monster;
    private int cooldown;
    private BlockPos targetBlock;
    private List<BlockPos> cluster;
    private int grabTicks;

    private static final int GRAB_DURATION = 16; // ticks the tentacle takes to pull blocks free

    public TentacleGrabGoal(FleshMonsterEntity monster) {
        this.monster = monster;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (monster.getTarget() != null) return false; // prioritize fighting players over snacking on walls
        if (cooldown-- > 0) return false;
        return findNearbyEdibleBlock() != null;
    }

    @Override
    public void start() {
        this.targetBlock = findNearbyEdibleBlock();
        this.grabTicks = 0;
        this.cluster = null;
        int baseCooldown = 50 - monster.getGrowthStage() * 7; // bigger = hungrier, less waiting
        this.cooldown = Math.max(15, baseCooldown);
    }

    @Override
    public boolean canContinueToUse() {
        return targetBlock != null && grabTicks < GRAB_DURATION && monster.getTarget() == null;
    }

    @Override
    public void tick() {
        if (targetBlock == null) return;

        monster.getLookControl().setLookAt(targetBlock.getX() + 0.5, targetBlock.getY() + 0.5, targetBlock.getZ() + 0.5);
        monster.getNavigation().moveTo(targetBlock.getX() + 0.5, targetBlock.getY(), targetBlock.getZ() + 0.5, 0.7D);

        grabTicks++;

        Level level = monster.level();
        if (level instanceof ServerLevel serverLevel) {
            double px = targetBlock.getX() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 0.6;
            double py = targetBlock.getY() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 0.6;
            double pz = targetBlock.getZ() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 0.6;
            // "tentacle" particle trail pulling toward the block
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, px, py, pz, 2, 0.05, 0.05, 0.05, 0.01);
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, px, py, pz, 1, 0.02, 0.02, 0.02, 0.0);

            if (grabTicks == GRAB_DURATION) {
                if (cluster == null) {
                    cluster = collectCluster(targetBlock, clusterSizeForStage(monster.getGrowthStage()));
                }

                int eaten = 0;
                for (BlockPos pos : cluster) {
                    BlockState state = serverLevel.getBlockState(pos);
                    if (state.isAir()) continue;
                    serverLevel.destroyBlock(pos, false); // removes the block from the world entirely, no drops
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
                    eaten++;
                }

                if (eaten > 0) {
                    serverLevel.playSound(null, targetBlock, SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 1.2f, 0.7f);
                    serverLevel.playSound(null, targetBlock, SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.0f, 0.5f);
                    monster.addGrowth(eaten);
                }
            }
        }
    }

    @Override
    public void stop() {
        this.targetBlock = null;
        this.cluster = null;
        monster.getNavigation().stop();
    }

    private static int clusterSizeForStage(int stage) {
        return switch (stage) {
            case 0 -> 1;
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 4;
            default -> 7;
        };
    }

    /** Is this block something the monster is even capable of eating at all? */
    private boolean isEdible(BlockState state, BlockPos pos) {
        if (state.isAir()) return false;
        if (state.is(ModTags.INEDIBLE)) return false;
        if (state.getDestroySpeed(monster.level(), pos) < 0) return false; // unbreakable (bedrock, barrier, etc.)
        if (!state.getFluidState().isEmpty()) return false;
        return true;
    }

    /** Stage 0 only nibbles half-blocks; every later stage can eat full blocks. */
    private boolean matchesStageBite(BlockState state, int stage) {
        if (stage > 0) return true;
        if (!state.is(BlockTags.SLABS)) return false;
        return !state.hasProperty(SlabBlock.TYPE) || state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE;
    }

    /** Breadth-first collect up to `size` connected edible blocks starting at the seed, for cluster-eating. */
    private List<BlockPos> collectCluster(BlockPos seed, int size) {
        List<BlockPos> result = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(seed);
        visited.add(seed);
        int stage = monster.getGrowthStage();
        Level level = monster.level();

        while (!queue.isEmpty() && result.size() < size) {
            BlockPos pos = queue.poll();
            BlockState state = level.getBlockState(pos);
            if (isEdible(state, pos) && matchesStageBite(state, stage)) {
                result.add(pos);
                for (BlockPos neighbor : new BlockPos[]{
                        pos.above(), pos.below(), pos.north(), pos.south(), pos.east(), pos.west()}) {
                    if (!visited.contains(neighbor) && neighbor.distSqr(seed) <= 9) { // keep clusters tight, radius ~2-3
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
        return result;
    }

    private BlockPos findNearbyEdibleBlock() {
        int radius = 5 + monster.getGrowthStage() * 4; // tentacles reach further as it grows
        BlockPos origin = monster.blockPosition();
        Level level = monster.level();
        int stage = monster.getGrowthStage();

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -3; dy <= radius / 2; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    // sparse scan for performance: skip most interior points, check a shell-ish pattern
                    if (((dx + dy + dz) & 3) != 0) continue;
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (isEdible(state, pos) && matchesStageBite(state, stage)) {
                        double dist = origin.distSqr(pos);
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = pos.immutable();
                        }
                    }
                }
            }
        }
        return best;
    }
}
