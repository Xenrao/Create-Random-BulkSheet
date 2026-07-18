package net.xenrao.create_random_bulksheet.recipe.fluid_extracting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.xenrao.create_random_bulksheet.recipe.RandomBulkSheetRecipes;

/**
 * "Fluid Extracting" recipe.
 * JSON'da "fluid" alanı:
 *   - direkt fluid id: "minecraft:lava"
 *   - tag: "#minecraft:lava"
 * ikisi de aynı alanda desteklenir.
 *
 * mbPerTickPerRpm: her tick, blogun uzerindeki RPM ile carpilip
 * bloğun gecici haznesine (fluidAmount) eklenir.
 *
 * requiresVoidStar: bu tarifin calismasi icin Void Star (eski "infinite" flag) sarti var mi.
 * Not: config'deki "enforceVoidStarRequirement" false ise bu sart tamamen yok sayilir (easy mode).
 */
public record FluidExtractingRecipe(
        Either<TagKey<Fluid>, Fluid> fluidInput,
        float mbPerTickPerRpm,
        boolean requiresVoidStar
) implements Recipe<RecipeInput> {

    private static final Codec<Either<TagKey<Fluid>, Fluid>> FLUID_INPUT_CODEC = Codec.STRING.comapFlatMap(
            str -> {
                if (str.startsWith("#")) {
                    ResourceLocation id = ResourceLocation.tryParse(str.substring(1));
                    if (id == null)
                        return DataResult.error(() -> "Gecersiz tag id: " + str);
                    return DataResult.success(Either.left(TagKey.create(Registries.FLUID, id)));
                }
                ResourceLocation id = ResourceLocation.tryParse(str);
                if (id == null)
                    return DataResult.error(() -> "Gecersiz fluid id: " + str);
                if (!BuiltInRegistries.FLUID.containsKey(id))
                    return DataResult.error(() -> "Bilinmeyen fluid: " + str);
                return DataResult.success(Either.right(BuiltInRegistries.FLUID.get(id)));
            },
            either -> either.map(
                    tag -> "#" + tag.location(),
                    fluid -> BuiltInRegistries.FLUID.getKey(fluid).toString()
            )
    );

    public static final MapCodec<FluidExtractingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            FLUID_INPUT_CODEC.fieldOf("fluid").forGetter(FluidExtractingRecipe::fluidInput),
            Codec.FLOAT.fieldOf("mb_per_tick_per_rpm").forGetter(FluidExtractingRecipe::mbPerTickPerRpm),
            Codec.BOOL.optionalFieldOf("requires_void_star", false).forGetter(FluidExtractingRecipe::requiresVoidStar)
    ).apply(inst, FluidExtractingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidExtractingRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> {
                boolean isTag = recipe.fluidInput.left().isPresent();
                buf.writeBoolean(isTag);
                ResourceLocation id = isTag
                        ? recipe.fluidInput.left().get().location()
                        : BuiltInRegistries.FLUID.getKey(recipe.fluidInput.right().get());
                buf.writeResourceLocation(id);
                buf.writeFloat(recipe.mbPerTickPerRpm);
                buf.writeBoolean(recipe.requiresVoidStar);
            },
            buf -> {
                boolean isTag = buf.readBoolean();
                ResourceLocation id = buf.readResourceLocation();
                Either<TagKey<Fluid>, Fluid> input = isTag
                        ? Either.left(TagKey.create(Registries.FLUID, id))
                        : Either.right(BuiltInRegistries.FLUID.get(id));
                float rate = buf.readFloat();
                boolean requiresStar = buf.readBoolean();
                return new FluidExtractingRecipe(input, rate, requiresStar);
            }
    );

    /**
     * Bu tarifin verilen sivi ile eslesip eslesmedigini kontrol eder.
     * Blok entity tarafindan dogrudan cagrilir - vanilla matches() burada kullanilmiyor.
     */
    public boolean matchesFluid(Fluid fluid) {
        return fluidInput.map(
                tag -> BuiltInRegistries.FLUID.wrapAsHolder(fluid).is(tag),
                f -> f == fluid
        );
    }

    // ================= Recipe<RecipeInput> boilerplate - bu tarif bir crafting grid'i kullanmiyor =================

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false; // Kullanilmiyor, eslesme matchesFluid() ile manuel yapiliyor
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return RandomBulkSheetRecipes.FLUID_EXTRACTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return RandomBulkSheetRecipes.FLUID_EXTRACTING_TYPE.get();
    }
}