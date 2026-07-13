package net.xenrao.create_random_bulksheet.blocks;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;
import net.xenrao.create_random_bulksheet.blocks.abyssal_energy_tank.AbyssalEnergyTankBlockEntity;
import net.xenrao.create_random_bulksheet.blocks.abyssal_fluid_tank.AbyssalFluidTankBlockEntity;
import net.xenrao.create_random_bulksheet.blocks.delayed_transporter.DelayedTransporterBlockEntity;
import net.xenrao.create_random_bulksheet.blocks.fan_result_transporter.FanResultTransporterBlockEntity;

public class RandomBulkSheetBlockEntities {

    private static final CreateRegistrate REGISTRATE = RandomBulkSheet.REGISTRATE;

    public static final BlockEntityEntry<DelayedTransporterBlockEntity> DELAYED_TRANSPORTER =
            REGISTRATE.blockEntity("delayed_transporter", DelayedTransporterBlockEntity::new)
                    .validBlocks(RandomBulkSheetBlocks.DELAYED_TRANSPORTER)
                    .register();


    public static final BlockEntityEntry<FanResultTransporterBlockEntity> FAN_RESULT_TRANSPORTER =
            REGISTRATE.blockEntity("fan_result_transporter", FanResultTransporterBlockEntity::new)
                    .validBlocks(RandomBulkSheetBlocks.FAN_RESULT_TRANSPORTER)
                    .register();


    public static final BlockEntityEntry<AbyssalFluidTankBlockEntity> ABYSSAL_FLUID_TANK =
            REGISTRATE.blockEntity("abyssal_fluid_tank", AbyssalFluidTankBlockEntity::new)
                    .validBlocks(RandomBulkSheetBlocks.ABYSSAL_FLUID_TANK)
                    .register();

    public static final BlockEntityEntry<AbyssalEnergyTankBlockEntity> ABYSSAL_ENERGY_TANK =
            REGISTRATE.blockEntity("abyssal_energy_tank", AbyssalEnergyTankBlockEntity::new)
                    .validBlocks(RandomBulkSheetBlocks.ABYSSAL_ENERGY_TANK)
                    .register();


    public static void register() {}
}
