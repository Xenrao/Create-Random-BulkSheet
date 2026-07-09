package net.xenrao.create_random_bulksheet;

import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlocks;
import net.xenrao.create_random_bulksheet.items.RandomBulkSheetItems;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(RandomBulkSheet.MODID)
public class RandomBulkSheet {
    public static final String MODID = "create_random_bulksheet";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RandomBulkSheet(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        RandomBulkSheetBlocks.BLOCKS.register(modEventBus);
        RandomBulkSheetBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        RandomBulkSheetItems.ITEMS.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
