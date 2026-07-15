package net.xenrao.create_random_bulksheet;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // --- Packager -> Vanilla Crafter entegrasyonu ---
    public static final ModConfigSpec.BooleanValue ENABLE_VANILLA_CRAFTER_UNPACKING = BUILDER
            .comment("Whether Create's Packager can deposit patterned packages directly into vanilla Crafter blocks.",
                    "Disable this if stress-free auto-crafting feels too strong for your server.")
            .define("enableVanillaCrafterUnpacking", true);

    // --- Abyssal Fluid Tank ---
    public static final ModConfigSpec.IntValue FLUID_TANK_BASE_CAPACITY = BUILDER
            .comment("Base capacity (in mB) of the Abyssal Fluid Tank.")
            .defineInRange("fluidTankBaseCapacity", 1000, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue FLUID_TANK_STAR_MB = BUILDER
            .comment("Extra capacity (in mB, before the x1000 multiplier) granted per Nether Star.")
            .defineInRange("fluidTankStarMb", 200, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue FLUID_TANK_NETHERITE_MB = BUILDER
            .comment("Extra capacity (in mB, before the x1000 multiplier) granted per Netherite Ingot.")
            .defineInRange("fluidTankNetheriteMb", 50, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue FLUID_TANK_DIAMOND_MB = BUILDER
            .comment("Extra capacity (in mB, before the x1000 multiplier) granted per Diamond.")
            .defineInRange("fluidTankDiamondMb", 10, 0, Integer.MAX_VALUE);

    // --- Abyssal Energy Tank ---
    public static final ModConfigSpec.IntValue ENERGY_TANK_BASE_CAPACITY = BUILDER
            .comment("Base capacity (in FE) of the Abyssal Energy Tank.")
            .defineInRange("energyTankBaseCapacity", 1000000, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ENERGY_TANK_STAR_CAP = BUILDER
            .comment("Extra FE capacity granted per Nether Star.")
            .defineInRange("energyTankStarCap", 500000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ENERGY_TANK_NETHERITE_CAP = BUILDER
            .comment("Extra FE capacity granted per Netherite Ingot.")
            .defineInRange("energyTankNetheriteCap", 100000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ENERGY_TANK_DIAMOND_CAP = BUILDER
            .comment("Extra FE capacity granted per Diamond.")
            .defineInRange("energyTankDiamondCap", 20000, 0, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();

}
