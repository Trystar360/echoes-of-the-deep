# Cosmology & Lore

[← Home](Home.md) · See also the full design doc: [`docs/cosmology.md`](../cosmology.md)

The mod is themed on **Walter Russell's cosmology** — the *two-way universe* of
**rhythmic balanced interchange**. It's flavour, not physics, but every block and
mechanic maps to one of Russell's ideas, so the tech tree reads as a working
model of his system.

## The premise

> *"The universe is a play of Light. All motion springs from a still magnetic
> centre of zero and returns to it, in equal giving and regiving."*

- **Light** is the one substance and the mod's energy. (Tracked internally as
  `ResonanceNode` / RU for save-compatibility; in-world it is always **Light**.)
- **Stillness** is the source. Energy is not created or destroyed — it is *wound
  up* out of the still centre and *unwound* back into it.
- **Rhythmic balanced interchange:** everything is paired **giving**
  (generation, centripetal compression, *charging*) and **regiving** (radiation,
  centrifugal expansion, *discharging*). The grid is one long balanced interchange.
- **Octaves:** tones repeat in octaves. The 16 wireless channels are octaves;
  tuning a device sets its octave.

This vocabulary surfaces in-game: relay modes read **Give / Regive / Stilled**,
the splitter reads **balanced (even split)** vs. **fill-first**, and the meter
reports **Light** rather than RU.

## How the gear maps to the cosmology

| In-world name | Russell concept | What it does |
| --- | --- | --- |
| **Stillness Core** | the still magnetic centre of zero | slow baseline Light generator — "motion springs from rest" |
| **Generative Coil** (Resonator) | generation / centripetal charging | winds ambient tone into stored Light |
| **Accumulator** (Capacitor) | locked potential | banks large Light |
| **Wave / Dense Wave Conduit** | the wave carries Light | wired transport |
| **Compressor** (Crusher) | compression = generative motion | presses matter denser (ore-doubling) |
| **Transmuter** (Attunement Furnace) | raising matter an octave | Light-powered smelting |
| **Wave Relay** + family | tones broadcast on an octave | wireless transport keyed by octave (channel) |
| **Amplitude Coil** (Amplifier) | amplitude of the wave | widens an octave's throughput |
| **Octave Repeater** (Echo Repeater) | octaves repeat across space | spans an octave across dimensions |
| **Polarity Coupler** (Conduit Coupler) | polarity bridges two conditions | couples wired grid to a wireless octave |
| **Interchange Splitter** | balanced interchange | even round-robin vs. fill-first |
| **Locked Potential Vault** (Chest) | locked potential | storage that lives on an octave |
| **Tone Relay** (Note Relay) | the divine tone | wireless redstone on an octave |
| **Radiator / Warmth Radiator** | radiation / centrifugal outpouring | pours Light back as life (crop growth) and heat (cooking) |
| **Polarity Field** | the two poles of one device | Attract (pull in) ↔ Repel (cast out) |
| **Balancer** | rhythmic balanced interchange | evens storage fill ratios across a network |
| **Centrifugal Thrusters** | centrifugal radiation = expansion | flight: the body thrown outward from centre |
| **Resonant tools** | tuned to the octave | deliberately strong — interchange gives back as freely as it takes |

## Why the gear is strong

Russell held that **energy is *carried*, not transmitted**, and that a body tuned
to its octave is sustained by the whole universe's interchange rather than by its
own small store. So Resonant gear is intentionally **over-tuned** — that's the
point, not an oversight. The numbers are named constants
(`ResonanceThrustersItem`, `ModItems.ECHO_MATERIAL`) so a pack author can
re-balance to taste.

## A note on internal names

To keep existing worlds loading, the mod id stays `echoes` and internal
identifiers (e.g. `echoes:resonator`, the `ResonanceNode` capability) are
unchanged. The "Octaves of the One" rework is a complete **display** reskin —
names, lore, tooltips, and framing — plus new content (the Stillness Core,
Radiators, Polarity Field, Balancer) that embodies it.
