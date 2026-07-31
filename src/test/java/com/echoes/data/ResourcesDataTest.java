package com.echoes.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Data-integrity regression coverage for the content added on top of the base
 * mod (Fabricator, Echo Bloom, Radiant Bloom, essence economy). These tests
 * read the real shipped JSON under src/main/resources, so a broken recipe,
 * loot table, lang key, or Bound-Light seed value fails the build instead of
 * failing silently in game.
 */
class ResourcesDataTest {

    private static final Path RES = Path.of("src/main/resources");
    private static final Path DATA = RES.resolve("data/echoes");
    private static final Path ASSETS = RES.resolve("assets/echoes");

    private static JsonObject readJson(Path p) {
        try {
            return JsonParser.parseString(Files.readString(p)).getAsJsonObject();
        } catch (Exception e) {
            return fail("unreadable/invalid JSON " + p + ": " + e);
        }
    }

    private static Stream<Path> jsonFiles(Path root) throws IOException {
        try (Stream<Path> s = Files.walk(root)) {
            return s.filter(f -> f.toString().endsWith(".json")).toList().stream();
        }
    }

    @Test
    void everyDataJsonParses() throws IOException {
        for (Path p : jsonFiles(DATA).toList()) {
            readJson(p);
        }
    }

    @Test
    void everyAssetJsonParses() throws IOException {
        for (Path p : jsonFiles(ASSETS).toList()) {
            readJson(p);
        }
    }

    @Test
    void everyBlockLootTableHasABlockstate() throws IOException {
        for (Path p : jsonFiles(DATA.resolve("loot_table/blocks")).toList()) {
            String name = p.getFileName().toString().replace(".json", "");
            assertTrue(Files.exists(ASSETS.resolve("blockstates/" + name + ".json")),
                    "loot table without blockstate: " + name);
        }
    }

    @Test
    void essenceRingRecipesAreBreakEvenAgainstLightValues() {
        // The transmutation economy must not create or destroy Bound Light at
        // the condense ring: 8 echo essence = 2 radiant dust exactly.
        JsonObject lv = readJson(DATA.resolve("light_values.json")).getAsJsonObject("values");
        long essence = lv.get("echoes:echo_essence").getAsLong();
        long dust = lv.get("echoes:radiant_dust").getAsLong();
        assertEquals(4096, essence, "echo essence seed value");
        assertEquals(8 * essence, 2 * dust, "echo essence ring must be exactly break-even");
    }

    @Test
    void radiantTierIsWoundAboveTheEchoTier() {
        JsonObject lv = readJson(DATA.resolve("light_values.json")).getAsJsonObject("values");
        long echo = lv.get("echoes:echo_essence").getAsLong();
        long radiant = lv.get("echoes:radiant_essence").getAsLong();
        assertTrue(radiant > echo, "tier-2 essence must outvalue tier-1");
        assertEquals(2 * echo, radiant, "tier-2 is exactly one echo octave above tier-1");
    }

    @Test
    void cropContentIsFullyWired() {
        // Seeds breeding + condense recipes exist for both tiers.
        for (String r : new String[]{"echo_bloom_seeds", "radiant_bloom_seeds",
                "radiant_dust_from_essence", "radiant_ingot_from_essence",
                "raw_echocite_from_essence", "drumstone_shard_from_essence",
                "silentite_crystal_from_essence"}) {
            assertTrue(Files.exists(DATA.resolve("recipe/" + r + ".json")), "missing recipe " + r);
        }
        // Loot tables for both crops.
        for (String b : new String[]{"echo_bloom", "radiant_bloom"}) {
            assertTrue(Files.exists(DATA.resolve("loot_table/blocks/" + b + ".json")), "missing loot " + b);
        }
        // Lang keys for the whole crop line.
        JsonObject lang = readJson(ASSETS.resolve("lang/en_us.json"));
        for (String k : new String[]{"block.echoes.echo_bloom", "item.echoes.echo_bloom_seeds",
                "item.echoes.echo_essence", "block.echoes.radiant_bloom",
                "item.echoes.radiant_bloom_seeds", "item.echoes.radiant_essence",
                "block.echoes.fabricator"}) {
            assertTrue(lang.has(k), "missing lang key " + k);
        }
    }

    @Test
    void cropLootTablesDropEssenceOnlyWhenMature() {
        for (String[] crop : new String[][]{
                {"echo_bloom", "echoes:echo_essence"},
                {"radiant_bloom", "echoes:radiant_essence"}}) {
            String json;
            try {
                json = Files.readString(DATA.resolve("loot_table/blocks/" + crop[0] + ".json"));
            } catch (IOException e) {
                json = null;
                fail("missing loot table " + crop[0]);
            }
            assertTrue(json.contains("\"age\": \"7\""), crop[0] + " essence drop must be gated on age 7");
            assertTrue(json.contains(crop[1]), crop[0] + " loot table must drop " + crop[1]);
        }
    }

    @Test
    void progressionTreeCoversTheNewSystems() {
        JsonObject lang = readJson(ASSETS.resolve("lang/en_us.json"));
        for (String a : new String[]{"fabricator", "echo_bloom", "echo_essence", "radiant_essence"}) {
            Path adv = DATA.resolve("advancement/great_work/" + a + ".json");
            assertTrue(Files.exists(adv), "missing advancement " + a);
            assertTrue(lang.has("advancement.echoes." + a + ".title"), "missing advancement lang " + a);
        }
        // Chain: radiant essence must parent to echo essence.
        JsonObject radiant = readJson(DATA.resolve("advancement/great_work/radiant_essence.json"));
        assertEquals("echoes:great_work/echo_essence", radiant.get("parent").getAsString());
    }

    @Test
    void patternContentIsFullyWired() {
        // Blank pattern recipe exists and yields the registered card.
        Path recipe = DATA.resolve("recipe/blank_pattern.json");
        assertTrue(Files.exists(recipe), "missing recipe blank_pattern");
        JsonObject r = readJson(recipe);
        assertEquals("echoes:blank_pattern", r.getAsJsonObject("result").get("id").getAsString());
        // Item definitions + models for both cards.
        for (String item : new String[]{"blank_pattern", "encoded_pattern"}) {
            assertTrue(Files.exists(ASSETS.resolve("items/" + item + ".json")), "missing item def " + item);
            assertTrue(Files.exists(ASSETS.resolve("models/item/" + item + ".json")), "missing item model " + item);
            assertTrue(Files.exists(ASSETS.resolve("textures/item/" + item + ".png")), "missing texture " + item);
        }
        // Lang: item names, tooltips, and the four fabricator messages.
        JsonObject lang = readJson(ASSETS.resolve("lang/en_us.json"));
        for (String k : new String[]{"item.echoes.blank_pattern", "item.echoes.encoded_pattern",
                "tooltip.echoes.pattern.empty", "tooltip.echoes.pattern.slots",
                "tooltip.echoes.pattern.hint", "message.echoes.pattern.saved",
                "message.echoes.pattern.loaded", "message.echoes.pattern.nothing",
                "message.echoes.pattern.invalid"}) {
            assertTrue(lang.has(k), "missing lang key " + k);
        }
    }

    @Test
    void abyssalTierIsWoundAboveTheRadiantTier() {
        JsonObject lv = readJson(DATA.resolve("light_values.json")).getAsJsonObject("values");
        long radiant = lv.get("echoes:radiant_essence").getAsLong();
        long abyssal = lv.get("echoes:abyssal_essence").getAsLong();
        long harmonic = lv.get("echoes:harmonic_mote").getAsLong();
        assertEquals(2 * radiant, abyssal, "tier-3 is exactly one octave above tier-2");
        assertEquals(8 * radiant, 4 * harmonic, "radiant ring to 4 harmonic motes must be exactly break-even");
    }

    @Test
    void abyssalContentIsFullyWired() {
        // Breeding + condense recipes for tier 3.
        for (String r : new String[]{"abyssal_bloom_seeds", "harmonic_motes_from_essence",
                "netherite_scrap_from_essence", "silentite_crystal_from_abyssal"}) {
            assertTrue(Files.exists(DATA.resolve("recipe/" + r + ".json")), "missing recipe " + r);
        }
        // Loot table gated on age 7, dropping abyssal essence.
        String loot;
        try {
            loot = Files.readString(DATA.resolve("loot_table/blocks/abyssal_bloom.json"));
        } catch (IOException e) {
            loot = null;
            fail("missing loot table abyssal_bloom");
        }
        assertTrue(loot.contains("\"age\": \"7\""), "abyssal essence drop must be gated on age 7");
        assertTrue(loot.contains("echoes:abyssal_essence"), "abyssal loot table must drop abyssal essence");
        // Blockstate + stage models + item defs.
        assertTrue(Files.exists(ASSETS.resolve("blockstates/abyssal_bloom.json")), "missing blockstate");
        for (int i = 0; i < 4; i++) {
            assertTrue(Files.exists(ASSETS.resolve("models/block/abyssal_bloom_stage" + i + ".json")),
                    "missing stage model " + i);
        }
        for (String item : new String[]{"abyssal_bloom_seeds", "abyssal_essence"}) {
            assertTrue(Files.exists(ASSETS.resolve("items/" + item + ".json")), "missing item def " + item);
        }
        // Lang keys.
        JsonObject lang = readJson(ASSETS.resolve("lang/en_us.json"));
        for (String k : new String[]{"block.echoes.abyssal_bloom", "item.echoes.abyssal_bloom_seeds",
                "item.echoes.abyssal_essence", "tooltip.echoes.desc.abyssal_bloom_seeds",
                "tooltip.echoes.desc.abyssal_essence", "advancement.echoes.abyssal_essence.title"}) {
            assertTrue(lang.has(k), "missing lang key " + k);
        }
        // Advancement chains off the radiant tier.
        JsonObject adv = readJson(DATA.resolve("advancement/great_work/abyssal_essence.json"));
        assertEquals("echoes:great_work/radiant_essence", adv.get("parent").getAsString());
    }
}
