package net.xenrao.create_random_bulksheet.radio;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.material.MapColor;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;

public class RadioRegister {
    private static final CreateRegistrate REGISTRATE = RandomBulkSheet.REGISTRATE;

    public static final BlockEntry<RadioBlock> RADIO =
            REGISTRATE.block("radio", RadioBlock::new)
                    .initialProperties(SharedProperties::wooden)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_AXE)
                    .properties(p -> p
                            .mapColor(MapColor.TERRACOTTA_BROWN)
                            .forceSolidOn()
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

    public static final BlockEntityEntry<RadioBlockEntity> RADIO_BE =
            REGISTRATE.blockEntity("radio", RadioBlockEntity::new)
                    .validBlocks(RadioRegister.RADIO)
                    .register();


    public static void register() {

    }
}
