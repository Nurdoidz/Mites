package com.nurdoidz.mites;

import com.mojang.logging.LogUtils;
import com.nurdoidz.mites.config.MitesCommonConfig;
import com.nurdoidz.mites.registry.MitesEntities;
import com.nurdoidz.mites.registry.MitesItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(Mites.MODID)
public class Mites {

    public static final String MODID = "mites";
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB, MODID);
    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> MITES_TAB = CREATIVE_MODE_TABS.register("",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("Mites"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> MitesItems.MITE_INSPECTOR.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(
                                MitesItems.MITE_INSPECTOR.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
                        output.accept(MitesItems.MITE_SPAWN_EGG.get());
                        output.accept(Items.HONEY_BOTTLE);
                        output.accept(Items.HONEY_BLOCK);
                    }).build());
    private static final Logger LOGGER = LogUtils.getLogger();

    public Mites() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MitesItems.register(modEventBus);
        MitesEntities.register(modEventBus);

        ModLoadingContext.get().registerConfig(Type.COMMON, MitesCommonConfig.SPEC, "mites-common.toml");
        CREATIVE_MODE_TABS.register(modEventBus);

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
