package com.echoes;

import com.echoes.block.entity.CrusherBlockEntity;
import com.echoes.energy.ResonanceNetworkManager;
import com.echoes.energy.ResonanceSources;
import com.echoes.recipe.ModRecipes;
import com.echoes.registry.ModBlockEntities;
import com.echoes.registry.ModBlocks;
import com.echoes.registry.ModComponents;
import com.echoes.registry.ModItemGroups;
import com.echoes.registry.ModItems;
import com.echoes.registry.ModScreens;
import com.echoes.registry.ModWorldGen;
import com.echoes.wireless.WirelessNetworkManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoesMod implements ModInitializer {
    public static final String MOD_ID = "echoes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModComponents.register();
        ModItems.register();
        ModBlocks.register();
        ModItemGroups.register();
        ModBlockEntities.register();
        ModScreens.register();
        ModRecipes.register();
        ModWorldGen.register();

        // Per-world Resonance network ticking.
        ResonanceNetworkManager.init();

        // Right-click any energy block with an empty hand to open its Info screen
        // (sneak-click for machines, which keep their function menu on a plain click).
        registerInfoOnRightClick();

        // Per-world wireless transport (Resonant Relay channels).
        WirelessNetworkManager.init();

        // Data-driven sound -> RU table for the ambient-capture mixin.
        ResonanceSources.register();

        // Data-driven Light Value table (EMC = Bound Light) for the transmutation economy.
        // Seeds load with datapacks; the full table is derived from the recipe graph once
        // recipes are available (server start) and after every /reload.
        com.echoes.transmute.LightValues.register();
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(
                com.echoes.transmute.LightValues::derive);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
                (server, resourceManager, success) -> com.echoes.transmute.LightValues.derive(server));

        // Expose the Crusher's inventory to hoppers/pipes via the Transfer API.
        // Top face inserts to input, other faces extract from output (see
        // ImplementedInventory#getAvailableSlots).
        ItemStorage.SIDED.registerForBlockEntity(
                (be, side) -> ContainerStorage.of(be, side), ModBlockEntities.CRUSHER);

        // Resonant Chest exposes its inventory to hoppers/pipes too.
        ItemStorage.SIDED.registerForBlockEntity(
                (be, side) -> ContainerStorage.of(be, side), ModBlockEntities.RESONANT_CHEST);

        // Attunement Furnace exposes its input/output to hoppers/pipes.
        ItemStorage.SIDED.registerForBlockEntity(
                (be, side) -> ContainerStorage.of(be, side), ModBlockEntities.ATTUNEMENT_FURNACE);

        // Optional cross-mod energy bridge — only when Team Reborn Energy is present.
        // Isolated in a separate class so its TR Energy references aren't loaded otherwise.
        if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
            com.echoes.compat.TeamRebornEnergyCompat.register();
        }

        LOGGER.info("Echoes of the Deep initialized.");
    }

    /**
     * Empty-hand right-click on any {@link com.echoes.energy.ResonanceNode} block opens
     * the read-only Info screen. Machines (which are {@link net.minecraft.world.MenuProvider}s)
     * keep opening their function menu on a plain click and surface Info on a sneak-click, so
     * nothing existing is shadowed and no tool is needed.
     */
    private static void registerInfoOnRightClick() {
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClientSide() || hand != net.minecraft.world.InteractionHand.MAIN_HAND
                    || !player.getMainHandItem().isEmpty()) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            net.minecraft.core.BlockPos pos = hit.getBlockPos();
            net.minecraft.world.level.block.entity.BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof com.echoes.energy.ResonanceNode)) return net.minecraft.world.InteractionResult.PASS;
            boolean machine = be instanceof net.minecraft.world.MenuProvider;
            if (machine && !player.isShiftKeyDown()) return net.minecraft.world.InteractionResult.PASS;
            if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                net.minecraft.network.chat.Component name = world.getBlockState(pos).getBlock().getName();
                sp.openMenu(new com.echoes.screen.InfoScreenFactory(name, pos.immutable()));
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        });
    }
}
