package com.echoes.registry;

import com.echoes.EchoesMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Attaches placed features to biomes. Echocite and Drumstone generate throughout
 * the Overworld; Silentite is rare and tied to the Deep Dark.
 */
public final class ModWorldGen {
    private ModWorldGen() {}

    private static ResourceKey<PlacedFeature> placed(String name) {
        return ResourceKey.of(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, name));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String name) {
        return ResourceKey.of(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, name));
    }

    public static final ResourceKey<PlacedFeature> ECHOCITE_ORE = placed("echocite_ore");
    public static final ResourceKey<PlacedFeature> DRUMSTONE_ORE = placed("drumstone_ore");
    public static final ResourceKey<PlacedFeature> SILENTITE_ORE = placed("silentite_ore");

    /** Lumewood tree — referenced by the sapling generator and by natural placement. */
    public static final ResourceKey<ConfiguredFeature<?, ?>> LUMEWOOD_TREE = configured("lumewood_tree");
    public static final ResourceKey<PlacedFeature> LUMEWOOD_TREE_PLACED = placed("lumewood_tree");

    public static void register() {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                ECHOCITE_ORE);
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                DRUMSTONE_ORE);
        // Silentite only seeds the Deep Dark — silence pooled in the deepest places.
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(Biomes.DEEP_DARK),
                GenerationStep.Feature.UNDERGROUND_ORES,
                SILENTITE_ORE);
        // Lumewood seeds itself sparsely through forested biomes.
        BiomeModifications.addFeature(
                BiomeSelectors.tag(net.minecraft.tags.BiomeTags.IS_FOREST),
                GenerationStep.Feature.VEGETAL_DECORATION,
                LUMEWOOD_TREE_PLACED);
    }
}
