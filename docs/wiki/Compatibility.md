# Compatibility

[← Home](Home.md)

The mod is standalone — it needs only **Fabric API**. Two optional integrations activate
when their mod is present and are completely inert otherwise.

## Team Reborn Energy (active when installed)

An optional bridge exposes the mod's Light buffers as Team Reborn **`EnergyStorage`** at
**1 RU = 1 E**, so other tech mods can read and feed the Resonance grid:

- Bridged blocks: the **Resonant Coil** and the **Wave Coupler**.
- Transaction-safe (a `SnapshotParticipant`), registered on `EnergyStorage.SIDED`.
- Gated by `FabricLoader.isModLoaded("team_reborn_energy")` and compiled against a tiny
  in-tree API stub that is **never shipped in the jar**, so nothing loads unless a
  26.1-compatible Team Reborn Energy is actually installed.

This is the recommended way to connect Echoes of the Deep to other Fabric tech mods: run a
cable from their machine into a **Wave Coupler**, and the wired Light grid feeds it.

## Trinkets (suggested — planned)

Trinkets is listed as a **suggested** dependency for the planned **Resonant Ring** (a worn
flight item that moves the Thrusters off the hotbar). That feature isn't implemented yet, so
Trinkets currently has **no effect** if installed — it's a forward-looking soft dep, not a
live integration. Flight today is the held **Resonant Thrusters**; see
[Items & Gear](Items-and-Gear.md).

## Recipe viewers (JEI / EMI)

Vanilla-style crafting and smelting recipes show in any recipe viewer automatically. The
custom **Compressor** (`crushing`) recipes don't register a viewer category — and can't
yet: no recipe-viewer mod has published a Minecraft 26.1 build at the time of writing.
Until the ecosystem catches up, the [Crafting & Progression](Crafting-and-Progression.md)
page and the [HTML wiki](https://trystar360.github.io/echoes-of-the-deep/) document every
recipe.

## Server config

Runtime tunables live in **`config/echoes.json`**, written with defaults on first launch:
the wireless **Hush Cost** toggle and rate, the **death-capture** Light amount, and the
**Resonant Thrusters**' capacity, drain, and speeds. The full key table is in
[Reference & FAQ](Reference-and-FAQ.md#server-config-configechoesjson).

## Modpacks & datapacks

Several systems are intentionally **data-driven** so packs can retune them without code:

- **Light Values** — `data/echoes/light_values.json` (seeds + blacklist; the rest is derived).
- **Ambient sound → Light** — `data/echoes/resonance_sources.json`.
- **Worldgen** — `data/echoes/worldgen/` + biome modifications.
- **Advancements** — `data/echoes/advancement/great_work/` (parent onto `echoes:great_work/<node>`).

Override any of them in a datapack.
