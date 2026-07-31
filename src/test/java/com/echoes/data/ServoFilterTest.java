package com.echoes.data;

import com.echoes.wireless.ServoFilter;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit coverage for the pure servo-filter core (parse/join/toggle/allows) —
 * the per-relay TD-style extraction whitelist. These run on the JVM without
 * bootstrapping Minecraft registries.
 */
class ServoFilterTest {

    @Test
    void parseEmptyAndBlank() {
        assertTrue(ServoFilter.parse("").isEmpty());
        assertTrue(ServoFilter.parse(null).isEmpty());
        assertTrue(ServoFilter.parse(" , , ").isEmpty());
    }

    @Test
    void parseJoinRoundTripsAndDropsDuplicates() {
        LinkedHashSet<String> f = ServoFilter.parse("echoes:echo_ingot, minecraft:iron_ingot,echoes:echo_ingot");
        assertEquals(2, f.size(), "duplicates are dropped");
        String raw = ServoFilter.join(f);
        assertEquals("echoes:echo_ingot,minecraft:iron_ingot", raw);
        assertEquals(f, ServoFilter.parse(raw), "parse(join(f)) == f");
    }

    @Test
    void emptyFilterAllowsEverything() {
        assertTrue(ServoFilter.allows(null, "minecraft:diamond"));
        assertTrue(ServoFilter.allows(new LinkedHashSet<>(), "minecraft:diamond"));
    }

    @Test
    void nonEmptyFilterAllowsOnlyListed() {
        LinkedHashSet<String> f = ServoFilter.parse("echoes:echo_essence");
        assertTrue(ServoFilter.allows(f, "echoes:echo_essence"));
        assertFalse(ServoFilter.allows(f, "echoes:radiant_essence"));
    }

    @Test
    void toggleAddsThenRemoves() {
        LinkedHashSet<String> f = new LinkedHashSet<>();
        assertEquals(ServoFilter.Toggle.ADDED, ServoFilter.toggle(f, "minecraft:gold_ingot"));
        assertTrue(f.contains("minecraft:gold_ingot"));
        assertEquals(ServoFilter.Toggle.REMOVED, ServoFilter.toggle(f, "minecraft:gold_ingot"));
        assertTrue(f.isEmpty());
    }

    @Test
    void toggleCapsAtLimit() {
        LinkedHashSet<String> f = new LinkedHashSet<>();
        for (int i = 0; i < ServoFilter.CAP; i++) {
            assertEquals(ServoFilter.Toggle.ADDED, ServoFilter.toggle(f, "minecraft:item" + i));
        }
        assertEquals(ServoFilter.Toggle.FULL, ServoFilter.toggle(f, "minecraft:overflow"));
        assertEquals(ServoFilter.CAP, f.size(), "a full filter rejects new entries");
        // …but toggling an existing entry out still works when full.
        assertEquals(ServoFilter.Toggle.REMOVED, ServoFilter.toggle(f, "minecraft:item0"));
        assertEquals(ServoFilter.CAP - 1, f.size());
    }
}
