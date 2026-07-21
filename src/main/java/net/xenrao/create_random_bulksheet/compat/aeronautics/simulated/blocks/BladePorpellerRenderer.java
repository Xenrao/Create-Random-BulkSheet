package net.xenrao.create_random_bulksheet.compat.aeronautics.simulated.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.xenrao.create_random_bulksheet.compat.aeronautics.RandomBulkSheetAeronauticsItems;
import net.xenrao.create_random_bulksheet.index.RandomBulkSheetPartialModels;
import org.joml.Quaternionf;

public class BladePorpellerRenderer<T extends BladePropellerBlockEntity> extends KineticBlockEntityRenderer<T> {
    public BladePorpellerRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void renderSafe(final T be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {

        BlockState state = getRenderedBlockState(be);
        RenderType type = getRenderType(be, state);
        renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);

        final Direction dir = state.getValue(BlockStateProperties.FACING);

        final VertexConsumer vb = buffer.getBuffer(RenderType.solid());

        int bladeAngle = be.bladeAngle;
        int bladeCount = be.getBladeCount();

        for (int i = 0; i < bladeCount; i++) {
            float positionOffset = (360f / bladeCount) * i * Mth.DEG_TO_RAD;

            boolean isLarge = be.inventory.getStackInSlot(i).is(RandomBulkSheetAeronauticsItems.LARGE_PROPELLER_BLADE);

            final SuperByteBuffer propeller = CachedBuffers.partialFacing(this.getCurrentModel(isLarge), state);

            final float angle = this.getAngle(partialTicks, dir, be);
            kineticRotationTransform(propeller, be, dir.getAxis(), angle + positionOffset, light);

            if (dir.getAxis().isHorizontal()) {
                propeller.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(dir.getOpposite())), Direction.UP);
            }
            if (dir.getAxis().isVertical()) {
                propeller.rotateCentered(AngleHelper.rad(AngleHelper.verticalAngle(dir.getOpposite())), Direction.EAST);
            }

            propeller.translate(0, 0, 0).rotateCentered(AngleHelper.rad(-90 - AngleHelper.verticalAngle(dir)), Direction.EAST);

            if (dir == Direction.NORTH || dir == Direction.SOUTH) {
                if (isLarge)
                    propeller.translate(0, 0, dir == Direction.SOUTH ? -0.5: 0.5);
                propeller.rotateAround(
                        new Quaternionf().rotateZ(Mth.DEG_TO_RAD * bladeAngle),
                        8 / 16f, 7 / 16f, 8 / 16f
                );
            }
            else if (dir == Direction.WEST || dir == Direction.EAST) {
                if (isLarge)
                    propeller.translate(dir == Direction.EAST ? -0.5: 0.5, 0, 0);
                propeller.rotateAround(
                        new Quaternionf().rotateX(Mth.DEG_TO_RAD * bladeAngle),
                        8 / 16f, 7 / 16f, 8 / 16f
                );
            }
            else if (dir == Direction.UP) {
                if (isLarge)
                    propeller.translate(0, -0.5,0);
                propeller.rotateAround(
                        new Quaternionf().rotateY(Mth.DEG_TO_RAD * bladeAngle),
                        8 / 16f, 8 / 16f, 9 / 16f
                );

            }
            else if (dir == Direction.DOWN) {
                if (isLarge)
                    propeller.translate(0, 0.5,0);
                propeller.rotateAround(
                        new Quaternionf().rotateY(Mth.DEG_TO_RAD * bladeAngle),
                        8 / 16f, 8 / 16f, 7 / 16f
                );
            }

            propeller.renderInto(ms, vb);
        }
    }

    public PartialModel getCurrentModel(boolean isLarge) {
        return isLarge ? RandomBulkSheetPartialModels.LARGE_BLADE : RandomBulkSheetPartialModels.SMALL_BLADE;
    }

    public float getAngle(final float partialTicks, final Direction dir, final T be) {
        float angle = be.getPreviousAngle() * (1f - partialTicks) + be.getAngle() * partialTicks;

        angle = angle / 180f * (float) Math.PI;

        return angle;
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final T be, final BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, state
                .getValue(BearingBlock.FACING)
                .getOpposite());
    }
}
