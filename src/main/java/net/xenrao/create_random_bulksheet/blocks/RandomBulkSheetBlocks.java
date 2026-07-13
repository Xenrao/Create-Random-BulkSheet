package net.xenrao.create_random_bulksheet.blocks;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;
import net.xenrao.create_random_bulksheet.blocks.abyssal_fluid_tank.AbyssalFluidTankBlock;
import net.xenrao.create_random_bulksheet.blocks.delayed_transporter.DelayedTransporterBlock;
import net.xenrao.create_random_bulksheet.blocks.fan_result_transporter.FanResultTransporterBlock;


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
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .properties(p -> p
                            .mapColor(MapColor.COLOR_GRAY)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .noOcclusion()
                            .isSuffocating((state, level, pos) -> false)
                            .isRedstoneConductor((state, level, pos) -> false)
                            .requiresCorrectToolForDrops()
                    )
                    .blockstate((ctx, prov) -> {})
                    .setData(ProviderType.LANG, (ctx, prov) -> {})
                    .item()
                    .model((ctx, prov) -> {})
                    .build()
                    .register();

    public static final BlockEntry<FanResultTransporterBlock> FAN_RESULT_TRANSPORTER =
            REGISTRATE.block("fan_result_transporter", FanResultTransporterBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .properties(p -> p
                            .mapColor(MapColor.TERRACOTTA_YELLOW)
                            .noOcclusion()
                            .isSuffocating((state, level, pos) -> false)
                            .isRedstoneConductor((state, level, pos) -> false)
                            .requiresCorrectToolForDrops()
                    )
                    .blockstate((ctx, prov) -> {})
                    .setData(ProviderType.LANG, (ctx, prov) -> {})
                    .item()
                    .model((ctx, prov) -> {})
                    .build()
                    .register();

    public static final BlockEntry<AbyssalFluidTankBlock> ABYSSAL_FLUID_TANK =
            REGISTRATE.block("abyssal_fluid_tank", AbyssalFluidTankBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .properties(p -> p
                            .mapColor(MapColor.TERRACOTTA_YELLOW)
                            .noOcclusion()
                            .isSuffocating((state, level, pos) -> false)
                            .isRedstoneConductor((state, level, pos) -> false)
                            .requiresCorrectToolForDrops()
                    )
                    .blockstate((ctx, prov) -> {})
                    .setData(ProviderType.LANG, (ctx, prov) -> {})
                    .item()
                    .properties(p -> p.rarity(Rarity.EPIC).fireResistant())
                    .model((ctx, prov) -> {})
                    .build()
                    .register();

    public static void register() {}
}