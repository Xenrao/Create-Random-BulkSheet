package net.xenrao.create_random_bulksheet.index;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;

public class RandomBulkSheetPartialModels {
    public static final PartialModel
            SMALL_BLADE = block("blade_propeller/small_propeller_blade"),
            LARGE_BLADE = block("blade_propeller/large_propeller_blade");

    private static PartialModel block(final String path) {
        return PartialModel.of(RandomBulkSheet.path("block/" + path));
    }

    public static void init() {
    }
}
