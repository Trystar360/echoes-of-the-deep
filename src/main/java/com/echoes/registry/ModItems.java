package com.echoes.registry;

import com.echoes.EchoesMod;
import com.echoes.item.ChannelAtlasItem;
import com.echoes.item.FrequencyTunerItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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

    // Wireless transport tools
    public static final Item FREQUENCY_TUNER = register("frequency_tuner",
            FrequencyTunerItem::new, new Item.Settings().maxCount(1));
    public static final Item CHANNEL_ATLAS   = register("channel_atlas",
            ChannelAtlasItem::new, new Item.Settings().maxCount(1));

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
