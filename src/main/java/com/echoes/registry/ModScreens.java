package com.echoes.registry;

import com.echoes.EchoesMod;
import com.echoes.screen.AttunementFurnaceScreenHandler;
import com.echoes.screen.ConfigScreenHandler;
import com.echoes.screen.CrusherScreenHandler;
import com.echoes.screen.HarmonicFilterScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class ModScreens {
    private ModScreens() {}

    public static final ScreenHandlerType<CrusherScreenHandler> CRUSHER =
            Registry.register(Registries.SCREEN_HANDLER,
                    Identifier.of(EchoesMod.MOD_ID, "crusher"),
                    new ScreenHandlerType<>(CrusherScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    public static final ScreenHandlerType<AttunementFurnaceScreenHandler> ATTUNEMENT_FURNACE =
            Registry.register(Registries.SCREEN_HANDLER,
                    Identifier.of(EchoesMod.MOD_ID, "attunement_furnace"),
                    new ScreenHandlerType<>(AttunementFurnaceScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    public static final ScreenHandlerType<HarmonicFilterScreenHandler> HARMONIC_FILTER =
            Registry.register(Registries.SCREEN_HANDLER,
                    Identifier.of(EchoesMod.MOD_ID, "harmonic_filter"),
                    new ScreenHandlerType<>(HarmonicFilterScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    /** Shared configuration screen for every Configurable device. */
    public static final ExtendedScreenHandlerType<ConfigScreenHandler, BlockPos> CONFIG =
            Registry.register(Registries.SCREEN_HANDLER,
                    Identifier.of(EchoesMod.MOD_ID, "config"),
                    new ExtendedScreenHandlerType<>(ConfigScreenHandler::new, BlockPos.PACKET_CODEC));

    public static void register() {
        EchoesMod.LOGGER.info("Registering screen handlers for {}", EchoesMod.MOD_ID);
    }
}
