package de.cadentem.quality_food.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Limits which quality types the matching items can get (data pack folder {@code quality_food/food_qualities}) <br>
 * Items without a matching rule can get every quality type - if multiple rules match, the one with the highest priority is used
 * @param items     Item ids or item tags (prefixed with {@code #})
 * @param qualities The quality types these items can get
 */
public record FoodQualities(List<String> items, List<Identifier> qualities, int priority) {
    public static final Codec<FoodQualities> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    Codec.STRING.validate(FoodQualities::validateItem).listOf().fieldOf("items").forGetter(FoodQualities::items),
                    Identifier.CODEC.listOf().fieldOf("qualities").forGetter(FoodQualities::qualities),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(FoodQualities::priority))
            .apply(builder, FoodQualities::new));

    public FoodQualities {
        items = List.copyOf(items);
        qualities = List.copyOf(qualities);
    }

    public static FoodQualities of(final TagKey<Item> tag, final Identifier... qualities) {
        return new FoodQualities(List.of("#" + tag.location()), List.of(qualities), 0);
    }

    public static FoodQualities of(final Item item, final Identifier... qualities) {
        return new FoodQualities(List.of(BuiltInRegistries.ITEM.getKey(item).toString()), List.of(qualities), 0);
    }

    public FoodQualities withPriority(int priority) {
        return new FoodQualities(items, qualities, priority);
    }

    private static DataResult<String> validateItem(final String item) {
        String id = item.startsWith("#") ? item.substring(1) : item;
        return Identifier.tryParse(id) != null ? DataResult.success(item) : DataResult.error(() -> "Invalid item or item tag: " + item);
    }
}
