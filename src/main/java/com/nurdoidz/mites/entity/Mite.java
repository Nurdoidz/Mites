package com.nurdoidz.mites.entity;

import com.nurdoidz.mites.init.EntityInit;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Mite extends Animal implements NeutralMob {

    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.HONEY_BOTTLE);
    private static final Ingredient ENTHRALL_ITEMS = Ingredient.of(Items.COBBLESTONE, Items.FLINT, Items.DIRT,
        Items.OAK_LOG, Items.BONE_MEAL, Items.CLAY_BALL, Items.CACTUS, Items.ICE);
    private static final EntityDataAccessor<Integer> REMAINING_ANGER_TIME = SynchedEntityData.defineId(Mite.class,
        EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> ENTHRALL = SynchedEntityData.defineId(Mite.class,
        EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DIGEST_TIME = SynchedEntityData.defineId(Mite.class,
        EntityDataSerializers.INT);
    private static final String NBT_ENTHRALL = "Enthrall";
    private static final String NBT_DIGEST_TIME = "DigestTime";
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private UUID persistentAngerTarget;
    private Enthrall enthrall;
    private int digestTime = 100;
    private boolean isDigesting = false;

    public Mite(EntityType<? extends Mite> pType, Level pLevel) {
        super(pType, pLevel);
        setEnthrall(Enthrall.NONE);
    }

    public static AttributeSupplier.Builder getMiteAttributes() {
        return Mob.createMobAttributes().add(Attributes.FOLLOW_RANGE, 20.0D).add(Attributes.MAX_HEALTH, 8.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            this.updatePersistentAnger((ServerLevel) this.level, true);
        }
        if (!this.level.isClientSide && this.isAlive() && !this.isBaby()) {
            Block block = this.level.getBlockState(this.blockPosition()).getBlock();
            if (block instanceof HoneyBlock) {
                if (!this.isDigesting) {
                    this.playSound(block.asItem().getEatingSound());
                }
                this.isDigesting = true;
            }
            if (--this.digestTime <= 0 && this.isDigesting) {
                this.playSound(SoundEvents.CHICKEN_EGG, 1.0F,
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                this.spawnAtLocation(this.enthrall.item);
                this.gameEvent(GameEvent.ENTITY_PLACE);
                this.digestTime = this.enthrall.baseDigestTime;
                this.isDigesting = false;
            }
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return this.isBaby() ? 0.13F / 2 : 0.13F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ClimbOnTopOfPowderSnowGoal(this, this.level));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2,
            new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return EntityInit.MITE.get().create(level);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return FOOD_ITEMS.test(stack);
    }

    public boolean isEnthrall(ItemStack stack) {
        return ENTHRALL_ITEMS.test(stack);
    }

    public Enthrall getEnthrall() {
        return this.enthrall;
    }

    public void setEnthrall(Enthrall pEnthrall) {
        this.enthrall = pEnthrall;
        this.entityData.set(ENTHRALL, pEnthrall.name);
        this.entityData.set(DIGEST_TIME, pEnthrall.baseDigestTime);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.isFood(itemstack)) {
            int i = this.getAge();
            if (!this.level.isClientSide && i == 0 && this.canFallInLove()) {
                this.usePlayerItem(player, hand, itemstack);
                this.setInLove(player);
                return InteractionResult.SUCCESS;
            }

            if (this.isBaby()) {
                this.usePlayerItem(player, hand, itemstack);
                this.ageUp(getSpeedUpSecondsWhenFeeding(-i), true);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }

            if (this.level.isClientSide) {
                return InteractionResult.CONSUME;
            }
        } else if (this.isEnthrall(itemstack)) {
            if (!this.level.isClientSide && !this.isBaby()) {
                this.usePlayerItem(player, hand, itemstack);
                return InteractionResult.SUCCESS;
            }

            if (this.level.isClientSide) {
                return InteractionResult.CONSUME;
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.entityData.set(REMAINING_ANGER_TIME, time);
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ENTHRALL, "plain");
        this.entityData.define(DIGEST_TIME, 100);
        this.entityData.define(REMAINING_ANGER_TIME, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putString(NBT_ENTHRALL, this.enthrall.name);
        pCompound.putInt(NBT_DIGEST_TIME, this.digestTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        setEnthrall(Enthrall.fromName(pCompound.getString(NBT_ENTHRALL)));
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    public enum Enthrall {
        NONE("plain", Items.AIR, new float[]{0.1F, 0.1F, 0.1F}, 100),
        STONE("stone", Items.COBBLESTONE, new float[]{0.6F, 0.6F, 0.6F}, 100),
        FLINT("flint", Items.FLINT, new float[]{0.1F, 0.1F, 0.1F}, 400),
        DIRT("dirt", Items.DIRT, new float[]{0.55F, 0.39F, 0.27F}, 300),
        WOOD("wood", Items.OAK_LOG, new float[]{0.84F, 0.61F, 0.41F}, 300),
        BONE("bone", Items.BONE_MEAL, new float[]{1.0F, 0.949F, 0.78F}, 500),
        CLAY("clay", Items.CLAY_BALL, new float[]{0.612F, 0.639F, 0.678F}, 200),
        CACTUS("cactus", Items.CACTUS, new float[]{0.388F, 0.588F, 0.196F}, 600),
        ICE("ice", Items.ICE, new float[]{0.561F, 0.682F, 0.91F}, 700),
        GRAVEL("gravel", Items.GRAVEL, new float[]{0.408F, 0.376F, 0.369F}, 400),
        SUGAR("sugar", Items.SUGAR, new float[]{0.99F, 0.99F, 0.99F}, 600),
        SAND("sand", Items.SAND, new float[]{0.816F, 0.749F, 0.573F}, 500),
        REDSTONE("redstone", Items.REDSTONE, new float[]{0.996F, 0.0F, 0.0F}, 600),
        COAL("coal", Items.COAL, new float[]{0.0F, 0.0F, 0.0F}, 600),
        GUNPOWDER("gunpowder", Items.GUNPOWDER, new float[]{0.129F, 0.278F, 0.173F}, 700),
        SLIME("slime", Items.SLIME_BALL, new float[]{0.541F, 0.773F, 0.506F}, 800),
        GLASS("glass", Items.GLASS, new float[]{0.804F, 0.906F, 0.906F}, 800),
        STRING("string", Items.STRING, new float[]{0.988F, 0.71F, 1.0F}, 800),
        IRON("iron", Items.IRON_NUGGET, new float[]{0.71F, 0.247F, 0.051F}, 900),
        OBSIDIAN("obsidian", Items.OBSIDIAN, new float[]{0.314F, 0.204F, 0.455F}, 1200),
        LAPIS("lapis", Items.LAPIS_LAZULI, new float[]{0.231F, 0.416F, 0.773F}, 1000),
        QUARTZ("quartz", Items.QUARTZ, new float[]{0.969F, 0.737F, 0.816F}, 1000),
        BLAZE("blaze", Items.BLAZE_POWDER, new float[]{0.882F, 0.49F, 0.106F}, 1600),
        GOLD("gold", Items.GOLD_NUGGET, new float[]{0.89F, 0.796F, 0.196F}, 1800),
        DIAMOND("diamond", Items.DIAMOND, new float[]{0.29F, 0.929F, 0.851F}, 2400),
        EMERALD("emerald", Items.EMERALD, new float[]{0.0F, 0.714F, 0.525F}, 2400);
        private final Item item;
        private final String name;
        private final float[] color;
        private final int baseDigestTime;

        Enthrall(String pName, Item pEnthrall, float[] pColor, int pBaseDigestTime) {
            this.name = pName;
            this.item = pEnthrall;
            this.color = pColor;
            this.baseDigestTime = pBaseDigestTime;
        }

        public static Enthrall fromName(String pName) {
            for (Enthrall enthrall : Enthrall.values()) {
                if (enthrall.name.equals(pName)) {
                    return enthrall;
                }
            }
            return Enthrall.NONE;
        }

        public int getBaseDigestTime() {
            return this.baseDigestTime;
        }

        public Item getItem() {
            return this.item;
        }

        public String getName() {
            return this.name;
        }

        public float[] getColor() {
            return this.color;
        }
    }
}
