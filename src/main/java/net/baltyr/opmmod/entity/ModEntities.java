package net.baltyr.opmmod.entity;

import net.baltyr.opmmod.OpmMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, OpmMod.MOD_ID);

    public static final RegistryObject<EntityType<SaitamaMob>> SAITAMA_MOB =
            ENTITY_TYPES.register("saitama_mob", () ->
                    EntityType.Builder.<SaitamaMob>of(SaitamaMob::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .clientTrackingRange(10)
                            .build(new ResourceLocation(OpmMod.MOD_ID, "saitama_mob").toString())
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}