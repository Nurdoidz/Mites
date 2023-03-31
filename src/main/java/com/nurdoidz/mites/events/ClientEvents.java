package com.nurdoidz.mites.events;

import com.nurdoidz.mites.Mites;
import com.nurdoidz.mites.client.models.MiteModel;
import com.nurdoidz.mites.client.renderer.MiteRenderer;
import com.nurdoidz.mites.init.EntityInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Mites.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void entityRenderers(RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.MITE.get(), MiteRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MiteModel.LAYER_LOCATION, MiteModel::createBodyLayer);
    }
}
