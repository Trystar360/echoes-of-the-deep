package com.echoes.registry;

import com.echoes.EchoesMod;
import com.echoes.block.entity.AttunementFurnaceBlockEntity;
import com.echoes.block.entity.ConduitBlockEntity;
import com.echoes.block.entity.ConduitCouplerBlockEntity;
import com.echoes.block.entity.CrusherBlockEntity;
import com.echoes.block.entity.DenseConduitBlockEntity;
import com.echoes.block.entity.ResonanceCapacitorBlockEntity;
import com.echoes.block.entity.RadiatorBlockEntity;
import com.echoes.block.entity.StillnessCoreBlockEntity;
import com.echoes.block.entity.EchoRepeaterBlockEntity;
import com.echoes.block.entity.HarmonicFilterBlockEntity;
import com.echoes.block.entity.NoteRelayBlockEntity;
import com.echoes.block.entity.ResonantAmplifierBlockEntity;
import com.echoes.block.entity.ResonantChestBlockEntity;
import com.echoes.block.entity.ResonantRelayBlockEntity;
import com.echoes.block.entity.ResonantSplitterBlockEntity;
import com.echoes.block.entity.ResonatorBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final BlockEntityType<ResonatorBlockEntity> RESONATOR =
            register("resonator", ResonatorBlockEntity::new, ModBlocks.RESONATOR);
    public static final BlockEntityType<ConduitBlockEntity> CONDUIT =
            register("tuning_conduit", ConduitBlockEntity::new, ModBlocks.TUNING_CONDUIT);
    public static final BlockEntityType<DenseConduitBlockEntity> DENSE_CONDUIT =
            register("dense_conduit", DenseConduitBlockEntity::new, ModBlocks.DENSE_CONDUIT);
    public static final BlockEntityType<ResonanceCapacitorBlockEntity> RESONANCE_CAPACITOR =
            register("resonance_capacitor", ResonanceCapacitorBlockEntity::new, ModBlocks.RESONANCE_CAPACITOR);
    public static final BlockEntityType<StillnessCoreBlockEntity> STILLNESS_CORE =
            register("stillness_core", StillnessCoreBlockEntity::new, ModBlocks.STILLNESS_CORE);
    public static final BlockEntityType<RadiatorBlockEntity> RADIATOR =
            register("radiator", RadiatorBlockEntity::new, ModBlocks.RADIATOR);
    public static final BlockEntityType<CrusherBlockEntity> CRUSHER =
            register("crusher", CrusherBlockEntity::new, ModBlocks.CRUSHER);
    public static final BlockEntityType<AttunementFurnaceBlockEntity> ATTUNEMENT_FURNACE =
            register("attunement_furnace", AttunementFurnaceBlockEntity::new, ModBlocks.ATTUNEMENT_FURNACE);
    public static final BlockEntityType<ResonantRelayBlockEntity> RESONANT_RELAY =
            register("resonant_relay", ResonantRelayBlockEntity::new, ModBlocks.RESONANT_RELAY);
    public static final BlockEntityType<ResonantAmplifierBlockEntity> RESONANT_AMPLIFIER =
            register("resonant_amplifier", ResonantAmplifierBlockEntity::new, ModBlocks.RESONANT_AMPLIFIER);
    public static final BlockEntityType<HarmonicFilterBlockEntity> HARMONIC_FILTER =
            register("harmonic_filter", HarmonicFilterBlockEntity::new, ModBlocks.HARMONIC_FILTER);
    public static final BlockEntityType<ResonantSplitterBlockEntity> RESONANT_SPLITTER =
            register("resonant_splitter", ResonantSplitterBlockEntity::new, ModBlocks.RESONANT_SPLITTER);
    public static final BlockEntityType<EchoRepeaterBlockEntity> ECHO_REPEATER =
            register("echo_repeater", EchoRepeaterBlockEntity::new, ModBlocks.ECHO_REPEATER);
    public static final BlockEntityType<ConduitCouplerBlockEntity> CONDUIT_COUPLER =
            register("conduit_coupler", ConduitCouplerBlockEntity::new, ModBlocks.CONDUIT_COUPLER);
    public static final BlockEntityType<ResonantChestBlockEntity> RESONANT_CHEST =
            register("resonant_chest", ResonantChestBlockEntity::new, ModBlocks.RESONANT_CHEST);
    public static final BlockEntityType<NoteRelayBlockEntity> NOTE_RELAY =
            register("note_relay", NoteRelayBlockEntity::new, ModBlocks.NOTE_RELAY);

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name, FabricBlockEntityTypeBuilder.Factory<T> factory, Block... blocks) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(EchoesMod.MOD_ID, name),
                FabricBlockEntityTypeBuilder.create(factory, blocks).build());
    }

    public static void register() {
        EchoesMod.LOGGER.info("Registering block entities for {}", EchoesMod.MOD_ID);
    }
}
