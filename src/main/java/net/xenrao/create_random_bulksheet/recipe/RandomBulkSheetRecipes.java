package net.xenrao.create_random_bulksheet.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;
import net.xenrao.create_random_bulksheet.recipe.fluid_extracting.FluidExtractingRecipe;
import net.xenrao.create_random_bulksheet.recipe.fluid_extracting.FluidExtractingRecipeSerializer;

import java.util.IdentityHashMap;
import java.util.Map;

public class RandomBulkSheetRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, RandomBulkSheet.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, RandomBulkSheet.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<FluidExtractingRecipe>> FLUID_EXTRACTING_TYPE =
            RECIPE_TYPES.register("fluid_extracting", () -> new RecipeType<FluidExtractingRecipe>() {
                @Override
                public String toString() {
                    return RandomBulkSheet.MODID + ":fluid_extracting";
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, FluidExtractingRecipeSerializer> FLUID_EXTRACTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("fluid_extracting", FluidExtractingRecipeSerializer::new);

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }

    // ================== Cache ==================
    private static volatile RecipeManager cachedManager = null;
    private static volatile Map<Fluid, FluidExtractingRecipe> fluidExtractingCache = Map.of();

    public static FluidExtractingRecipe getFluidExtractingRecipe(Level level, Fluid fluid) {
        RecipeManager currentManager = level.getRecipeManager();
        if (currentManager != cachedManager) {
            rebuildCache(currentManager);
            cachedManager = currentManager;
        }
        return fluidExtractingCache.get(fluid);
    }

    private static void rebuildCache(RecipeManager manager) {
        Map<Fluid, FluidExtractingRecipe> map = new IdentityHashMap<>();
        for (var holder : manager.getAllRecipesFor(FLUID_EXTRACTING_TYPE.get())) {
            FluidExtractingRecipe recipe = holder.value();
            for (Fluid fluid : BuiltInRegistries.FLUID) {
                if (recipe.matchesFluid(fluid))
                    map.putIfAbsent(fluid, recipe);
            }
        }
        fluidExtractingCache = map;
    }
}