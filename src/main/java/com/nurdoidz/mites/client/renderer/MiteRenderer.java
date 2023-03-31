package com.nurdoidz.mites.client.renderer;

import com.nurdoidz.mites.Mites;
import com.nurdoidz.mites.client.models.MiteModel;
import com.nurdoidz.mites.entities.Mite;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MiteRenderer extends MobRenderer<Mite, MiteModel> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Mites.MODID,
        "textures/entities/mite.png");

    public MiteRenderer(
        Context context) {
        super(context, new MiteModel(context.bakeLayer(MiteModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(Mite p_114482_) {
        return TEXTURE;
    }
}
