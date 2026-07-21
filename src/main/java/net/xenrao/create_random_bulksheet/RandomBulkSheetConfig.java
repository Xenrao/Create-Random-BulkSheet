package net.xenrao.create_random_bulksheet;

import net.neoforged.neoforge.common.ModConfigSpec;

public class RandomBulkSheetConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // --- Abyssal Fluid Tank ---
    static {
        BUILDER.push("blocks");
        BUILDER.push("abyssal_fluid_tank");
    }
    public static final ModConfigSpec.IntValue ABYSSAL_FLUID_TANK_BASE_CAPACITY = BUILDER
            .comment("Base capacity (in mB) of the Abyssal Fluid Tank.")
            .defineInRange("fluidTankBaseCapacity", 1000000, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ABYSSAL_FLUID_TANK_STAR_CAPACITY_BONUS = BUILDER
            .comment("Extra capacity (in mB, before the x1000 multiplier) granted per Nether Star.")
            .defineInRange("fluidTankStarMb", 200000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ABYSSAL_FLUID_TANK_NETHERITE_CAPACITY_BONUS = BUILDER
            .comment("Extra capacity (in mB, before the x1000 multiplier) granted per Netherite Ingot.")
            .defineInRange("fluidTankNetheriteMb", 50000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ABYSSAL_FLUID_TANK_DIAMOND_CAPACITY_BONUS = BUILDER
            .comment("Extra capacity (in mB, before the x1000 multiplier) granted per Diamond.")
            .defineInRange("fluidTankDiamondMb", 10000, 0, Integer.MAX_VALUE);

    static {
        BUILDER.pop();
    }

    // --- Abyssal Fluid Extractor ---
    static {
        BUILDER.push("abyssal_fluid_extractor");
    }
    public static final ModConfigSpec.BooleanValue ABYSSAL_FLUID_EXTRACTOR_ENFORCE_VOID_STAR = BUILDER
            .comment("If false, the Void Star requirement is ignored entirely - both for recipes that set",
                    "requires_void_star and for the hardcoded rule that non-vanilla fluids need it when no",
                    "recipe is found. Think of this as an easy-mode toggle.")
            .define("enforceVoidStarRequirement", true);

    public static final ModConfigSpec.DoubleValue ABYSSAL_FLUID_EXTRACTOR_VANILLA_FLUID_RATE_PER_RPM = BUILDER
            .comment("For vanilla water/lava with no matching recipe: mB produced per tick, per RPM.",
                    "(water and lava share this same value)")
            .defineInRange("vanillaFluidRatePerRpm", 0.001, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue ABYSSAL_FLUID_EXTRACTOR_NON_VANILLA_FLUID_RATE_PER_RPM = BUILDER
            .comment("For any other fluid with no matching recipe: mB produced per tick, per RPM.")
            .defineInRange("nonVanillaFluidRatePerRpm", 0.00015, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue ABYSSAL_FLUID_EXTRACTOR_MAX_BUFFER_MB = BUILDER
            .comment("Maximum amount of fluid (in mB) the extractor can hold in its internal buffer",
                    "before extraction pauses until it is drained.")
            .defineInRange("maxBufferMb", 500.0, 1, Double.MAX_VALUE);

    static {
        BUILDER.pop();
    }
    // --- Abyssal Energy Tank ---
    static {
        BUILDER.push("abyssal_energy_tank");
    }

    public static final ModConfigSpec.IntValue ABYSSAL_ENERGY_TANK_BASE_CAPACITY = BUILDER
            .comment("Base capacity (in FE) of the Abyssal Energy Tank.")
            .defineInRange("energyTankBaseCapacity", 10000000, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ABYSSAL_ENERGY_TANK_STAR_CAPACITY_BONUS = BUILDER
            .comment("Extra FE capacity granted per Nether Star.")
            .defineInRange("energyTankStarCap", 500000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ABYSSAL_ENERGY_TANK_NETHERITE_CAPACITY_BONUS = BUILDER
            .comment("Extra FE capacity granted per Netherite Ingot.")
            .defineInRange("energyTankNetheriteCap", 100000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ABYSSAL_ENERGY_TANK_DIAMOND_CAPACITY_BONUS = BUILDER
            .comment("Extra FE capacity granted per Diamond.")
            .defineInRange("energyTankDiamondCap", 20000, 0, Integer.MAX_VALUE);

    static {
        BUILDER.pop();
    }

    // --- Blade Propeller ---
    static {
        BUILDER.push("blade_propeller");
    }
    public static final ModConfigSpec.IntValue BLADE_PROPELLER_MAX_BLADE_ANGLE = BUILDER
            .comment("Blade Propeller max allowed blade angle")
            .defineInRange("bladePropellerMaxAngle", 45, 1, 90);

    static {
        BUILDER.pop();
        BUILDER.pop();
    }

    // --- kinetics ---
    static {
        BUILDER.push("kinetics");
    }
    public static final ModConfigSpec.DoubleValue ABYSSAL_FLUID_EXTRACTOR_STRESS_IMPACT = BUILDER
            .comment("Abyssal Fluid Extractor Stress Impact Value")
            .defineInRange("extractorStressImpact", 512, 1, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue BLADE_PROPELLER_STRESS_IMPACT = BUILDER
            .comment("Blade Propeller Stress Impact Value",
                    "Create Aeronautics must be installed for the block to be usable.")
            .defineInRange("bladePropellerStressImpact", 4, 1, Double.MAX_VALUE);

    static {
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();

}