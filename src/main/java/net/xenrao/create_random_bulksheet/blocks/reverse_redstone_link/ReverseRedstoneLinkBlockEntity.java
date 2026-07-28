package net.xenrao.create_random_bulksheet.blocks.reverse_redstone_link;

import com.simibubi.create.content.redstone.link.RedstoneLinkBlock;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ReverseRedstoneLinkBlockEntity extends RedstoneLinkBlockEntity {

    public ReverseRedstoneLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Override
    public void setSignal(int power) {
        int reversedPower = 15 - power;
        super.setSignal(reversedPower);

        if (level != null && !level.isClientSide) {
            BlockState blockState = getBlockState();

            if ((getReceivedSignal() > 0) != blockState.getValue(RedstoneLinkBlock.POWERED)) {
                level.setBlockAndUpdate(worldPosition, blockState.cycle(RedstoneLinkBlock.POWERED));
            }

            Direction attachedFace = blockState.getValue(RedstoneLinkBlock.FACING).getOpposite();
            BlockPos attachedPos = worldPosition.relative(attachedFace);
            level.blockUpdated(worldPosition, level.getBlockState(worldPosition).getBlock());
            level.blockUpdated(attachedPos, level.getBlockState(attachedPos).getBlock());
        }
    }

}