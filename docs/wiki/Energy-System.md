# Energy System

[← Home](Home.md)

**Light** (tracked internally as **RU**, Resonance Units) is the mod's energy. It moves on
a **wired** network and, separately, over **wireless** channels (see
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

Any energy blocks that **touch face-to-face** automatically share one network — a
generator sitting directly against a machine needs no conduit at all. **Conduits** are
ordinary members that exist to span gaps between clusters. Each network, every tick:

1. Sums available **supply** (providers first, then storage).
2. Gathers **demand** from consumers that have work to do.
3. Distributes supply using a **largest-remainder proportional allocation**, up to the
   network's **throughput budget**.

That allocation is the important part: under scarcity, **every consumer gets a share
proportional to its demand**, and the leftover from rounding goes to the **most-starved**
consumers first — so nothing starves and nothing is wasted. When there's surplus and no
demand, it **tops up storage, emptiest bank first**, so cells fill evenly.

### Throughput

A network of directly-touching blocks transfers **freely** — no ceiling. Once a network
contains conduits, its per-tick budget is the **sum of every conduit's cap**, so longer,
denser, or higher-tier lines genuinely move more:

| Conduit | Contributes |
| --- | --- |
| **Wave Conduit** | 1,000 Light/t each |
| **Dense Wave Conduit** | 16,000 Light/t each (×16) |
| **Octave Conduit** | 64,000 Light/t each (×64) |

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
cell hoards — the grid "breathes evenly." (It moves Light, never creates it: what flows
into under-filled cells is exactly what was drawn from over-filled ones.)

## Reading, tuning & security

Light is invisible, so:

- **Light Meter** (handheld) — right-click any device to read its role, stored / capacity
  Light, demand, and conduit throughput.
- **Frequency Tuner** (right-click) — opens the device **configuration GUI**: wireless
  channel/octave, **redstone** behaviour (always / needs-signal / off-on-signal),
  block-specific **tuning** (e.g. generation rate, radius), and **per-face I/O** on the
  inventory machines (Compressor, Transmuter, Wave Chest). Storage and generator blocks
  also emit a **comparator** signal scaled to their fill.
- **Ownership** — every configurable device belongs to whoever **places** it. The owner
  can flip its security from *Public* (anyone may open/configure) to *Private* (owner
  only) in the same GUI. Conduits carry no config at all — nothing to tune, nothing to
  lock.

## Persistence & performance

- Network **topology persists** across restarts; the manager merges/splits networks
  **incrementally** on place/break — no per-tick flood fill — and drops cached nodes when
  their chunk unloads.
- Very large networks (2,000+ blocks) **stagger** their distribution pass; buffers on
  every node absorb the stagger, so steady-state throughput is unaffected.
- The wireless side **self-heals**: every device carries its channel/mode in NBT and
  re-registers on load.
- The allocation math itself lives in a pure, unit-tested class (`EnergyMath`) — fairness
  and conservation have regression tests.

## Cross-mod energy

An optional **Team Reborn Energy** bridge exposes RU buffers as `EnergyStorage` (1 RU = 1
E) so other tech mods can read and feed the grid. See [Compatibility](Compatibility.md).
