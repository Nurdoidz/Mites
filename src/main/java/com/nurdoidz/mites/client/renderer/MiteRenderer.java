package com.nurdoidz.mites.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nurdoidz.mites.Mites;
import com.nurdoidz.mites.client.models.MiteModel;
import com.nurdoidz.mites.client.renderer.layers.MiteTypeLayer;
import com.nurdoidz.mites.entity.Mite;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MiteRenderer extends MobRenderer<Mite, MiteModel> {

    private static final ResourceLocation MITE_LOCATION = new ResourceLocation(Mites.MODID,
        "textures/entity/mite/mite.png");

    public MiteRenderer(
        Context context) {
        super(context, new MiteModel(context.bakeLayer(MiteModel.LAYER_LOCATION)), 0.3f);
        this.addLayer(new MiteTypeLayer(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Mite mite) {
        return MITE_LOCATION;
    }

    @Override
    protected void scale(Mite mite, @NotNull PoseStack poseStack, float f) {
        if (mite.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        } else {
            poseStack.scale(1.0F, 1.0F, 1.0F);
        }
    }
}
