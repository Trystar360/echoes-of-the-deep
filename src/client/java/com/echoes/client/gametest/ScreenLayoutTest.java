package com.echoes.client.gametest;

import com.echoes.client.screen.AttunementFurnaceScreen;
import com.echoes.client.screen.ConfigScreen;
import com.echoes.client.screen.CrusherScreen;
import com.echoes.client.screen.HarmonicFilterScreen;
import com.echoes.client.screen.TransmutationTableScreen;
import com.echoes.screen.AttunementFurnaceScreenHandler;
import com.echoes.screen.ConfigScreenHandler;
import com.echoes.screen.CrusherScreenHandler;
import com.echoes.screen.HarmonicFilterScreenHandler;
import com.echoes.screen.TransmutationTableScreenHandler;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.Function;

/**
 * Dev-only client gametest (not shipped — excluded from the jar). Joins a fresh
 * singleplayer world and screenshots each of the mod's five screens so the
 * 26.1 {@code GuiGraphicsExtractor} layout (panel, slots, labels, buttons) can be
 * inspected visually. Run with {@code ./gradlew runClientGametest}.
 */
public class ScreenLayoutTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        // Minimise the chunks the loading screen must compile before going in-game —
        // under headless software GL a normal render distance never finishes loading.
        context.runOnClient(client -> {
            client.options.renderDistance().set(2);
            client.options.simulationDistance().set(5);
        });
        // A FLAT world generates spawn chunks almost instantly — a normal world's spawn-area
        // prep is far too slow under headless software rendering and stalls the test.
        try (TestSingleplayerContext sp = context.worldBuilder()
                .setUseConsistentSettings(true)
                .adjustSettings(state -> {
                    Holder<WorldPreset> flat = state.getSettings().worldgenLoadContext()
                            .lookupOrThrow(Registries.WORLD_PRESET)
                            .getOrThrow(WorldPresets.FLAT);
                    state.setWorldType(new WorldCreationUiState.WorldTypeEntry(flat));
                })
                .create()) {
            // Just need a joined player for the inventory; GUI screens render over the
            // world, so we don't wait for chunk *rendering* (which stalls under headless
            // software GL) — a short settle is enough.
            sp.getClientLevel().waitForChunksDownload();
            context.waitTicks(20);

            shot(context, "crusher",
                    inv -> new CrusherScreen(new CrusherScreenHandler(1, inv), inv, Component.literal("Compressor")));
            shot(context, "attunement_furnace",
                    inv -> new AttunementFurnaceScreen(new AttunementFurnaceScreenHandler(1, inv), inv, Component.literal("Attunement Furnace")));
            shot(context, "harmonic_filter",
                    inv -> new HarmonicFilterScreen(new HarmonicFilterScreenHandler(1, inv), inv, Component.literal("Harmonic Filter")));
            shot(context, "transmutation_table",
                    inv -> new TransmutationTableScreen(new TransmutationTableScreenHandler(1, inv), inv, Component.literal("Transmutation Table")));
            // Config screen: place a channel device so the panel shows real control rows
            // (channel / octave / redstone / 6 side toggles) instead of an empty spec.
            BlockPos cfgPos = new BlockPos(2, -60, 2);
            sp.getServer().runCommand("setblock %d %d %d echoes:wave_relay"
                    .formatted(cfgPos.getX(), cfgPos.getY(), cfgPos.getZ()));
            context.waitTicks(10);
            shot(context, "config",
                    inv -> new ConfigScreen(new ConfigScreenHandler(1, inv, cfgPos), inv, Component.literal("Configure")));

            context.runOnClient(client -> client.setScreen(null));
        }
    }

    /** Open a screen on the client thread, let it settle, capture a screenshot. */
    private void shot(ClientGameTestContext context, String name, Function<Inventory, Screen> factory) {
        context.runOnClient(client -> client.setScreen(factory.apply(client.player.getInventory())));
        context.waitTicks(3);
        context.takeScreenshot("echoes_" + name);
        context.waitTick();
    }
}
