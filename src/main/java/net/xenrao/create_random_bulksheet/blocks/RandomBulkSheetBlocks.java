package net.xenrao.create_random_bulksheet.blocks;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.xenrao.create_random_bulksheet.blocks.delayed_transporter.*;

public class RandomBulkSheetBlocks {
    // DeferredRegister.Blocks yerine standart DeferredRegister<Block> kullanıyoruz
    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK, "create_random_bulksheet");

    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.block.Block, DelayedTransporterBlock> DELAYED_TRANSPORTER =
            BLOCKS.register("delayed_transporter",
                    () -> new DelayedTransporterBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(2.0f))
            );
}
