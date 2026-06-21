package com.echoes.registry;

import com.echoes.EchoesMod;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ModItemGroups {
    private ModItemGroups() {}

    public static final ResourceKey<CreativeModeTab> ECHOES = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "main"));

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ECHOES, CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.echoes.main"))
                .icon(() -> new ItemStack(ModBlocks.RESONATOR))
                .displayItems((params, output) -> {
                    output.accept(ModBlocks.ECHOCITE_ORE);
                    output.accept(ModBlocks.DEEPSLATE_ECHOCITE_ORE);
                    output.accept(ModBlocks.DRUMSTONE_ORE);
                    output.accept(ModBlocks.SILENTITE_ORE);
                    output.accept(ModBlocks.STILLNESS_CORE);
                    output.accept(ModBlocks.RESONATOR);
                    output.accept(ModBlocks.TUNING_CONDUIT);
                    output.accept(ModBlocks.DENSE_CONDUIT);
                    output.accept(ModBlocks.RESONANCE_CAPACITOR);
                    output.accept(ModBlocks.CRUSHER);
                    output.accept(ModBlocks.ATTUNEMENT_FURNACE);
                    output.accept(ModBlocks.RADIATOR);
                    output.accept(ModBlocks.WARMTH_RADIATOR);
                    output.accept(ModBlocks.POLARITY_FIELD);
                    output.accept(ModBlocks.BALANCER);
                    output.accept(ModBlocks.RESONANT_RELAY);
                    output.accept(ModBlocks.RESONANT_AMPLIFIER);
                    output.accept(ModBlocks.HARMONIC_FILTER);
                    output.accept(ModBlocks.RESONANT_SPLITTER);
                    output.accept(ModBlocks.ECHO_REPEATER);
                    output.accept(ModBlocks.CONDUIT_COUPLER);
                    output.accept(ModBlocks.RESONANT_CHEST);
                    output.accept(ModBlocks.NOTE_RELAY);
                    output.accept(ModBlocks.GREATER_ACCUMULATOR);
                    output.accept(ModBlocks.OCTAVE_COIL);
                    output.accept(ModBlocks.OCTAVE_CONDUIT);
                    output.accept(ModBlocks.STORM_CALLER);
                    // Phase II — The Octave Grove
                    output.accept(ModBlocks.LUMEWOOD_LOG);
                    output.accept(ModBlocks.LUMEWOOD_WOOD);
                    output.accept(ModBlocks.LUMEWOOD_PLANKS);
                    output.accept(ModBlocks.LUMEWOOD_STAIRS);
                    output.accept(ModBlocks.LUMEWOOD_SLAB);
                    output.accept(ModBlocks.LUMEWOOD_FENCE);
                    output.accept(ModBlocks.LUMEWOOD_FENCE_GATE);
                    output.accept(ModBlocks.LUMEWOOD_TRAPDOOR);
                    output.accept(ModBlocks.LUMEWOOD_LEAVES);
                    output.accept(ModBlocks.LUMEWOOD_SAPLING);
                    output.accept(ModBlocks.LUMEBLOOM);
                    output.accept(ModBlocks.LUME_LANTERN);
                    output.accept(ModBlocks.VERDANT_LOAM);
                    output.accept(ModBlocks.ECHOCITE_BRICKS);
                    output.accept(ModBlocks.ECHOCITE_BRICK_STAIRS);
                    output.accept(ModBlocks.ECHOCITE_BRICK_SLAB);
                    output.accept(ModItems.FREQUENCY_TUNER);
                    output.accept(ModItems.CHANNEL_ATLAS);
                    output.accept(ModItems.RESONANCE_METER);
                    output.accept(ModItems.RESONANCE_THRUSTERS);
                    output.accept(ModItems.RESONANT_PICKAXE);
                    output.accept(ModItems.RESONANT_AXE);
                    output.accept(ModItems.RESONANT_SHOVEL);
                    output.accept(ModItems.RESONANT_SWORD);
                    output.accept(ModItems.RESONANT_HOE);
                    output.accept(ModItems.RAW_ECHOCITE);
                    output.accept(ModItems.DRUMSTONE_SHARD);
                    output.accept(ModItems.SILENTITE_CRYSTAL);
                    output.accept(ModItems.ECHOCITE_DUST);
                    output.accept(ModItems.ECHO_INGOT);
                    output.accept(ModItems.DULL_INGOT);
                    output.accept(ModItems.RESONANT_SLAG);
                    output.accept(ModItems.DRUM_CORE);
                    output.accept(ModItems.ECHO_DUST);
                    output.accept(ModItems.OCTAVE_SEED);
                    output.accept(ModItems.RADIANT_DUST);
                    output.accept(ModItems.RADIANT_INGOT);
                    // The Verdant Octave — transmutation economy
                    output.accept(ModBlocks.TRANSMUTATION_TABLE);
                    output.accept(ModItems.TRANSMUTATION_TABLET);
                    output.accept(ModItems.LIGHT_MOTE);
                    output.accept(ModItems.TONIC_MOTE);
                    output.accept(ModItems.MEDIANT_MOTE);
                    output.accept(ModItems.DOMINANT_MOTE);
                    output.accept(ModItems.HARMONIC_MOTE);
                    output.accept(ModItems.OCTAVE_STAR_1);
                    output.accept(ModItems.OCTAVE_STAR_2);
                    output.accept(ModItems.OCTAVE_STAR_3);
                    output.accept(ModItems.OCTAVE_STAR_4);
                    output.accept(ModItems.OCTAVE_STAR_5);
                    output.accept(ModItems.OCTAVE_STAR_6);
                })
                .build());
    }
}
