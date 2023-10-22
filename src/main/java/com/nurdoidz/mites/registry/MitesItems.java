package com.nurdoidz.mites.registry;

import com.nurdoidz.mites.Mites;
import com.nurdoidz.mites.item.MiteInspector;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MitesItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Mites.MODID);

    public static final RegistryObject<ForgeSpawnEggItem> MITE_SPAWN_EGG = ITEMS.register("mite_spawn_egg",
            () -> new ForgeSpawnEggItem(MitesEntities.MITE, 0x5E422C, 0x262626,
                    new Properties()
                            .tab(CreativeModeTab.TAB_MISC)
                            .stacksTo(16)));
    public static final RegistryObject<Item> MITE_INSPECTOR = ITEMS.register("mite_inspector",
            () -> new MiteInspector(new Item.Properties()
                    .tab(CreativeModeTab.TAB_TOOLS)
                    .stacksTo(1)));

    public static void register(IEventBus pEventBus) {

        ITEMS.register(pEventBus);
    }
}
