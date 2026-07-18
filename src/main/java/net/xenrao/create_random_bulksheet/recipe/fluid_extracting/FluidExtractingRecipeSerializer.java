package net.xenrao.create_random_bulksheet.recipe.fluid_extracting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FluidExtractingRecipeSerializer implements RecipeSerializer<FluidExtractingRecipe> {

    @Override
    public MapCodec<FluidExtractingRecipe> codec() {
        return FluidExtractingRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FluidExtractingRecipe> streamCodec() {
        return FluidExtractingRecipe.STREAM_CODEC;
    }
}