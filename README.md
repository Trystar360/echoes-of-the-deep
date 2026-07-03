# Echoes of the Deep

[![Build](https://github.com/Trystar360/echoes-of-the-deep/actions/workflows/build.yml/badge.svg)](https://github.com/Trystar360/echoes-of-the-deep/actions/workflows/build.yml)

A Fabric tech mod for Minecraft **26.1.2**, themed on **Walter Russell's cosmology** —
the *two-way universe* of **rhythmic balanced interchange**. Draw **Light** from the
still centre of zero, wind it up through the octaves by **generation** (compression /
charging), pour it back out by **radiation** (expansion / discharging), and spend it
across a wired *and* wireless grid to run machines, fly, farm, and transmute matter.

> Light is **carried, not consumed**. The cosmology is *flavour, not physics* — but
> every block maps to one of Russell's ideas, so the tech tree reads as a working model
> of his system. See [`docs/cosmology.md`](docs/cosmology.md).
>
> Energy is tracked internally as **RU** for save-compatibility; everywhere a player
> looks, it's **Light**.

## 📖 Wiki

The full, illustrated wiki lives here:

### → **https://trystar360.github.io/echoes-of-the-deep/**

One page per block and item, with **clickable crafting grids** (every ingredient links
to its own page). It's generated straight from the mod's own textures, recipes, and lang
file by [`scripts/build_wiki_site.py`](scripts/build_wiki_site.py) and rebuilt on every
push by the [Pages workflow](.github/workflows/pages.yml), so it never drifts from
source. The long-form guide pages in [`docs/wiki/`](docs/wiki/Home.md) mirror to the
repo's **Wiki tab** automatically.

## The loop, end to end

Everything below is craftable from scratch in survival, and the in-game
**[Great Work advancement tree](docs/wiki/The-Great-Work.md)** guides you through it
step by step.

**1 · Mine & refine.** Echocite ore (Overworld + deepslate) drops **Raw Echocite** →
smelt to an **Echo Ingot**, the core of every recipe. Drumstone and Silentite (Deep
Dark) add the **Drum Core** and **Silentite Crystal** branches.

**2 · Generate Light.** The **Resonant Coil** winds ambient sound into stored Light; the
**Stillness Core** trickles Light from rest (4/t); the **Octave Coil** is a strong late
generator (24/t, tunable); the **Storm Caller** banks lightning (40,000 per strike).

**3 · Carry & bank it.** Energy blocks that touch **auto-join one network**; **Wave
Conduits** span the gaps and set its throughput budget (1,000/t each → Dense 16,000/t →
Octave 64,000/t), shared with a fair, no-starvation distribution. **Resonance Cells**
bank it (250,000 → Greater 2,000,000). The **Balancer** keeps every cell evenly filled.

**4 · Spend it.** The **Compressor** doubles ore, the **Transmuter** smelts any furnace
recipe with no fuel, and the **radiation** family pours Light back into the world — the
**Growth Radiator** (grows crops), **Warmth Radiator** (cooks drops, melts ice), and
**Polarity Field** (attract items / repel mobs). The **Resonant Thrusters** give
look-direction flight, negating fall damage while you thrust.

**5 · Go wireless.** Tune two or more devices to the same **channel** (an octave, one per
dye colour) and they resonate — beaming **items, fluids, and Light** with no conduit. The
**Wave Relay** anchors a family of channel gadgets: **Amplifier** (throughput),
**Filter** (item whitelist), **Splitter** (round-robin / fill-first), **Repeater**
(cross-dimension), **Coupler** (bridge to the wired grid), **Chest** (storage on a
channel), and **Signal Relay** (wireless redstone). The **Frequency Tuner** and **Channel
Atlas** manage and inspect it all.

**6 · Transmute (the Light economy).** Every item carries a **Light Value** — its *Bound
Light* (Russell's "matter is condensed Light"), derived across the entire recipe graph.
The **Transmutation Table** (and portable **Tablet**) is your personal Bound-Light
account: **dissolve** matter to bank its value, **withdraw** it as **Mote** coins
(Light → Tonic → Mediant → Dominant → Harmonic, ×4 per octave), or **condense** any item
you've attuned back out of the pool. **Octave Stars** carry Bound Light in your pocket.

**7 · Build the garden.** The **Lumewood** tree (a full glowing wood set), **Lumebloom**,
**Lume Lantern**, **Echocite Bricks**, and **Verdant Loam** (a soil that pulses Light to
grow nearby plants) make a luminous building palette that's useful *and* pretty.

## Under the hood

- **Wired energy** — a `ResonanceNode` capability with provider / consumer / storage
  roles and bounded `ResonanceStorage` buffers. Any energy blocks that touch
  face-to-face share one `ResonanceNetwork`; conduits span gaps and their summed caps
  become the network's per-tick **throughput budget** (a network with no conduits
  transfers freely). Distribution is a **largest-remainder proportional allocation**
  (fair under scarcity, no starvation, surplus tops up the emptiest banks first) whose
  numeric core lives in `EnergyMath` — pure `long[]` math with a JUnit regression suite,
  because that exact logic has produced real bugs twice. The `ResonanceNetworkManager`
  merges and splits networks incrementally on place/break — no per-tick flood fill —
  invalidates cached nodes on chunk unload, and persists topology across restarts.
- **Wireless transport** — a server-global roster keyed by `GlobalPos`, bounded by a
  per-channel tick budget (widened by Amplifiers, hard-capped) so big builds can't stall
  the tick. Items/fluids ride the Fabric Transfer API (vanilla chests & tanks work); RU
  bridges the node grid.
- **Ambient capture** — a `LivingEntity#die` mixin (default 25 RU, configurable) and a
  `Level#playSound` mixin charge the nearest Resonant Coil from a **data-driven
  sound→RU table** ([`data/echoes/resonance_sources.json`](src/main/resources/data/echoes/resonance_sources.json),
  reloadable and modpack-extendable): note blocks, anvils, bells, explosions, thunder…
  Coil lookup goes through a per-chunk `ResonatorIndex`, since sounds fire constantly.
- **The Light-Value economy** — a small hand-authored **seed** set
  ([`light_values.json`](src/main/resources/data/echoes/light_values.json)) is
  authoritative; every other item's value (vanilla *or* modded) is **derived** by
  propagating values through the whole recipe graph to a fixed point (cheapest
  `sum(inputs)/output`). The min-and-floor rule means you can never craft *up* in value,
  so ore progression stays safe. Modpacks get sensible values for free and can override
  via datapack.
- **Machines & the device GUI** — a `crushing` recipe type with optional byproducts;
  machines share an `AbstractMachineBlockEntity` base (buffer, config, redstone gating,
  progress) so per-machine code is just recipes. The **Frequency Tuner** opens a shared
  configuration GUI: wireless channel/octave, redstone behaviour, block-specific tuning,
  per-face I/O on the inventory machines (Compressor, Transmuter, Wave Chest), and
  **ownership** — a device belongs to whoever places it and can be flipped from
  *Public* to *Private* so only its owner may open or configure it.
- **Worldgen** — configured/placed features for Echocite & Drumstone (Overworld) and
  Silentite (Deep Dark), plus the Lumewood grove, attached via `BiomeModifications`.
- **Compatibility** — an optional **Team Reborn Energy** bridge (1 RU = 1 E) and a
  **Trinkets** soft-dependency, both inert when the mod is absent.

## Configuration

Server tunables live in **`config/echoes.json`**, written with defaults on first launch:

| Key | Default | What it does |
|---|---|---|
| `hushCost` / `hushRuPerSender` | `false` / `20` | Opt-in wireless tax: cargo broadcasts drain Light per active sender. |
| `deathRu` | `25` | Light captured by the nearest Coil when something dies (0 disables). |
| `thrusterCapacity` | `1,000,000` | Resonant Thrusters' Light reserve. |
| `thrusterDrainPerTick` | `8` | Flight cost. |
| `thrusterFlySpeed` / `thrusterSprintSpeed` | `0.85` / `1.45` | Flight speeds, blocks per tick. |

Deeper systems are data-driven instead — Light Values, the sound→RU table, worldgen, and
the advancement tree are all datapack-overridable JSON (see the wiki's
[Compatibility](docs/wiki/Compatibility.md) page).

## Build & run

Requires **JDK 25** (Minecraft 26.1 needs Java 25) — or nothing at all: the Foojay
toolchain resolver auto-downloads a matching JDK if none is installed. Built against
Minecraft **26.1.2** with **Mojang official mappings** — 26.1 is the first unobfuscated
Minecraft and Fabric dropped Yarn — using Fabric Loom `1.17`, Fabric Loader `0.19.3`,
Fabric API `0.152.1+26.1.2`, Gradle `9.5`.

```bash
./gradlew build              # compile + unit tests → build/libs/echoes-of-the-deep-<version>.jar
./gradlew test               # just the unit tests (EnergyMath regression suite)
./gradlew runClient          # playtest in single-player
./gradlew runServer          # headless smoke test (accept the EULA in run/eula.txt)
./gradlew runClientGametest  # dev-only: screenshot every screen for a layout check
```

`./run.sh <task>` is a thin wrapper that exports a `JAVA_HOME` for you if your system
Java isn't 25. To install, drop the built jar into `.minecraft/mods/` alongside **Fabric
Loader** (≥ 0.19.0) and **Fabric API** for 26.1. CI compiles and tests every push and PR
([`build.yml`](.github/workflows/build.yml)).

### Releasing

Pushing a `v*` tag builds the mod and publishes a GitHub Release automatically
([`release.yml`](.github/workflows/release.yml)). It also mirrors the jar to Modrinth
and/or CurseForge, each gated independently so an unconfigured repo doesn't fail the
release — set these per platform to enable it:

| Platform | Repo variable | Repo secret |
|---|---|---|
| Modrinth | `MODRINTH_ID` (project ID) | `MODRINTH_TOKEN` ([modrinth.com](https://modrinth.com) → Settings → API Tokens) |
| CurseForge | `CURSEFORGE_ID` (numeric project ID) | `CURSEFORGE_TOKEN` ([console.curseforge.com](https://console.curseforge.com/)) |

## Art & generation

Every texture is **procedurally generated** by [`gen_textures.py`](gen_textures.py) in one
cohesive *"deep resonance"* style — sculk-dark bases, patinated bronze bezels, teal Light
with bloom, and a recurring sound-wave ripple motif (amber for percussive gear, amethyst
for dimensional). Machine blocks use directional models — a glowing front over a shared
bronze casing — so the whole family reads as one material; the cores frame-animate so they
breathe. The advancement tree is generated by [`gen_advancements.py`](gen_advancements.py),
and the wiki visuals by the scripts in [`scripts/`](scripts/).

## Layout

```
src/main/java/com/echoes/
  energy/    ResonanceNode/Storage/Network/Manager, EnergyMath, ResonatorIndex,
             ResonanceEvents, ResonanceSources
  wireless/  WirelessNetworkManager, WirelessDevice, RelayMode
  transmute/ LightValues (EMC derivation), TransmutationState (per-player account)
  block/     blocks + block/entity/ (AbstractMachineBlockEntity, Coil, Cell, Conduit,
             machines, relays, garden)
  item/      tools, Thrusters, tuner/atlas/meter, Tablet, Octave Stars
  recipe/    CrushingRecipe, ModRecipes
  screen/    Crusher / Furnace / Filter / Transmutation / Config handlers
  config/    device configuration model + EchoesConfig (config/echoes.json)
  compat/    Team Reborn Energy bridge
  registry/  ModBlocks, ModItems, ModBlockEntities, ModScreens, ModItemGroups, ModWorldGen
  mixin/     LivingEntityMixin, LevelSoundMixin (ambient capture + fall immunity)
src/client/java/com/echoes/client/  EchoesClient + screen/
src/test/java/com/echoes/          unit tests (EnergyMath regression suite)
src/main/resources/  fabric.mod.json, echoes.mixins.json, assets/, data/
```

## Contributing

Bug reports and PRs welcome. Translating the mod into another language is a
self-contained, no-build-step contribution — see [`CONTRIBUTING.md`](CONTRIBUTING.md).

## License

[MIT](LICENSE).
