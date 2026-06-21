# Octaves of the One

A Fabric tech mod for Minecraft **26.1.2**, themed on **Walter Russell's cosmology** —
the *two-way universe* of **rhythmic balanced interchange**. Draw **Light** from the
still centre of zero, wind it up through the octaves by **generation** (compression /
charging), pour it back out by **radiation** (expansion / discharging), and spend it
across a wired *and* wireless grid to run machines, fly, farm, and transmute matter.

> Light is **carried, not consumed**. The cosmology is *flavour, not physics* — but
> every block maps to one of Russell's ideas, so the tech tree reads as a working model
> of his system. See [`docs/cosmology.md`](docs/cosmology.md).
>
> Internally the namespace stays `echoes` and energy is tracked as **RU** for
> save-compatibility; everywhere a player looks, it's **Light**.

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

**3 · Carry & bank it.** **Wave Conduits** move Light on a wired network with a fair,
no-starvation distribution (1,000/t → Dense 16,000/t → Octave 64,000/t). **Resonance
Cells** bank it (250,000 → Greater 2,000,000). The **Balancer** keeps every cell evenly
filled.

**4 · Spend it.** The **Compressor** doubles ore, the **Transmuter** smelts any furnace
recipe with no fuel, and the **radiation** family pours Light back into the world — the
**Growth Radiator** (grows crops), **Warmth Radiator** (cooks drops, melts ice), and
**Polarity Field** (attract items / repel mobs). The **Resonant Thrusters** give
look-direction flight with fall immunity.

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
  roles, bounded `ResonanceStorage` buffers, and a `ResonanceNetwork` that distributes
  with a **largest-remainder proportional allocation** (fair under scarcity, no
  starvation, surplus tops up the emptiest banks first). The `ResonanceNetworkManager`
  merges and splits networks incrementally on conduit place/break — no per-tick flood
  fill — and persists topology across restarts.
- **Wireless transport** — a server-global roster keyed by `GlobalPos`, bounded by a
  per-channel tick budget (widened by Amplifiers, hard-capped) so big builds can't stall
  the tick. Items/fluids ride the Fabric Transfer API (vanilla chests & tanks work); RU
  bridges the node grid.
- **Ambient capture** — a `LivingEntity#onDeath` mixin (25 RU) and a
  `ServerWorld#playSound` mixin charge the nearest Resonant Coil from a **data-driven
  sound→RU table** ([`data/echoes/resonance_sources.json`](src/main/resources/data/echoes/resonance_sources.json),
  reloadable and modpack-extendable): note blocks, anvils, bells, explosions, thunder…
- **The Light-Value economy** — a small hand-authored **seed** set
  ([`light_values.json`](src/main/resources/data/echoes/light_values.json)) is
  authoritative; every other item's value (vanilla *or* modded) is **derived** by
  propagating values through the whole recipe graph to a fixed point (cheapest
  `sum(inputs)/output`). The min-and-floor rule means you can never craft *up* in value,
  so ore progression stays safe. Modpacks get sensible values for free and can override
  via datapack.
- **Custom machine recipes** — a `crushing` recipe type with optional byproducts, plus
  synced screens and a shared **device configuration GUI** (channel, redstone behaviour,
  per-face I/O, block-specific tuning) opened with the Frequency Tuner.
- **Worldgen** — configured/placed features for Echocite & Drumstone (Overworld) and
  Silentite (Deep Dark), plus the Lumewood grove, attached via `BiomeModifications`.
- **Compatibility** — an optional **Team Reborn Energy** bridge (1 RU = 1 E) and a
  **Trinkets** soft-dependency, both inert when the mod is absent.

## Build & run

Requires **JDK 25** (Minecraft 26.1 needs Java 25). Built against Minecraft **26.1.2**
with **Mojang official mappings** — 26.1 is the first unobfuscated Minecraft and Fabric
dropped Yarn — using Fabric Loom `1.17`, Fabric Loader `0.19.3`, Fabric API
`0.152.1+26.1.2`, Gradle `9.5`.

```bash
./gradlew build              # → build/libs/echoes-of-the-deep-0.2.0.jar
./gradlew runClient          # playtest in single-player
./gradlew runServer          # headless smoke test (accept the EULA in run/eula.txt)
./gradlew runClientGametest  # dev-only: screenshot every screen for a layout check
```

`./run.sh <task>` is a thin wrapper that exports a `JAVA_HOME` for you if your system
Java isn't 25. To install, drop the built jar into `.minecraft/mods/` alongside **Fabric
Loader** and **Fabric API** for 26.1.

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
  energy/    ResonanceNode/Storage/Network/Manager, ResonanceEvents, ResonanceSources
  wireless/  WirelessNetworkManager, WirelessDevice, RelayMode
  transmute/ LightValues (EMC derivation), TransmutationState (per-player account)
  block/     blocks + block/entity/ (Coil, Cell, Conduit, machines, relays, garden)
  item/      tools, Thrusters, tuner/atlas/meter, Tablet, Octave Stars
  recipe/    CrushingRecipe, ModRecipes
  screen/    Crusher / Furnace / Filter / Transmutation / Config handlers
  config/    device configuration model
  compat/    Team Reborn Energy bridge
  registry/  ModBlocks, ModItems, ModBlockEntities, ModScreens, ModItemGroups, ModWorldGen
  mixin/     LivingEntityMixin, ServerWorldMixin (ambient capture + fall immunity)
src/client/java/com/echoes/client/  EchoesClient + screen/
src/main/resources/  fabric.mod.json, echoes.mixins.json, assets/, data/
```

## License

[MIT](LICENSE).
