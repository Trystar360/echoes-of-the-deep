# Getting Started

[← Home](Home.md)

This page takes you from a fresh world to your first self-running Light grid. The in-game
**[Great Work](The-Great-Work.md)** advancement tree mirrors these steps, so you can also
open your advancements (`L`) and just follow the toasts.

## Install

1. Install **Fabric Loader** (≥ 0.16.0) and **Fabric API** for Minecraft **1.21.4**.
2. Drop `echoes-of-the-deep-0.1.0.jar` into `.minecraft/mods/`.
3. Launch. Everything is in the **Octaves of the One** creative tab, but you never need
   creative — it's all survival-craftable.

Building from source instead? See the
[README](https://github.com/Trystar360/echoes-of-the-deep#build--run): `./gradlew build`
(JDK 21).

## Your first hour

### 1. Find Echocite
**Echocite Ore** generates throughout the Overworld (deepslate variant lower down). Mine
it with a stone pickaxe or better for **Raw Echocite** — the base of the entire mod.
→ *advancement: "Octaves of the One"*

### 2. Make Echo Ingots
Smelt or blast **Raw Echocite** into an **Echo Ingot**, the core crafting material. *(Once
your grid runs, crush Raw Echocite in a Compressor for doubled dust, then smelt the dust —
free ore-doubling.)* → *"The First Tone"*

### 3. Generate Light
Craft a **Resonant Coil** (iron + Echocite Dust + Echo Ingot). It **winds nearby sound into
stored Light** — place it near anything noisy (a mob farm, note blocks, an anvil) and it
charges. → *"A Winding Engine"*

For a passive trickle that needs no sound, the **Stillness Core** makes 4 Light/t from rest
(needs Silentite from the Deep Dark).

### 4. Carry and bank it
- **Wave Conduit** carries Light on a wired network (1,000/t). Run a line from your Coil to
  a machine. → *"Carried, Not Consumed"*
- **Resonance Cell** banks up to 250,000 Light so surplus isn't wasted. → *"Banked Light"*

A network shares Light **fairly** — under scarcity every machine gets a proportional share
and no one starves. See [Energy System](Energy-System.md).

### 5. Spend it
Place a machine beside your conduit line:
- **Compressor** — doubles ore into dust (then smelt it). → *"Doubling Down"*
- **Transmuter** — runs *any* furnace recipe with Light and **no fuel**. → *"Fuelless Fire"*
- **Growth Radiator** — pours Light back into the world as life, growing nearby crops. The
  **radiation** half of the cosmology. → *"Light as Life"*

### 6. Read the grid
Light is invisible. Craft the **Light Meter** and right-click any device to read its role,
stored Light, demand, and throughput. The **Frequency Tuner** (sneak-right-click a device)
opens its configuration GUI — channel, redstone behaviour, per-face I/O, and tuning.

## Where to go next

- **Fly:** the **Resonant Thrusters** — hold *use* to fly where you look, with fall
  immunity. Recharge on a Coil or Cell. → *"Where You Look"*
- **Go wireless:** the **Wave Relay** beams items, fluids, and Light over a channel with no
  conduit. See [Wireless Transport](Wireless-Transport.md). → *"Cut the Cord"*
- **Climb the octaves:** the **Octave Seed → Radiant Ingot** chain unlocks the high-octave
  tier (Greater Resonance Cell, Octave Coil/Conduit, Storm Caller). See
  [Crafting & Progression](Crafting-and-Progression.md).
- **Transmute matter:** the **Transmutation Table** turns the Light economy into a
  conversion-and-duplication system. See [Transmutation & Light Values](Transmutation.md).

See [Blocks](Blocks.md) and [Items & Gear](Items-and-Gear.md) for the full catalogue.
