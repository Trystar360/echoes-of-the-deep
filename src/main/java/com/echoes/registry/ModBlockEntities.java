package com.echoes.registry;

import com.echoes.EchoesMod;
import com.echoes.block.entity.ConduitBlockEntity;
import com.echoes.block.entity.CrusherBlockEntity;
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
    public static final BlockEntityType<CrusherBlockEntity> CRUSHER =
            register("crusher", CrusherBlockEntity::new, ModBlocks.CRUSHER);

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
