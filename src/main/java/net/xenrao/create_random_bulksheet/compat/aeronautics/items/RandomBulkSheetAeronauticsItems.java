package net.xenrao.create_random_bulksheet.compat.aeronautics.items;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;
import net.xenrao.create_random_bulksheet.compat.aeronautics.items.propeller_blades.LargePropellerBlade;
import net.xenrao.create_random_bulksheet.compat.aeronautics.items.propeller_blades.SmallPropellerBlade;

public class RandomBulkSheetAeronauticsItems {

    private static final CreateRegistrate REGISTRATE = RandomBulkSheet.REGISTRATE;

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public static final ItemEntry<SmallPropellerBlade> SMALL_PROPELLER_BLADE =
            REGISTRATE.item("small_propeller_blade", SmallPropellerBlade::new)
                    .properties(p -> p
                    )
                    .model((ctx, prov) -> {
                    })
                    .setData(ProviderType.LANG, (ctx, prov) -> {
                    })
                    .register();

    public static final ItemEntry<LargePropellerBlade> LARGE_PROPELLER_BLADE =
            REGISTRATE.item("large_propeller_blade", LargePropellerBlade::new)
                    .properties(p -> p
                    )
                    .model((ctx, prov) -> {
                    })
                    .setData(ProviderType.LANG, (ctx, prov) -> {
                    })
                    .register();

    public static void register() {
    }
}