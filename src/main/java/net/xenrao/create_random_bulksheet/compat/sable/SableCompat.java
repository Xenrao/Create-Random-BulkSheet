package net.xenrao.create_random_bulksheet.compat.sable;

import dev.ryanhcode.sable.ActiveSableCompanion;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.joml.Vector3d;

// DİKKAT: Bu sınıfa sadece SableCompatDispatcher.isLoaded() true iken eriş.
// Bu sınıf ayrı tutulduğu için Sable yüklenmemişse JVM bunu hiç resolve etmeye çalışmaz.
public class SableCompat {

    public static IItemHandler grabCapability(Level level, BlockPos checkPos, Direction opposite) {
        ActiveSableCompanion helper = Sable.HELPER;
        Vector3d dir = new Vector3d(opposite.getStepX(), opposite.getStepY(), opposite.getStepZ());

        SubLevel parentSublevel = helper.getContaining(level, checkPos);
        if (parentSublevel != null) {
            parentSublevel.logicalPose().transformNormalInverse(dir);
        }

        Vector3d includeSublevelDir = new Vector3d(dir);
        return helper.runIncludingSubLevels(
                level,
                checkPos.getCenter(),
                false,
                parentSublevel,
                (sublevel, pos) -> {
                    includeSublevelDir.set(dir);
                    if (sublevel != null) {
                        sublevel.logicalPose().transformNormal(includeSublevelDir);
                    }
                    return level.getCapability(
                            Capabilities.ItemHandler.BLOCK, pos,
                            Direction.getNearest(includeSublevelDir.x, includeSublevelDir.y, includeSublevelDir.z)
                    );
                }
        );
    }
}