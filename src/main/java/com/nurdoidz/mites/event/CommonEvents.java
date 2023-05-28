package com.nurdoidz.mites.event;

import com.nurdoidz.mites.Mites;
import com.nurdoidz.mites.entity.Mite;
import com.nurdoidz.mites.init.EntityInit;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Mites.MODID, bus = Bus.MOD)
public class CommonEvents {

    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityInit.MITE.get(), Mite.getMiteAttributes().build());
    }
}
