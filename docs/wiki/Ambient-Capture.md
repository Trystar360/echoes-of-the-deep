# Ambient Capture

[← Home](Home.md)

The **Resonant Coil** doesn't burn fuel — it **winds world sound into Light**. When
something noisy happens near a Coil, the nearest one captures a little Light.

## How it works

Two hooks feed Light to the nearest **Resonant Coil** within **8 blocks**:

1. **Mob deaths** — any living entity dying grants **25 RU** to the nearest Coil.
2. **World sounds** — when the server plays a sound listed in the **sound→RU table**, its
   value charges the nearest Coil.

Only the single nearest Coil claims each event, so stacking Coils around one sound source
doesn't multiply the yield — spread them across multiple sources instead.

## The sound table

The table is **data-driven** and lives in
[`data/echoes/resonance_sources.json`](https://github.com/Trystar360/echoes-of-the-deep/blob/main/src/main/resources/data/echoes/resonance_sources.json),
read by a reload listener (so `/reload` picks up edits). The defaults:

| Sound | RU |
| --- | --- |
| Note block (harp / bass) | 8 |
| Note block (bell) | 12 |
| Bell use | 10 |
| Anvil land | 40 |
| Explosion | 40 |
| Beacon activate | 100 |
| Thunder | 2,000 |

## Build ideas

- A wall of **note blocks** on a redstone clock next to a Coil is a clean, quiet generator.
- A **mob farm** drop zone over a Coil turns kills into Light.
- **Anvils** falling on a loop, or a TNT/explosion farm, give bigger bursts.
- **Thunderstorms** are huge but rare — for reliable storm power, build the **Storm Caller**
  instead (see [Blocks](Blocks.md)).

## For modpack makers

Add or retune entries — including **other mods'** sound events — by overriding the JSON in a
datapack. The key is the sound event id; the value is RU per occurrence. This is the
intended way to balance ambient generation for a pack.
