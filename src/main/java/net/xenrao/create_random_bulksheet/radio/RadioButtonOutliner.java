package net.xenrao.create_random_bulksheet.radio;

import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = RandomBulkSheet.MODID, value = Dist.CLIENT)
public class RadioButtonOutliner {

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;

        if (mc.hitResult instanceof BlockHitResult hitResult) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof RadioBlock) {
                Direction facing = state.getValue(RadioBlock.FACING);
                if (hitResult.getDirection() == facing) {
                    Vec3 hit = hitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
                    double u = 0;
                    double v = hit.y * 16.0;

                    if (facing == Direction.NORTH) u = hit.x * 16.0;
                    else if (facing == Direction.SOUTH) u = (1.0 - hit.x) * 16.0;
                    else if (facing == Direction.EAST) u = (1.0 - hit.z) * 16.0;
                    else if (facing == Direction.WEST) u = hit.z * 16.0;

                    Integer button = null;
                    if (v >= 3.0 && v <= 5.0) {
                        if (u >= 4.0 && u <= 6.0) button = 1;       // Önceki
                        else if (u >= 7.0 && u <= 9.0) button = 2;  // Çal/Durdur
                        else if (u >= 10.0 && u <= 12.0) button = 3;// Sonraki
                    }

                    if (button != null) {
                        double minX = 0, minZ = 0;
                        if (button == 1) { minX = 4; minZ = 4; }
                        if (button == 2) { minX = 7; minZ = 7; }
                        if (button == 3) { minX = 10; minZ = 10; }

                        // Buton 2x2 pixel (16'da 2 birim)
                        double maxX = minX + 2;
                        double maxZ = minZ + 2;

                        // Yüzeyin Z eksenini FACING'e göre ayarla (Bloğun 14. ve 16. pixelleri arasında)
                        AABB box;
                        if (facing == Direction.NORTH) {
                            box = new AABB(minX / 16.0, 3.0 / 16.0, 14.0 / 16.0, maxX / 16.0, 5.0 / 16.0, 16.0 / 16.0);
                        } else if (facing == Direction.SOUTH) {
                            box = new AABB((16.0 - maxX) / 16.0, 3.0 / 16.0, 0.0, (16.0 - minX) / 16.0, 5.0 / 16.0, 2.0 / 16.0);
                        } else if (facing == Direction.EAST) {
                            box = new AABB(0.0, 3.0 / 16.0, (16.0 - maxX) / 16.0, 2.0 / 16.0, 5.0 / 16.0, (16.0 - minX) / 16.0);
                        } else { // WEST
                            box = new AABB(14.0 / 16.0, 3.0 / 16.0, minX / 16.0, 16.0 / 16.0, 5.0 / 16.0, maxX / 16.0);
                        }

                        AABB worldBox = box.move(pos);

                        // Texture olmadan düz çizgi olarak çiz
                        Outliner.getInstance().showAABB("radio_button", worldBox)
                                .colored(0xFF_7A0000) // Koyu Kırmızı
                                .lineWidth(1 / 64f);
                    }
                }
            }
        }
    }
}