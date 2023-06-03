package com.nurdoidz.mites.event;

import com.nurdoidz.mites.Mites;
import com.nurdoidz.mites.entity.Mite;
import com.nurdoidz.mites.registry.MitesEntities;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Mites.MODID, bus = Bus.MOD)
public class CommonEvents {

    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent pEvent) {

        pEvent.put(MitesEntities.MITE.get(), Mite.getMiteAttributes().build());
    }

    @SubscribeEvent
    public static void entitySpawnRestriction(SpawnPlacementRegisterEvent pEvent) {

        pEvent.register(MitesEntities.MITE.get(), SpawnPlacements.Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES,
                Mite::checkMiteSpawnRules, Operation.REPLACE);
    }
}
