package com.fleshterror.entity;

import com.fleshterror.entity.ai.TentacleGrabGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.BossEvent;

/**
 * A slow, growing horror of flesh. Starts small and harmless-looking, but every structure
 * block its tentacles rip out feeds its growth. At max growth stage it is a wither-storm-scale
 * colossus that shrugs off most damage and levels builds.
 */
public class FleshMonsterEntity extends Monster {

    public static final int MAX_STAGE = 4;
    // cumulative growth points required to advance FROM stage i TO stage i+1
    private static final int[] POINTS_TO_NEXT_STAGE = {6, 16, 32, 55};

    private static final EntityDataAccessor<Integer> DATA_GROWTH_STAGE =
            SynchedEntityData.defineId(FleshMonsterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_GROWTH_POINTS =
            SynchedEntityData.defineId(FleshMonsterEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

    public FleshMonsterEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_GROWTH_STAGE, 0);
        this.entityData.define(DATA_GROWTH_POINTS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TentacleGrabGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ---------------- Growth system ----------------

    public int getGrowthStage() {
        return this.entityData.get(DATA_GROWTH_STAGE);
    }

    public void setGrowthStage(int stage) {
        int clamped = Math.max(0, Math.min(MAX_STAGE, stage));
        this.entityData.set(DATA_GROWTH_STAGE, clamped);
        this.refreshDimensions();
        applyStageAttributes();
    }

    public int getGrowthPoints() {
        return this.entityData.get(DATA_GROWTH_POINTS);
    }

    /** Call this whenever the monster tears a block out of a structure. */
    public void addGrowth(int points) {
        if (this.getGrowthStage() >= MAX_STAGE) return;
        int total = this.getGrowthPoints() + points;
        int stage = this.getGrowthStage();
        int needed = POINTS_TO_NEXT_STAGE[stage];
        if (total >= needed) {
            total -= needed;
            setGrowthStage(stage + 1);
            this.heal(this.getMaxHealth());
            this.level().broadcastEntityEvent(this, (byte) 60); // grow burst particles, handled client-side by default hurt anim as fallback
        }
        this.entityData.set(DATA_GROWTH_POINTS, Math.max(0, total));
    }

    private void applyStageAttributes() {
        int stage = this.getGrowthStage();
        double healthMult = 1.0 + stage * 1.35;      // stage4 ~ 6.4x base health
        double dmgMult = 1.0 + stage * 0.6;          // stage4 ~ 3.4x base damage
        double speedMult = Math.max(0.45, 1.0 - stage * 0.12); // slows down as it gets huge

        double baseHealth = 40.0D;
        double baseDamage = 5.0D;
        double baseSpeed = 0.22D;

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth * healthMult);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(baseDamage * dmgMult);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(baseSpeed * speedMult);
        this.getAttribute(Attributes.ARMOR).setBaseValue(4.0 + stage * 4.0);
    }

    /** Scale factor applied to the render size and hitbox for the current stage. */
    public float getStageScale() {
        return 1.0f + this.getGrowthStage() * 0.85f; // stage 0 = 1.0x, stage 4 = 4.4x
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions base = EntityDimensions.scalable(1.2f, 1.6f);
        float scale = getStageScale();
        return base.scale(scale);
    }

    // ---------------- Boss bar (shown once it's grown large enough to matter) ----------------

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.getGrowthStage() >= 2) this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.bossEvent.setName(this.getDisplayName().copy().append(" [" + growthStageName() + "]"));
        if (this.getGrowthStage() < 2) {
            for (ServerPlayer p : this.bossEvent.getPlayers()) this.bossEvent.removePlayer(p);
        }
    }

    public String growthStageName() {
        return switch (this.getGrowthStage()) {
            case 0 -> "Spawnling";
            case 1 -> "Juvenile";
            case 2 -> "Adult";
            case 3 -> "Elder";
            default -> "Colossus";
        };
    }

    // ---------------- Sounds ----------------
    // Deliberately distorted/deeper than a normal zombie: mixes in Ravager sound events
    // (which are already lower and gnarlier) and then pitches everything down hard,
    // getting deeper still as it grows.

    @Override
    protected SoundEvent getAmbientSound() {
        return this.getGrowthStage() >= 2 ? SoundEvents.RAVAGER_AMBIENT : SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return this.getGrowthStage() >= 2 ? SoundEvents.RAVAGER_HURT : SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.getGrowthStage() >= 2 ? SoundEvents.RAVAGER_DEATH : SoundEvents.ZOMBIE_DEATH;
    }

    @Override
    public float getVoicePitch() {
        // Base zombie/ravager pitch is ~1.0; this drags it way down into a distorted growl,
        // and it keeps sinking further as the monster grows.
        float base = 0.55f - this.getGrowthStage() * 0.08f;
        return Math.max(0.22f, base + (this.random.nextFloat() - 0.5f) * 0.05f);
    }

    @Override
    public float getSoundVolume() {
        return 1.3f + this.getGrowthStage() * 0.6f;
    }

    // ---------------- Persistence ----------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GrowthStage", this.getGrowthStage());
        tag.putInt("GrowthPoints", this.getGrowthPoints());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_GROWTH_STAGE, tag.getInt("GrowthStage"));
        this.entityData.set(DATA_GROWTH_POINTS, tag.getInt("GrowthPoints"));
        this.refreshDimensions();
        applyStageAttributes();
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        super.die(source);
        this.bossEvent.removeAllPlayers();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false; // this thing doesn't despawn once it's awake
    }
}
