# Compatibility

[← Home](Home.md)

The mod is standalone — it needs only **Fabric API**. Two optional integrations activate
when their mod is present and are completely inert otherwise.

## Team Reborn Energy (active when installed)

An optional bridge exposes the mod's Light buffers as Team Reborn **`EnergyStorage`** at
**1 RU = 1 E**, so other tech mods can read and feed the Resonance grid:

- Bridged blocks: the **Resonant Coil** and the **Wave Coupler**.
- Transaction-safe (a `SnapshotParticipant`), registered on `EnergyStorage.SIDED`.
- Gated by `FabricLoader.isModLoaded("team_reborn_energy")` and compiled as a
  `modCompileOnly` soft dependency, so the class is never touched unless a 1.21.x-compatible
  Team Reborn Energy is installed.

This is the recommended way to connect Octaves of the One to other Fabric tech mods: run a
cable from their machine into a **Wave Coupler**, and the wired Light grid feeds it.

## Trinkets (suggested — planned)

Trinkets is listed as a **suggested** dependency for the planned **Resonant Ring** (a worn
flight item that moves the Thrusters off the hotbar). That feature isn't implemented yet, so
Trinkets currently has **no effect** if installed — it's a forward-looking soft dep, not a
live integration. Flight today is the held **Resonant Thrusters**; see
[Items & Gear](Items-and-Gear.md).

## Recipe viewers (JEI / EMI)

Vanilla-style crafting and smelting recipes show in any recipe viewer automatically. The
custom **Compressor** (`crushing`) recipes don't yet register a viewer category, so they
aren't browsable in JEI/EMI — a known limitation. Until then, the
[Crafting & Progression](Crafting-and-Progression.md) page and the
[HTML wiki](https://trystar360.github.io/echoes-of-the-deep/) document them.

## Modpacks & datapacks

Several systems are intentionally **data-driven** so packs can retune them without code:

- **Light Values** — `data/echoes/light_values.json` (seeds + blacklist; the rest is derived).
- **Ambient sound → Light** — `data/echoes/resonance_sources.json`.
- **Worldgen** — `data/echoes/worldgen/` + biome modifications.
- **Advancements** — `data/echoes/advancement/great_work/` (parent onto `echoes:great_work/<node>`).

Override any of them in a datapack.
