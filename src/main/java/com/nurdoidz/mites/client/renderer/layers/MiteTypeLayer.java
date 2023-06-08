package com.nurdoidz.mites.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nurdoidz.mites.Mites;
import com.nurdoidz.mites.client.models.MiteModel;
import com.nurdoidz.mites.entity.Mite;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MiteTypeLayer extends RenderLayer<Mite, MiteModel> {

    private static final String MITE_LOCATION = "textures/entity/mite/type/";

    public MiteTypeLayer(RenderLayerParent<Mite, MiteModel> pRenderer) {

        super(pRenderer);
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight,
            Mite pLivingEntity,
            float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw,
            float pHeadPitch) {

        if (!pLivingEntity.isInvisible()) {
            Mite.Enthrall miteEnthrall = pLivingEntity.getEnthrall();
            renderColoredCutoutModel(this.getParentModel(),
                    new ResourceLocation(Mites.MODID, MITE_LOCATION + miteEnthrall.getName() + ".png"), pPoseStack,
                    pBuffer, pPackedLight, pLivingEntity, 1.0F, 1.0F, 1.0F);
        }
    }
}
