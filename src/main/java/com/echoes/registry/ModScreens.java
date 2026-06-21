package com.echoes.registry;

import com.echoes.EchoesMod;
import com.echoes.screen.AttunementFurnaceScreenHandler;
import com.echoes.screen.ConfigScreenHandler;
import com.echoes.screen.CrusherScreenHandler;
import com.echoes.screen.HarmonicFilterScreenHandler;
import com.echoes.screen.TransmutationTableScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;

public final class ModScreens {
    private ModScreens() {}

    public static final MenuType<CrusherScreenHandler> CRUSHER =
            Registry.register(BuiltInRegistries.SCREEN_HANDLER,
                    Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "compressor"),
                    new MenuType<>(CrusherScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    public static final MenuType<AttunementFurnaceScreenHandler> ATTUNEMENT_FURNACE =
            Registry.register(BuiltInRegistries.SCREEN_HANDLER,
                    Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "transmuter"),
                    new MenuType<>(AttunementFurnaceScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    public static final MenuType<HarmonicFilterScreenHandler> HARMONIC_FILTER =
            Registry.register(BuiltInRegistries.SCREEN_HANDLER,
                    Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "wave_filter"),
                    new MenuType<>(HarmonicFilterScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    public static final MenuType<TransmutationTableScreenHandler> TRANSMUTATION_TABLE =
            Registry.register(BuiltInRegistries.SCREEN_HANDLER,
                    Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "transmutation_table"),
                    new MenuType<>(TransmutationTableScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    /** Shared configuration screen for every Configurable device. */
    public static final ExtendedMenuType<ConfigScreenHandler, BlockPos> CONFIG =
            Registry.register(BuiltInRegistries.SCREEN_HANDLER,
                    Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "config"),
                    new ExtendedMenuType<>(ConfigScreenHandler::new, BlockPos.STREAM_CODEC));

    public static void register() {
        EchoesMod.LOGGER.info("Registering screen handlers for {}", EchoesMod.MOD_ID);
    }
}
