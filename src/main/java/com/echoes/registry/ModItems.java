package com.echoes.registry;

import com.echoes.EchoesMod;
import com.echoes.item.ChannelAtlasItem;
import com.echoes.item.FrequencyTunerItem;
import com.echoes.item.ResonanceMeterItem;
import com.echoes.item.ResonanceThrustersItem;
import com.echoes.item.ResonantAxeItem;
import com.echoes.item.ResonantHoeItem;
import com.echoes.item.ResonantPickaxeItem;
import com.echoes.item.ResonantShovelItem;
import com.echoes.item.ResonantSwordItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public final class ModItems {
    private ModItems() {}

    // Raw drops
    public static final Item RAW_ECHOCITE   = register("raw_echocite", Item::new, new Item.Settings());
    public static final Item DRUMSTONE_SHARD = register("drumstone_shard", Item::new, new Item.Settings());
    public static final Item SILENTITE_CRYSTAL = register("silentite_crystal", Item::new, new Item.Settings());

    // Processed
    public static final Item ECHOCITE_DUST  = register("echocite_dust", Item::new, new Item.Settings());
    public static final Item ECHO_INGOT     = register("echo_ingot", Item::new, new Item.Settings());
    public static final Item DULL_INGOT     = register("dull_ingot", Item::new, new Item.Settings());
    public static final Item RESONANT_SLAG  = register("resonant_slag", Item::new, new Item.Settings());
    public static final Item DRUM_CORE      = register("drum_core", Item::new, new Item.Settings());
    public static final Item ECHO_DUST      = register("echo_dust", Item::new, new Item.Settings());

    // Phase II — inert-gas Seed (progression catalyst) + transmutation chain
    public static final Item OCTAVE_SEED    = register("octave_seed", Item::new, new Item.Settings());
    public static final Item RADIANT_DUST   = register("radiant_dust", Item::new, new Item.Settings());
    public static final Item RADIANT_INGOT  = register("radiant_ingot", Item::new, new Item.Settings());

    // Wireless transport tools
    public static final Item FREQUENCY_TUNER = register("wave_tuner",
            FrequencyTunerItem::new, new Item.Settings().maxCount(1));
    public static final Item CHANNEL_ATLAS   = register("wave_atlas",
            ChannelAtlasItem::new, new Item.Settings().maxCount(1));
    public static final Item RESONANCE_METER = register("light_meter",
            ResonanceMeterItem::new, new Item.Settings().maxCount(1));
    public static final Item RESONANCE_THRUSTERS = register("resonant_thrusters",
            ResonanceThrustersItem::new, new Item.Settings().maxCount(1));

    // Resonant tools — deliberately strong: faster than netherite, tougher, highly
    // enchantable, mines anything. "Energy is carried, not transmitted" — W. Russell.
    public static final TagKey<Item> RESONANT_REPAIR =
            TagKey.of(RegistryKeys.ITEM, Identifier.of(EchoesMod.MOD_ID, "resonant_repair"));
    public static final ToolMaterial ECHO_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4000, 12.0f, 5.0f, 22, RESONANT_REPAIR);

    public static final Item RESONANT_PICKAXE = register("resonant_pickaxe",
            s -> new ResonantPickaxeItem(ECHO_MATERIAL, 1.5f, -2.6f, s), new Item.Settings());
    public static final Item RESONANT_AXE = register("resonant_axe",
            s -> new ResonantAxeItem(ECHO_MATERIAL, 6.0f, -3.0f, s), new Item.Settings());
    public static final Item RESONANT_SHOVEL = register("resonant_shovel",
            s -> new ResonantShovelItem(ECHO_MATERIAL, 1.5f, -3.0f, s), new Item.Settings());
    public static final Item RESONANT_SWORD = register("resonant_sword",
            s -> new ResonantSwordItem(ECHO_MATERIAL, 4.0f, -2.2f, s), new Item.Settings());
    public static final Item RESONANT_HOE = register("resonant_hoe",
            s -> new ResonantHoeItem(ECHO_MATERIAL, -2.0f, 0.0f, s), new Item.Settings());

    public static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        Identifier id = Identifier.of(EchoesMod.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item item = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void register() {
        EchoesMod.LOGGER.info("Registering items for {}", EchoesMod.MOD_ID);
    }
}
