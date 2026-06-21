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
import com.echoes.block.TransmutationTableBlock;
import com.echoes.block.ResonantAmplifierBlock;
import com.echoes.block.ResonantChestBlock;
import com.echoes.block.ResonantRelayBlock;
import com.echoes.block.ResonantSplitterBlock;
import com.echoes.block.ResonatorBlock;
import com.echoes.block.GreaterAccumulatorBlock;
import com.echoes.block.VerdantLoamBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.function.Function;

public final class ModBlocks {
    private ModBlocks() {}

    // Ores
    public static final Block ECHOCITE_ORE = register("echocite_ore",
            Block::new, BlockBehaviour.Properties.of().strength(3.0f).requiresCorrectToolForDrops().sound(SoundType.STONE));
    public static final Block DEEPSLATE_ECHOCITE_ORE = register("deepslate_echocite_ore",
            Block::new, BlockBehaviour.Properties.of().strength(4.5f).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE));
    public static final Block DRUMSTONE_ORE = register("drumstone_ore",
            Block::new, BlockBehaviour.Properties.of().strength(4.5f).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE));
    public static final Block SILENTITE_ORE = register("silentite_ore",
            Block::new, BlockBehaviour.Properties.of().strength(5.0f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST_BLOCK));

    // Machines / grid
    public static final Block RESONATOR = register("resonant_coil",
            ResonatorBlock::new, BlockBehaviour.Properties.of().strength(3.5f).requiresCorrectToolForDrops().noOcclusion());
    public static final Block TUNING_CONDUIT = register("wave_conduit",
            ConduitBlock::new, BlockBehaviour.Properties.of().strength(1.5f).noOcclusion());
    public static final Block DENSE_CONDUIT = register("dense_wave_conduit",
            DenseConduitBlock::new, BlockBehaviour.Properties.of().strength(2.0f).requiresCorrectToolForDrops().noOcclusion());
    public static final Block RESONANCE_CAPACITOR = register("resonance_cell",
            ResonanceCapacitorBlock::new, BlockBehaviour.Properties.of().strength(3.0f).requiresCorrectToolForDrops().noOcclusion());
    public static final Block STILLNESS_CORE = register("stillness_core",
            StillnessCoreBlock::new, BlockBehaviour.Properties.of().strength(4.0f).requiresCorrectToolForDrops().noOcclusion());
    public static final Block RADIATOR = register("growth_radiator",
            RadiatorBlock::new, BlockBehaviour.Properties.of().strength(3.0f).requiresCorrectToolForDrops().noOcclusion()
                    .lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 12 : 2));
    public static final Block WARMTH_RADIATOR = register("warmth_radiator",
            WarmthRadiatorBlock::new, BlockBehaviour.Properties.of().strength(3.0f).requiresCorrectToolForDrops().noOcclusion()
                    .lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 14 : 2));
    public static final Block POLARITY_FIELD = register("polarity_field",
            PolarityFieldBlock::new, BlockBehaviour.Properties.of().strength(3.0f).requiresCorrectToolForDrops().noOcclusion()
                    .lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 8 : 2));
    public static final Block BALANCER = register("balancer",
            BalancerBlock::new, BlockBehaviour.Properties.of().strength(3.0f).requiresCorrectToolForDrops().noOcclusion());
    public static final Block CRUSHER = register("compressor",
            CrusherBlock::new, BlockBehaviour.Properties.of().strength(3.5f).requiresCorrectToolForDrops());
    public static final Block ATTUNEMENT_FURNACE = register("transmuter",
            AttunementFurnaceBlock::new, BlockBehaviour.Properties.of().strength(3.5f).requiresCorrectToolForDrops());
    public static final Block RESONANT_RELAY = register("wave_relay",
            ResonantRelayBlock::new, BlockBehaviour.Properties.of().strength(2.0f).noOcclusion());

    // Wireless transport family
    public static final Block RESONANT_AMPLIFIER = register("wave_amplifier",
            ResonantAmplifierBlock::new, BlockBehaviour.Properties.of().strength(2.0f).noOcclusion());
    public static final Block HARMONIC_FILTER = register("wave_filter",
            HarmonicFilterBlock::new, BlockBehaviour.Properties.of().strength(2.0f).noOcclusion());
    public static final Block RESONANT_SPLITTER = register("wave_splitter",
            ResonantSplitterBlock::new, BlockBehaviour.Properties.of().strength(2.0f).noOcclusion());
    public static final Block ECHO_REPEATER = register("wave_repeater",
            EchoRepeaterBlock::new, BlockBehaviour.Properties.of().strength(2.5f).requiresCorrectToolForDrops().noOcclusion());
    public static final Block CONDUIT_COUPLER = register("wave_coupler",
            ConduitCouplerBlock::new, BlockBehaviour.Properties.of().strength(2.5f).requiresCorrectToolForDrops().noOcclusion());
    public static final Block RESONANT_CHEST = register("wave_chest",
            ResonantChestBlock::new, BlockBehaviour.Properties.of().strength(2.5f).noOcclusion());
    public static final Block NOTE_RELAY = register("signal_relay",
            NoteRelayBlock::new, BlockBehaviour.Properties.of().strength(2.0f).noOcclusion());

    // ============================================================ Phase II — The Octave Grove
    // Wood / block set types for Lumewood (custom tree). Names are plain strings (no signs).
    public static final BlockSetType LUMEWOOD_SET = new BlockSetType("echoes_lumewood");
    public static final WoodType LUMEWOOD_WOOD_TYPE = new WoodType("echoes_lumewood", LUMEWOOD_SET);

    /** Simple tree: a single regular variant points at the Lumewood configured feature. */
    public static final TreeGrower LUMEWOOD_TREE_GEN = new TreeGrower(
            "echoes_lumewood", 0.0f,
            Optional.empty(), Optional.empty(),
            Optional.of(ModWorldGen.LUMEWOOD_TREE), Optional.empty(),
            Optional.empty(), Optional.empty());

    private static BlockBehaviour.Properties wood() {
        return BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD);
    }

    // Lumewood building set
    public static final Block LUMEWOOD_LOG = register("lumewood_log",
            RotatedPillarBlock::new, BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.WOOD)
                    .lightLevel(s -> 4));
    public static final Block LUMEWOOD_WOOD = register("lumewood_wood",
            RotatedPillarBlock::new, BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.WOOD)
                    .lightLevel(s -> 4));
    public static final Block LUMEWOOD_PLANKS = register("lumewood_planks", Block::new, wood());
    public static final Block LUMEWOOD_STAIRS = register("lumewood_stairs",
            s -> new StairBlock(LUMEWOOD_PLANKS.defaultBlockState(), s) {}, wood());
    public static final Block LUMEWOOD_SLAB = register("lumewood_slab", SlabBlock::new, wood());
    public static final Block LUMEWOOD_FENCE = register("lumewood_fence", FenceBlock::new, wood());
    public static final Block LUMEWOOD_FENCE_GATE = register("lumewood_fence_gate",
            s -> new FenceGateBlock(LUMEWOOD_WOOD_TYPE, s), wood());
    public static final Block LUMEWOOD_TRAPDOOR = register("lumewood_trapdoor",
            s -> new TrapDoorBlock(LUMEWOOD_SET, s) {},
            BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.WOOD).noOcclusion());
    public static final Block LUMEWOOD_LEAVES = register("lumewood_leaves",
            LeavesBlock::new, BlockBehaviour.Properties.of().strength(0.2f).randomTicks()
                    .sound(SoundType.GRASS).noOcclusion().lightLevel(s -> 6));
    public static final Block LUMEWOOD_SAPLING = register("lumewood_sapling",
            s -> new SaplingBlock(LUMEWOOD_TREE_GEN, s) {},
            BlockBehaviour.Properties.of().noCollision().randomTicks().instabreak()
                    .sound(SoundType.GRASS).lightLevel(s -> 4));

    // Garden
    public static final Block LUMEBLOOM = register("lumebloom",
            s -> new FlowerBlock(MobEffects.GLOWING, 5.0f, s),
            BlockBehaviour.Properties.of().noCollision().instabreak()
                    .sound(SoundType.GRASS).lightLevel(s -> 7));
    public static final Block LUME_LANTERN = register("lume_lantern",
            Block::new, BlockBehaviour.Properties.of().strength(0.4f).sound(SoundType.GLASS)
                    .noOcclusion().lightLevel(s -> 15));
    public static final Block VERDANT_LOAM = register("verdant_loam",
            VerdantLoamBlock::new, BlockBehaviour.Properties.of().strength(0.6f)
                    .sound(SoundType.ROOTED_DIRT));

    // Stone building materials
    public static final Block ECHOCITE_BRICKS = register("echocite_bricks",
            Block::new, BlockBehaviour.Properties.of().strength(2.0f, 6.0f).requiresCorrectToolForDrops()
                    .sound(SoundType.STONE).lightLevel(s -> 3));
    public static final Block ECHOCITE_BRICK_STAIRS = register("echocite_brick_stairs",
            s -> new StairBlock(ECHOCITE_BRICKS.defaultBlockState(), s) {},
            BlockBehaviour.Properties.of().strength(2.0f, 6.0f).requiresCorrectToolForDrops()
                    .sound(SoundType.STONE).lightLevel(s -> 3));
    public static final Block ECHOCITE_BRICK_SLAB = register("echocite_brick_slab",
            SlabBlock::new, BlockBehaviour.Properties.of().strength(2.0f, 6.0f).requiresCorrectToolForDrops()
                    .sound(SoundType.STONE).lightLevel(s -> 3));

    // Octave II — Greater Resonance Cell (tiered storage)
    public static final Block GREATER_ACCUMULATOR = register("greater_resonance_cell",
            GreaterAccumulatorBlock::new, BlockBehaviour.Properties.of().strength(3.5f)
                    .requiresCorrectToolForDrops().noOcclusion());

    // Octave II — higher-octave generation & throughput (Radiant-tier)
    public static final Block OCTAVE_COIL = register("octave_coil",
            OctaveCoilBlock::new, BlockBehaviour.Properties.of().strength(4.0f).requiresCorrectToolForDrops()
                    .noOcclusion().lightLevel(s -> 7));
    public static final Block OCTAVE_CONDUIT = register("octave_conduit",
            OctaveConduitBlock::new, BlockBehaviour.Properties.of().strength(2.5f).requiresCorrectToolForDrops()
                    .noOcclusion().lightLevel(s -> 4));

    // The Verdant Octave — the transmutation economy (EMC = Bound Light)
    public static final Block TRANSMUTATION_TABLE = register("transmutation_table",
            TransmutationTableBlock::new, BlockBehaviour.Properties.of().strength(3.5f)
                    .requiresCorrectToolForDrops().noOcclusion().lightLevel(s -> 6));

    // Phase VII — Storm Caller (lightning generation)
    public static final Block STORM_CALLER = register("storm_caller",
            StormCallerBlock::new, BlockBehaviour.Properties.of().strength(4.5f).requiresCorrectToolForDrops()
                    .noOcclusion().lightLevel(s -> 6));

    public static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        Identifier id = Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, name);
        ResourceKey<Block> blockKey = ResourceKey.of(Registries.BLOCK, id);
        Block block = factory.apply(settings.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        ResourceKey<Item> itemKey = ResourceKey.of(Registries.ITEM, id);
        Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BlockItem(block, new Item.Properties().setId(itemKey).useBlockPrefixedTranslationKey()));
        return block;
    }

    public static void register() {
        EchoesMod.LOGGER.info("Registering blocks for {}", EchoesMod.MOD_ID);
    }
}
