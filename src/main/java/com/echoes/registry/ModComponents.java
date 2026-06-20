package com.echoes.registry;

import com.echoes.EchoesMod;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/** Custom item data components. */
public final class ModComponents {
    private ModComponents() {}

    /** RU stored on an item (e.g. the Resonance Thrusters' internal buffer). */
    public static final ComponentType<Integer> STORED_RU = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(EchoesMod.MOD_ID, "stored_ru"),
            ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.INTEGER).build());

    /** Bound Light stored on an item (the Octave Star's portable EMC buffer). */
    public static final ComponentType<Long> STORED_LIGHT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(EchoesMod.MOD_ID, "stored_light"),
            ComponentType.<Long>builder().codec(Codec.LONG).packetCodec(PacketCodecs.VAR_LONG).build());

    public static void register() {
        EchoesMod.LOGGER.info("Registering data components for {}", EchoesMod.MOD_ID);
    }
}
