# Ores & Worldgen

[← Home](Home.md)

Three ores and one tree, attached to vanilla biomes via `BiomeModifications`.

## Ores

| Ore | Drops | Where | Generation |
| --- | --- | --- | --- |
| **Echocite Ore** (+ deepslate) | Raw Echocite | Overworld, most biomes | vein size 8, ~12 tries/chunk, **Y −20…60** (trapezoid — densest in the middle) |
| **Drumstone Ore** | Drumstone Shard | Overworld | vein size 6, ~5 tries/chunk, **Y −48…24** (trapezoid) |
| **Silentite Ore** | Silentite Crystal | **Deep Dark only** | vein size 4, ~4 tries/chunk, **Y −58…−8** (uniform) — rare |

- **Echocite** is the base of the entire mod — you'll have plenty from ordinary mining. Mine
  with a stone pickaxe or better.
- **Drumstone** opens the percussive branch (Drum Core → alternate Coil, Thrusters).
- **Silentite** is intentionally scarce and gated behind reaching the **Deep Dark**; it's
  the silence the high-octave tier is built on (Stillness Core, Octave Seed).

## The Lumewood Grove

A custom **glowing tree** generates as small groves in forest biomes. It drops a
**Lumewood Sapling** (plant it on grass or **Verdant Loam** to grow more) and yields the
full **Lumewood** building set — log, wood, planks, stairs, slab, fence, gate, trapdoor,
glowing leaves — plus the **Lumebloom** flower for décor. See [Blocks](Blocks.md).

## For modpack makers

Worldgen is standard data — configured/placed features under
`data/echoes/worldgen/` — wired to biomes in `ModWorldGen` via Fabric `BiomeModifications`.
Override the JSON in a datapack to retune spawn rate, height, or biome targeting; disable an
ore by removing its biome modification or emptying the placed feature.
