package net.xenrao.create_random_bulksheet.blocks.delayed_transporter;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;

public class DelayedTransporterBlock extends Block implements IBE<DelayedTransporterBlockEntity> {

    private static final VoxelShape SHAPE = Shapes.or(
            Shapes.box(0, 0, 0, 1, 1, 1)
    );

    public DelayedTransporterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
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