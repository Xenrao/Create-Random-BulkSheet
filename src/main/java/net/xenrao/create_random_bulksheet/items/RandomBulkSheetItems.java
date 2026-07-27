package net.xenrao.create_random_bulksheet.items;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;
import net.xenrao.create_random_bulksheet.compat.aeronautics.items.RandomBulkSheetAeronauticsItems;
import net.xenrao.create_random_bulksheet.compat.aeronautics.AeronauticsCompatDispatcher;
import net.xenrao.create_random_bulksheet.items.void_star.VoidStar;

public class RandomBulkSheetItems {

    private static final CreateRegistrate REGISTRATE = RandomBulkSheet.REGISTRATE;

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public static final ItemEntry<VoidStar> VOID_STAR =
            REGISTRATE.item("void_star", VoidStar::new)
                    .properties(p -> p
                            .stacksTo(16)
                            .fireResistant()
                            .rarity(Rarity.EPIC)
                    )
                    .model((ctx, prov) -> {
                    })
                    .setData(ProviderType.LANG, (ctx, prov) -> {
                    })
                    .register();

    public static void register() {
        if (AeronauticsCompatDispatcher.isLoaded())
            RandomBulkSheetAeronauticsItems.register();
    }
}