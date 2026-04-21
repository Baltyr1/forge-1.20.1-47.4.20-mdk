package net.baltyr.opmmod.item;

import net.baltyr.opmmod.OpmMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OpmMod.MOD_ID);

    public static final RegistryObject<Item> YEN = ITEMS.register("yen",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TIGER_CLASS_ESSENCE = ITEMS.register("tiger_class_essence",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ORICHALCUM = ITEMS.register("orichalcum",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_ORICHALCUM = ITEMS.register("raw_orichalcum",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> NICHIRIN_SWORD = ITEMS.register("nichirin_sword",
            () -> new SwordItem(Tiers.NETHERITE, 4, -2.4f, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
