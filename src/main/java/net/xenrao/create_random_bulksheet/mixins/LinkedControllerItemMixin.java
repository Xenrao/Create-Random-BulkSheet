package net.xenrao.create_random_bulksheet.mixins;

import com.simibubi.create.content.redstone.link.controller.LinkedControllerClientHandler;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerItem;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.xenrao.create_random_bulksheet.blocks.reverse_redstone_link.ReverseRedstoneLinkBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LinkedControllerItem.class)
public class LinkedControllerItemMixin {

    @Inject(method = "onItemUseFirst", at = @At("HEAD"), cancellable = true)
    private void onReverseLinkUse(ItemStack stack, UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir) {
        Player player = ctx.getPlayer();
        if (player == null) return;
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState hitState = world.getBlockState(pos);

        if (hitState.getBlock() instanceof ReverseRedstoneLinkBlock && player.mayBuild() && !player.isShiftKeyDown()) {
            if (world.isClientSide) {
                CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> LinkedControllerClientHandler.toggleBindMode(pos));
            }
            player.getCooldowns().addCooldown((LinkedControllerItem) (Object) this, 2);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}