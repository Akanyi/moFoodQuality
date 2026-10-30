package de.cadentem.quality_food.data;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/** Quality types are provided through {@link de.cadentem.quality_food.registry.QFApiPack} */
@EventBusSubscriber
public class DataGen {
    @SubscribeEvent
    public static void configureDataGen(final GatherDataEvent.Client event) {
        QFBlockTags blockTags = new QFBlockTags(event.getGenerator().getPackOutput(), event.getLookupProvider());
        event.getGenerator().addProvider(true, blockTags);
        event.getGenerator().addProvider(true, new QFItemTags(event.getGenerator().getPackOutput(), event.getLookupProvider()));
        event.getGenerator().addProvider(true, new QFLootModifiers(event.getGenerator().getPackOutput(), event.getLookupProvider()));
        event.getGenerator().addProvider(true, new QFEffectTags(event.getGenerator().getPackOutput(), event.getLookupProvider()));
        event.getGenerator().addProvider(true, new QFEntityTypeTags(event.getGenerator().getPackOutput(), event.getLookupProvider()));
    }
}
