package net.xenrao.create_random_bulksheet.blocks.reverse_redstone_link;

import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlockEntity;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ReverseRedstoneLinkBlockEntity extends RedstoneLinkBlockEntity {

    public ReverseRedstoneLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Override
    public void setSignal(int power) {
        // ✅ RECEIVER: 15 - sinyal
        int reversedPower = 15 - power;
        super.setSignal(reversedPower);

        // Hemen güncelle
        if (level != null && !level.isClientSide) {
            BlockState blockState = getBlockState();

            // ✅ POWERED STATE: reversedPower > 0 ise true
            if ((getReceivedSignal() > 0) != blockState.getValue(RedstoneLinkBlock.POWERED)) {
                level.setBlockAndUpdate(worldPosition, blockState.cycle(RedstoneLinkBlock.POWERED));
            }

            Direction attachedFace = blockState.getValue(RedstoneLinkBlock.FACING).getOpposite();
            BlockPos attachedPos = worldPosition.relative(attachedFace);
            level.blockUpdated(worldPosition, level.getBlockState(worldPosition).getBlock());
            level.blockUpdated(attachedPos, level.getBlockState(attachedPos).getBlock());
        }
    }

    // transmit() override YOK - Block class hallediyor
}