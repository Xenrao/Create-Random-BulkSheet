package net.xenrao.create_random_bulksheet;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockStressValues;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlocks;
import net.xenrao.create_random_bulksheet.blocks.abyssal_energy_tank.AbyssalEnergyTankBlockEntity;
import net.xenrao.create_random_bulksheet.blocks.abyssal_fluid_extractor.AbyssalFluidExtractorBlockEntity;
import net.xenrao.create_random_bulksheet.blocks.abyssal_fluid_tank.AbyssalFluidTankBlockEntity;
import net.xenrao.create_random_bulksheet.blocks.delayed_transporter.DelayedTransporterBlockEntity;
import net.xenrao.create_random_bulksheet.items.RandomBulkSheetItems;
import net.xenrao.create_random_bulksheet.recipe.RandomBulkSheetRecipes;
import org.slf4j.Logger;

@Mod(RandomBulkSheet.MODID)
public class RandomBulkSheet {
    public static final String MODID = "create_random_bulksheet";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(RandomBulkSheet.MODID)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    public RandomBulkSheet(IEventBus modEventBus, ModContainer modContainer) {
        REGISTRATE.registerEventListeners(modEventBus);

        RandomBulkSheetItems.register();
        RandomBulkSheetBlocks.register();
        RandomBulkSheetBlockEntities.register();

        RandomBulkSheetCreativeTabs.TABS.register(modEventBus);

        RandomBulkSheetRecipes.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onRegisterCapabilities);


        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, RandomBulkSheetConfig.SPEC);
    }

    @SuppressWarnings("UnstableApiUsage")
    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        DelayedTransporterBlockEntity.registerCapabilities(event);
        AbyssalFluidTankBlockEntity.registerCapabilities(event);
        AbyssalFluidExtractorBlockEntity.registerCapabilities(event);
        AbyssalEnergyTankBlockEntity.registerCapabilities(event);
        RandomBulkSheetBlockStressValues.register();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

}