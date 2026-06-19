package com.echoes.registry;

import com.echoes.EchoesMod;
import com.echoes.block.AttunementFurnaceBlock;
import com.echoes.block.BalancerBlock;
import com.echoes.block.ConduitBlock;
import com.echoes.block.ConduitCouplerBlock;
import com.echoes.block.CrusherBlock;
import com.echoes.block.DenseConduitBlock;
import com.echoes.block.PolarityFieldBlock;
import com.echoes.block.RadiatorBlock;
import com.echoes.block.ResonanceCapacitorBlock;
import com.echoes.block.StillnessCoreBlock;
import com.echoes.block.WarmthRadiatorBlock;
import com.echoes.block.EchoRepeaterBlock;
import com.echoes.block.HarmonicFilterBlock;
import com.echoes.block.NoteRelayBlock;
import com.echoes.block.OctaveCoilBlock;
import com.echoes.block.OctaveConduitBlock;
import com.echoes.block.StormCallerBlock;
import com.echoes.block.ResonantAmplifierBlock;
import com.echoes.block.ResonantChestBlock;
import com.echoes.block.ResonantRelayBlock;
import com.echoes.block.ResonantSplitterBlock;
import com.echoes.block.ResonatorBlock;
import com.echoes.block.GreaterAccumulatorBlock;
import com.echoes.block.VerdantLoamBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.WoodType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import java.util.Optional;
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
    public static final Block DENSE_CONDUIT = register("dense_conduit",
            DenseConduitBlock::new, AbstractBlock.Settings.create().strength(2.0f).requiresTool().nonOpaque());
    public static final Block RESONANCE_CAPACITOR = register("resonance_capacitor",
            ResonanceCapacitorBlock::new, AbstractBlock.Settings.create().strength(3.0f).requiresTool().nonOpaque());
    public static final Block STILLNESS_CORE = register("stillness_core",
            StillnessCoreBlock::new, AbstractBlock.Settings.create().strength(4.0f).requiresTool().nonOpaque());
    public static final Block RADIATOR = register("radiator",
            RadiatorBlock::new, AbstractBlock.Settings.create().strength(3.0f).requiresTool().nonOpaque()
                    .luminance(s -> s.get(Properties.LIT) ? 12 : 2));
    public static final Block WARMTH_RADIATOR = register("warmth_radiator",
            WarmthRadiatorBlock::new, AbstractBlock.Settings.create().strength(3.0f).requiresTool().nonOpaque()
                    .luminance(s -> s.get(Properties.LIT) ? 14 : 2));
    public static final Block POLARITY_FIELD = register("polarity_field",
            PolarityFieldBlock::new, AbstractBlock.Settings.create().strength(3.0f).requiresTool().nonOpaque()
                    .luminance(s -> s.get(Properties.LIT) ? 8 : 2));
    public static final Block BALANCER = register("balancer",
            BalancerBlock::new, AbstractBlock.Settings.create().strength(3.0f).requiresTool().nonOpaque());
    public static final Block CRUSHER = register("crusher",
            CrusherBlock::new, AbstractBlock.Settings.create().strength(3.5f).requiresTool());
    public static final Block ATTUNEMENT_FURNACE = register("attunement_furnace",
            AttunementFurnaceBlock::new, AbstractBlock.Settings.create().strength(3.5f).requiresTool());
    public static final Block RESONANT_RELAY = register("resonant_relay",
            ResonantRelayBlock::new, AbstractBlock.Settings.create().strength(2.0f).nonOpaque());

    // Wireless transport family
    public static final Block RESONANT_AMPLIFIER = register("resonant_amplifier",
            ResonantAmplifierBlock::new, AbstractBlock.Settings.create().strength(2.0f).nonOpaque());
    public static final Block HARMONIC_FILTER = register("harmonic_filter",
            HarmonicFilterBlock::new, AbstractBlock.Settings.create().strength(2.0f).nonOpaque());
    public static final Block RESONANT_SPLITTER = register("resonant_splitter",
            ResonantSplitterBlock::new, AbstractBlock.Settings.create().strength(2.0f).nonOpaque());
    public static final Block ECHO_REPEATER = register("echo_repeater",
            EchoRepeaterBlock::new, AbstractBlock.Settings.create().strength(2.5f).requiresTool().nonOpaque());
    public static final Block CONDUIT_COUPLER = register("conduit_coupler",
            ConduitCouplerBlock::new, AbstractBlock.Settings.create().strength(2.5f).requiresTool().nonOpaque());
    public static final Block RESONANT_CHEST = register("resonant_chest",
            ResonantChestBlock::new, AbstractBlock.Settings.create().strength(2.5f).nonOpaque());
    public static final Block NOTE_RELAY = register("note_relay",
            NoteRelayBlock::new, AbstractBlock.Settings.create().strength(2.0f).nonOpaque());

    // ============================================================ Phase II — The Octave Grove
    // Wood / block set types for Lumewood (custom tree). Names are plain strings (no signs).
    public static final BlockSetType LUMEWOOD_SET = new BlockSetType("echoes_lumewood");
    public static final WoodType LUMEWOOD_WOOD_TYPE = new WoodType("echoes_lumewood", LUMEWOOD_SET);

    /** Simple tree: a single regular variant points at the Lumewood configured feature. */
    public static final SaplingGenerator LUMEWOOD_TREE_GEN = new SaplingGenerator(
            "echoes_lumewood", 0.0f,
            Optional.empty(), Optional.empty(),
            Optional.of(ModWorldGen.LUMEWOOD_TREE), Optional.empty(),
            Optional.empty(), Optional.empty());

    private static AbstractBlock.Settings wood() {
        return AbstractBlock.Settings.create().strength(2.0f, 3.0f).sounds(BlockSoundGroup.WOOD).burnable();
    }

    // Lumewood building set
    public static final Block LUMEWOOD_LOG = register("lumewood_log",
            PillarBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD)
                    .burnable().luminance(s -> 4));
    public static final Block LUMEWOOD_WOOD = register("lumewood_wood",
            PillarBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD)
                    .burnable().luminance(s -> 4));
    public static final Block LUMEWOOD_PLANKS = register("lumewood_planks", Block::new, wood());
    public static final Block LUMEWOOD_STAIRS = register("lumewood_stairs",
            s -> new StairsBlock(LUMEWOOD_PLANKS.getDefaultState(), s) {}, wood());
    public static final Block LUMEWOOD_SLAB = register("lumewood_slab", SlabBlock::new, wood());
    public static final Block LUMEWOOD_FENCE = register("lumewood_fence", FenceBlock::new, wood());
    public static final Block LUMEWOOD_FENCE_GATE = register("lumewood_fence_gate",
            s -> new FenceGateBlock(LUMEWOOD_WOOD_TYPE, s), wood());
    public static final Block LUMEWOOD_TRAPDOOR = register("lumewood_trapdoor",
            s -> new TrapdoorBlock(LUMEWOOD_SET, s) {},
            AbstractBlock.Settings.create().strength(3.0f).sounds(BlockSoundGroup.WOOD).nonOpaque());
    public static final Block LUMEWOOD_LEAVES = register("lumewood_leaves",
            LeavesBlock::new, AbstractBlock.Settings.create().strength(0.2f).ticksRandomly()
                    .sounds(BlockSoundGroup.GRASS).nonOpaque().burnable().luminance(s -> 6));
    public static final Block LUMEWOOD_SAPLING = register("lumewood_sapling",
            s -> new SaplingBlock(LUMEWOOD_TREE_GEN, s) {},
            AbstractBlock.Settings.create().noCollision().ticksRandomly().breakInstantly()
                    .sounds(BlockSoundGroup.GRASS).luminance(s -> 4));

    // Garden
    public static final Block LUMEBLOOM = register("lumebloom",
            s -> new FlowerBlock(StatusEffects.GLOWING, 5.0f, s),
            AbstractBlock.Settings.create().noCollision().breakInstantly()
                    .sounds(BlockSoundGroup.GRASS).luminance(s -> 7));
    public static final Block LUME_LANTERN = register("lume_lantern",
            Block::new, AbstractBlock.Settings.create().strength(0.4f).sounds(BlockSoundGroup.GLASS)
                    .nonOpaque().luminance(s -> 15));
    public static final Block VERDANT_LOAM = register("verdant_loam",
            VerdantLoamBlock::new, AbstractBlock.Settings.create().strength(0.6f)
                    .sounds(BlockSoundGroup.ROOTED_DIRT));

    // Stone building materials
    public static final Block ECHOCITE_BRICKS = register("echocite_bricks",
            Block::new, AbstractBlock.Settings.create().strength(2.0f, 6.0f).requiresTool()
                    .sounds(BlockSoundGroup.STONE).luminance(s -> 3));
    public static final Block ECHOCITE_BRICK_STAIRS = register("echocite_brick_stairs",
            s -> new StairsBlock(ECHOCITE_BRICKS.getDefaultState(), s) {},
            AbstractBlock.Settings.create().strength(2.0f, 6.0f).requiresTool()
                    .sounds(BlockSoundGroup.STONE).luminance(s -> 3));
    public static final Block ECHOCITE_BRICK_SLAB = register("echocite_brick_slab",
            SlabBlock::new, AbstractBlock.Settings.create().strength(2.0f, 6.0f).requiresTool()
                    .sounds(BlockSoundGroup.STONE).luminance(s -> 3));

    // Octave II — Greater Accumulator (tiered storage)
    public static final Block GREATER_ACCUMULATOR = register("greater_accumulator",
            GreaterAccumulatorBlock::new, AbstractBlock.Settings.create().strength(3.5f)
                    .requiresTool().nonOpaque());

    // Octave II — higher-octave generation & throughput (Radiant-tier)
    public static final Block OCTAVE_COIL = register("octave_coil",
            OctaveCoilBlock::new, AbstractBlock.Settings.create().strength(4.0f).requiresTool()
                    .nonOpaque().luminance(s -> 7));
    public static final Block OCTAVE_CONDUIT = register("octave_conduit",
            OctaveConduitBlock::new, AbstractBlock.Settings.create().strength(2.5f).requiresTool()
                    .nonOpaque().luminance(s -> 4));

    // Phase VII — Storm Caller (lightning generation)
    public static final Block STORM_CALLER = register("storm_caller",
            StormCallerBlock::new, AbstractBlock.Settings.create().strength(4.5f).requiresTool()
                    .nonOpaque().luminance(s -> 6));

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
