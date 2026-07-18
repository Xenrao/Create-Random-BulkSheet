package net.xenrao.create_random_bulksheet.compat.sable.blocks.redstone_weight;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class RedstoneWeightBlock extends Block implements IWrenchable {

    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final IntegerProperty MULTIPLIER = IntegerProperty.create("multiplier", 1, 5);

    public RedstoneWeightBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(POWER, 0)
                .setValue(MULTIPLIER, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER, MULTIPLIER);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide)
            return;

        int power = level.getBestNeighborSignal(pos);
        if (state.getValue(POWER) != power) {
            level.setBlock(pos, state.setValue(POWER, power), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(stack.getItem() instanceof WrenchItem))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;

        int current = state.getValue(MULTIPLIER);
        int next = current >= 5 ? 1 : current + 1;

        level.setBlock(pos, state.setValue(MULTIPLIER, next), Block.UPDATE_CLIENTS);
        level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.6f, 1.0f + (next * 0.05f));

        float scale = next * 0.25f;
        String scaleText = String.format("%.2f", scale);
        player.displayClientMessage(
                Component.literal("Weight Multiplier: " + next + " (" + scaleText + " per signal strength)"),
                true
        );

        return ItemInteractionResult.SUCCESS;
    }
}