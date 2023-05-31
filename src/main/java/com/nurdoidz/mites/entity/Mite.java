package com.nurdoidz.mites.entity;

import com.nurdoidz.mites.registry.MitesEntities;
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
import net.minecraft.network.chat.Component;
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
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Mite.class,
        EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_ENTHRALL_TYPE = SynchedEntityData.defineId(Mite.class,
        EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> DATA_APPETITE = SynchedEntityData.defineId(Mite.class,
        EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_GREED = SynchedEntityData.defineId(Mite.class,
        EntityDataSerializers.BYTE);
    private static final String NBT_ENTHRALL_TYPE = "EnthrallType";
    private static final String NBT_APPETITE = "Appetite";
    private static final String NBT_GREED = "Greed";
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private UUID persistentAngerTarget;
    private int digestTimeLeft = 100;
    private boolean isDigesting = false;

    public Mite(EntityType<? extends Mite> pType, Level pLevel) {
        super(pType, pLevel);
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
            if (this.isAlive() && !this.isBaby()) {
                Block block = this.level.getBlockState(this.blockPosition()).getBlock();
                if (block instanceof HoneyBlock) {
                    if (!this.isDigesting) {
                        this.tryToHealFromEating();
                        this.playSound(block.asItem().getEatingSound());
                    }
                    this.isDigesting = true;
                }
                if (--this.digestTimeLeft <= 0 && this.isDigesting) {
                    double chance = Formulas.getRollPercentage(this.getGreed());
                    int count = 0;
                    for (int i = 0; i < 4; i++) {
                        double roll = this.random.nextDouble();
                        if (roll <= chance) {
                            count++;
                        }
                    }
                    ItemStack stack = new ItemStack(this.getEnthrall().getItem());
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
    }

    private void tryToHealFromEating() {
        int roll = this.random.nextInt(6);
        if (roll == 0) {
            this.heal(1.0F);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override
    protected float getStandingEyeHeight(@NotNull Pose pPose, @NotNull EntityDimensions pDimensions) {
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
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        Mite otherParent = (Mite) pOtherParent;
        Mite child = MitesEntities.MITE.get().create(pLevel);
        if (child != null) {
            child.setEnthrall(this.getOffspringEnthrall(this, otherParent));
        }
        Formulas.applyIvInheritance(this, otherParent, child);
        return child;
    }

    private Enthrall getOffspringEnthrall(Mite pFather, Mite pMother) {
        Map<Enthrall, Double> enthrallCandidates = new HashMap<>();
        Enthrall father = pFather.getEnthrall();
        Enthrall mother = pMother.getEnthrall();
        enthrallCandidates.put(father, 0.);
        enthrallCandidates.put(mother, 0.);
        for (Enthrall enthrall : Enthrall.values()) {
            if (enthrall.getParents().contains(father) && enthrall.getParents().contains(mother)) {
                enthrallCandidates.put(enthrall, 0.);
            }
        }
        Random die = new Random();
        for (Enthrall enthrall : enthrallCandidates.keySet()) {
            int roll = die.nextInt(101);
            enthrallCandidates.replace(enthrall, (double) roll / (double) enthrall.getConversion());
        }
        return Collections.max(enthrallCandidates.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    @Override
    public boolean isFood(@NotNull ItemStack pStack) {
        return FOOD_ITEMS.test(pStack);
    }

    public boolean isEnthrallItem(ItemStack pStack) {
        for (Enthrall enthrall : Enthrall.values()) {
            if (enthrall.getEnthrallingItems().contains(pStack.getItem())) {
                return true;
            }
        }
        return false;
    }

    public Enthrall getEnthrall() {
        return Enthrall.fromName(this.entityData.get(DATA_ENTHRALL_TYPE));
    }

    public void setEnthrall(Enthrall pEnthrall) {
        this.entityData.set(DATA_ENTHRALL_TYPE, pEnthrall.getName());
    }

    private int getFinalDigestTime() {
        return Formulas.getFinalDigestTime(this.getEnthrall().getBaseDigestTime(), this.getAppetite());
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player pPlayer, @NotNull InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (this.isFood(itemstack)) {
            int i = this.getAge();
            if (!this.level.isClientSide && i == 0 && this.canFallInLove()) {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                this.tryToHealFromEating();
                this.setInLove(pPlayer);
                return InteractionResult.SUCCESS;
            }

            if (this.isBaby()) {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                this.tryToHealFromEating();
                this.ageUp(getSpeedUpSecondsWhenFeeding(-i), true);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }

            if (this.level.isClientSide) {
                return InteractionResult.CONSUME;
            }
        } else if (this.isEnthrallItem(itemstack)) {
            if (!this.level.isClientSide && !this.isBaby()) {
                Enthrall convertedEnthrall = Enthrall.fromItem(itemstack);
                Enthrall thisEnthrall = this.getEnthrall();
                if (thisEnthrall != convertedEnthrall && convertedEnthrall.isConvertibleByItem()) {
                    Map<Enthrall, Double> enthrallCandidates = new HashMap<>();
                    if (convertedEnthrall == Enthrall.NONE) {
                        enthrallCandidates.put(Enthrall.NONE, 0.);
                    } else if (convertedEnthrall == Enthrall.ICE) {
                        if (thisEnthrall == Enthrall.SNOW) {
                            enthrallCandidates.put(Enthrall.ICE, 0.);
                        }
                    } else if (thisEnthrall == Enthrall.NONE) {
                        enthrallCandidates.put(convertedEnthrall, 0.);
                    }
                    if (enthrallCandidates.isEmpty()) {
                        return InteractionResult.PASS;
                    }
                    this.usePlayerItem(pPlayer, pHand, itemstack);
                    this.tryToHealFromEating();
                    this.playSound(itemstack.getEatingSound());
                    enthrallCandidates.put(Enthrall.NONE, 0.);
                    Random die = new Random();
                    for (Enthrall enthrall : enthrallCandidates.keySet()) {
                        int roll = die.nextInt(101);
                        enthrallCandidates.replace(enthrall, (double) roll / (double) enthrall.getConversion());
                    }
                    final Enthrall finalEnthrall = Collections.max(enthrallCandidates.entrySet(),
                        Map.Entry.comparingByValue()).getKey();
                    if (thisEnthrall != finalEnthrall) {
                        this.setEnthrall(finalEnthrall);
                    }
                }
                return InteractionResult.SUCCESS;
            }

            if (this.level.isClientSide) {
                return InteractionResult.CONSUME;
            }
        } else if (itemstack.isEmpty()) {
            if (!this.level.isClientSide) {
                pPlayer.sendSystemMessage(Component.literal(
                    "Type: " + this.getEnthrall().getName() + ", Appetite: " + this.getAppetite() + ", Greed: "
                        + this.getGreed()));
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.CONSUME;
            }
        }

        return super.mobInteract(pPlayer, pHand);
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int pTime) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, pTime);
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID pTarget) {
        this.persistentAngerTarget = pTarget;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ENTHRALL_TYPE, "plain");
        this.entityData.define(DATA_APPETITE, Formulas.getNewIV());
        this.entityData.define(DATA_GREED, Formulas.getNewIV());
        this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putString(NBT_ENTHRALL_TYPE, this.getEnthrall().getName());
        pCompound.putInt(NBT_APPETITE, this.getAppetite());
        pCompound.putInt(NBT_GREED, this.getGreed());
    }

    public byte getGreed() {
        return this.entityData.get(DATA_GREED);
    }

    public void setGreed(byte pGreed) {
        this.entityData.set(DATA_GREED, pGreed);
    }

    public byte getAppetite() {
        return this.entityData.get(DATA_APPETITE);
    }

    public void setAppetite(byte pAppetite) {
        this.entityData.set(DATA_APPETITE, pAppetite);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains(NBT_ENTHRALL_TYPE, 8)) {
            this.setEnthrall(Enthrall.fromName(pCompound.getString(NBT_ENTHRALL_TYPE)));
        } else {
            this.setEnthrall(Enthrall.NONE);
        }
        if (pCompound.contains(NBT_APPETITE, 99)) {
            this.setAppetite(pCompound.getByte(NBT_APPETITE));
        } else {
            this.setAppetite(Formulas.getNewIV());
        }
        if (pCompound.contains(NBT_GREED, 99)) {
            this.setGreed(pCompound.getByte(NBT_GREED));
        } else {
            this.setGreed(Formulas.getNewIV());
        }
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    public enum Enthrall {
        NONE("plain", Items.AIR, 100, 10, new HashSet<>(),
            Stream.of(Items.ROTTEN_FLESH).collect(Collectors.toCollection(HashSet::new))),
        STONE("stone", Items.COBBLESTONE, 100, 13, new HashSet<>(),
            Stream.of(Items.STONE).collect(Collectors.toCollection(HashSet::new))),
        FLINT("flint", Items.FLINT, 400, 15, new HashSet<>(),
            Stream.of(Items.FLINT).collect(Collectors.toCollection(HashSet::new))),
        DIRT("dirt", Items.DIRT, 300, 14, new HashSet<>(),
            Stream.of(Items.DIRT).collect(Collectors.toCollection(HashSet::new))),
        WOOD("wood", Items.OAK_LOG, 300, 15, new HashSet<>(),
            Stream.of(Items.OAK_LOG).collect(Collectors.toCollection(HashSet::new))),
        BONE("bone", Items.BONE_MEAL, 500, 15, new HashSet<>(),
            Stream.of(Items.COD, Items.SALMON, Items.TROPICAL_FISH).collect(Collectors.toCollection(HashSet::new))),
        CLAY("clay", Items.CLAY_BALL, 200, 14, new HashSet<>(),
            Stream.of(Items.CLAY).collect(Collectors.toCollection(HashSet::new))),
        CACTUS("cactus", Items.CACTUS, 600, 16, new HashSet<>(),
            Stream.of(Items.GREEN_DYE).collect(Collectors.toCollection(HashSet::new))),
        SNOW("snow", Items.SNOWBALL, 200, 14, new HashSet<>(),
            Stream.of(Items.SNOW_BLOCK).collect(Collectors.toCollection(HashSet::new))),
        ICE("ice", Items.ICE, 700, 16, new HashSet<>(),
            Stream.of(Items.ICE).collect(Collectors.toCollection(HashSet::new))),
        GRAVEL("gravel", Items.GRAVEL, 400, 15,
            Stream.of(STONE, FLINT).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        SAND("sand", Items.SAND, 500, 15,
            Stream.of(GRAVEL, FLINT).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        COAL("coal", Items.COAL, 600, 17,
            Stream.of(WOOD, FLINT).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        REDSTONE("redstone", Items.REDSTONE, 600, 17,
            Stream.of(SAND, COAL).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        SUGAR("sugar", Items.SUGAR, 600, 16,
            Stream.of(REDSTONE, BONE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        GUNPOWDER("gunpowder", Items.GUNPOWDER, 700, 17,
            Stream.of(REDSTONE, SUGAR).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        SLIME("slime", Items.SLIME_BALL, 800, 17,
            Stream.of(CACTUS, SUGAR).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        STRING("string", Items.STRING, 800, 16,
            Stream.of(SUGAR, FLINT).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        IRON("iron", Items.IRON_NUGGET, 900, 18,
            Stream.of(GUNPOWDER, BONE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        LAPIS("lapis", Items.LAPIS_LAZULI, 1000, 19,
            Stream.of(ICE, REDSTONE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        QUARTZ("quartz", Items.QUARTZ, 1000, 19,
            Stream.of(LAPIS, BONE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        BLAZE("blaze", Items.BLAZE_POWDER, 1600, 20,
            Stream.of(GUNPOWDER, FLINT).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        OBSIDIAN("obsidian", Items.OBSIDIAN, 1200, 19,
            Stream.of(ICE, BLAZE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        GLASS("glass", Items.GLASS, 800, 17,
            Stream.of(SAND, BLAZE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        GOLD("gold", Items.GOLD_NUGGET, 1800, 20,
            Stream.of(IRON, BLAZE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        DIAMOND("diamond", Items.DIAMOND, 2400, 22,
            Stream.of(COAL, BLAZE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>()),
        EMERALD("emerald", Items.EMERALD, 2400, 22,
            Stream.of(DIAMOND, SLIME).collect(Collectors.toCollection(HashSet::new)), new HashSet<>());
        private final Item item;
        private final String name;
        private final int baseDigestTime;
        private final int conversion;
        private final Set<Enthrall> parents;
        private final Set<Item> enthrallingItems;

        Enthrall(String pName, Item pEnthrallItem, int pBaseDigestTime, int pConversion,
            Set<Enthrall> pParents, Set<Item> pEnthrallingItems) {
            this.name = pName;
            this.item = pEnthrallItem;
            this.baseDigestTime = pBaseDigestTime;
            this.conversion = pConversion;
            this.parents = pParents;
            this.enthrallingItems = pEnthrallingItems;
        }

        public static Enthrall fromName(String pName) {
            for (Enthrall enthrall : Enthrall.values()) {
                if (enthrall.name.equals(pName)) {
                    return enthrall;
                }
            }
            return Enthrall.NONE;
        }

        public static Enthrall fromItem(ItemStack pStack) {
            if (pStack.isEmpty()) {
                return Enthrall.NONE;
            }
            Item item = pStack.getItem();
            for (Enthrall enthrall : Enthrall.values()) {
                if (enthrall.enthrallingItems.contains(item)) {
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

        public int getConversion() {
            return this.conversion;
        }

        public boolean isConvertibleByItem() {
            return !this.enthrallingItems.isEmpty();
        }

        public Set<Enthrall> getParents() {
            return Collections.unmodifiableSet(this.parents);
        }

        public Set<Item> getEnthrallingItems() {
            return Collections.unmodifiableSet(this.enthrallingItems);
        }
    }
}
