package net.xenrao.create_random_bulksheet.compat.aeronautics;

import net.neoforged.fml.ModList;

public class AeronauticsCompatDispatcher {
    private static final boolean AERONAUTICS_LOADED = ModList.get().isLoaded("aeronautics");

    public static boolean isLoaded() {
        return AERONAUTICS_LOADED;
    }
}
