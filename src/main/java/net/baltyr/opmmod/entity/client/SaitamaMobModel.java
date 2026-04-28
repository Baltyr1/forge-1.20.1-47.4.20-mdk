package net.baltyr.opmmod.entity.client;

import net.baltyr.opmmod.OpmMod;
import net.baltyr.opmmod.entity.SaitamaMob;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SaitamaMobModel extends GeoModel<SaitamaMob> {

    @Override
    public ResourceLocation getModelResource(SaitamaMob entity) {
        return new ResourceLocation(OpmMod.MOD_ID, "geo/saitama_mob.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SaitamaMob entity) {
        return new ResourceLocation(OpmMod.MOD_ID, "textures/entity/saitama_mob.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SaitamaMob entity) {
        return new ResourceLocation(OpmMod.MOD_ID, "animations/saitama_mob.animation.json");
    }
}