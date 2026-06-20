# Blocks

[← Home](Home.md)

Every block, grouped by role. Names lead with the **in-world display name**; the internal
id is `echoes:<snake_case>` of the same name. For numbers see [Energy System](Energy-System.md)
and [Reference & FAQ](Reference-and-FAQ.md); for recipes see
[Crafting & Progression](Crafting-and-Progression.md).

## Ores

| Block | Drops | Notes |
| --- | --- | --- |
| **Echocite Ore** / **Deepslate Echocite Ore** | Raw Echocite | Overworld; the base of every recipe. |
| **Drumstone Ore** | Drumstone Shard | Overworld; percussive branch (Drum Core). |
| **Silentite Ore** | Silentite Crystal | Deep Dark only; rare. |

See [Ores & Worldgen](Ores-and-Worldgen.md).

## Generators (providers)

| Block | What it does |
| --- | --- |
| **Resonant Coil** | Winds ambient sound into stored Light (10,000 buffer). The starter generator. |
| **Stillness Core** | Trickles 4 Light/t from rest — no sound needed (50,000 buffer). |
| **Octave Coil** | Strong late generator, 24 Light/t (tunable), 300,000 buffer; comparator output. |
| **Storm Caller** | Calls lightning during thunderstorms and banks 40,000 Light/strike (400,000 buffer). Needs open sky; self-struck bolts are cosmetic. |

## Storage

| Block | Capacity |
| --- | --- |
| **Resonance Cell** | 250,000 Light; comparator-readable. |
| **Greater Resonance Cell** | 2,000,000 Light; comparator-readable. |
| **Balancer** | Evens Light across all storage on its network (not a bank itself). |

## Carriers (conduits)

| Block | Throughput |
| --- | --- |
| **Wave Conduit** | 1,000 Light/t |
| **Dense Wave Conduit** | 16,000 Light/t |
| **Octave Conduit** | 64,000 Light/t |

## Machines (consumers)

| Block | What it does |
| --- | --- |
| **Compressor** | Crushes ore into **doubled dust** using Light; custom `crushing` recipes with optional byproducts (raw echocite → ~15% Resonant Slag). Hopper-friendly. |
| **Transmuter** | Smelts **any furnace recipe** with Light and no fuel. |
| **Growth Radiator** | Radiates Light as life — grows nearby crops and saplings; glows. |
| **Warmth Radiator** | Radiates heat — cooks dropped items (vanilla smelting) and melts nearby snow/ice. |
| **Polarity Field** | Toggle **Attract** (pull in items & XP) or **Repel** (cast mobs out). Two poles, one device. |

## Wireless (channel devices)

The **Wave Relay** and its family broadcast over a channel (octave). Full details in
[Wireless Transport](Wireless-Transport.md).

| Block | Role |
| --- | --- |
| **Wave Relay** | Beams items, fluids, and Light over a channel. |
| **Wave Amplifier** | Widens a channel's throughput (up to ×16). |
| **Wave Filter** | Restricts a channel's items to a whitelist (ghost-slot GUI). |
| **Wave Splitter** | Even round-robin vs. fill-first delivery. |
| **Wave Repeater** | Extends a channel across dimensions. |
| **Wave Coupler** | Bridges the wired grid onto a wireless channel. |
| **Wave Chest** | 27-slot storage that lives on a channel. |
| **Signal Relay** | Broadcasts redstone over a channel. |

## Transmutation

| Block | What it does |
| --- | --- |
| **Transmutation Table** | Your Bound-Light terminal: dissolve matter, withdraw Motes, condense attuned items. See [Transmutation](Transmutation.md). |

## The Octave Grove (garden & décor)

A luminous building palette that's useful *and* pretty. Most grow from the **Lumewood
grove** worldgen or craft from its planks.

| Block | What it does |
| --- | --- |
| **Lumewood** set | Log, wood, planks, stairs, slab, fence, gate, trapdoor, glowing leaves & sapling — a full glowing wood family. |
| **Lumebloom** | A glowing flower; grants Glowing when stood in. |
| **Lume Lantern** | A full-bright décor block of woven Light. |
| **Verdant Loam** | Living soil — pulses Light upward to grow nearby plants over a tunable radius/interval. |
| **Echocite Bricks** (+ stairs, slab) | Luminous masonry crafted from Echocite Dust. |
