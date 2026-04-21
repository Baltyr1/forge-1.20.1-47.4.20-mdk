package net.baltyr.opmmod.datagen;

import net.baltyr.opmmod.OpmMod;
import net.baltyr.opmmod.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, OpmMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.ORICHALCUM_BLOCK);
        blockWithItem(ModBlocks.RAW_ORICHALCUM_BLOCK);
        blockWithItem(ModBlocks.ORICHALCUM_ORE);
        blockWithItem(ModBlocks.DEEPSLATE_ORICHALCUM_ORE);
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }
}
