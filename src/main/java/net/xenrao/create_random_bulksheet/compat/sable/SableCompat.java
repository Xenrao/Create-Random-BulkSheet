package net.xenrao.create_random_bulksheet.compat.sable;

import dev.ryanhcode.sable.ActiveSableCompanion;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.joml.Vector3d;
import org.joml.Vector3dc;

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

    public static GlobalPos getParentPosition(Level level, BlockPos pos) {
        if (level == null) return null;

        ActiveSableCompanion helper = Sable.HELPER;
        SubLevel subLevel = helper.getContaining(level, pos);

        if (subLevel == null) return null;

        Level parentLevel = subLevel.getLevel();
        if (parentLevel == null) return null;

        Pose3dc pose = subLevel.logicalPose();
        Vector3dc position = pose.position(); // translation() yerine position() kullanıyoruz

        BlockPos parentPos = new BlockPos(
                (int) Math.floor(position.x()),
                (int) Math.floor(position.y()),
                (int) Math.floor(position.z())
        );

        return GlobalPos.of(parentLevel.dimension(), parentPos);
    }
}