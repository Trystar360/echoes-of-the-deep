# Reference & FAQ

[← Home](Home.md)

## Mod identity

| Field | Value |
| --- | --- |
| Display name | Octaves of the One |
| Mod id | `echoes` |
| Archive | `echoes-of-the-deep` |
| Maven group | `com.echoes` |
| Version | 0.1.0 |
| Minecraft | 1.21.4 |
| Loader | Fabric ≥ 0.16.0 |
| Yarn mappings | 1.21.4+build.8 |
| Java | 21 |
| License | MIT |
| Author | Trystar360 |

## Constants cheat-sheet

> In-code defaults; a pack author can re-tune them. RU is shown in-game as Light.

### Storage capacities
| Block | RU |
| --- | --- |
| Resonant Coil | 10,000 |
| Stillness Core | 50,000 |
| Resonance Cell | 250,000 |
| Compressor / Transmuter buffer | 1,000 |
| Growth Radiator / Warmth Radiator / Polarity Field buffer | 3,000 |
| Resonant Thrusters (item) | 1,000,000 |

### Rates
| Thing | Value |
| --- | --- |
| Stillness Core generation | +4 RU/t |
| Wave Conduit throughput | 1,000 RU/t |
| Dense Wave Conduit throughput | 16,000 RU/t |
| Thrusters flight cost | ~8 RU/t |
| Mob death capture | 25 RU (≤ 8 blocks) |
| Balancer | ~2,000 RU/t every 10t |
| Growth Radiator | ~300 RU/grow, ~8 tries / 10t, 4×2 radius |
| Warmth Radiator | ~60 RU/cook, ~4 radius |
| Polarity Field | ~20 RU/action / 5t, radius 6 |
| Network stagger threshold | >256 conduits → tick every 4t |

### Wireless budget (per channel, per tick)
| Cargo | Per sender | Base cap | × amplifiers |
| --- | --- | --- | --- |
| Items | 8 | 64 | up to ×16 |
| Fluids | 1,000 mB | 8,000 mB | up to ×16 |
| Light | 1,000 RU | 16,000 RU | up to ×16 |

Channels: **16** (dye colours). Min devices to tick: **2**. Hush Cost: **off by
default**.

### Echo tool material
Mining speed 12.0 · durability 4,000 · mines anything · enchantability 22 ·
repaired with Echo Ingot (`#echoes:resonant_repair`).

## Block / item id list

**Blocks:** `echocite_ore`, `deepslate_echocite_ore`, `drumstone_ore`,
`silentite_ore`, `stillness_core`, `resonant_coil`, `wave_conduit`,
`dense_wave_conduit`, `resonance_cell`, `compressor`, `transmuter`,
`growth_radiator`, `warmth_radiator`, `polarity_field`, `balancer`, `wave_relay`,
`wave_amplifier`, `wave_filter`, `wave_splitter`, `wave_repeater`,
`wave_coupler`, `wave_chest`, `signal_relay`.

**Items:** `raw_echocite`, `echocite_dust`, `echo_ingot`, `echo_dust`,
`dull_ingot`, `resonant_slag`, `drumstone_shard`, `drum_core`,
`silentite_crystal`, `wave_tuner`, `wave_atlas`, `light_meter`,
`resonant_thrusters`, `resonant_pickaxe`, `resonant_axe`, `resonant_shovel`,
`resonant_sword`, `resonant_hoe`.

## FAQ

**My Resonant Coil isn't charging.**
It needs **sound**. Put it within 8 blocks of mob deaths, or near note blocks,
anvils, bells, or explosions. See [Ambient Capture](Ambient-Capture.md). Check
the level with a **Light Meter**.

**How do I see how much Light something holds?**
Craft a **Light Meter** and right-click the device. Storage nodes also emit a
comparator signal (0–15) for their fill ratio.

**My machine isn't getting power even though my Coil is full.**
Confirm they're on the **same network** (an unbroken Wave Conduit path) and that
the conduit throughput is enough — swap to **Dense Wave Conduit** for hungry
consumers. The Light Meter shows demand vs. throughput.

**Do the wireless relays need power?**
No, not by default — the base relay is intentionally cheap. The optional **Hush
Cost** would add a small Light tax per sender, but it's off by default.

**Two relays on the same colour aren't talking.**
Both must be on the same **channel** (dye colour) and in compatible **modes**
(one Send, one Receive). Cross-dimension channels need an **Wave Repeater**.

**Can other tech mods use my Light?**
Yes, if **Team Reborn Energy** is installed — the Resonant Coil and Polarity
Coupler bridge at 1 RU = 1 E. See [Compatibility](Compatibility.md).

**Will my world break if I update names?**
No. The "Octaves of the One" reskin is display-only; internal ids stay `echoes:*`,
so saves remain compatible.

**Where's the crushing recipe in JEI/EMI?**
Recipe-viewer support for the custom `crushing` type isn't added yet (on the
roadmap). Crafting/smelting/blasting recipes show up normally.

## Roadmap

The project's forward plan lives in [`docs/roadmap.md`](../roadmap.md), organized
by Russell's cosmology (Phase I generation↔radiation, II the octaves, III the
wave, …). Highlights still open: octave tiers, wearable thrusters, a network
visualizer, advancements, a Great Resonance Cell multiblock, the Silentite "silence"
path, and true emissive textures.
