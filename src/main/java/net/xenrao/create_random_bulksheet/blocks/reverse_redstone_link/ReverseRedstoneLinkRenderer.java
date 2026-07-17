package net.xenrao.create_random_bulksheet.blocks.reverse_redstone_link;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.redstone.link.LinkRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class ReverseRedstoneLinkRenderer implements BlockEntityRenderer<ReverseRedstoneLinkBlockEntity> {

    public ReverseRedstoneLinkRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ReverseRedstoneLinkBlockEntity be, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        LinkRenderer.renderOnBlockEntity(be, partialTicks, poseStack, buffer, light, overlay);
    }
}