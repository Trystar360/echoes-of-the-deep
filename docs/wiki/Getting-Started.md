# Getting Started

[← Home](Home.md)

This page takes you from zero to a working **Light** grid powering your first
machine.

## Installing

Drop the built jar into a 1.21.4 instance alongside **Fabric Loader** and
**Fabric API**:

```
.minecraft/mods/
  fabric-api-*.jar
  echoes-of-the-deep-0.1.0.jar
```

No client-side mod is required — the mod runs on dedicated servers and in
single-player alike (`"environment": "*"`).

## Building from source

Requires **JDK 21**.

```bash
export JAVA_HOME=/path/to/jdk-21      # the build needs a JDK 21
./gradlew build                       # → build/libs/echoes-of-the-deep-0.1.0.jar
./gradlew runClient                   # playtest in single-player
./gradlew runServer                   # headless smoke test (accept run/eula.txt)
```

Or use the bundled `./run.sh <task>` wrapper, which sets `JAVA_HOME` for you.
The Gradle wrapper is pinned to 8.12.

## Your first hour

### 1. Mine Echocite

**Echocite Ore** generates throughout the Overworld (stone and deepslate
variants), roughly **y −20 to 60**. Mining it drops **Raw Echocite**
(Fortune-affected). See [Ores & Worldgen](Ores-and-Worldgen.md).

### 2. Make Echo Ingots

You have two routes from **Raw Echocite**:

- **Smelt / blast** it directly → **Echo Ingot** (1:1).
- **Crush** it in a Compressor → **2× Echocite Dust** (ore-doubling, plus a
  ~15% **Resonant Slag** byproduct), then smelt the dust → **Echo Ingot**.

**Echo Ingot** is the backbone crafting material for nearly everything.

### 3. Build the energy core

Craft these three blocks (recipes on [Crafting & Progression](Crafting-and-Progression.md)):

1. **Generative Coil** (`echoes:resonator`) — generates + stores Light.
2. **Wave Conduit** (`echoes:tuning_conduit`) — carries Light between blocks (×4 per craft).
3. **Compressor** (`echoes:crusher`) — your first Light **consumer** (ore-doubling).

Place the Coil, run a line of Conduit from it to the Compressor, and they form a
single **network**. The Coil charges from **ambient sound** (see below); the
Compressor draws from the grid to crush ore.

### 4. Feed the Coil with sound

A **Generative Coil** charges itself from nearby world sound — note blocks,
bells, anvils landing, explosions, and (rarely, hugely) thunder. Mob deaths
within 8 blocks also add Light. Park a Coil near a note-block contraption or an
anvil to keep it topped up. Full table on [Ambient Capture](Ambient-Capture.md).

### 5. Read your grid

Craft a **Light Meter** (`echoes:resonance_meter`) and right-click any device to
print its role, stored/capacity Light, demand, and conduit throughput — Light is
otherwise invisible.

## Where to go next

- Bank surplus in an **Accumulator** (250k Light) and add a **Stillness Core**
  for steady passive generation — see [Blocks](Blocks.md).
- Skip conduits entirely with the **Wave Relay** wireless system — see
  [Wireless Transport](Wireless-Transport.md).
- Craft **Centrifugal Thrusters** for sound-powered flight, and a set of
  **Resonant tools** — see [Items & Gear](Items-and-Gear.md).
