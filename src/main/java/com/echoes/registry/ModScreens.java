package com.echoes.registry;

import com.echoes.EchoesMod;
import com.echoes.screen.AttunementFurnaceScreenHandler;
import com.echoes.screen.CrusherScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

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

    public static void register() {
        EchoesMod.LOGGER.info("Registering screen handlers for {}", EchoesMod.MOD_ID);
    }
}
