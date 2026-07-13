package net.xenrao.create_random_bulksheet.items;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Rarity;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;
import net.xenrao.create_random_bulksheet.items.void_star.VoidStar;

public class RandomBulkSheetItems {

    private static final Registrate REGISTRATE = RandomBulkSheet.registrate();

    public static final ItemEntry<VoidStar> VOID_STAR =
            REGISTRATE.item("void_star", VoidStar::new)
                    .properties(p -> p
                            .stacksTo(16)
                            .fireResistant()
                            .rarity(Rarity.EPIC)
                    )
                    .model((ctx, prov) -> {})
                    .setData(ProviderType.LANG, (ctx, prov) -> {})
                    .register();

    public static void register() {}
}