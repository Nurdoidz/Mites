package com.nurdoidz.mites.entity;

import com.nurdoidz.mites.init.EntityInit;
import com.nurdoidz.mites.util.Formulas;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    private static final EntityDataAccessor<Integer> APPETITE = SynchedEntityData.defineId(Mite.class,
        EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GREED = SynchedEntityData.defineId(Mite.class,
        EntityDataSerializers.INT);
    private static final String NBT_ENTHRALL = "Enthrall";
    private static final String NBT_DIGEST_TIME_LEFT = "DigestTimeLeft";
    private static final String NBT_APPETITE = "Appetite";
    private static final String NBT_GREED = "Greed";
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private UUID persistentAngerTarget;
    private Enthrall enthrall;
    private int digestTimeLeft = 100;
    private boolean isDigesting = false;
    private int appetite;
    private int greed;

    public Mite(EntityType<? extends Mite> pType, Level pLevel) {
        super(pType, pLevel);
        setEnthrall(Enthrall.NONE);
        this.appetite = new Random().nextInt(32);
        this.greed = new Random().nextInt(32);
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
            if (--this.digestTimeLeft <= 0 && this.isDigesting) {
                double chance = Formulas.getRollPercentage(this.greed);
                Random random = new Random();
                int count = 0;
                for (int i = 0; i < 4; i++) {
                    double roll = random.nextDouble();
                    if (roll <= chance) {
                        count++;
                    }
                }
                ItemStack stack = new ItemStack(this.enthrall.item);
                stack.setCount(count);
                this.spawnAtLocation(stack);
                if (count > 0) {
                    this.playSound(SoundEvents.CHICKEN_EGG, 1.0F,
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                }
                this.gameEvent(GameEvent.ENTITY_PLACE);
                this.digestTimeLeft = this.getFinalDigestTime();
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
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        Mite otherParent = (Mite) pOtherParent;
        Mite child = EntityInit.MITE.get().create(pLevel);
        child.setEnthrall(this.getOffspringEnthrall(this, otherParent));
        return child;
    }

    private Enthrall getOffspringEnthrall(Mite pFather, Mite pMother) {
        Map<Enthrall, Double> enthralls = new HashMap<>();
        enthralls.put(pFather.enthrall, 0.);
        enthralls.put(pMother.enthrall, 0.);
        for (Enthrall enthrall : Enthrall.values()) {
            if (enthrall.parents.contains(pFather.enthrall) && enthrall.parents.contains(pMother.enthrall)) {
                enthralls.put(enthrall, 0.);
            }
        }
        Random die = new Random();
        for (Enthrall enthrall : enthralls.keySet()) {
            int roll = die.nextInt(101);
            enthralls.replace(enthrall, (double) roll / (double) enthrall.conversion);
        }
        return Collections.max(enthralls.entrySet(), Map.Entry.comparingByValue()).getKey();
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

    private int getFinalDigestTime() {
        return Formulas.getFinalDigestTime(this.enthrall.baseDigestTime, this.appetite);
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
        this.entityData.define(APPETITE, 0);
        this.entityData.define(GREED, 0);
        this.entityData.define(REMAINING_ANGER_TIME, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putString(NBT_ENTHRALL, this.enthrall.name);
        pCompound.putInt(NBT_DIGEST_TIME_LEFT, this.digestTimeLeft);
        pCompound.putInt(NBT_APPETITE, this.appetite);
        pCompound.putInt(NBT_GREED, this.greed);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        setEnthrall(Enthrall.fromName(pCompound.getString(NBT_ENTHRALL)));
        this.digestTimeLeft = pCompound.getInt(NBT_DIGEST_TIME_LEFT);
        this.appetite = pCompound.getInt(NBT_APPETITE);
        this.greed = pCompound.getInt(NBT_GREED);
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    public enum Enthrall {
        NONE("plain", Items.PAPER, new float[]{0.1F, 0.1F, 0.1F}, 100, 10, new HashSet<>()),
        STONE("stone", Items.COBBLESTONE, new float[]{0.6F, 0.6F, 0.6F}, 100, 13, new HashSet<>()),
        FLINT("flint", Items.FLINT, new float[]{0.1F, 0.1F, 0.1F}, 400, 15, new HashSet<>()),
        DIRT("dirt", Items.DIRT, new float[]{0.55F, 0.39F, 0.27F}, 300, 14, new HashSet<>()),
        WOOD("wood", Items.OAK_LOG, new float[]{0.84F, 0.61F, 0.41F}, 300, 15, new HashSet<>()),
        BONE("bone", Items.BONE_MEAL, new float[]{1.0F, 0.949F, 0.78F}, 500, 15, new HashSet<>()),
        CLAY("clay", Items.CLAY_BALL, new float[]{0.612F, 0.639F, 0.678F}, 200, 14, new HashSet<>()),
        CACTUS("cactus", Items.CACTUS, new float[]{0.388F, 0.588F, 0.196F}, 600, 16, new HashSet<>()),
        ICE("ice", Items.ICE, new float[]{0.561F, 0.682F, 0.91F}, 700, 16, new HashSet<>()),
        GRAVEL("gravel", Items.GRAVEL, new float[]{0.408F, 0.376F, 0.369F}, 400, 15,
            Stream.of(STONE, FLINT).collect(Collectors.toCollection(HashSet::new))),
        SAND("sand", Items.SAND, new float[]{0.816F, 0.749F, 0.573F}, 500, 15,
            Stream.of(GRAVEL, FLINT).collect(Collectors.toCollection(HashSet::new))),
        COAL("coal", Items.COAL, new float[]{0.0F, 0.0F, 0.0F}, 600, 17,
            Stream.of(WOOD, FLINT).collect(Collectors.toCollection(HashSet::new))),
        REDSTONE("redstone", Items.REDSTONE, new float[]{0.996F, 0.0F, 0.0F}, 600, 17,
            Stream.of(SAND, COAL).collect(Collectors.toCollection(HashSet::new))),
        SUGAR("sugar", Items.SUGAR, new float[]{0.99F, 0.99F, 0.99F}, 600, 16,
            Stream.of(REDSTONE, BONE).collect(Collectors.toCollection(HashSet::new))),
        GUNPOWDER("gunpowder", Items.GUNPOWDER, new float[]{0.129F, 0.278F, 0.173F}, 700, 17,
            Stream.of(REDSTONE, SUGAR).collect(Collectors.toCollection(HashSet::new))),
        SLIME("slime", Items.SLIME_BALL, new float[]{0.541F, 0.773F, 0.506F}, 800, 17,
            Stream.of(CACTUS, SUGAR).collect(Collectors.toCollection(HashSet::new))),
        STRING("string", Items.STRING, new float[]{0.988F, 0.71F, 1.0F}, 800, 16,
            Stream.of(SUGAR, FLINT).collect(Collectors.toCollection(HashSet::new))),
        IRON("iron", Items.IRON_NUGGET, new float[]{0.71F, 0.247F, 0.051F}, 900, 18,
            Stream.of(GUNPOWDER, BONE).collect(Collectors.toCollection(HashSet::new))),
        LAPIS("lapis", Items.LAPIS_LAZULI, new float[]{0.231F, 0.416F, 0.773F}, 1000, 19,
            Stream.of(ICE, REDSTONE).collect(Collectors.toCollection(HashSet::new))),
        QUARTZ("quartz", Items.QUARTZ, new float[]{0.969F, 0.737F, 0.816F}, 1000, 19,
            Stream.of(LAPIS, BONE).collect(Collectors.toCollection(HashSet::new))),
        BLAZE("blaze", Items.BLAZE_POWDER, new float[]{0.882F, 0.49F, 0.106F}, 1600, 20,
            Stream.of(GUNPOWDER, FLINT).collect(Collectors.toCollection(HashSet::new))),
        OBSIDIAN("obsidian", Items.OBSIDIAN, new float[]{0.314F, 0.204F, 0.455F}, 1200, 19,
            Stream.of(ICE, BLAZE).collect(Collectors.toCollection(HashSet::new))),
        GLASS("glass", Items.GLASS, new float[]{0.804F, 0.906F, 0.906F}, 800, 17,
            Stream.of(SAND, BLAZE).collect(Collectors.toCollection(HashSet::new))),
        GOLD("gold", Items.GOLD_NUGGET, new float[]{0.89F, 0.796F, 0.196F}, 1800, 20,
            Stream.of(IRON, BLAZE).collect(Collectors.toCollection(HashSet::new))),
        DIAMOND("diamond", Items.DIAMOND, new float[]{0.29F, 0.929F, 0.851F}, 2400, 22,
            Stream.of(COAL, BLAZE).collect(Collectors.toCollection(HashSet::new))),
        EMERALD("emerald", Items.EMERALD, new float[]{0.0F, 0.714F, 0.525F}, 2400, 22,
            Stream.of(DIAMOND, SLIME).collect(Collectors.toCollection(HashSet::new)));
        private final Item item;
        private final String name;
        private final float[] color;
        private final int baseDigestTime;
        private final int conversion;
        private final Set<Enthrall> parents;

        Enthrall(String pName, Item pEnthrallItem, float[] pColor, int pBaseDigestTime, int pConversion,
            Set<Enthrall> pParents) {
            this.name = pName;
            this.item = pEnthrallItem;
            this.color = pColor;
            this.baseDigestTime = pBaseDigestTime;
            this.conversion = pConversion;
            this.parents = pParents;
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

        public int getConversion() {
            return this.conversion;
        }
    }
}
