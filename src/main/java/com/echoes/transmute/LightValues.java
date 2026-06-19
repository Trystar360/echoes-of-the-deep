package com.echoes.transmute;

import com.echoes.EchoesMod;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * The data-driven <b>Light Value</b> table — the mod's EMC, reskinned to Russell's
 * cosmology: every item carries the amount of <i>Bound Light</i> wound into it to exist.
 *
 * <p>Loads {@code data/echoes/light_values.json} (merging across every datapack, so
 * modpacks can extend or override it) and is queried by the Transmutation Table when it
 * dissolves matter into banked Bound Light. Reloads with {@code /reload}. An item absent
 * from the table — or explicitly blacklisted with a value of {@code 0} — cannot be
 * dissolved, which is how unique/exploit items are kept out of the economy.
 */
public final class LightValues implements SimpleSynchronousResourceReloadListener {
    private LightValues() {}

    private static final Map<Identifier, Long> VALUES = new HashMap<>();
    private static final Identifier FILE = Identifier.of(EchoesMod.MOD_ID, "light_values.json");
    private static final Identifier LISTENER_ID = Identifier.of(EchoesMod.MOD_ID, "light_values");

    /** Bound Light wound into one of this item, or 0 if it has no value / is blacklisted. */
    public static long get(Item item) {
        return VALUES.getOrDefault(Registries.ITEM.getId(item), 0L);
    }

    /** Bound Light for a whole stack. */
    public static long get(ItemStack stack) {
        return get(stack.getItem()) * stack.getCount();
    }

    public static int size() { return VALUES.size(); }

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new LightValues());
    }

    @Override
    public Identifier getFabricId() { return LISTENER_ID; }

    @Override
    public void reload(ResourceManager manager) {
        VALUES.clear();
        for (Resource resource : manager.getAllResources(FILE)) {
            try (Reader reader = resource.getReader()) {
                JsonObject root = JsonHelper.deserialize(reader);
                JsonObject values = JsonHelper.getObject(root, "values", new JsonObject());
                for (Map.Entry<String, com.google.gson.JsonElement> e : values.entrySet()) {
                    Identifier id = Identifier.tryParse(e.getKey());
                    if (id != null && e.getValue().isJsonPrimitive()) {
                        VALUES.put(id, Math.max(0L, e.getValue().getAsLong()));
                    }
                }
            } catch (Exception ex) {
                EchoesMod.LOGGER.warn("Failed to read light_values.json from {}: {}",
                        resource.getPackId(), ex.toString());
            }
        }
        EchoesMod.LOGGER.info("Loaded {} Light Values (Bound Light / EMC)", VALUES.size());
    }
}
