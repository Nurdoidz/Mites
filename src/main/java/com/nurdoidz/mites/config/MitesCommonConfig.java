package com.nurdoidz.mites.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class MitesCommonConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> NONE_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> STONE_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> FLINT_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> DIRT_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> WOOD_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> BONE_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> CLAY_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> CACTUS_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> SNOW_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> ICE_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> GRAVEL_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> SAND_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> COAL_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> REDSTONE_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> SUGAR_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> GUNPOWDER_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> SLIME_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> STRING_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> IRON_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> LAPIS_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> QUARTZ_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> BLAZE_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> OBSIDIAN_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> GLASS_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> GOLD_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> DIAMOND_ENTHRALL_BASE_DIGEST_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> EMERALD_ENTHRALL_BASE_DIGEST_TIME;

    public static final ForgeConfigSpec.ConfigValue<Integer> NONE_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> STONE_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> FLINT_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> DIRT_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> WOOD_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> BONE_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> CLAY_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> CACTUS_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> SNOW_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> ICE_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> GRAVEL_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> SAND_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> COAL_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> REDSTONE_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> SUGAR_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> GUNPOWDER_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> SLIME_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> STRING_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> IRON_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> LAPIS_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> QUARTZ_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> BLAZE_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> OBSIDIAN_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> GLASS_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> GOLD_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> DIAMOND_ENTHRALL_CONVERSION;
    public static final ForgeConfigSpec.ConfigValue<Integer> EMERALD_ENTHRALL_CONVERSION;

    static {
        BUILDER.push("Mite Enthralls base digest time");

        NONE_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Plain Mite to try creating its resource.")
                .define("Plain Mite base digest time", 100);
        STONE_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Stone Mite to try creating its resource.")
                .define("Stone Mite base digest time", 100);
        FLINT_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Flint Mite to try creating its resource.")
                .define("Flint Mite base digest time", 400);
        DIRT_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Dirt Mite to try creating its resource.")
                .define("Dirt Mite base digest time", 300);
        WOOD_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Wood Mite to try creating its resource.")
                .define("Wood Mite base digest time", 300);
        BONE_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Bone Mite to try creating its resource.")
                .define("Bone Mite base digest time", 500);
        CLAY_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Clay Mite to try creating its resource.")
                .define("Clay Mite base digest time", 200);
        CACTUS_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Cactus Mite to try creating its resource.")
                .define("Cactus Mite base digest time", 600);
        SNOW_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Snow Mite to try creating its resource.")
                .define("Snow Mite base digest time", 200);
        ICE_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes an Ice Mite to try creating its resource.")
                .define("Ice Mite base digest time", 700);
        GRAVEL_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Gravel Mite to try creating its resource.")
                .define("Gravel Mite base digest time", 400);
        SAND_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Sand Mite to try creating its resource.")
                .define("Sand Mite base digest time", 500);
        COAL_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Coal Mite to try creating its resource.")
                .define("Coal Mite base digest time", 600);
        REDSTONE_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Redstone Mite to try creating its resource.")
                .define("Redstone Mite base digest time", 600);
        SUGAR_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Sugar Mite to try creating its resource.")
                .define("Sugar Mite base digest time", 600);
        GUNPOWDER_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Gunpowder Mite to try creating its resource.")
                .define("Gunpowder Mite base digest time", 700);
        SLIME_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Slime Mite to try creating its resource.")
                .define("Slime Mite base digest time", 800);
        STRING_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a String Mite to try creating its resource.")
                .define("String Mite base digest time", 800);
        IRON_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes an Iron Mite to try creating its resource.")
                .define("Iron Mite base digest time", 75);
        LAPIS_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Lapis Mite to try creating its resource.")
                .define("Lapis Mite base digest time", 1000);
        QUARTZ_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Quartz Mite to try creating its resource.")
                .define("Quartz Mite base digest time", 1000);
        BLAZE_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Blaze Mite to try creating its resource.")
                .define("Blaze Mite base digest time", 1600);
        OBSIDIAN_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes an Obsidian Mite to try creating its resource.")
                .define("Obsidian Mite base digest time", 1200);
        GLASS_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Glass Mite to try creating its resource.")
                .define("Glass Mite base digest time", 800);
        GOLD_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Gold Mite to try creating its resource.")
                .define("Gold Mite base digest time", 100);
        DIAMOND_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes a Diamond Mite to try creating its resource.")
                .define("Diamond Mite base digest time", 2400);
        EMERALD_ENTHRALL_BASE_DIGEST_TIME = BUILDER
                .comment("Base time in ticks it takes an Emerald Mite to try creating its resource.")
                .define("Emerald Mite base digest time", 2400);

        BUILDER.pop();
        BUILDER.push("Mite Enthralls conversion factor");

        NONE_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Plain Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Plain Mite conversion factor", 10);
        STONE_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Stone Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Stone Mite conversion factor", 13);
        FLINT_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Flint Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Flint Mite conversion factor", 15);
        DIRT_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Dirt Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Dirt Mite conversion factor", 14);
        WOOD_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Wood Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Wood Mite conversion factor", 15);
        BONE_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Bone Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Bone Mite conversion factor", 15);
        CLAY_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Clay Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Clay Mite conversion factor", 14);
        CACTUS_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Cactus Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Cactus Mite conversion factor", 16);
        SNOW_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Snow Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Snow Mite conversion factor", 14);
        ICE_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Ice Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Ice Mite conversion factor", 16);
        GRAVEL_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Gravel Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Gravel Mite conversion factor", 15);
        SAND_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Sand Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Sand Mite conversion factor", 15);
        COAL_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Coal Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Coal Mite conversion factor", 17);
        REDSTONE_ENTHRALL_CONVERSION = BUILDER
                .comment(
                        "Weight factor for Redstone Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Redstone Mite conversion factor", 17);
        SUGAR_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Sugar Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Sugar Mite conversion factor", 16);
        GUNPOWDER_ENTHRALL_CONVERSION = BUILDER
                .comment(
                        "Weight factor for Gunpowder Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Gunpowder Mite conversion factor", 17);
        SLIME_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Slime Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Slime Mite conversion factor", 17);
        STRING_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for String Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("String Mite conversion factor", 16);
        IRON_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Iron Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Iron Mite conversion factor", 18);
        LAPIS_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Lapis Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Lapis Mite conversion factor", 19);
        QUARTZ_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Quartz Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Quartz Mite conversion factor", 19);
        BLAZE_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Blaze Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Blaze Mite conversion factor", 20);
        OBSIDIAN_ENTHRALL_CONVERSION = BUILDER
                .comment(
                        "Weight factor for Obsidian Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Obsidian Mite conversion factor", 19);
        GLASS_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Glass Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Glass Mite conversion factor", 17);
        GOLD_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Gold Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Gold Mite conversion factor", 20);
        DIAMOND_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Diamond Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Diamond Mite conversion factor", 22);
        EMERALD_ENTHRALL_CONVERSION = BUILDER
                .comment("Weight factor for Emerald Mite. Used when converting and breeding. Higher values mean rarer.")
                .define("Emerald Mite conversion factor", 22);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

}
