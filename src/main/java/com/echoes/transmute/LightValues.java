package com.echoes.transmute;

import com.echoes.EchoesMod;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.SingleStackRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The mod's EMC: every item's <b>Light Value</b> (its <i>Bound Light</i>).
 *
 * <p>Values come from two layers. A small, hand-authored <b>seed</b> set
 * ({@code data/echoes/light_values.json} — primitives that aren't craftable from anything
 * cheaper: ores, mob drops, plants, plus explicit overrides) is loaded and is
 * authoritative. Everything else — every craftable item, <b>vanilla or modded</b> — is
 * then <b>derived</b> by propagating values through the entire recipe graph: an item's
 * value is the cheapest {@code sum(inputs) / outputCount} over all recipes that make it,
 * iterated to a fixed point. So a modpack gets sensible values for free, and can extend
 * or override the seeds with its own datapack copy of the JSON.
 */
public final class LightValues implements SimpleSynchronousResourceReloadListener {
    private LightValues() {}

    private static final Map<Identifier, Long> SEEDS = new HashMap<>();      // authoritative
    private static final Set<Identifier> BLACKLIST = new HashSet<>();         // never valued
    private static volatile Map<Identifier, Long> DERIVED = Map.of();         // recipe-derived

    private static final Identifier FILE = Identifier.of(EchoesMod.MOD_ID, "light_values.json");
    private static final Identifier LISTENER_ID = Identifier.of(EchoesMod.MOD_ID, "light_values");
    private static final int MAX_PASSES = 16;

    /** Items that must never carry value (unobtainable / exploit blocks). */
    private static final Set<Identifier> DEFAULT_BLACKLIST = Set.of(
            id("bedrock"), id("barrier"), id("command_block"), id("chain_command_block"),
            id("repeating_command_block"), id("structure_block"), id("structure_void"),
            id("jigsaw"), id("light"), id("debug_stick"), id("knowledge_book"),
            id("end_portal_frame"), id("reinforced_deepslate"), id("budding_amethyst"),
            id("spawner"), id("petrified_oak_slab"), id("farmland"), id("dirt_path"));

    private static Identifier id(String path) { return Identifier.ofVanilla(path); }

    /** Bound Light wound into one of this item, or 0 if it has no value / is blacklisted. */
    public static long get(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        if (BLACKLIST.contains(id)) return 0L;
        Long seed = SEEDS.get(id);
        if (seed != null) return seed;
        return DERIVED.getOrDefault(id, 0L);
    }

    public static long get(ItemStack stack) {
        return get(stack.getItem()) * stack.getCount();
    }

    /** Total number of valued items (seeds + derived). */
    public static int size() {
        Set<Identifier> all = new HashSet<>(SEEDS.keySet());
        all.addAll(DERIVED.keySet());
        all.removeAll(BLACKLIST);
        return all.size();
    }

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new LightValues());
    }

    @Override
    public Identifier getFabricId() { return LISTENER_ID; }

    /** Reload only re-reads the JSON seeds; derivation needs the recipe manager (see {@link #derive}). */
    @Override
    public void reload(ResourceManager manager) {
        SEEDS.clear();
        BLACKLIST.clear();
        BLACKLIST.addAll(DEFAULT_BLACKLIST);
        for (Resource resource : manager.getAllResources(FILE)) {
            try (Reader reader = resource.getReader()) {
                JsonObject root = JsonHelper.deserialize(reader);
                JsonObject values = JsonHelper.getObject(root, "values", new JsonObject());
                for (Map.Entry<String, com.google.gson.JsonElement> e : values.entrySet()) {
                    Identifier id = Identifier.tryParse(e.getKey());
                    if (id == null || !e.getValue().isJsonPrimitive()) continue;
                    long v = e.getValue().getAsLong();
                    if (v <= 0) BLACKLIST.add(id);
                    else SEEDS.put(id, v);
                }
                if (root.has("blacklist")) {
                    for (com.google.gson.JsonElement el : JsonHelper.getArray(root, "blacklist")) {
                        Identifier id = Identifier.tryParse(el.getAsString());
                        if (id != null) BLACKLIST.add(id);
                    }
                }
            } catch (Exception ex) {
                EchoesMod.LOGGER.warn("Failed to read light_values.json from {}: {}",
                        resource.getPackId(), ex.toString());
            }
        }
        EchoesMod.LOGGER.info("Loaded {} Light Value seeds ({} blacklisted)", SEEDS.size(), BLACKLIST.size());
    }

    // --- recipe-graph derivation ---------------------------------------------------

    private static final SingleStackRecipeInput SINGLE_EMPTY = new SingleStackRecipeInput(ItemStack.EMPTY);

    /** Recompute derived values from the server's full recipe set (vanilla + every mod). */
    public static void derive(MinecraftServer server) {
        RegistryWrapper.WrapperLookup lookup = server.getRegistryManager();
        Collection<RecipeEntry<?>> recipes = server.getRecipeManager().values();

        Map<Identifier, Long> values = new HashMap<>(SEEDS); // working set; seeds are the floor
        boolean changed = true;
        int pass = 0;
        while (changed && pass++ < MAX_PASSES) {
            changed = false;
            for (RecipeEntry<?> entry : recipes) {
                Recipe<?> recipe = entry.value();
                ItemStack out = output(recipe, lookup);
                if (out.isEmpty()) continue;
                Identifier outId = Registries.ITEM.getId(out.getItem());
                if (SEEDS.containsKey(outId) || BLACKLIST.contains(outId)) continue; // authoritative / forbidden

                List<Ingredient> ingredients = recipe.getIngredientPlacement().getIngredients();
                if (ingredients.isEmpty()) continue;

                long total = 0;
                boolean resolvable = true;
                for (Ingredient ing : ingredients) {
                    long best = cheapest(ing, values);
                    if (best < 0) { resolvable = false; break; }
                    total += best;
                }
                if (!resolvable) continue;

                long per = Math.max(1, total / Math.max(1, out.getCount()));
                Long cur = values.get(outId);
                if (cur == null || per < cur) {
                    values.put(outId, per);
                    changed = true;
                }
            }
        }
        values.keySet().removeAll(SEEDS.keySet()); // get() prefers seeds anyway; keep DERIVED lean
        DERIVED = values;
        EchoesMod.LOGGER.info("Derived Light Values: {} seeds + {} from recipes = {} valued items",
                SEEDS.size(), DERIVED.size(), size());
    }

    /**
     * The nominal output of a recipe. Crafting / cooking / stonecutting recipes return a
     * fixed result and ignore the input, so an empty input of the right type is enough.
     * Other (special / input-dependent) recipe types are skipped.
     */
    private static ItemStack output(Recipe<?> recipe, RegistryWrapper.WrapperLookup lookup) {
        try {
            if (recipe instanceof CraftingRecipe crafting) {
                ItemStack out = crafting.craft(CraftingRecipeInput.EMPTY, lookup);
                return out == null ? ItemStack.EMPTY : out;
            }
            if (recipe instanceof SingleStackRecipe single) { // smelting/blasting/smoking/campfire/stonecutting
                ItemStack out = single.craft(SINGLE_EMPTY, lookup);
                return out == null ? ItemStack.EMPTY : out;
            }
        } catch (Throwable t) {
            // input-dependent or special recipe — skip
        }
        return ItemStack.EMPTY;
    }

    /** Cheapest known value among an ingredient's matching items, or -1 if none is valued. */
    private static long cheapest(Ingredient ingredient, Map<Identifier, Long> values) {
        long best = -1;
        for (var entry : ingredient.getMatchingItems().toList()) {
            Identifier id = Registries.ITEM.getId(entry.value());
            if (BLACKLIST.contains(id)) continue;
            Long v = values.get(id);
            if (v != null && v > 0 && (best < 0 || v < best)) best = v;
        }
        return best;
    }
}
