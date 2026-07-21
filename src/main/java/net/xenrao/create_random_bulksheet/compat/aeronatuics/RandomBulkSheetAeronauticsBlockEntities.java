package net.xenrao.create_random_bulksheet.compat.aeronatuics;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;
import net.xenrao.create_random_bulksheet.compat.aeronatuics.simulated.blocks.BladePorpellerRenderer;
import net.xenrao.create_random_bulksheet.compat.aeronatuics.simulated.blocks.BladePropellerBlockEntity;

public class RandomBulkSheetAeronauticsBlockEntities {
    private static final CreateRegistrate REGISTRATE = RandomBulkSheet.REGISTRATE;

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public static final BlockEntityEntry<BladePropellerBlockEntity> BLADE_PROPELLER =
            REGISTRATE.blockEntity("blade_propeller", BladePropellerBlockEntity::new)
                    //.visual(() -> ShaftVisual::new, false)
                    .renderer(() -> BladePorpellerRenderer::new)
                    .validBlocks(RandomBulkSheetAeronauticsBlocks.BLADE_PROPELLER)
                    .register();


    public static void register() {
    }
}
