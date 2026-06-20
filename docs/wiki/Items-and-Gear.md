# Items & Gear

[← Home](Home.md)

Materials, tools, flight, the transmutation coins, and the handheld diagnostics. For
recipes see [Crafting & Progression](Crafting-and-Progression.md).

## Materials & intermediates

| Item | What it's for |
| --- | --- |
| **Raw Echocite** | Smelt into an Echo Ingot, or crush for doubled dust. |
| **Echocite Dust** | Smelt into an Echo Ingot (crushed from raw echocite). |
| **Echo Ingot** | The core crafting material of the mod. |
| **Echo Dust** | Echocite + glowstone dust — crafts the Wave Atlas. |
| **Resonant Slag** | ~15% Compressor byproduct — smelt into a Dull Ingot. |
| **Dull Ingot** | A cheap alternate conduit material. |
| **Drumstone Shard** | Four make a Drum Core. |
| **Drum Core** | An alternate Coil membrane; also powers the Thrusters. |
| **Silentite Crystal** | Silent crystal of the Deep Dark — for the Stillness Core. |
| **Octave Seed** | The octave's inert rest point — the catalyst that opens transmutation. |
| **Radiant Dust** → **Radiant Ingot** | Charged matter, a full octave higher; builds the high-octave tier. |

## Resonant tools

A full set — **pickaxe, axe, shovel, sword, hoe** — on the **Echo** tool material.
Deliberately over-tuned: **4,000 durability**, mining speed **12** (faster than netherite),
high enchantability (**22**), and it mines anything. Repair with **Echo Ingots**. Framed
in-world as gear "tuned to the octave — rhythmic balanced interchange."

## Resonant Thrusters

Look-direction flight, powered by Light:

- **Hold *use*** to fly the way you look; **sprint** = faster, **sneak** = hover/brake.
- **Fall-damage immunity** while you carry a charged set.
- Cheap to fly off a big reserve. **Recharge** by right-clicking a **Resonant Coil**,
  **Resonance Cell**, or **Wave Coupler**.
- Portable Light lives on the item itself (a `stored_ru` data component) — fully
  server-side, no client mod needed.

→ advancement *"Where You Look."* See [Getting Started](Getting-Started.md).

## Transmutation items

| Item | What it does |
| --- | --- |
| **Transmutation Tablet** | The portable terminal — opens your Bound-Light account anywhere (maxes at one). |
| **Light / Tonic / Mediant / Dominant / Harmonic Mote** | Bound-Light coins (64 → 16,384, ×4 per octave). |
| **Octave Star I–VI** | Portable Bound-Light batteries (100,000 → 102,400,000, ×4 per tier). |

Full details in [Transmutation & Light Values](Transmutation.md).

## Handheld diagnostics & tools

| Item | What it does |
| --- | --- |
| **Light Meter** | Right-click a device to read its role, stored / capacity Light, demand, and throughput. |
| **Frequency Tuner** | Copy/paste a wireless channel between devices; **sneak-use** opens the device configuration GUI. |
| **Channel Atlas** | Lists the devices active on each channel (octave). |

> **Design note.** The gear is intentionally strong. In-world that's justified by *Light is
> carried, not consumed* — a device tuned to its octave gives back as freely as the grid
> pours in. Tune the constants in `ResonanceThrustersItem` / `ModItems.ECHO_MATERIAL` to
> taste.
