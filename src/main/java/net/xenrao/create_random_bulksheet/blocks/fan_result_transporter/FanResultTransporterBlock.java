package net.xenrao.create_random_bulksheet.blocks.fan_result_transporter;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.redstone.DirectedDirectionalBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.item.ItemHelper;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;

public class FanResultTransporterBlock extends DirectedDirectionalBlock implements IBE<FanResultTransporterBlockEntity>, IWrenchable, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public FanResultTransporterBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(WATERLOGGED, false));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2, 0, 2, 14, 1, 14),
            Block.box(0, 1, 0, 16, 5, 16)
    );

    private static final VoxelShaper SHAPER = VoxelShaper.forDirectional(SHAPE, Direction.DOWN);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPER.get(getTargetDirection(state));
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
                    .isShiftKeyDown() ? facing.getOpposite() : facing;
        }

        if (preferredFacing.getAxis() == Direction.Axis.Y) {
            state = state.setValue(TARGET, preferredFacing == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR);
            preferredFacing = context.getHorizontalDirection();
        }

        return state
                .setValue(FACING, preferredFacing)
                .setValue(WATERLOGGED, context.getLevel()
                        .getFluidState(context.getClickedPos())
                        .isSourceOfType(Fluids.WATER));
    }

    @Override
    public Class<FanResultTransporterBlockEntity> getBlockEntityClass() {
        return FanResultTransporterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FanResultTransporterBlockEntity> getBlockEntityType() {
        return RandomBulkSheetBlockEntities.FAN_RESULT_TRANSPORTER.get();
    }

}