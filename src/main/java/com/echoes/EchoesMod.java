package com.echoes;

import com.echoes.block.entity.CrusherBlockEntity;
import com.echoes.energy.ResonanceNetworkManager;
import com.echoes.recipe.ModRecipes;
import com.echoes.registry.ModBlockEntities;
import com.echoes.registry.ModBlocks;
import com.echoes.registry.ModItemGroups;
import com.echoes.registry.ModItems;
import com.echoes.registry.ModScreens;
import com.echoes.registry.ModWorldGen;
import com.echoes.wireless.WirelessNetworkManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoesMod implements ModInitializer {
    public static final String MOD_ID = "echoes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.register();
        ModBlocks.register();
        ModItemGroups.register();
        ModBlockEntities.register();
        ModScreens.register();
        ModRecipes.register();
        ModWorldGen.register();

        // Per-world Resonance network ticking.
        ResonanceNetworkManager.init();

        // Per-world wireless transport (Resonant Relay channels).
        WirelessNetworkManager.init();

        // Expose the Crusher's inventory to hoppers/pipes via the Transfer API.
        // Top face inserts to input, other faces extract from output (see
        // ImplementedInventory#getAvailableSlots).
        ItemStorage.SIDED.registerForBlockEntity(
                (be, side) -> InventoryStorage.of(be, side), ModBlockEntities.CRUSHER);

        LOGGER.info("Echoes of the Deep initialized.");
    }
}
