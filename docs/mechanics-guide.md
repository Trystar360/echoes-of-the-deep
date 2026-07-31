# Echoes of the Deep — Mechanics Guide

One mod, one economy: **Light**. Everything generates it, stores it, moves it,
spends it — or converts it into matter and back. This guide shows how the four
system families interlock into a single cohesive loop.

## The Light grid (generation → storage → transport → machines)

- **Generate**: Resonant Coil (baseline), Octave Coil (late-game), Storm Caller
  (thunderstorm windfall), Stillness Core (slow, free).
- **Store**: Resonance Cell (250k) → Greater Resonance Cell (2M). The Balancer
  evens charge across the whole grid.
- **Move**: Wave Conduit (1k/t) → Dense Wave Conduit (16k/t) → Octave Conduit
  (64k/t), with per-face I/O configuration on every machine (input / output /
  both / disabled per side, plus redstone gates: always on / needs signal /
  off on signal).
- **Spend**: Compressor (ore doubling + byproducts), Transmuter (fuel-less
  smelting), Resonant Fabricator (autocrafting), Growth/Warmth Radiators.
- **Tune**: every machine has two augment slots — Acceleration Coils (faster,
  costlier) and Efficiency Dampers (cheaper). The Compressor also takes Yield
  Resonators.

## Wireless channels (AE2-style)

The Wave Coupler bridges the wired grid onto a named channel. On a channel,
Wave Relays move items, fluids and Light; Wave Chests add channel storage;
Wave Filters whitelist, Wave Splitters pick round-robin vs. fill-first, Wave
Amplifiers double throughput, Wave Repeaters cross dimensions, and the Signal
Relay broadcasts redstone. The Wave Atlas lists every device per channel; the
Wave Tuner copy/pastes channel assignments; the Light Meter reads any device.

The **Wave Terminal** is the AE2-style wireless terminal: sneak-right-click any
channel device to bind the terminal to that channel, then right-click anywhere
to open the channel's Resonant Chest storage remotely (up to two chests merged
into one live view). The terminal honors locks and redstone gating — it only
reaches chests you may access whose redstone mode currently allows operation.

Every wireless device honors the same TD-style redstone control as wired
machines (always on / needs signal / off on signal, set in its config screen)
— and "powered" means a live local signal **or** a live Signal Relay bus on
its channel, so one Signal Relay can switch an entire channel's relays on or
off from anywhere. A gated-off device stops sending, receiving, and lending
its chest storage until power returns.

Every SEND Wave Relay also carries a **servo filter** (TD-style, per-endpoint):
right-click the relay with an item to tune it in (click again to untune; up to
nine tones). A filtered sender only extracts the listed items from its attached
inventory, and it composes with the channel-wide Wave Filter by intersection —
an item must pass both. Empty-hand click still cycles Give/Regive/Stilled, so
one ore bay can feed several channels with different cargo each.

Because the Fabricator's crafting grid is a real inventory, a Wave Chest (or
hopper) can restock its ingredients and pull its products — the channel
network doubles as your AE2-style autocrafting supply chain.

## Botanicals (Mystical-Agriculture-style)

- **Verdant Loam**: living soil — pulses Light upward, bonemealing plants above
  it (tunable radius/interval).
- **Growth Radiator**: spends banked Light to grow crops/saplings in range.
- **Echo Bloom**: the resource crop. Seeds (lumebloom + echo dust + wheat
  seeds) grow on farmland or Verdant Loam — both auras accelerate it. Mature
  blooms drop **Echo Essence** (Fortune helps).

Echo Essence condenses into resources:

| Recipe | Yield |
| --- | --- |
| E E E (row) | 2 Raw Echocite |
| E over E | 1 Drumstone Shard |
| 2x2 block | 1 Silentite Crystal |
| ring of 8 | 2 Radiant Dust |

So a lit, watered bloom farm is a slow but infinite source of every ore tier.

- **Radiant Bloom (tier 2)**: breed seeds from Echo Bloom Seeds charged with
  four Radiant Dust. It takes root **only in living Verdant Loam** and yields
  **Radiant Essence** — a ring of eight condenses straight into a Radiant
  Ingot (8,192 Bound Light apiece if dissolved instead). A radiant farm is the
  botanical route into the high-octave tier.

- **Abyssal Bloom (tier 3)**: breed seeds from Radiant Bloom Seeds charged
  with four Harmonic Motes. It also demands Verdant Loam — and **darkness**:
  any light level above 4 stalls it, so farm it in caves, roofed rooms, or
  the deep dark. Mature blooms drop **Abyssal Essence** (16,384 Bound Light
  apiece — one full octave above radiant). A ring of eight Radiant Essence
  condenses into four Harmonic Motes (exactly break-even), a ring of eight
  Abyssal Essence yields **Netherite Scrap**, and a 2x2 square yields four
  Silentite Crystals — the endgame sink for a maxed-out farm.

## Transmutation (ProjectE-style)

The Transmutation Table dissolves items into **Bound Light** (the EMC figure,
auto-derived from the recipe graph seeded by `data/echoes/light_values.json`)
and lets you re-create anything you've learned. Motes are the currency
denominations (×4 per octave), Octave Stars are portable batteries, and the
Transmutation Tablet is the pocket terminal.

Echo Essence carries a seed value of 4,096 Bound Light, so the bloom farm and
the table feed each other: farm essence → condense resources → dissolve the
surplus → withdraw whatever you actually need.

The **Resonant Condenser** automates the whole loop (ProjectE's Energy
Condenser): bound to your account, it dissolves anything hoppered into its
top/sides into Bound Light — learning each tone as it goes — and automatically
re-creates one target item into its bottom slot, paying the value from your
pool. Click it with an item to set the target (the item isn't consumed),
sneak-click to clear, empty-hand click for a balance readout. A bloom farm on
a hopper chain becomes a fully automatic ore factory; honors redstone control
like every machine.

## The loop

Mine Echocite → double it in the Compressor → build conduits, cells and
radiators → plant Echo Blooms under a Growth Radiator → condense essence into
materials → dissolve surplus at the Transmutation Table → spend Bound Light on
the high-octave tier (Octave Coil, Greater Resonance Cell, Resonant tools,
Thrusters). Every system consumes what another produces; Light is the only
currency that matters.
