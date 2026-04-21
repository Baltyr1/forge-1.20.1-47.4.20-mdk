package net.baltyr.opmmod.datagen;

import net.baltyr.opmmod.OpmMod;
import net.baltyr.opmmod.block.ModBlocks;
import net.baltyr.opmmod.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, OpmMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.ORICHALCUM_BLOCK.get(),
                        ModBlocks.RAW_ORICHALCUM_BLOCK.get(),
                        ModBlocks.ORICHALCUM_ORE.get(),
                        ModBlocks.DEEPSLATE_ORICHALCUM_ORE.get());

        this.tag(Tags.Blocks.NEEDS_NETHERITE_TOOL)
                .add(ModBlocks.ORICHALCUM_BLOCK.get(),
                        ModBlocks.ORICHALCUM_ORE.get(),
                        ModBlocks.DEEPSLATE_ORICHALCUM_ORE.get(),
                        ModBlocks.RAW_ORICHALCUM_BLOCK.get());
    }
}
