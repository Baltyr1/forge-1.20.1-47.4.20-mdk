package net.baltyr.opmmod.item;

import net.baltyr.opmmod.OpmMod;
import net.baltyr.opmmod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.awt.*;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OpmMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> OPM_TAB = CREATIVE_MODE_TABS.register("opm_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.YEN.get()))
                    .title(Component.translatable("creativetab.opm_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.YEN.get());
                        pOutput.accept(ModItems.TIGER_CLASS_ESSENCE.get());
                        pOutput.accept(ModItems.NICHIRIN_SWORD.get());

                        pOutput.accept(ModBlocks.ORICHALQUE_BLOCK.get());
                        pOutput.accept(ModBlocks.RAW_ORICHALQUE_BLOCK.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}


