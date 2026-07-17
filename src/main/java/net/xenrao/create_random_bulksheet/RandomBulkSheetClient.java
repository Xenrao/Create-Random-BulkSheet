package net.xenrao.create_random_bulksheet;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.xenrao.create_random_bulksheet.blocks.reverse_redstone_link.ReverseRedstoneLinkRenderer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlocks;

@Mod(value = RandomBulkSheet.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = RandomBulkSheet.MODID, value = Dist.CLIENT)

public class RandomBulkSheetClient {
    public RandomBulkSheetClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(RandomBulkSheetBlocks.REVERSE_REDSTONE_LINK.get(), RenderType.cutout());
        });

    }
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                RandomBulkSheetBlockEntities.REVERSE_REDSTONE_LINK.get(),
                ReverseRedstoneLinkRenderer::new
        );
    }
}
