package com.nurdoidz.mites.registry;

import com.nurdoidz.mites.Mites;
import com.nurdoidz.mites.entity.Mite;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MitesEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
        ForgeRegistries.ENTITY_TYPES, Mites.MODID);

    public static final RegistryObject<EntityType<Mite>> MITE = ENTITY_TYPES.register("mite",
        () -> EntityType.Builder.of(Mite::new, MobCategory.CREATURE)
            .sized(0.4F, 0.3F)
            .build(Mites.MODID + ":mite"));

    public static void register(IEventBus pEventBus) {
        ENTITY_TYPES.register(pEventBus);
    }
}
