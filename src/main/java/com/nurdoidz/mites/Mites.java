package com.nurdoidz.mites;

import com.mojang.logging.LogUtils;
import com.nurdoidz.mites.config.MitesCommonConfig;
import com.nurdoidz.mites.registry.MitesEntities;
import com.nurdoidz.mites.registry.MitesItems;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Mites.MODID)
public class Mites {

    public static final String MODID = "mites";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Mites() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MitesItems.register(modEventBus);
        MitesEntities.register(modEventBus);

        ModLoadingContext.get().registerConfig(Type.COMMON, MitesCommonConfig.SPEC, "mites-common.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }

}
