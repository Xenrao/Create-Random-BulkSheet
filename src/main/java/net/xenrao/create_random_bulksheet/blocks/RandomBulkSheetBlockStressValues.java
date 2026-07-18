package net.xenrao.create_random_bulksheet.blocks;

import com.simibubi.create.api.stress.BlockStressValues;
import net.xenrao.create_random_bulksheet.RandomBulkSheetConfig;

public class RandomBulkSheetBlockStressValues {
    public static void register() {
        BlockStressValues.IMPACTS.register(
                RandomBulkSheetBlocks.ABYSSAL_FLUID_EXTRACTOR.get(),
                () -> RandomBulkSheetConfig.EXTRACTOR_STRESS_IMPACT.get()
        );
    }
}
