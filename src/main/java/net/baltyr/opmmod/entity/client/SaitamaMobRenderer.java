package net.baltyr.opmmod.entity.client;

import net.baltyr.opmmod.OpmMod;
import net.baltyr.opmmod.entity.SaitamaMob;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SaitamaMobRenderer extends HumanoidMobRenderer<SaitamaMob, PlayerModel<SaitamaMob>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(OpmMod.MOD_ID, "textures/entity/saitama_mob.png");

    public SaitamaMobRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(SaitamaMob entity) {
        return TEXTURE;
    }
}