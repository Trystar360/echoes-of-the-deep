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
| **Resonant Coil** | Provider + Storage | 10,000 buffer; charges from ambient sound |
| **Stillness Core** | Provider | 4 Light/t passive; 50,000 buffer |
| **Octave Coil** | Provider | 24 Light/t (tunable); 300,000 buffer |
| **Storm Caller** | Provider + Storage | 40,000 per lightning strike; 400,000 buffer |
| **Resonance Cell** | Storage | 250,000 |
| **Greater Resonance Cell** | Storage | 2,000,000 |

The **Balancer** nudges every storage node on its network toward the same fill ratio, so no
cell hoards — the grid "breathes evenly."

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
