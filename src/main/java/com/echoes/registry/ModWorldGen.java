package com.echoes.registry;

import com.echoes.EchoesMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;

/** Attaches placed features to biomes. Echocite generates in the Overworld. */
public final class ModWorldGen {
    private ModWorldGen() {}

    public static final RegistryKey<net.minecraft.world.gen.feature.PlacedFeature> ECHOCITE_ORE =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(EchoesMod.MOD_ID, "echocite_ore"));

    public static void register() {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                ECHOCITE_ORE);
        // Silentite is wired separately with BiomeSelectors.includeByKey(BiomeKeys.DEEP_DARK).
    }
}
