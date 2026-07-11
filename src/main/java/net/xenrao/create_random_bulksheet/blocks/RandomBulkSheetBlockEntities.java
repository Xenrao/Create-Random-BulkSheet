package net.xenrao.create_random_bulksheet.blocks;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.xenrao.create_random_bulksheet.blocks.delayed_transporter.DelayedTransporterBlockEntity;
import net.xenrao.create_random_bulksheet.blocks.fan_result_transporter.FanResultTransporterBlockEntity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;



public class RandomBulkSheetBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "create_random_bulksheet");

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DelayedTransporterBlockEntity>> DELAYED_TRANSPORTER =
            BLOCK_ENTITIES.register("delayed_transporter", () ->
                    BlockEntityType.Builder.of(
                            // Lambda'yı burada güvenli hale getiriyoruz
                            (pos, state) -> new DelayedTransporterBlockEntity(
                                    RandomBulkSheetBlockEntities.DELAYED_TRANSPORTER.get(),
                                    pos,
                                    state
                            ),
                            RandomBulkSheetBlocks.DELAYED_TRANSPORTER.get()
                    ).build(null) // DataFixer tipi (genelde null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FanResultTransporterBlockEntity>> FAN_RESULT_TRANSPORTER =
            BLOCK_ENTITIES.register("fan_result_transporter", () ->
                    BlockEntityType.Builder.of(
                            (pos, state) -> new FanResultTransporterBlockEntity(
                                    RandomBulkSheetBlockEntities.FAN_RESULT_TRANSPORTER.get(),
                                    pos,
                                    state
                            ),
                            RandomBulkSheetBlocks.FAN_RESULT_TRANSPORTER.get()
                    ).build(null) // DataFixer tipi (genelde null)
            );
}
