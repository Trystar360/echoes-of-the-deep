package com.echoes.config;

import com.echoes.EchoesMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Server-side mod settings, loaded once at init from {@code config/echoes.json}.
 * The file is created with defaults on first launch so every knob is discoverable;
 * delete a key (or the file) to fall back to its default. All values are read
 * through {@link #get()} so a future reload command only has to swap the instance.
 *
 * <p>Deliberately plain JSON with no config-library dependency — the mod's other
 * data files (light values, resonance sources) already speak Gson.
 */
public final class EchoesConfig {

    // --- wireless ---
    /** Hush Cost: broadcasting items/fluids drains RU per active sender per tick. */
    public boolean hushCost = false;
    /** RU drained per active sender per tick when hushCost is enabled. */
    public long hushRuPerSender = 20;

    // --- ambient capture ---
    /** RU captured by the nearest Resonator when a living entity dies (0 disables). */
    public int deathRu = 25;

    // --- Resonance Thrusters ---
    /** RU reserve of one pair of thrusters. */
    public int thrusterCapacity = 1_000_000;
    /** RU drained per tick of flight. */
    public int thrusterDrainPerTick = 8;
    /** Flight speed, blocks per tick. */
    public double thrusterFlySpeed = 0.85;
    /** Sprint-boosted flight speed, blocks per tick. */
    public double thrusterSprintSpeed = 1.45;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static EchoesConfig instance = new EchoesConfig();

    public static EchoesConfig get() { return instance; }

    /** Load {@code config/echoes.json}, writing a defaults file if it doesn't exist. */
    public static void load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve(EchoesMod.MOD_ID + ".json");
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                EchoesConfig loaded = GSON.fromJson(reader, EchoesConfig.class);
                if (loaded != null) instance = loaded;
                EchoesMod.LOGGER.info("Loaded config from {}", file);
            } catch (Exception ex) {
                EchoesMod.LOGGER.warn("Failed to read {} — using defaults: {}", file, ex.toString());
            }
            return;
        }
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(instance, writer);
            EchoesMod.LOGGER.info("Wrote default config to {}", file);
        } catch (Exception ex) {
            EchoesMod.LOGGER.warn("Failed to write default config {}: {}", file, ex.toString());
        }
    }
}
