# `fluid_extracting` Recipe Reference

**Example file location:**
```
data/create_random_bulksheet/recipe/fluid_extracting/<name>.json
```

These recipes define which fluids the **Abyssal Fluid Extractor** block can produce, and at what rate.
This is **not** a vanilla crafting recipe — there's no crafting grid or result item. The block entity
matches fluids directly via its own `matchesFluid()` method.

## Format

```json
{
  "type": "create_random_bulksheet:fluid_extracting",
  "fluid": "<fluid id or #tag>",
  "mb_per_tick_per_rpm": <float>,
  "requires_void_star": <true/false, optional, default false>
}
```

## Fields

### `fluid` (required)

Two formats are supported in the same field:

| Format | Example | Meaning |
|---|---|---|
| Direct fluid id | `"minecraft:lava"` | Matches exactly one fluid |
| Fluid tag | `"#minecraft:lava"` | Matches any fluid in that tag (leading `#`) |

If the id is invalid or not in the registry, the recipe fails to load — you'll see `Unknown fluid: ...`
in the log.

### `mb_per_tick_per_rpm` (required, float)

Every tick, this value is multiplied by the block's current RPM and added to the block's internal
fluid buffer (`fluidAmount`). Actual production rate:

```
mb/tick = mb_per_tick_per_rpm * current_RPM
```

Small values (roughly `0.001`–`0.1`) are generally sane; at high RPM you can overflow fast, so keep
the config's `maxBufferMb` in mind.

### `requires_void_star` (optional, default: `false`)

If `true`, this recipe only works while the block has been put into "infinite" mode via a Void Star.
If the config option `enforceVoidStarRequirement` is `false`, this requirement is ignored entirely
(easy-mode toggle, can be disabled in config).

## Examples

**Direct fluid id** (lava, requires a Void Star):

```json
{
  "type": "create_random_bulksheet:fluid_extracting",
  "fluid": "minecraft:lava",
  "mb_per_tick_per_rpm": 0.1,
  "requires_void_star": true
}
```

**Using a tag** (any fluid tagged `water`, no Void Star required):

```json
{
  "type": "create_random_bulksheet:fluid_extracting",
  "fluid": "#minecraft:water",
  "mb_per_tick_per_rpm": 0.0012
}
```

## Notes

- If multiple recipes match the same fluid, which one gets used is undefined (`matchesFluid` returns
  the first match) — avoid overlapping recipes.
- `requires_void_star: false` + a fluid tag is a good combo for writing a broad "default" recipe
  (e.g. a low base rate for anything tagged `flammable`).
- If there's no recipe here for vanilla water/lava, the extractor falls back to the config's
  `vanillaFluidRatePerRpm` value (read from `RandomBulkSheetConfig` in code) — not 100% verified
  against the block entity logic, worth a check if it matters for your setup.