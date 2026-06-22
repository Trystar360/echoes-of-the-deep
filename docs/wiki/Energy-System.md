# Energy System

[← Home](Home.md)

**Light** (tracked internally as **RU**, Resonance Units) is the mod's energy. It moves on
a **wired** network of conduits and, separately, over **wireless** channels (see
[Wireless Transport](Wireless-Transport.md)). This page covers the wired grid.

## Node roles

Every energy block is a `ResonanceNode` with one or more roles:

| Role | Meaning | Examples |
| --- | --- | --- |
| **Provider** | makes or sources Light | Resonant Coil, Stillness Core, Octave Coil, Storm Caller |
| **Storage** | banks Light (gives and takes) | Resonance Cell, Greater Resonance Cell |
| **Consumer** | draws Light to do work | Compressor, Transmuter, Radiators, Polarity Field |
| **Carrier** | moves Light between nodes | Wave / Dense / Octave Conduit |

Light is **carried, not consumed** in the cosmology sense — but a consumer's buffer does
drain as it works, and the network refills it.

## The wired network

Conduits form a **network** (a connected component). Each network, every tick:

1. Sums available **supply** (providers first, then storage).
2. Gathers **demand** from consumers that have work to do.
3. Distributes supply up to a **throughput budget** (the sum of its conduits' caps) using a
   **largest-remainder proportional allocation**.

That allocation is the important part: under scarcity, **every consumer gets a share
proportional to its demand**, and the leftover from rounding goes to the **most-starved**
consumers first — so nothing starves and nothing is wasted. When there's surplus and no
demand, it **tops up storage, emptiest bank first**, so cells fill evenly.

### Throughput tiers

| Conduit | Throughput |
| --- | --- |
| **Wave Conduit** | 1,000 Light/t |
| **Dense Wave Conduit** | 16,000 Light/t (×16) |
| **Octave Conduit** | 64,000 Light/t (×64) |

A network's total budget is the sum of its conduits' caps, so longer/denser lines move more.

## Generation & storage at a glance

| Block | Role | Number |
| --- | --- | --- |
| **Resonant Coil** | Provider + Storage | 10,000 buffer; converts ambient sound (×1 → ×3 in an array) |
| **Stillness Core** | Provider | 4 Light/t (×1 → ×2); the array **anchor**; 50,000 buffer |
| **Octave Coil** | Provider | 24 Light/t base (×1 → ×4 in an array); 300,000 buffer |
| **Storm Caller** | Provider + Storage | 40,000 per strike (×1 → ×3 in an array); 400,000 buffer |
| **Resonance Cell** | Storage | 250,000 |
| **Greater Resonance Cell** | Storage | 2,000,000 |

The **Balancer** nudges every storage node on its network toward the same fill ratio, so no
cell hoards — the grid "breathes evenly."

## Standing-wave resonance — generators that reward building

A lone generator works at its base rate, but generators don't have to stand alone. The
cosmos is **rhythmic balanced interchange** — standing waves with nodes and antinodes — and
generators play that game directly.

Every generator carries an **Octave** dial (set it with the Frequency Tuner). Generators
tuned to the **same octave** within **10 blocks** form a **resonant array** — but they only
reinforce one another when placed on that octave's **antinode lattice**:

- The **resonant spacing** for octave *o* is **`2 + o` blocks** (octave 0 → 2, octave 8 → 10).
  Higher octave = higher pitch = a wider standing wave.
- A partner sitting *exactly* on an antinode (a whole number of spacings away, on-axis)
  reinforces fully; one more than a block off the antinode does nothing. A sloppy clump
  barely resonates; a clean, axis-aligned lattice at the right pitch resonates fully.
- Up to about **four well-placed partners** saturate the effect.

Each generator turns that coupling into its own **resonance multiplier**:

| Generator | In the array | Multiplier (lone → full) |
| --- | --- | --- |
| **Octave Coil** | the **workhorse** — scales hardest with a tuned lattice | ×1 → **×4** (24 → 96 Light/t) |
| **Stillness Core** | the **anchor** — humble output, but counts as a **×1.5 antinode** that empowers every partner. Build your lattice around it. | ×1 → ×2 (4 → 8 Light/t) |
| **Storm Caller** | the **amplified spire** — a tuned spire array catches a bigger discharge | ×1 → **×3** (40k → 120k per strike) |
| **Resonant Coil** | the **sound converter** — an arrayed Coil converts ambient sound far more efficiently | ×1 → **×3** captured |

Resonance is invisible, so the **Light Meter** reports it on any generator —
`Resonance: ×N.NN (k in-tune, antinode spacing M)` — and a generator that's ringing in a
strong array drifts up a few **note particles** so you can see the lattice working.

## Reading & tuning the grid

Light is invisible, so:

- **Light Meter** (handheld) — right-click any device to read its role, stored / capacity
  Light, demand, and conduit throughput.
- **Frequency Tuner** (sneak-right-click) — opens the device **configuration GUI**:
  wireless channel, **redstone** behaviour (always / needs-signal / off-on-signal), **per-face
  I/O**, and block-specific **tuning** (e.g. generation rate, radius). Storage and
  generator blocks also emit a **comparator** signal scaled to their fill.

## Persistence & performance

- Network **topology persists** across restarts (saved to a `PersistentState`); the manager
  merges/splits networks **incrementally** on conduit place/break — no per-tick flood fill.
- Large networks **stagger** their tick so they don't all compute on the same tick.
- The wireless side **self-heals**: every device carries its channel/mode in NBT and
  re-registers on load.

## Cross-mod energy

An optional **Team Reborn Energy** bridge exposes RU buffers as `EnergyStorage` (1 RU = 1
E) so other tech mods can read and feed the grid. See [Compatibility](Compatibility.md).
