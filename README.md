# moFoodQuality

> [!IMPORTANT]
> **This is an unofficial fork** of [Quality Food](https://github.com/SiverDX/quality_food) by Cadentem, maintained by LANS. It is not made or supported by the original author - please report issues for this version at [Akanyi/moFoodQuality](https://github.com/Akanyi/moFoodQuality/issues), not on the original repository.
>
> **这是一个非官方分支**，基于 Cadentem 的 [Quality Food](https://github.com/SiverDX/quality_food)，由 LANS 维护，原作者不提供支持。此版本的问题请反馈到 [Akanyi/moFoodQuality](https://github.com/Akanyi/moFoodQuality/issues)，不要提交到原仓库。

- The mod id stays `quality_food`, so existing worlds, data packs and configs keep working - it replaces the original mod and cannot be installed together with it
- Additions compared to the original: [API](#api) for quality types and the material presets (wood, stone, copper, netherite, ender), sequence-based [Levels](#levels) and per-food [Food Qualities](#food-qualities)

# General
This branch targets Minecraft 26.1.2 with NeoForge and Java 25.

This mod adds quality to food and certain food-related material (e.g. crops) through data components

Quality can:
- Increase nutrition and saturation
- Improve positive effects and diminish (or outright remove) negative effects gained from eating food
- Impact the result of crafted (quality applicable) items (the `minecraft:luck` will increase the amount of rolls)

---

Quality Types are defined per datapack (`data/<namespace>/quality_food/quality_types/`), syntax example:

```json
{
  "amplifier_modifier": 1,
  "chance": 0.1,
  "cooking_bonus": 0.00390625,
  "crafting_bonus": 0.15,
  "duration_multiplier": 1.5,
  "effects": [
    {
      "applicable_to": "#c:foods/fruit",
      "effects": [
        {
          "effect": {
            "probability": 0.6,
            "type": "minecraft:apply_effects",
            "effects": [
              {
                "amplifier": 2,
                "duration": 100,
                "id": "minecraft:absorption",
                "neoforge:cures": [
                  "protected_by_totem",
                  "milk"
                ],
                "show_icon": true
              }
            ]
          },
          "chance": 0.5
        },
        {
          "effect": {
            "probability": 0.2,
            "type": "minecraft:apply_effects",
            "effects": [
              {
                "duration": 40,
                "id": "minecraft:regeneration",
                "neoforge:cures": [
                  "protected_by_totem",
                  "milk"
                ],
                "show_icon": true
              }
            ]
          },
          "chance": 1
        }
      ]
    }
  ],
  "icon": "quality_food:quality_icon/iron",
  "level": 1,
  "nutrition_multiplier": 1.5,
  "probability_multiplier": 1.25,
  "saturation_multiplier": 1.25,
  "weight": 1,
  "min_weight": 0,
  "name": {
    "translate": "quality_type.quality_food.iron"
  }
}
```
- `level`: The position of the quality type - higher level ones will be rolled first (can be `0` or negative to be placed below iron, see [Levels](#levels))
- `chance`: Chance of this quality being applied
  - Value between `0` and `1` (1 being 100%)
- `nutrition_multiplier`: The amount the nutrition will be multiplied by
- `saturation_multiplier`: The amount the saturation will be multiplied by
- `icon`: The icon shown for the quality (texture needs to be in the `textures/gui/sprites` directory)
- (Optional) `duration_multiplier`: The amount the duration of existing beneficial effects will be multiplied by (or divided if the effect is harmful)
  - If the duration reaches 0 during modifications the effect will be removed
- (Optional) `probability_multiplier`: The amount the probability of the beneficial effect will be multiplied by (or divided if the effect is harmful)
  - If the probability reaches 0 during modifications the effect will be removed
- (Optional) `amplifier_multiplier`: The amount the amplifier of the beneficial effect will be increased by (or decreased if the effect is harmful)
  - If the amplifier goes below 0 during modifications the effect will be removed
- (Optional) `crafting_bonus`: Additive bonus to the chance of rolling for quality (actual bonus will be `crafting_bonus / quality_applicable_ingredients`)
  - **Example**: 1x diamond quality (bonus of 0.7) + 1x iron quality (bonus of 0.15) ingredients = (0.7 / 2) + (0.15 / 2) = total bonus of 0.425
  - Value between `0` and `1` (1 being 100%)
- (Optional) `cooking_bonus`: The bonus each cooked item of this quality will provide for the stored bonus of the cooking block entity
  - Value between `0` and `1` (1 being 100%)
- (Optional) `cooking_bonus_divisor`: The stored cooking bonus is divided by this value when the result rolls for this quality
  - Needs to be greater than `0` - if not set it will be `level * level` (minimum `1`)
- (Optional) `egg_hatch_bonus`: Subtracted from the vanilla `1 in 8` chance of a thrown egg (with this quality) spawning a chicken
  - If not set it will be `level * 2`
- (Optional) `egg_multi_hatch_bonus`: Subtracted from the vanilla `1 in 32` chance of a hatching egg spawning 4 chickens
  - If not set it will be `level * 4`
- (Optional) `effects`: List of effect configurations
  - (Optional) `applicable_to`: The item (or item tag) these effects will be applied to - if empty it will be applied to all items
  - `effects`: List of effects which will be applied
    - `effect`: The effect (see the single effect [here](https://minecraft.wiki/w/Data_component_format#food))
    - (Optional) `chance`: The chance this effect will be applied to the item when the quality is applied
      - Value between `0` and `1` (1 being 100%)
      - If not supplied the value will be `1` 

The actual quality data component which gets attached to items looks like this:

```json
{
  components: {
    "quality_food:quality": {
      level: 2,
      type: "quality_food:gold"
    }
  },
  count: 1,
  id: "minecraft:apple"
}
```

# API
Quality types can also be defined in code through `QualityFoodAPI` (call it during mod construction or common setup):

```java
// Enable material presets (wood, stone, copper, netherite and ender are not registered by default)
QualityFoodAPI.registerMaterialQualities(MaterialQuality.NETHERITE, MaterialQuality.ENDER);

// Change the values of an API quality type (including the built-in iron, gold and diamond)
QualityFoodAPI.modifyQualityType(MaterialQuality.GOLD.id(), builder -> builder.chance(0.05).nutritionMultiplier(2.25));

// Register a new quality type
QualityFoodAPI.registerQualityType(Identifier.fromNamespaceAndPath("examplemod", "ruby"), QualityTypeBuilder.create()
        .level(6).chance(0.002).weight(3.5).minWeight(2.5)
        .nutritionMultiplier(2.75).saturationMultiplier(1.9)
        .icon(Identifier.fromNamespaceAndPath("examplemod", "quality_icon/ruby"))
        .name(Component.translatable("quality_type.examplemod.ruby")));

// Limit which quality types an item (or item tag) can get
QualityFoodAPI.registerFoodQualities(Identifier.fromNamespaceAndPath("examplemod", "bread"), FoodQualities.of(Items.BREAD, MaterialQuality.WOOD.id(), MaterialQuality.STONE.id(), MaterialQuality.COPPER.id()));
```

- The API quality types (including iron, gold and diamond) and food quality rules are provided by the built-in data pack `quality_food/api` with the lowest priority, so a data pack file with the same id still overrides them
- The material presets use fixed levels: wood `-2`, stone `-1`, copper `0`, iron `1`, gold `2`, diamond `3`, netherite `4`, ender `5`
  - The values of a quality type never depend on which other quality types are registered, so enabling wood, stone or copper does not change iron, gold or diamond

# Levels
The `level` of a quality type only defines its position - the level of an item is the position of its quality type in the sequence of all registered quality types (starting at `1`)
- Without additional quality types iron / gold / diamond are the levels `1` / `2` / `3`, with wood, stone and copper they become `4` / `5` / `6`
- The level stored on items is re-calculated when they are loaded, so items from an existing world are moved up as well (and still stack with new ones)
- Values which are derived from the level (see `cooking_bonus_divisor`, `egg_hatch_bonus` and `egg_multi_hatch_bonus`) use the defined `level` (values below `1` count as `0`), not the position

# Food Qualities
Defined per datapack (`data/<namespace>/quality_food/food_qualities/`) - limits the quality types the matching items can get:

```json
{
  "items": ["minecraft:bread", "#c:foods/cooked_meat"],
  "qualities": ["quality_food:wood", "quality_food:stone", "quality_food:copper"],
  "priority": 0
}
```

- `items`: Item ids or item tags (prefixed with `#`)
- `qualities`: The quality types these items can get - a higher quality type which is not listed will not be rolled for (lower listed ones still can)
- (Optional) `priority`: If multiple rules match an item the one with the highest priority is used (default `0`)
- Items without a matching rule can get every quality type
- Existing items keep their quality, the rules only apply when quality is applied (rolls, crafting, cooking, commands)
  - Cooking uses the lowest ingredient quality as base - if the result is not allowed to have it, it can only be upgraded to a higher allowed quality

# Commands
- `/quality_food give`: Gives you an item with the defined quality (only lists item which are applicable to quality)
- `/quality_food apply`: Applies a quality to an item
- `/quality_food remove`: Removes quality from an item

# Configuration
There is a farmland configuration which allows you to define a bonus (can also be negative, i.e. 0.5) based on the farm block the crop is planted on
- Example: `farmland_config = ["3;#minecraft:crops;farmersdelight:rich_soil_farmland;1.25"]`
  - `3` is the index (configurations are tested with the lowest one first - the first matching one will be applied) (needs to be positive)
  - `#minecraft:crops` is the crop block (can be a tag or a single block)
  - `farmersdelight:rich_soil_farmland` is the farmland block (can be a tag or a single block)
  - `1.25` is the multiplier to be applied (needs to be positive - values below `1` will reduce the chance)

Naturally generated crops (i.e. not planted by a player) and loot (e.g. chest loot) only roll for the quality types listed in the `Balance` section:
- `natural_harvest_quality_types` / `natural_loot_quality_types`: both default to `["quality_food:iron", "quality_food:gold", "quality_food:diamond"]`
- These replace `max_natural_harvest_quality_level` / `max_natural_loot_quality_level`

---

All items with food properties should be applicable to quality by default
- For other items (e.g. materials) the item tag `quality_food:material_whitelist` is used
- In case some items should not have quality applied the item tag `quality_food:blacklist` is used

Only blocks within the block tag `quality_food:quality_blocks` will support (i.e. retain) quality
- If a block is applicable to quality the item for said block will be too

---

For crafting (crafting table) there are three configs:
- `retain_quality_recipes`: The result will retain the quality of the ingredients, examples:
  - If all items are `diamond` quality the result will be `diamond`
  - If three items are `gold` quality and two are `diamond` the result will be `gold`
  - If two items are `iron` quality and the rest have none then the result will also have none
- `no_quality_recipes`: Entries will not roll for quality (useful in case items can be crated back and forth)
- `handle_compacting`: If enabled then (de)compacting results should retain quality automatically without having to specify the relevant recipes

Cooking quality items will store a quality bonus within the furnace / cooking pot / ...
- The bonus is defined in the quality type with `cooking_bonus`
- Once enough bonus is stored particles will start to show
  - A higher bonus results in more particles
  - This can be disabled through the client config
- Once you take out the result the stored bonus will be used up and grant a higher chance to a quality result
- The base quality of the cooked item is the lowest quality ingredient used when cooking
  - 30x gold ingredients = 30x gold result (+ chance of upgrading to diamond)
  - 25x gold + 5x iron = 30x iron result (+ chance of upgrading to gold or diamond)

# Compatibility
- [Jade](https://www.curseforge.com/minecraft/mc-mods/jade)
  - Quality tooltip
- [Tom's Simple Storage Mod](https://www.curseforge.com/minecraft/mc-mods/toms-storage)
  - Apply quality when crafting
- [Harvest with ease](https://www.curseforge.com/minecraft/mc-mods/harvest-with-ease)
  - Properly roll quality when auto harvesting
- [RightClickHarvest](https://www.curseforge.com/minecraft/mc-mods/rightclickharvest)
  - Properly roll quality when auto harvesting
- [FastWorkbench](https://www.curseforge.com/minecraft/mc-mods/fastworkbench)
  - Quality gets properly handled when using the crafting bench

This is mostly about block interaction / quality application through crafting
- If a mod adds a new crafting block then quality may not apply correctly
- Items should generally be fine

# Misc

![Example](https://i.imgur.com/hUnpNUh.png)

Credits for the quality icons go to https://twitter.com/concernedape
