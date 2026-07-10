package net.xenrao.create_random_bulksheet.blocks.delayed_transporter;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.redstone.DirectedDirectionalBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;

public class DelayedTransporterBlock extends DirectedDirectionalBlock implements IBE<DelayedTransporterBlockEntity>, IWrenchable {

    public DelayedTransporterBlock(Properties properties) {
        super(properties);
    }

    public static Direction getTargetDirection(BlockState pState) {
        switch ((AttachFace) pState.getValue(TARGET)) {
            case CEILING:
                return Direction.UP;
            case FLOOR:
                return Direction.DOWN;
            default:
                return pState.getValue(FACING);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();

        Direction preferredFacing = null;
        for (Direction face : context.getNearestLookingDirections()) {
            BlockEntity be = context.getLevel()
                    .getBlockEntity(context.getClickedPos()
                            .relative(face));
            if (be != null && (be.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), null) != null ||
                    be.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), null) != null)) {
                preferredFacing = face;
                break;
            }
        }

        if (preferredFacing == null) {
            Direction facing = context.getNearestLookingDirection();
            preferredFacing = context.getPlayer() != null && context.getPlayer()
                    .isShiftKeyDown() ? facing : facing.getOpposite();
        }

        if (preferredFacing.getAxis() == Direction.Axis.Y) {
            state = state.setValue(TARGET, preferredFacing == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR);
            preferredFacing = context.getHorizontalDirection();
        }

        return state.setValue(FACING, preferredFacing);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean pIsMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof DelayedTransporterBlockEntity delayedTransporterBE))
                return;
            ItemHelper.dropContents(world, pos, delayedTransporterBE.inventory);
            world.removeBlockEntity(pos);
        }
    }

    @Override
    public Class<DelayedTransporterBlockEntity> getBlockEntityClass() {
        return DelayedTransporterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DelayedTransporterBlockEntity> getBlockEntityType() {
        return RandomBulkSheetBlockEntities.DELAYED_TRANSPORTER.get();
    }
}