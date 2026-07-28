package net.xenrao.create_random_bulksheet;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlocks;
import net.xenrao.create_random_bulksheet.compat.aeronautics.blocks.RandomBulkSheetAeronauticsBlocks;
import net.xenrao.create_random_bulksheet.compat.aeronautics.items.RandomBulkSheetAeronauticsItems;
import net.xenrao.create_random_bulksheet.compat.aeronautics.AeronauticsCompatDispatcher;
import net.xenrao.create_random_bulksheet.compat.sable.SableCompatDispatcher;
import net.xenrao.create_random_bulksheet.compat.sable.blocks.RandomBulkSheetSableBlocks;
import net.xenrao.create_random_bulksheet.items.RandomBulkSheetItems;

public class RandomBulkSheetCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(
                    Registries.CREATIVE_MODE_TAB,
                    RandomBulkSheet.MODID
            );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RANDOM_BULKSHEET_TAB =
            TABS.register("random_bulksheet",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.random_bulksheet"))
                            .icon(RandomBulkSheetItems.VOID_STAR::asStack)

                            .displayItems((parameters, output) -> {
                                //Item
                                output.accept(RandomBulkSheetItems.VOID_STAR.asStack());

                                //Block
                                output.accept(RandomBulkSheetBlocks.ABYSSAL_ENERGY_TANK.asStack());
                                output.accept(RandomBulkSheetBlocks.ABYSSAL_FLUID_TANK.asStack());
                                output.accept(RandomBulkSheetBlocks.ABYSSAL_FLUID_EXTRACTOR.asStack());
                                output.accept(RandomBulkSheetBlocks.FAN_RESULT_TRANSPORTER.asStack());
                                output.accept(RandomBulkSheetBlocks.DELAYED_TRANSPORTER.asStack());
                                output.accept(RandomBulkSheetBlocks.REVERSE_REDSTONE_LINK.asStack());

                                //Compat
                                if (SableCompatDispatcher.isLoaded()) {
                                    //Block
                                    output.accept(RandomBulkSheetSableBlocks.REDSTONE_WEIGHT.asStack());
                                }
                                if (AeronauticsCompatDispatcher.isLoaded()) {
                                    //Block
                                    output.accept(RandomBulkSheetAeronauticsBlocks.BLADE_PROPELLER.asStack());

                                    //Item
                                    output.accept(RandomBulkSheetAeronauticsItems.SMALL_PROPELLER_BLADE.asStack());
                                    output.accept(RandomBulkSheetAeronauticsItems.LARGE_PROPELLER_BLADE.asStack());
                                }

                            })

                            .build()
            );
}