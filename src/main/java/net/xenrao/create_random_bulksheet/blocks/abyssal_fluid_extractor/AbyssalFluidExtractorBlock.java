package net.xenrao.create_random_bulksheet.blocks.abyssal_fluid_extractor;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;
import net.xenrao.create_random_bulksheet.items.RandomBulkSheetItems;

public class AbyssalFluidExtractorBlock extends HorizontalKineticBlock implements IWrenchable, IBE<AbyssalFluidExtractorBlockEntity>, IRotate {

    public AbyssalFluidExtractorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<AbyssalFluidExtractorBlockEntity> getBlockEntityClass() {
        return AbyssalFluidExtractorBlockEntity.class;
    }
    @Override
    public BlockEntityType<? extends AbyssalFluidExtractorBlockEntity> getBlockEntityType() {
        return RandomBulkSheetBlockEntities.ABYSSAL_FLUID_EXTRACTOR.get();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING)
                .getAxis();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction prefferedSide = getPreferredHorizontalFacing(context);
        if (prefferedSide != null)
            return defaultBlockState().setValue(HORIZONTAL_FACING, prefferedSide);
        return super.getStateForPlacement(context);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(HORIZONTAL_FACING)
                .getAxis();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(RandomBulkSheetItems.VOID_STAR.get()))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof AbyssalFluidExtractorBlockEntity be) {
                ItemStack held = player.getItemInHand(hand);
                return be.addGems(player, held);
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pIsMoving && !pState.is(pNewState.getBlock())) {
            if (!pLevel.isClientSide) {
                if (pLevel.getBlockEntity(pPos) instanceof AbyssalFluidExtractorBlockEntity be) {
                    if (be.hasVoidStar)
                        popResource(pLevel, pPos,
                                new ItemStack(RandomBulkSheetItems.VOID_STAR.get(), 1));
                }
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
}
