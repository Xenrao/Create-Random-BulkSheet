package net.xenrao.create_random_bulksheet.compat.aeronautics.simulated.blocks;

import com.simibubi.create.foundation.item.ItemHelper;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlockEntity;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xenrao.create_random_bulksheet.compat.aeronautics.RandomBulkSheetAeronauticsBlockEntities;
import net.xenrao.create_random_bulksheet.compat.aeronautics.RandomBulkSheetAeronauticsItems;


public class BladePropellerBlock extends BasePropellerBlock {


    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0.001, 0, 16, 12, 16),
            Block.box(2, 12, 2, 14, 13, 14),
            Block.box(4, 13, 4, 12, 14, 12)
    );

    private static final VoxelShaper SHAPER = VoxelShaper.forDirectional(SHAPE, Direction.UP);

    public BladePropellerBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPER.get(state.getValue(FACING));
    }

    @Override
    public BlockEntityType<? extends BasePropellerBlockEntity> getBlockEntityType() {
        return RandomBulkSheetAeronauticsBlockEntities.BLADE_PROPELLER.get();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BladePropellerBlockEntity pbe) {
                if (pbe.getBladeCount() > 0) {
                    pbe.removeBlade(player);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(stack.is(RandomBulkSheetAeronauticsItems.SMALL_PROPELLER_BLADE) || stack.is(RandomBulkSheetAeronauticsItems.LARGE_PROPELLER_BLADE)))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof BladePropellerBlockEntity pbe) {
                if (pbe.getBladeCount() == 8)
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                pbe.addBlade(stack,player);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.hasBlockEntity() && (pState.getBlock() != pNewState.getBlock() || !pNewState.hasBlockEntity())) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (!(be instanceof BladePropellerBlockEntity bpbe))
                return;
            ItemHelper.dropContents(pLevel, pPos, bpbe.inventory);
            pLevel.removeBlockEntity(pPos);
        }
    }
}