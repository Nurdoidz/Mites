package com.nurdoidz.mites.entity;

import com.nurdoidz.mites.config.MitesCommonConfig;
import com.nurdoidz.mites.item.InspectorTool;
import com.nurdoidz.mites.registry.MitesEntities;
import com.nurdoidz.mites.registry.MitesItems;
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
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
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
    private static final Ingredient INSPECTOR_ITEM = Ingredient.of(MitesItems.MITE_INSPECTOR.get());
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

    public static Set<Enthrall> getEnthrallCandidates(Enthrall pFather, Enthrall pMother) {

        Set<Enthrall> results = new HashSet<>();
        results.add(pFather);
        results.add(pMother);
        results.add(Enthrall.NONE);
        if (pFather.equals(pMother)) return results;
        for (Enthrall enthrall : Enthrall.values()) {
            if (enthrall.getParents().contains(pFather) && enthrall.getParents().contains(pMother)) {
                results.add(enthrall);
            }
        }
        return results;
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
        for (Enthrall enthrall : getEnthrallCandidates(pFather.getEnthrall(), pMother.getEnthrall())) {
            enthrallCandidates.put(enthrall, 0.);
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

    public boolean isInspectorItem(@NotNull ItemStack pStack) {

        return INSPECTOR_ITEM.test(pStack);
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
        } else if (this.isInspectorItem(itemstack)) {
            if (!this.level.isClientSide) {
                this.handleInspectorTool(pPlayer, (InspectorTool) itemstack.getItem());
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

    private void handleInspectorTool(Player pPlayer, InspectorTool pInspector) {

        if (this.level.isClientSide) {
            return;
        }
        if (pPlayer.isShiftKeyDown()) {
            switch (pInspector.next(this.getEnthrall())) {
                case PARENT -> pPlayer.sendSystemMessage(
                        MutableComponent.create(new TranslatableContents("entity.mites.inspector_selected_parent")).withStyle(ChatFormatting.YELLOW));
                case OFFSPRING -> {
                    MutableComponent component = MutableComponent.create(
                            new TranslatableContents("entity.mites.inspector_selected_offspring")).withStyle(ChatFormatting.YELLOW);
                    MutableComponent delimiter = MutableComponent.create(
                            new TranslatableContents("entity.mites.inspector_selected_offspring.delimiter"));
                    int count = 1;
                    for (Enthrall enthrall : pInspector.getOffspring()) {
                        component.append(MutableComponent.create(enthrall.getTranslatableName()));
                        if (pInspector.getOffspring().size() > count) {
                            component.append(delimiter);
                        }
                        count++;
                    }
                    pPlayer.sendSystemMessage(component);
                }
                default -> {
                }
            }
        } else {
            pPlayer.sendSystemMessage(
                    MutableComponent.create(this.getEnthrall().getTranslatableName()).withStyle(ChatFormatting.YELLOW)
                            .append(MutableComponent.create(
                                    new TranslatableContents("entity.mites.inspector_type_suffix")))
                            .append(MutableComponent.create(
                                    AppetiteInspector.fromValue(this.getAppetite()).getTranslatable()))
                            .append(MutableComponent.create(
                                    GreedInspector.fromValue(this.getGreed()).getTranslatable())));
        }

    }

    public enum GreedInspector {
        WORST(new TranslatableContents("entity.mites.greed_inspector.worst")),
        BAD(new TranslatableContents("entity.mites.greed_inspector.bad")),
        OK(new TranslatableContents("entity.mites.greed_inspector.ok")),
        GOOD(new TranslatableContents("entity.mites.greed_inspector.good")),
        BEST(new TranslatableContents("entity.mites.greed_inspector.best"));
        private final TranslatableContents translatable;

        GreedInspector(TranslatableContents pTranslatable) {

            this.translatable = pTranslatable;
        }

        public static GreedInspector fromValue(byte pValue) {

            if (pValue < 1) {
                return GreedInspector.WORST;
            } else if (pValue <= 10) {
                return GreedInspector.BAD;
            } else if (pValue <= 20) {
                return GreedInspector.OK;
            } else if (pValue <= 30) {
                return GreedInspector.GOOD;
            } else {
                return GreedInspector.BEST;
            }
        }

        public TranslatableContents getTranslatable() {

            return this.translatable;
        }

    }

    public enum AppetiteInspector {
        WORST(new TranslatableContents("entity.mites.appetite_inspector.worst")),
        BAD(new TranslatableContents("entity.mites.appetite_inspector.bad")),
        OK(new TranslatableContents("entity.mites.appetite_inspector.ok")),
        GOOD(new TranslatableContents("entity.mites.appetite_inspector.good")),
        BEST(new TranslatableContents("entity.mites.appetite_inspector.best"));
        private final TranslatableContents translatable;

        AppetiteInspector(TranslatableContents pTranslatable) {

            this.translatable = pTranslatable;
        }

        public static AppetiteInspector fromValue(byte pValue) {

            if (pValue < 1) {
                return AppetiteInspector.WORST;
            } else if (pValue <= 10) {
                return AppetiteInspector.BAD;
            } else if (pValue <= 20) {
                return AppetiteInspector.OK;
            } else if (pValue <= 30) {
                return AppetiteInspector.GOOD;
            } else {
                return AppetiteInspector.BEST;
            }
        }

        public TranslatableContents getTranslatable() {

            return this.translatable;
        }

    }

    public enum Enthrall {
        NONE("plain", Items.AIR, MitesCommonConfig.NONE_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.NONE_ENTHRALL_CONVERSION.get(), new HashSet<>(),
                Stream.of(Items.ROTTEN_FLESH).collect(Collectors.toCollection(HashSet::new)),
                new TranslatableContents("entity.mites.enthrall.none.name")),
        STONE("stone", Items.COBBLESTONE, MitesCommonConfig.STONE_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.STONE_ENTHRALL_CONVERSION.get(), new HashSet<>(),
                Stream.of(Items.STONE).collect(Collectors.toCollection(HashSet::new)),
                new TranslatableContents("entity.mites.enthrall.stone.name")),
        FLINT("flint", Items.FLINT, MitesCommonConfig.FLINT_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.FLINT_ENTHRALL_CONVERSION.get(), new HashSet<>(),
                Stream.of(Items.FLINT).collect(Collectors.toCollection(HashSet::new)),
                new TranslatableContents("entity.mites.enthrall.flint.name")),
        DIRT("dirt", Items.DIRT, MitesCommonConfig.DIRT_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.DIRT_ENTHRALL_CONVERSION.get(), new HashSet<>(),
                Stream.of(Items.DIRT).collect(Collectors.toCollection(HashSet::new)),
                new TranslatableContents("entity.mites.enthrall.dirt.name")),
        WOOD("wood", Items.OAK_LOG, MitesCommonConfig.WOOD_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.WOOD_ENTHRALL_CONVERSION.get(), new HashSet<>(),
                Stream.of(Items.OAK_LOG).collect(Collectors.toCollection(HashSet::new)),
                new TranslatableContents("entity.mites.enthrall.wood.name")),
        BONE("bone", Items.BONE_MEAL, MitesCommonConfig.BONE_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.BONE_ENTHRALL_CONVERSION.get(), new HashSet<>(),
                Stream.of(Items.COD, Items.SALMON, Items.TROPICAL_FISH).collect(Collectors.toCollection(HashSet::new)),
                new TranslatableContents("entity.mites.enthrall.bone.name")),
        CLAY("clay", Items.CLAY_BALL, MitesCommonConfig.CLAY_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.CLAY_ENTHRALL_CONVERSION.get(), new HashSet<>(),
                Stream.of(Items.CLAY).collect(Collectors.toCollection(HashSet::new)),
                new TranslatableContents("entity.mites.enthrall.clay.name")),
        CACTUS("cactus", Items.CACTUS, MitesCommonConfig.CACTUS_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.CACTUS_ENTHRALL_CONVERSION.get(), new HashSet<>(),
                Stream.of(Items.GREEN_DYE).collect(Collectors.toCollection(HashSet::new)),
                new TranslatableContents("entity.mites.enthrall.cactus.name")),
        SNOW("snow", Items.SNOWBALL, MitesCommonConfig.SNOW_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.SNOW_ENTHRALL_CONVERSION.get(), new HashSet<>(),
                Stream.of(Items.SNOW_BLOCK).collect(Collectors.toCollection(HashSet::new)),
                new TranslatableContents("entity.mites.enthrall.snow.name")),
        ICE("ice", Items.ICE, MitesCommonConfig.ICE_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.ICE_ENTHRALL_CONVERSION.get(), new HashSet<>(),
                Stream.of(Items.ICE).collect(Collectors.toCollection(HashSet::new)),
                new TranslatableContents("entity.mites.enthrall.ice.name")),
        GRAVEL("gravel", Items.GRAVEL, MitesCommonConfig.GRAVEL_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.GRAVEL_ENTHRALL_CONVERSION.get(),
                Stream.of(STONE, FLINT).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.gravel.name")),
        SAND("sand", Items.SAND, MitesCommonConfig.SAND_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.SAND_ENTHRALL_CONVERSION.get(),
                Stream.of(GRAVEL, FLINT).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.sand.name")),
        COAL("coal", Items.COAL, MitesCommonConfig.COAL_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.COAL_ENTHRALL_CONVERSION.get(),
                Stream.of(WOOD, FLINT).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.coal.name")),
        REDSTONE("redstone", Items.REDSTONE, MitesCommonConfig.REDSTONE_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.REDSTONE_ENTHRALL_CONVERSION.get(),
                Stream.of(SAND, COAL).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.redstone.name")),
        SUGAR("sugar", Items.SUGAR, MitesCommonConfig.SUGAR_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.SUGAR_ENTHRALL_CONVERSION.get(),
                Stream.of(REDSTONE, BONE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.sugar.name")),
        GUNPOWDER("gunpowder", Items.GUNPOWDER, MitesCommonConfig.GUNPOWDER_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.GUNPOWDER_ENTHRALL_CONVERSION.get(),
                Stream.of(REDSTONE, SUGAR).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.gunpowder.name")),
        SLIME("slime", Items.SLIME_BALL, MitesCommonConfig.SLIME_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.SLIME_ENTHRALL_CONVERSION.get(),
                Stream.of(CACTUS, SUGAR).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.slime.name")),
        STRING("string", Items.STRING, MitesCommonConfig.STRING_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.STRING_ENTHRALL_CONVERSION.get(),
                Stream.of(SUGAR, FLINT).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.string.name")),
        IRON("iron", Items.IRON_NUGGET, MitesCommonConfig.IRON_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.IRON_ENTHRALL_CONVERSION.get(),
                Stream.of(GUNPOWDER, BONE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.iron.name")),
        LAPIS("lapis", Items.LAPIS_LAZULI, MitesCommonConfig.LAPIS_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.LAPIS_ENTHRALL_CONVERSION.get(),
                Stream.of(ICE, REDSTONE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.lapis.name")),
        QUARTZ("quartz", Items.QUARTZ, MitesCommonConfig.QUARTZ_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.QUARTZ_ENTHRALL_CONVERSION.get(),
                Stream.of(LAPIS, BONE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.quartz.name")),
        BLAZE("blaze", Items.BLAZE_POWDER, MitesCommonConfig.BLAZE_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.BLAZE_ENTHRALL_CONVERSION.get(),
                Stream.of(GUNPOWDER, FLINT).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.blaze.name")),
        OBSIDIAN("obsidian", Items.OBSIDIAN, MitesCommonConfig.OBSIDIAN_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.OBSIDIAN_ENTHRALL_CONVERSION.get(),
                Stream.of(ICE, BLAZE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.obsidian.name")),
        GLASS("glass", Items.GLASS, MitesCommonConfig.GLASS_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.GLASS_ENTHRALL_CONVERSION.get(),
                Stream.of(SAND, BLAZE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.glass.name")),
        GOLD("gold", Items.GOLD_NUGGET, MitesCommonConfig.GOLD_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.GOLD_ENTHRALL_CONVERSION.get(),
                Stream.of(IRON, BLAZE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.gold.name")),
        DIAMOND("diamond", Items.DIAMOND, MitesCommonConfig.DIAMOND_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.DIAMOND_ENTHRALL_CONVERSION.get(),
                Stream.of(COAL, BLAZE).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.diamond.name")),
        EMERALD("emerald", Items.EMERALD, MitesCommonConfig.EMERALD_ENTHRALL_BASE_DIGEST_TIME.get(),
                MitesCommonConfig.EMERALD_ENTHRALL_CONVERSION.get(),
                Stream.of(DIAMOND, SLIME).collect(Collectors.toCollection(HashSet::new)), new HashSet<>(),
                new TranslatableContents("entity.mites.enthrall.emerald.name"));
        private final Item item;
        private final String name;
        private final int baseDigestTime;
        private final int conversion;
        private final Set<Enthrall> parents;
        private final Set<Item> enthrallingItems;

        private final TranslatableContents translatableName;

        Enthrall(String pName, Item pEnthrallItem, int pBaseDigestTime, int pConversion,
                Set<Enthrall> pParents, Set<Item> pEnthrallingItems, TranslatableContents pTranslatableName) {

            this.name = pName;
            this.item = pEnthrallItem;
            this.baseDigestTime = pBaseDigestTime;
            this.conversion = pConversion;
            this.parents = pParents;
            this.enthrallingItems = pEnthrallingItems;
            this.translatableName = pTranslatableName;
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

        public TranslatableContents getTranslatableName() {

            return this.translatableName;
        }
    }
}
