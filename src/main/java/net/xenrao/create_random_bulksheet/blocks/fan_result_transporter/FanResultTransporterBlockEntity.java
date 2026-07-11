package net.xenrao.create_random_bulksheet.blocks.fan_result_transporter;



import com.simibubi.create.content.kinetics.fan.processing.FanProcessing;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.xenrao.create_random_bulksheet.compat.sable.SableCompatDispatcher;

import java.util.EnumMap;
import java.util.List;

public class FanResultTransporterBlockEntity extends SmartBlockEntity {

    private final EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> capCaches = new EnumMap<>(Direction.class);

    public FanResultTransporterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private Direction outputDir() {
        return FanResultTransporterBlock.getTargetDirection(getBlockState());
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    private IItemHandler grabCapability(Direction side) {
        BlockPos checkPos = worldPosition.relative(side);
        Direction opposite = side.getOpposite();

        IItemHandler cached;
        if (level instanceof ServerLevel serverLevel) {
            cached = capCaches.computeIfAbsent(side, s ->
                    BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, serverLevel, checkPos, opposite)
            ).getCapability();
        } else {
            cached = level.getCapability(Capabilities.ItemHandler.BLOCK, checkPos, opposite);
        }

        return SableCompatDispatcher.grabCapabilityWithFallback(level, cached, checkPos, opposite);
    }

    @Override
    public void invalidate() {
        capCaches.clear();
        super.invalidate();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide) return;
        scanAndCollect();
    }

    private boolean isProcessingFinished(ItemEntity itemEntity) {
        return itemEntity.getPersistentData()
                .getBoolean("FanProcessingFinished");
    }

    private void scanAndCollect() {
        IItemHandler outputHandler = grabCapability(outputDir());

        if (outputHandler == null)
            return;

        AABB scanArea = new AABB(worldPosition);

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, scanArea);

        if (items.isEmpty())
            return;

        for (ItemEntity itemEntity : items) {
            if (!isProcessingFinished(itemEntity))
                continue;

            ItemStack stack = itemEntity.getItem();

            ItemStack remainder = ItemHandlerHelper.insertItem(
                    outputHandler,
                    stack,
                    false
            );

            if (remainder.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(remainder);
            }

            //break;
        }
    }
}