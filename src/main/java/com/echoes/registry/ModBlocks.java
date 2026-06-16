package com.echoes.registry;

import com.echoes.EchoesMod;
import com.echoes.block.ConduitBlock;
import com.echoes.block.CrusherBlock;
import com.echoes.block.ResonantRelayBlock;
import com.echoes.block.ResonatorBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public final class ModBlocks {
    private ModBlocks() {}

    // Ores
    public static final Block ECHOCITE_ORE = register("echocite_ore",
            Block::new, AbstractBlock.Settings.create().strength(3.0f).requiresTool().sounds(BlockSoundGroup.STONE));
    public static final Block DEEPSLATE_ECHOCITE_ORE = register("deepslate_echocite_ore",
            Block::new, AbstractBlock.Settings.create().strength(4.5f).requiresTool().sounds(BlockSoundGroup.DEEPSLATE));
    public static final Block DRUMSTONE_ORE = register("drumstone_ore",
            Block::new, AbstractBlock.Settings.create().strength(4.5f).requiresTool().sounds(BlockSoundGroup.DEEPSLATE));
    public static final Block SILENTITE_ORE = register("silentite_ore",
            Block::new, AbstractBlock.Settings.create().strength(5.0f).requiresTool().sounds(BlockSoundGroup.AMETHYST_BLOCK));

    // Machines / grid
    public static final Block RESONATOR = register("resonator",
            ResonatorBlock::new, AbstractBlock.Settings.create().strength(3.5f).requiresTool().nonOpaque());
    public static final Block TUNING_CONDUIT = register("tuning_conduit",
            ConduitBlock::new, AbstractBlock.Settings.create().strength(1.5f).nonOpaque());
    public static final Block CRUSHER = register("crusher",
            CrusherBlock::new, AbstractBlock.Settings.create().strength(3.5f).requiresTool());
    public static final Block RESONANT_RELAY = register("resonant_relay",
            ResonantRelayBlock::new, AbstractBlock.Settings.create().strength(2.0f).nonOpaque());

    public static Block register(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        Identifier id = Identifier.of(EchoesMod.MOD_ID, name);
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
        Block block = factory.apply(settings.registryKey(blockKey));
        Registry.register(Registries.BLOCK, blockKey, block);

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
        Registry.register(Registries.ITEM, itemKey,
                new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey()));
        return block;
    }

    public static void register() {
        EchoesMod.LOGGER.info("Registering blocks for {}", EchoesMod.MOD_ID);
    }
}
