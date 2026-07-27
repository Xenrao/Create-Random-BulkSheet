package net.xenrao.create_random_bulksheet.blocks;

import com.simibubi.create.api.stress.BlockStressValues;
import net.xenrao.create_random_bulksheet.RandomBulkSheetConfig;
import net.xenrao.create_random_bulksheet.compat.aeronautics.AeronauticsCompatDispatcher;
import net.xenrao.create_random_bulksheet.compat.aeronautics.blocks.RandomBulkSheetAeronauticsBlocks;
import net.xenrao.create_random_bulksheet.compat.sable.SableCompatDispatcher;

public class RandomBulkSheetBlockStressValues {
    public static void register() {
        BlockStressValues.IMPACTS.register(
                RandomBulkSheetBlocks.ABYSSAL_FLUID_EXTRACTOR.get(),
                () -> RandomBulkSheetConfig.ABYSSAL_FLUID_EXTRACTOR_STRESS_IMPACT.get()
        );
        if (AeronauticsCompatDispatcher.isLoaded())
            BlockStressValues.IMPACTS.register(
                    RandomBulkSheetAeronauticsBlocks.BLADE_PROPELLER.get(),
                    () -> RandomBulkSheetConfig.BLADE_PROPELLER_STRESS_IMPACT.get()
            );
    }
}
