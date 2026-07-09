package net.xenrao.create_random_bulksheet.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlocks;

public class RandomBulkSheetItems {

    // 1. Moduna ait item'ları kaydetmek için DeferredRegister oluşturuyoruz
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, RandomBulkSheet.MODID);

    // 2. Bloğunun envanterde görünecek olan eşya (Item) halini buraya kaydediyoruz
    public static final DeferredHolder<Item, BlockItem> DELAYED_TRANSPORTER =
            ITEMS.register("delayed_transporter",
                    () -> new BlockItem(RandomBulkSheetBlocks.DELAYED_TRANSPORTER.get(), new Item.Properties())
            );

    // İleride create tabanlı başka düz item'lar (mesela yeni bir sac/sheet, dişli vs.)
    // eklemek istersen doğrudan altına şu şekilde ekleyebilirsin:
    // public static final DeferredHolder<Item, Item> RAW_BULK_SHEET =
    //         ITEMS.register("raw_bulksheet", () -> new Item(new Item.Properties()));
}