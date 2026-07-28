package net.xenrao.create_random_bulksheet.blocks.reverse_redstone_link;

import com.simibubi.create.content.redstone.link.RedstoneLinkBlock;

import com.simibubi.create.content.redstone.link.controller.LinkedControllerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;

public class ReverseRedstoneLinkBlock extends RedstoneLinkBlock {

    public ReverseRedstoneLinkBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReverseRedstoneLinkBlockEntity(
                RandomBulkSheetBlockEntities.REVERSE_REDSTONE_LINK.get(), pos, state
        );
    }

    @Override
    public void updateTransmittedSignal(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide) return;
        if (state.getValue(RECEIVER)) return;

        int rawPower = 0;
        for (net.minecraft.core.Direction direction : net.createmod.catnip.data.Iterate.directions)
            rawPower = Math.max(level.getSignal(pos.relative(direction), direction), rawPower);
        for (net.minecraft.core.Direction direction : net.createmod.catnip.data.Iterate.directions)
            rawPower = Math.max(level.getSignal(pos.relative(direction), net.minecraft.core.Direction.UP), rawPower);

        int reversedPower = 15 - rawPower;

        boolean previouslyPowered = state.getValue(POWERED);
        if (previouslyPowered != (reversedPower > 0))
            level.setBlock(pos, state.setValue(POWERED, reversedPower > 0), 2);

        int transmit = reversedPower;
        withBlockEntityDo(level, pos, be -> be.transmit(transmit));
    }
}