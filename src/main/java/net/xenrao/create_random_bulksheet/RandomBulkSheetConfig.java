package net.xenrao.create_random_bulksheet;

import net.neoforged.neoforge.common.ModConfigSpec;

public class RandomBulkSheetConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // --- Packager -> Vanilla Crafter entegrasyonu ---
    public static final ModConfigSpec.BooleanValue ENABLE_VANILLA_CRAFTER_UNPACKING = BUILDER
            .comment("Whether Create's Packager can deposit patterned packages directly into vanilla Crafter blocks.",
                    "Disable this if stress-free auto-crafting feels too strong for your server.")
            .define("enableVanillaCrafterUnpacking", true);

    static {
        BUILDER.push("Abyssal Blocks");
        BUILDER.push("abyssal_fluid_tank");
    }
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

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.push("abyssal_fluid_extractor");
    }
    // --- Abyssal Fluid Extractor ---
    public static final ModConfigSpec.BooleanValue EXTRACTOR_ENFORCE_VOID_STAR = BUILDER
            .comment("If false, the Void Star requirement is ignored entirely - both for recipes that set",
                    "requires_void_star and for the hardcoded rule that non-vanilla fluids need it when no",
                    "recipe is found. Think of this as an easy-mode toggle.")
            .define("enforceVoidStarRequirement", true);

    public static final ModConfigSpec.DoubleValue EXTRACTOR_VANILLA_FLUID_RATE_PER_RPM = BUILDER
            .comment("For vanilla water/lava with no matching recipe: mB produced per tick, per RPM.",
                    "(water and lava share this same value)")
            .defineInRange("vanillaFluidRatePerRpm", 0.001, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue EXTRACTOR_NON_VANILLA_FLUID_RATE_PER_RPM = BUILDER
            .comment("For any other fluid with no matching recipe: mB produced per tick, per RPM.")
            .defineInRange("nonVanillaFluidRatePerRpm", 0.00015, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue EXTRACTOR_MAX_BUFFER_MB = BUILDER
            .comment("Maximum amount of fluid (in mB) the extractor can hold in its internal buffer",
                    "before extraction pauses until it is drained.")
            .defineInRange("maxBufferMb", 500.0, 1, Double.MAX_VALUE);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.push("abyssal_energy_tank");
    }

    // --- Abyssal Energy Tank ---
    public static final ModConfigSpec.IntValue ENERGY_TANK_BASE_CAPACITY = BUILDER
            .comment("Base capacity (in FE) of the Abyssal Energy Tank.")
            .defineInRange("energyTankBaseCapacity", 10000000, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ENERGY_TANK_STAR_CAP = BUILDER
            .comment("Extra FE capacity granted per Nether Star.")
            .defineInRange("energyTankStarCap", 500000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ENERGY_TANK_NETHERITE_CAP = BUILDER
            .comment("Extra FE capacity granted per Netherite Ingot.")
            .defineInRange("energyTankNetheriteCap", 100000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ENERGY_TANK_DIAMOND_CAP = BUILDER
            .comment("Extra FE capacity granted per Diamond.")
            .defineInRange("energyTankDiamondCap", 20000, 0, Integer.MAX_VALUE);

    static {
        BUILDER.pop();
        BUILDER.pop();
    }

    // --- kinetics ---
    static {
        BUILDER.push("kinetics");
    }
    public static final ModConfigSpec.DoubleValue EXTRACTOR_STRESS_IMPACT = BUILDER
            .comment("Abyssal Fluid Extractor Stress Impact Value")
            .defineInRange("extractorStressImpact", 1024, 1, Double.MAX_VALUE);


    static {
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();

}