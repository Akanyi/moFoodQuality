package de.cadentem.quality_food.api;

import de.cadentem.quality_food.QualityFood;
import de.cadentem.quality_food.api.event.QualityFoodEvent;
import de.cadentem.quality_food.api.event.QualityRollEvent;
import de.cadentem.quality_food.api.event.QualityValidCheckEvent;
import de.cadentem.quality_food.core.attachments.AnimalData;
import de.cadentem.quality_food.core.attachments.AttachmentHandler;
import de.cadentem.quality_food.core.attachments.BlockData;
import de.cadentem.quality_food.core.attachments.LevelData;
import de.cadentem.quality_food.core.codecs.Quality;
import de.cadentem.quality_food.core.codecs.QualityType;
import de.cadentem.quality_food.util.HarvestContext;
import de.cadentem.quality_food.util.QualityUtils;
import de.cadentem.quality_food.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Public API and extension registry for Quality Food.
 */
public final class QualityFoodAPI {
    private static final List<QualityConsumeEffect> GLOBAL_CONSUME_EFFECTS = new CopyOnWriteArrayList<>();
    private static final Map<Identifier, List<QualityConsumeEffect>> TYPED_CONSUME_EFFECTS = new ConcurrentHashMap<>();
    private static final List<QualityFoodPropertiesModifier> FOOD_PROPERTIES_MODIFIERS = new CopyOnWriteArrayList<>();
    private static final List<QualityConsumableModifier> CONSUMABLE_MODIFIERS = new CopyOnWriteArrayList<>();
    private static final List<QualityChanceModifier> CHANCE_MODIFIERS = new CopyOnWriteArrayList<>();
    private static final List<Predicate<ItemStack>> ITEM_VALIDATORS = new CopyOnWriteArrayList<>();
    private static final List<Predicate<Block>> BLOCK_VALIDATORS = new CopyOnWriteArrayList<>();

    private static final Object QUALITY_TYPE_LOCK = new Object();
    private static final Map<Identifier, QualityType> QUALITY_TYPES = new LinkedHashMap<>();
    private static final List<Map.Entry<Identifier, Consumer<QualityTypeBuilder>>> QUALITY_TYPE_MODIFIERS = new ArrayList<>();
    private static final Map<Identifier, FoodQualities> FOOD_QUALITIES = new LinkedHashMap<>();

    static {
        registerMaterialQualities(MaterialQuality.IRON, MaterialQuality.GOLD, MaterialQuality.DIAMOND);
    }

    private QualityFoodAPI() { }

    /* --- Quality Types --- */

    /**
     * Registers (or replaces) a quality type <br>
     * API quality types are provided by a built-in data pack at the lowest priority, so data packs can still override them by id <br>
     * Needs to be called before a world is loaded (e.g. during mod construction or common setup)
     * @throws IllegalStateException If the builder has invalid values
     */
    public static void registerQualityType(final Identifier id, final QualityTypeBuilder builder) {
        QualityType type = builder.build();

        synchronized (QUALITY_TYPE_LOCK) {
            QUALITY_TYPES.put(id, type);
        }
    }

    public static void registerQualityType(final ResourceKey<QualityType> key, final QualityTypeBuilder builder) {
        registerQualityType(key.identifier(), builder);
    }

    /**
     * Modifies an API quality type (including the built-in iron, gold and diamond) - modifiers are applied in registration order <br>
     * Quality types which only exist in data packs cannot be modified this way
     */
    public static void modifyQualityType(final Identifier id, final Consumer<QualityTypeBuilder> modifier) {
        synchronized (QUALITY_TYPE_LOCK) {
            QUALITY_TYPE_MODIFIERS.add(Map.entry(id, modifier));
        }
    }

    public static void modifyQualityType(final ResourceKey<QualityType> key, final Consumer<QualityTypeBuilder> modifier) {
        modifyQualityType(key.identifier(), modifier);
    }

    /**
     * Registers the presets of the given materials (already registered ones are kept) <br>
     * Their defined levels are fixed anchors, so e.g. enabling {@link MaterialQuality#WOOD} moves iron, gold and diamond up in the sequence without changing their values
     */
    public static void registerMaterialQualities(final MaterialQuality... materials) {
        synchronized (QUALITY_TYPE_LOCK) {
            for (MaterialQuality material : materials) {
                QUALITY_TYPES.putIfAbsent(material.id(), material.builder().build());
            }
        }
    }

    /**
     * Registers (or replaces) a rule which limits the quality types the matching items can get <br>
     * Provided through the same built-in data pack as the API quality types, so data packs can override it by id <br>
     * Needs to be called before a world is loaded (e.g. during mod construction or common setup)
     */
    public static void registerFoodQualities(final Identifier id, final FoodQualities rule) {
        synchronized (QUALITY_TYPE_LOCK) {
            FOOD_QUALITIES.put(id, rule);
        }
    }

    public static Map<Identifier, FoodQualities> getFoodQualities() {
        synchronized (QUALITY_TYPE_LOCK) {
            return Map.copyOf(FOOD_QUALITIES);
        }
    }

    /** The final API quality types (with the modifiers applied) - entries which end up invalid are skipped (and logged) */
    public static Map<Identifier, QualityType> resolveQualityTypes() {
        Map<Identifier, QualityTypeBuilder> builders = new LinkedHashMap<>();
        List<Map.Entry<Identifier, Consumer<QualityTypeBuilder>>> modifiers;

        synchronized (QUALITY_TYPE_LOCK) {
            QUALITY_TYPES.forEach((id, type) -> builders.put(id, QualityTypeBuilder.copyOf(type)));
            modifiers = List.copyOf(QUALITY_TYPE_MODIFIERS);
        }

        for (Map.Entry<Identifier, Consumer<QualityTypeBuilder>> modifier : modifiers) {
            QualityTypeBuilder builder = builders.get(modifier.getKey());

            if (builder == null) {
                QualityFood.LOG.warn("Cannot modify quality type [{}] since it was not registered through the API", modifier.getKey());
                continue;
            }

            modifier.getValue().accept(builder);
        }

        Map<Identifier, QualityType> resolved = new LinkedHashMap<>();

        builders.forEach((id, builder) -> {
            try {
                resolved.put(id, builder.build());
            } catch (IllegalStateException exception) {
                QualityFood.LOG.error("Skipping invalid quality type [{}]: {}", id, exception.getMessage());
            }
        });

        return resolved;
    }

    /* --- Registration API --- */

    /**
     * Registers a global custom consume effect triggered whenever any item with valid quality is consumed.
     */
    public static void registerConsumeEffect(final QualityConsumeEffect effect) {
        GLOBAL_CONSUME_EFFECTS.add(effect);
    }

    /**
     * Registers a custom consume effect triggered when an item with the specified {@link QualityType} key is consumed.
     */
    public static void registerConsumeEffect(final ResourceKey<QualityType> qualityTypeKey, final QualityConsumeEffect effect) {
        registerConsumeEffect(qualityTypeKey.identifier(), effect);
    }

    /**
     * Registers a custom consume effect triggered when an item with the specified quality type identifier is consumed.
     */
    public static void registerConsumeEffect(final Identifier qualityTypeId, final QualityConsumeEffect effect) {
        TYPED_CONSUME_EFFECTS.computeIfAbsent(qualityTypeId, key -> new CopyOnWriteArrayList<>()).add(effect);
    }

    /**
     * Registers a custom consume effect triggered when an item matching the given predicate and having valid quality is consumed.
     */
    public static void registerConsumeEffect(final BiPredicate<ItemStack, Quality> filter, final QualityConsumeEffect effect) {
        GLOBAL_CONSUME_EFFECTS.add((level, user, stack, consumable, quality, qualityType) -> {
            if (filter.test(stack, quality)) {
                effect.onConsume(level, user, stack, consumable, quality, qualityType);
            }
        });
    }

    /**
     * Registers a modifier that can customize the {@link FoodProperties} of an item with quality.
     */
    public static void registerFoodPropertiesModifier(final QualityFoodPropertiesModifier modifier) {
        FOOD_PROPERTIES_MODIFIERS.add(modifier);
    }

    /**
     * Registers a modifier that can customize the {@link Consumable} component (e.g. consume duration, status effects) of an item with quality.
     */
    public static void registerConsumableModifier(final QualityConsumableModifier modifier) {
        CONSUMABLE_MODIFIERS.add(modifier);
    }

    /**
     * Registers a modifier that can adjust the roll chance of candidate qualities across generic, harvest, crafting, and cooking rolls.
     */
    public static void registerChanceModifier(final QualityChanceModifier modifier) {
        CHANCE_MODIFIERS.add(modifier);
    }

    /**
     * Registers a predicate that allows additional items to be eligible for quality.
     */
    public static void registerItemValidator(final Predicate<ItemStack> validator) {
        ITEM_VALIDATORS.add(validator);
    }

    /**
     * Registers a predicate that allows additional blocks to be eligible for quality storage/harvesting.
     */
    public static void registerBlockValidator(final Predicate<Block> validator) {
        BLOCK_VALIDATORS.add(validator);
    }

    /* --- Query & Manipulation API --- */

    public static boolean hasQuality(final ItemStack stack) {
        return QualityUtils.hasQuality(stack);
    }

    public static Quality getQuality(final ItemStack stack) {
        return QualityUtils.getQuality(stack);
    }

    public static Holder<QualityType> getQualityType(final ItemStack stack) {
        return QualityUtils.getType(stack);
    }

    public static boolean applyQuality(final ItemStack stack, final Quality quality, boolean canUpgrade) {
        return QualityUtils.applyQuality(stack, quality, canUpgrade);
    }

    public static boolean applyQuality(final ItemStack stack, final Holder<QualityType> type) {
        return QualityUtils.applyQuality(stack, type);
    }

    public static void rollQuality(final ItemStack stack, @Nullable final Player player, final RegistryAccess access) {
        QualityUtils.applyQuality(stack, player, access);
    }

    public static void rollHarvestQuality(final HarvestContext context) {
        QualityUtils.applyHarvestQuality(context);
    }

    public static void rollCraftingQuality(final ItemStack stack, final Collection<ItemStack> ingredients, @Nullable final Player player, final RegistryAccess access) {
        QualityUtils.applyQuality(stack, ingredients, player, access);
    }

    public static Quality getBlockQuality(final LevelAccessor level, final BlockPos position) {
        return LevelData.get(level, position);
    }

    public static void setBlockQuality(final LevelAccessor level, final BlockPos position, final Quality quality) {
        LevelData.set(level, position, quality);
    }

    public static void removeBlockQuality(final LevelAccessor level, final BlockPos position) {
        LevelData.remove(level, position);
    }

    public static double getAnimalPotential(@Nullable final Entity entity) {
        return AnimalData.getPotential(entity);
    }

    public static void setAnimalPotential(final Entity entity, double potential) {
        AnimalData data = AnimalData.get(entity);
        if (data != null) {
            data.potential = Mth.clamp(potential, 0, 1);
        }
    }

    public static BlockData getCookingBlockData(final BlockEntity blockEntity) {
        return blockEntity.getData(AttachmentHandler.BLOCK_DATA);
    }

    public static void incrementCookingQuality(final BlockEntity blockEntity, final Collection<ItemStack> ingredients, final ItemStack result) {
        Utils.incrementQuality(blockEntity, ingredients, result);
    }

    public static void useCookingQuality(final BlockEntity blockEntity, final ItemStack stack, @Nullable final Player player) {
        Utils.useQuality(blockEntity, stack, player);
    }

    /* --- Internal Dispatch Hooks --- */

    @ApiStatus.Internal
    public static double fireQualityRoll(final QualityChanceModifier.Context context, double baseChance) {
        double chance = baseChance;
        for (QualityChanceModifier modifier : CHANCE_MODIFIERS) {
            chance = modifier.modify(context, chance);
        }

        QualityRollEvent event = new QualityRollEvent(context, chance);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return -1;
        }

        return event.getChance();
    }

    @ApiStatus.Internal
    public static FoodProperties applyFoodPropertiesModifiers(final ItemStack stack, final FoodProperties original, final FoodProperties computed, final Quality quality, final Holder<QualityType> qualityType) {
        FoodProperties current = computed;
        for (QualityFoodPropertiesModifier modifier : FOOD_PROPERTIES_MODIFIERS) {
            current = modifier.modify(stack, current, quality, qualityType);
        }
        return current;
    }

    @ApiStatus.Internal
    public static Consumable applyConsumableModifiers(final ItemStack stack, final Consumable current, final Quality quality, final Holder<QualityType> qualityType) {
        Consumable modified = current;
        for (QualityConsumableModifier modifier : CONSUMABLE_MODIFIERS) {
            modified = modifier.modify(stack, modified, quality, qualityType);
        }
        return modified;
    }

    @ApiStatus.Internal
    public static void onFoodConsumed(final Level level, final LivingEntity user, final ItemStack stack, final Consumable consumable) {
        if (!QualityUtils.hasQuality(stack)) {
            return;
        }

        Quality quality = QualityUtils.getQuality(stack);
        Holder<QualityType> qualityType = quality.getType();

        QualityFoodEvent.Consumed event = new QualityFoodEvent.Consumed(level, user, stack, consumable, quality, qualityType);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return;
        }

        for (QualityConsumeEffect effect : GLOBAL_CONSUME_EFFECTS) {
            effect.onConsume(level, user, stack, consumable, quality, qualityType);
        }

        List<QualityConsumeEffect> typedEffects = TYPED_CONSUME_EFFECTS.get(quality.type());
        if (typedEffects != null) {
            for (QualityConsumeEffect effect : typedEffects) {
                effect.onConsume(level, user, stack, consumable, quality, qualityType);
            }
        }
    }

    @ApiStatus.Internal
    public static QualityValidCheckEvent.Result checkItemValid(final ItemStack stack, boolean checkBlock) {
        for (Predicate<ItemStack> validator : ITEM_VALIDATORS) {
            if (validator.test(stack)) {
                return QualityValidCheckEvent.Result.ALLOW;
            }
        }

        QualityValidCheckEvent.Item event = new QualityValidCheckEvent.Item(stack, checkBlock);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult();
    }

    @ApiStatus.Internal
    public static QualityValidCheckEvent.Result checkBlockValid(final Block block, boolean checkItem) {
        for (Predicate<Block> validator : BLOCK_VALIDATORS) {
            if (validator.test(block)) {
                return QualityValidCheckEvent.Result.ALLOW;
            }
        }

        QualityValidCheckEvent.BlockCheck event = new QualityValidCheckEvent.BlockCheck(block, checkItem);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult();
    }
}

