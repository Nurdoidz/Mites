package com.nurdoidz.mites.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nurdoidz.mites.Mites;
import com.nurdoidz.mites.client.models.MiteModel;
import com.nurdoidz.mites.entity.Mite;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class MiteTailLayer extends RenderLayer<Mite, MiteModel> {

    private static final ResourceLocation MITE_TAIL_LOCATION = new ResourceLocation(Mites.MODID,
        "textures/entity/mite/mite_tail.png");

    public MiteTailLayer(RenderLayerParent<Mite, MiteModel> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, Mite pLivingEntity,
        float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw,
        float pHeadPitch) {
        if (!pLivingEntity.isInvisible()) {
            Mite.Enthrall miteEnthrall = pLivingEntity.getEnthrall();
            if (miteEnthrall != Mite.Enthrall.NONE) {
                float[] color = pLivingEntity.getEnthrall().getColor();
                renderColoredCutoutModel(this.getParentModel(), MITE_TAIL_LOCATION, pPoseStack, pBuffer, pPackedLight,
                    pLivingEntity, color[0], color[1], color[2]);
            }
        }
    }
}
