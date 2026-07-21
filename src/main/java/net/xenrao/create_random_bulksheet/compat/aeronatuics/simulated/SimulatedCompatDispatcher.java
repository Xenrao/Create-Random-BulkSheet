package net.xenrao.create_random_bulksheet.compat.aeronatuics.simulated;

import net.neoforged.fml.ModList;

public class SimulatedCompatDispatcher {
    private static final boolean SABLE_LOADED = ModList.get().isLoaded("sable");

    public static boolean isLoaded() {
        return SABLE_LOADED;
    }
}
