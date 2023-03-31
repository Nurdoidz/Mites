package com.nurdoidz.mites.client.models;

import com.nurdoidz.mites.Mites;
import com.nurdoidz.mites.entities.Mite;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

public class MiteModel extends SilverfishModel<Mite> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
        new ResourceLocation(
            Mites.MODID, "mite"), "main");

    public MiteModel(ModelPart root) {
        super(root);
    }
}
