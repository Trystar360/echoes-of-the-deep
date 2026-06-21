package com.echoes.registry;

import com.echoes.EchoesMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

/** Custom item data components. */
public final class ModComponents {
    private ModComponents() {}

    /** RU stored on an item (e.g. the Resonance Thrusters' internal buffer). */
    public static final DataComponentType<Integer> STORED_RU = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "stored_ru"),
            DataComponentType.<Integer>builder().codec(Codec.INT).packetCodec(ByteBufCodecs.VAR_INT).build());

    /** Bound Light stored on an item (the Octave Star's portable EMC buffer). */
    public static final DataComponentType<Long> STORED_LIGHT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "stored_light"),
            DataComponentType.<Long>builder().codec(Codec.LONG).packetCodec(ByteBufCodecs.VAR_LONG).build());

    public static void register() {
        EchoesMod.LOGGER.info("Registering data components for {}", EchoesMod.MOD_ID);
    }
}
