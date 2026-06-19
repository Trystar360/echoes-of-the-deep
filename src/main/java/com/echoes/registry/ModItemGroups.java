package com.echoes.registry;

import com.echoes.EchoesMod;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItemGroups {
    private ModItemGroups() {}

    public static final RegistryKey<ItemGroup> ECHOES = RegistryKey.of(
            RegistryKeys.ITEM_GROUP, Identifier.of(EchoesMod.MOD_ID, "main"));

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, ECHOES, ItemGroup.create(ItemGroup.Row.TOP, 0)
                .displayName(Text.translatable("itemGroup.echoes.main"))
                .icon(() -> new ItemStack(ModBlocks.RESONATOR))
                .entries((ctx, entries) -> {
                    entries.add(ModBlocks.ECHOCITE_ORE);
                    entries.add(ModBlocks.DEEPSLATE_ECHOCITE_ORE);
                    entries.add(ModBlocks.DRUMSTONE_ORE);
                    entries.add(ModBlocks.SILENTITE_ORE);
                    entries.add(ModBlocks.STILLNESS_CORE);
                    entries.add(ModBlocks.RESONATOR);
                    entries.add(ModBlocks.TUNING_CONDUIT);
                    entries.add(ModBlocks.DENSE_CONDUIT);
                    entries.add(ModBlocks.RESONANCE_CAPACITOR);
                    entries.add(ModBlocks.CRUSHER);
                    entries.add(ModBlocks.ATTUNEMENT_FURNACE);
                    entries.add(ModBlocks.RADIATOR);
                    entries.add(ModBlocks.WARMTH_RADIATOR);
                    entries.add(ModBlocks.POLARITY_FIELD);
                    entries.add(ModBlocks.BALANCER);
                    entries.add(ModBlocks.RESONANT_RELAY);
                    entries.add(ModBlocks.RESONANT_AMPLIFIER);
                    entries.add(ModBlocks.HARMONIC_FILTER);
                    entries.add(ModBlocks.RESONANT_SPLITTER);
                    entries.add(ModBlocks.ECHO_REPEATER);
                    entries.add(ModBlocks.CONDUIT_COUPLER);
                    entries.add(ModBlocks.RESONANT_CHEST);
                    entries.add(ModBlocks.NOTE_RELAY);
                    entries.add(ModBlocks.GREATER_ACCUMULATOR);
                    // Phase II — The Octave Grove
                    entries.add(ModBlocks.LUMEWOOD_LOG);
                    entries.add(ModBlocks.LUMEWOOD_WOOD);
                    entries.add(ModBlocks.LUMEWOOD_PLANKS);
                    entries.add(ModBlocks.LUMEWOOD_STAIRS);
                    entries.add(ModBlocks.LUMEWOOD_SLAB);
                    entries.add(ModBlocks.LUMEWOOD_FENCE);
                    entries.add(ModBlocks.LUMEWOOD_FENCE_GATE);
                    entries.add(ModBlocks.LUMEWOOD_TRAPDOOR);
                    entries.add(ModBlocks.LUMEWOOD_LEAVES);
                    entries.add(ModBlocks.LUMEWOOD_SAPLING);
                    entries.add(ModBlocks.LUMEBLOOM);
                    entries.add(ModBlocks.LUME_LANTERN);
                    entries.add(ModBlocks.VERDANT_LOAM);
                    entries.add(ModBlocks.ECHOCITE_BRICKS);
                    entries.add(ModBlocks.ECHOCITE_BRICK_STAIRS);
                    entries.add(ModBlocks.ECHOCITE_BRICK_SLAB);
                    entries.add(ModItems.FREQUENCY_TUNER);
                    entries.add(ModItems.CHANNEL_ATLAS);
                    entries.add(ModItems.RESONANCE_METER);
                    entries.add(ModItems.RESONANCE_THRUSTERS);
                    entries.add(ModItems.RESONANT_PICKAXE);
                    entries.add(ModItems.RESONANT_AXE);
                    entries.add(ModItems.RESONANT_SHOVEL);
                    entries.add(ModItems.RESONANT_SWORD);
                    entries.add(ModItems.RESONANT_HOE);
                    entries.add(ModItems.RAW_ECHOCITE);
                    entries.add(ModItems.DRUMSTONE_SHARD);
                    entries.add(ModItems.SILENTITE_CRYSTAL);
                    entries.add(ModItems.ECHOCITE_DUST);
                    entries.add(ModItems.ECHO_INGOT);
                    entries.add(ModItems.DULL_INGOT);
                    entries.add(ModItems.RESONANT_SLAG);
                    entries.add(ModItems.DRUM_CORE);
                    entries.add(ModItems.ECHO_DUST);
                    entries.add(ModItems.OCTAVE_SEED);
                    entries.add(ModItems.RADIANT_DUST);
                    entries.add(ModItems.RADIANT_INGOT);
                })
                .build());
    }
}
