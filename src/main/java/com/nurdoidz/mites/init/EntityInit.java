package com.nurdoidz.mites.init;

import com.nurdoidz.mites.Mites;
import com.nurdoidz.mites.entities.Mite;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
        ForgeRegistries.ENTITY_TYPES, Mites.MODID);

    public static final RegistryObject<EntityType<Mite>> MITE = ENTITY_TYPES.register("mite",
        () -> EntityType.Builder.of(Mite::new,
            MobCategory.CREATURE).sized(0.4F, 0.3F).build(Mites.MODID + ":mite"));
}
