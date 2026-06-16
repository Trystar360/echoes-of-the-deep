package com.echoes.energy;

import com.echoes.EchoesMod;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * The data-driven sound → RU table. Loads {@code data/echoes/resonance_sources.json}
 * (merging across every datapack, so modpacks can extend or override it) and is
 * queried by {@code ServerWorldMixin} when a sound plays. Reloads with {@code /reload}.
 */
public final class ResonanceSources implements SimpleSynchronousResourceReloadListener {
    private ResonanceSources() {}

    private static final Map<Identifier, Integer> SOURCES = new HashMap<>();
    private static final Identifier FILE = Identifier.of(EchoesMod.MOD_ID, "resonance_sources.json");
    private static final Identifier LISTENER_ID = Identifier.of(EchoesMod.MOD_ID, "resonance_sources");

    /** RU emitted by this sound, or 0 if it isn't a resonance source. */
    public static int ru(RegistryEntry<SoundEvent> sound) {
        Identifier id = sound.getKey().map(net.minecraft.registry.RegistryKey::getValue).orElseGet(() -> sound.value().id());
        return SOURCES.getOrDefault(id, 0);
    }

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ResonanceSources());
    }

    @Override
    public Identifier getFabricId() { return LISTENER_ID; }

    @Override
    public void reload(ResourceManager manager) {
        SOURCES.clear();
        for (Resource resource : manager.getAllResources(FILE)) {
            try (Reader reader = resource.getReader()) {
                JsonObject root = JsonHelper.deserialize(reader);
                JsonObject sources = JsonHelper.getObject(root, "sources", new JsonObject());
                for (Map.Entry<String, com.google.gson.JsonElement> e : sources.entrySet()) {
                    Identifier id = Identifier.tryParse(e.getKey());
                    if (id != null && e.getValue().isJsonPrimitive()) {
                        SOURCES.put(id, e.getValue().getAsInt());
                    }
                }
            } catch (Exception ex) {
                EchoesMod.LOGGER.warn("Failed to read resonance_sources.json from {}: {}",
                        resource.getPackId(), ex.toString());
            }
        }
        EchoesMod.LOGGER.info("Loaded {} resonance sound sources", SOURCES.size());
    }
}
