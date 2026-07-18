package net.xenrao.create_random_bulksheet.compat.sable.blocks;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.MapColor;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;
import net.xenrao.create_random_bulksheet.compat.sable.blocks.redstone_weight.RedstoneWeightBlock;

public class RandomBulkSheetSableBlocks {
    private static final CreateRegistrate REGISTRATE = RandomBulkSheet.REGISTRATE;

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public static final BlockEntry<RedstoneWeightBlock> REDSTONE_WEIGHT =
            REGISTRATE.block("redstone_weight", RedstoneWeightBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .properties(p -> p
                            .mapColor(MapColor.COLOR_LIGHT_GRAY)
                            .requiresCorrectToolForDrops()
                    )
                    .blockstate((ctx, prov) -> {
                    })
                    .setData(ProviderType.LANG, (ctx, prov) -> {
                    })
                    .item()
                    .model((ctx, prov) -> {
                    })
                    .build()
                    .register();

    public static void register() {
    }
}
