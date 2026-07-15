package net.xenrao.create_random_bulksheet.blocks.abyssal_fluid_tank;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;
import net.xenrao.create_random_bulksheet.items.RandomBulkSheetItems;

public class AbyssalFluidTankBlock extends Block implements IWrenchable, IBE<AbyssalFluidTankBlockEntity> {

    public AbyssalFluidTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<AbyssalFluidTankBlockEntity> getBlockEntityClass() {
        return AbyssalFluidTankBlockEntity.class;
    }
    @Override
    public BlockEntityType<? extends AbyssalFluidTankBlockEntity> getBlockEntityType() {
        return RandomBulkSheetBlockEntities.ABYSSAL_FLUID_TANK.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty() || !(stack.is(Items.NETHER_STAR) || stack.is(Items.NETHERITE_INGOT) || stack.is(Items.DIAMOND) || stack.is(RandomBulkSheetItems.VOID_STAR.get())))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        //if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof AbyssalFluidTankBlockEntity tank) {
                ItemStack held = player.getItemInHand(hand);
                return tank.addGems(player, held);
            }
        //}

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);

                if (be instanceof AbyssalFluidTankBlockEntity tank) {

                    if (tank.star_count > 0)
                        popResource(level, pos,
                                new ItemStack(Items.NETHER_STAR, tank.star_count));

                    if (tank.netherite_count > 0)
                        popResource(level, pos,
                                new ItemStack(Items.NETHERITE_INGOT, tank.netherite_count));

                    if (tank.diamond_count > 0)
                        popResource(level, pos,
                                new ItemStack(Items.DIAMOND, tank.diamond_count));

                    if (tank.infinite)
                        popResource(level, pos,
                                new ItemStack(RandomBulkSheetItems.VOID_STAR.get(), 1));
                }
            }

            level.removeBlockEntity(pos);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
