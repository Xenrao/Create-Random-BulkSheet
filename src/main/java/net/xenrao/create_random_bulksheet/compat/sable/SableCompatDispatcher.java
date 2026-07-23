package net.xenrao.create_random_bulksheet.compat.sable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandler;

public class SableCompatDispatcher {

    private static final boolean SABLE_LOADED = ModList.get().isLoaded("sable");

    public static boolean isLoaded() {
        return SABLE_LOADED;
    }

    // Normal capability aramasını dener, bulamazsa (ve Sable yüklüyse) sub-level üzerinden dener
    public static IItemHandler grabCapabilityWithFallback(Level level, IItemHandler normalResult,
                                                          BlockPos checkPos, Direction opposite) {
        if (normalResult != null) return normalResult;
        if (!SABLE_LOADED) return null;
        return SableCompat.grabCapability(level, checkPos, opposite);
    }

    /**
     * Eğer bu level bir Sable sub-level ise, ana dünyadaki (parent) Sable bloğunun GlobalPos'unu döndürür.
     * Değilse null döner.
     */
    public static GlobalPos getParentPosition(Level level, BlockPos pos) {
        if (!SABLE_LOADED) return null;
        return SableCompat.getParentPosition(level, pos);
    }
}