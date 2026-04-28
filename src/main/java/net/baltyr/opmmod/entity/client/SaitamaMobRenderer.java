package net.baltyr.opmmod.entity.client;

import net.baltyr.opmmod.OpmMod;
import net.baltyr.opmmod.entity.SaitamaMob;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SaitamaMobRenderer extends GeoEntityRenderer<SaitamaMob> {

    public SaitamaMobRenderer(EntityRendererProvider.Context context) {
        super(context, new SaitamaMobModel());
    }

    @Override
    public ResourceLocation getTextureLocation(SaitamaMob entity) {
        return new ResourceLocation(OpmMod.MOD_ID, "textures/entity/saitama_mob.png");
    }
}