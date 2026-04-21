package net.baltyr.opmmod.datagen;

import net.baltyr.opmmod.OpmMod;
import net.baltyr.opmmod.block.ModBlocks;
import net.baltyr.opmmod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OpmMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.ORICHALCUM);
        simpleItem(ModItems.RAW_ORICHALCUM);

        simpleItem(ModItems.YEN);
        simpleItem(ModItems.TIGER_CLASS_ESSENCE);
        simpleItem(ModItems.NICHIRIN_SWORD);

    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(OpmMod.MOD_ID, "item/" + item.getId().getPath()));
    }
}

