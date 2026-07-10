package net.xenrao.create_random_bulksheet.blocks;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;
import net.xenrao.create_random_bulksheet.blocks.delayed_transporter.DelayedTransporterBlock;


public class RandomBulkSheetBlocks {

    private static final Registrate REGISTRATE = RandomBulkSheet.registrate();
    /*
    static {
        REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB);
    }
     */
    public static final BlockEntry<DelayedTransporterBlock> DELAYED_TRANSPORTER =
            REGISTRATE.block("delayed_transporter", DelayedTransporterBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)   // <- BUNU üretmeye devam etsin, istediğin bu
                    .properties(p -> p
                            .mapColor(MapColor.COLOR_GRAY)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .noOcclusion()
                            .isSuffocating((state, level, pos) -> false)
                            .isRedstoneConductor((state, level, pos) -> false)
                            .requiresCorrectToolForDrops()
                    )
                    .blockstate((ctx, prov) -> {})   // <- blockstate/model datagen'i SUSTUR, elle yönetiyorsun
                    .setData(ProviderType.LANG, (ctx, prov) -> {})
                    .item()
                    .model((ctx, prov) -> {})
                    .build()
                    .register();

    public static void register() {}
}