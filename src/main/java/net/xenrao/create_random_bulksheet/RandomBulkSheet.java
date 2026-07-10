package net.xenrao.create_random_bulksheet;

import com.tterrag.registrate.Registrate;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlocks;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.xenrao.create_random_bulksheet.blocks.delayed_transporter.DelayedTransporterBlockEntity;

@Mod(RandomBulkSheet.MODID)
public class RandomBulkSheet {
    public static final String MODID = "create_random_bulksheet";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final Registrate REGISTRATE = Registrate.create(MODID);

    public static Registrate registrate() {
        return REGISTRATE;
    }

    public RandomBulkSheet(IEventBus modEventBus, ModContainer modContainer) {
        // 1. ÖNCE Registrate event listener'larını kaydet
        //REGISTRATE.registerEventListeners(modEventBus);

        // 2. SONRA block sınıfını yükle
        RandomBulkSheetBlocks.register();
        RandomBulkSheetBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        // 3. Diğer listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onRegisterCapabilities);
        modEventBus.addListener(this::addCreative);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        DelayedTransporterBlockEntity.registerCapabilities(event);
    }
    private void commonSetup(FMLCommonSetupEvent event) {}

    private void addCreative(BuildCreativeModeTabContentsEvent event) {}

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}
}