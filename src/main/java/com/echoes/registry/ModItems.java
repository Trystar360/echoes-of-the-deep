package com.echoes.registry;

import com.echoes.EchoesMod;
import com.echoes.item.ChannelAtlasItem;
import com.echoes.item.FrequencyTunerItem;
import com.echoes.item.ResonanceMeterItem;
import com.echoes.item.ResonanceThrustersItem;
import com.echoes.item.ResonantToolItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public final class ModItems {
    private ModItems() {}

    // Raw drops
    public static final Item RAW_ECHOCITE   = register("raw_echocite", Item::new, new Item.Properties());
    public static final Item DRUMSTONE_SHARD = register("drumstone_shard", Item::new, new Item.Properties());
    public static final Item SILENTITE_CRYSTAL = register("silentite_crystal", Item::new, new Item.Properties());

    // Processed
    public static final Item ECHOCITE_DUST  = register("echocite_dust", Item::new, new Item.Properties());
    public static final Item ECHO_INGOT     = register("echo_ingot", Item::new, new Item.Properties());
    public static final Item DULL_INGOT     = register("dull_ingot", Item::new, new Item.Properties());
    public static final Item RESONANT_SLAG  = register("resonant_slag", Item::new, new Item.Properties());
    public static final Item DRUM_CORE      = register("drum_core", Item::new, new Item.Properties());
    public static final Item ECHO_DUST      = register("echo_dust", Item::new, new Item.Properties());

    // Phase II — inert-gas Seed (progression catalyst) + transmutation chain
    public static final Item OCTAVE_SEED    = register("octave_seed", Item::new, new Item.Properties());
    public static final Item RADIANT_DUST   = register("radiant_dust", Item::new, new Item.Properties());
    public static final Item RADIANT_INGOT  = register("radiant_ingot", Item::new, new Item.Properties());

    // The Verdant Octave — charged-Light currency (the EMC "coins"). Each tone is
    // Light wound one octave higher; the ladder doubles as the Bound-Light denomination
    // scale (×4 per octave). Withdrawn from a Transmutation Table's banked Bound Light.
    public static final Item LIGHT_MOTE    = register("light_mote", Item::new, new Item.Properties());
    public static final Item TONIC_MOTE    = register("tonic_mote", Item::new, new Item.Properties());
    public static final Item MEDIANT_MOTE  = register("mediant_mote", Item::new, new Item.Properties());
    public static final Item DOMINANT_MOTE = register("dominant_mote", Item::new, new Item.Properties());
    public static final Item HARMONIC_MOTE = register("harmonic_mote", Item::new, new Item.Properties());

    /** The Mote ladder in ascending octave order (denominations of Bound Light). */
    public static final Item[] MOTES = { LIGHT_MOTE, TONIC_MOTE, MEDIANT_MOTE, DOMINANT_MOTE, HARMONIC_MOTE };
    /** Light Value of each Mote (×4 per octave): Light=64 … Harmonic=16384. */
    public static final long[] MOTE_VALUES = { 64L, 256L, 1024L, 4096L, 16384L };

    // The portable transmutation terminal (per-player Bound-Light account).
    public static final Item TRANSMUTATION_TABLET = register("transmutation_tablet",
            com.echoes.item.TransmutationTabletItem::new, new Item.Properties().maxCount(1));

    // Octave Stars — portable Bound-Light batteries, six tiers (×4 capacity per tier).
    public static final Item OCTAVE_STAR_1 = register("octave_star_1",
            s -> new com.echoes.item.OctaveStarItem(1, 100_000L, s), new Item.Properties().maxCount(1));
    public static final Item OCTAVE_STAR_2 = register("octave_star_2",
            s -> new com.echoes.item.OctaveStarItem(2, 400_000L, s), new Item.Properties().maxCount(1));
    public static final Item OCTAVE_STAR_3 = register("octave_star_3",
            s -> new com.echoes.item.OctaveStarItem(3, 1_600_000L, s), new Item.Properties().maxCount(1));
    public static final Item OCTAVE_STAR_4 = register("octave_star_4",
            s -> new com.echoes.item.OctaveStarItem(4, 6_400_000L, s), new Item.Properties().maxCount(1));
    public static final Item OCTAVE_STAR_5 = register("octave_star_5",
            s -> new com.echoes.item.OctaveStarItem(5, 25_600_000L, s), new Item.Properties().maxCount(1));
    public static final Item OCTAVE_STAR_6 = register("octave_star_6",
            s -> new com.echoes.item.OctaveStarItem(6, 102_400_000L, s), new Item.Properties().maxCount(1));
    public static final Item[] OCTAVE_STARS = {
            OCTAVE_STAR_1, OCTAVE_STAR_2, OCTAVE_STAR_3, OCTAVE_STAR_4, OCTAVE_STAR_5, OCTAVE_STAR_6 };

    // Wireless transport tools
    public static final Item FREQUENCY_TUNER = register("wave_tuner",
            FrequencyTunerItem::new, new Item.Properties().maxCount(1));
    public static final Item CHANNEL_ATLAS   = register("wave_atlas",
            ChannelAtlasItem::new, new Item.Properties().maxCount(1));
    public static final Item RESONANCE_METER = register("light_meter",
            ResonanceMeterItem::new, new Item.Properties().maxCount(1));
    public static final Item RESONANCE_THRUSTERS = register("resonant_thrusters",
            ResonanceThrustersItem::new, new Item.Properties().maxCount(1));

    // Resonant tools — deliberately strong: faster than netherite, tougher, highly
    // enchantable, mines anything. "Energy is carried, not transmitted" — W. Russell.
    public static final TagKey<Item> RESONANT_REPAIR =
            TagKey.of(Registries.ITEM, Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "resonant_repair"));
    public static final ToolMaterial ECHO_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4000, 12.0f, 5.0f, 22, RESONANT_REPAIR);

    public static final Item RESONANT_PICKAXE = register("resonant_pickaxe",
            ResonantToolItem::new, new Item.Properties().pickaxe(ECHO_MATERIAL, 1.5f, -2.6f));
    public static final Item RESONANT_AXE = register("resonant_axe",
            ResonantToolItem::new, new Item.Properties().axe(ECHO_MATERIAL, 6.0f, -3.0f));
    public static final Item RESONANT_SHOVEL = register("resonant_shovel",
            ResonantToolItem::new, new Item.Properties().shovel(ECHO_MATERIAL, 1.5f, -3.0f));
    public static final Item RESONANT_SWORD = register("resonant_sword",
            ResonantToolItem::new, new Item.Properties().sword(ECHO_MATERIAL, 4.0f, -2.2f));
    public static final Item RESONANT_HOE = register("resonant_hoe",
            ResonantToolItem::new, new Item.Properties().hoe(ECHO_MATERIAL, -2.0f, 0.0f));

    public static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        Identifier id = Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.of(Registries.ITEM, id);
        Item item = factory.apply(settings.registryKey(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void register() {
        EchoesMod.LOGGER.info("Registering items for {}", EchoesMod.MOD_ID);
    }
}
