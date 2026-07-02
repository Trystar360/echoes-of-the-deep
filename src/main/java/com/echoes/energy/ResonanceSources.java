package com.echoes.energy;

import com.echoes.EchoesMod;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.PackType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * The data-driven sound → RU table. Loads {@code data/echoes/resonance_sources.json}
 * (merging across every datapack, so modpacks can extend or override it) and is
 * queried by {@code LevelSoundMixin} when a sound plays. Reloads with {@code /reload}.
 */
public final class ResonanceSources implements SimpleSynchronousResourceReloadListener {
    private ResonanceSources() {}

    private static final Map<Identifier, Integer> SOURCES = new HashMap<>();
    private static final Identifier FILE = Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "resonance_sources.json");
    private static final Identifier LISTENER_ID = Identifier.fromNamespaceAndPath(EchoesMod.MOD_ID, "resonance_sources");

    /** RU emitted by this sound, or 0 if it isn't a resonance source. */
    public static int ru(Holder<SoundEvent> sound) {
        Identifier id = sound.unwrapKey().map(net.minecraft.resources.ResourceKey::identifier).orElseGet(() -> sound.value().location());
        return SOURCES.getOrDefault(id, 0);
    }

    public static void register() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new ResonanceSources());
    }

    @Override
    public Identifier getFabricId() { return LISTENER_ID; }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        SOURCES.clear();
        for (Resource resource : manager.getResourceStack(FILE)) {
            try (Reader reader = resource.openAsReader()) {
                JsonObject root = GsonHelper.parse(reader);
                JsonObject sources = GsonHelper.getAsJsonObject(root, "sources", new JsonObject());
                for (Map.Entry<String, com.google.gson.JsonElement> e : sources.entrySet()) {
                    Identifier id = Identifier.tryParse(e.getKey());
                    if (id != null && e.getValue().isJsonPrimitive()) {
                        SOURCES.put(id, e.getValue().getAsInt());
                    }
                }
            } catch (Exception ex) {
                EchoesMod.LOGGER.warn("Failed to read resonance_sources.json from {}: {}",
                        resource.sourcePackId(), ex.toString());
            }
        }
        EchoesMod.LOGGER.info("Loaded {} resonance sound sources", SOURCES.size());
    }
}
