package net.xenrao.create_random_bulksheet.data;

import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class RandomBulkSheetModelGen {

    // --- Basit tek-texture küp (cubeAll benzeri, ama klasörlü) ---
    public static ModelFile simpleCube(DataGenContext<Block, ? extends Block> ctx, BlockStateProvider prov) {
        ResourceLocation texture = prov.modLoc("block/" + ctx.getName() + "/block");
        return prov.models().cubeAll(ctx.getName(), texture);
    }

    // --- Elle yazılmış custom model'e referans (Blockbench export) ---
    public static ModelFile existingCustomModel(DataGenContext<Block, ? extends Block> ctx, BlockStateProvider prov) {
        return prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName() + "/block"));
    }

    // --- Her yüzeye farklı texture, ama hepsi kendi klasöründe ---
    public static ModelFile perFaceCube(DataGenContext<Block, ? extends Block> ctx, BlockStateProvider prov) {
        String base = "block/" + ctx.getName() + "/";
        return prov.models().cube(ctx.getName(),
                prov.modLoc(base + "down"),
                prov.modLoc(base + "up"),
                prov.modLoc(base + "north"),
                prov.modLoc(base + "south"),
                prov.modLoc(base + "east"),
                prov.modLoc(base + "west")
        );
    }

    // --- Item modeli, aynı isimdeki blok modeline parent olur ---
    public static <I extends Item, P> NonNullFunction<ItemBuilder<I, P>, P> customItemModel() {
        return b -> b.model((ctx, prov) -> prov.withExistingParent(
                ctx.getName(),
                prov.modLoc("block/" + ctx.getName() + "/block")
        )).build();
    }
}