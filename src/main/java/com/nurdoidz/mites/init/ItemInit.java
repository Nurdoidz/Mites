package com.nurdoidz.mites.init;

import com.nurdoidz.mites.Mites;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        ForgeRegistries.ITEMS,
        Mites.MODID);

    public static final RegistryObject<ForgeSpawnEggItem> MITE_SPAWN_EGG = ITEMS.register(
        "mite_spawn_egg",
        () -> new ForgeSpawnEggItem(EntityInit.MITE, 0x33658A, 0xF6AE2D, props().tab(
            CreativeModeTab.TAB_MISC).stacksTo(16)));

    private static Properties props() {
        return new Properties();
    }

}
