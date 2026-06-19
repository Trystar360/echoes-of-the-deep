package com.echoes.registry;

import com.echoes.EchoesMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;

/**
 * Attaches placed features to biomes. Echocite and Drumstone generate throughout
 * the Overworld; Silentite is rare and tied to the Deep Dark.
 */
public final class ModWorldGen {
    private ModWorldGen() {}

    private static RegistryKey<PlacedFeature> placed(String name) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(EchoesMod.MOD_ID, name));
    }

    private static RegistryKey<ConfiguredFeature<?, ?>> configured(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier.of(EchoesMod.MOD_ID, name));
    }

    public static final RegistryKey<PlacedFeature> ECHOCITE_ORE = placed("echocite_ore");
    public static final RegistryKey<PlacedFeature> DRUMSTONE_ORE = placed("drumstone_ore");
    public static final RegistryKey<PlacedFeature> SILENTITE_ORE = placed("silentite_ore");

    /** Lumewood tree — referenced by the sapling generator and by natural placement. */
    public static final RegistryKey<ConfiguredFeature<?, ?>> LUMEWOOD_TREE = configured("lumewood_tree");
    public static final RegistryKey<PlacedFeature> LUMEWOOD_TREE_PLACED = placed("lumewood_tree");

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
                BiomeSelectors.includeByKey(BiomeKeys.DEEP_DARK),
                GenerationStep.Feature.UNDERGROUND_ORES,
                SILENTITE_ORE);
        // Lumewood seeds itself sparsely through forested biomes.
        BiomeModifications.addFeature(
                BiomeSelectors.tag(net.minecraft.registry.tag.BiomeTags.IS_FOREST),
                GenerationStep.Feature.VEGETAL_DECORATION,
                LUMEWOOD_TREE_PLACED);
    }
}
