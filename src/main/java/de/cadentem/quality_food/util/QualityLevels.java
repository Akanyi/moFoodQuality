package de.cadentem.quality_food.util;

import com.google.common.collect.MapMaker;
import de.cadentem.quality_food.core.codecs.QualityType;
import de.cadentem.quality_food.registry.QFComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.Arrays;
import java.util.Map;

/**
 * The level of a quality is the position of its type in the sequence of all registered quality types, ordered by {@link QualityType#level()} <br>
 * The defined level is only an anchor (for the order and the derived values) - adding a lower quality type moves the others up in the sequence without changing their values
 */
public final class QualityLevels {
    private static final Map<HolderLookup.RegistryLookup<QualityType>, int[]> ANCHORS = new MapMaker().weakKeys().makeMap();

    private QualityLevels() { }

    public static int get(final Holder<QualityType> type) {
        return get(type.value());
    }

    /** @return {@code 0} for {@link QualityType#NONE}, otherwise the position (starting at 1) of the quality type in the sequence */
    public static int get(final QualityType type) {
        if (type == QualityType.NONE) {
            return 0;
        }

        HolderLookup.RegistryLookup<QualityType> lookup = CommonHooks.resolveLookup(QFComponents.QUALITY_TYPE_REGISTRY);

        if (lookup == null) {
            return Math.max(1, type.level());
        }

        int[] anchors = ANCHORS.computeIfAbsent(lookup, QualityLevels::collectAnchors);
        int index = Arrays.binarySearch(anchors, type.level());
        return (index >= 0 ? index : -index - 1) + 1;
    }

    private static int[] collectAnchors(final HolderLookup.RegistryLookup<QualityType> lookup) {
        return lookup.listElements().mapToInt(type -> type.value().level()).distinct().sorted().toArray();
    }
}
