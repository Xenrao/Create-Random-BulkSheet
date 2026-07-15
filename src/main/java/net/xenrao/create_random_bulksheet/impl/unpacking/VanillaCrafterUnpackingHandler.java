package net.xenrao.create_random_bulksheet.impl.unpacking;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.packager.unpacking.UnpackingHandler;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.impl.unpacking.DefaultUnpackingHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;

public enum VanillaCrafterUnpackingHandler implements UnpackingHandler {
    INSTANCE;

    @Override
    public boolean unpack(Level level, BlockPos pos, BlockState state, Direction side, List<ItemStack> items,
                          @Nullable PackageOrderWithCrafts orderContext, boolean simulate) {

        if (!PackageOrderWithCrafts.hasCraftingInformation(orderContext))
            return DefaultUnpackingHandler.INSTANCE.unpack(level, pos, state, side, items, orderContext, simulate);

        List<BigItemStack> craftingContext = orderContext.getCraftingInformation();

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CrafterBlockEntity crafter))
            return false;

        int max = Math.min(9, craftingContext.size());

        outer:
        for (int slot = 0; slot < max; slot++) {
            BigItemStack target = craftingContext.get(slot);
            if (target == null || target.stack.isEmpty())
                continue;

            // slot zaten dolu mu kontrolü
            if (!crafter.getItem(slot).isEmpty())
                continue;

            for (ItemStack stack : items) {
                if (stack.isEmpty())
                    continue;
                if (!ItemStack.isSameItemSameComponents(stack, target.stack))
                    continue;

                if (!simulate) {
                    crafter.setItem(slot, stack.copyWithCount(1));
                }
                stack.shrink(1);
                continue outer;
            }
        }

        for (ItemStack item : items) {
            if (!item.isEmpty())
                return false;
        }
        return true;
    }
}