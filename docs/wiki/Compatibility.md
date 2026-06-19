# Compatibility

[← Home](Home.md)

Both integrations are **optional soft dependencies** (`"suggests"` in
`fabric.mod.json`). The mod runs fully standalone; these only activate when the
other mod is present.

## Team Reborn Energy (RU ↔ E bridge)

An optional bridge exposes the mod's Light buffers to other tech mods as Team
Reborn `EnergyStorage`, at **1 RU = 1 E**.

- **Bridged blocks:** the **Generative Coil** (`resonator`) and the **Polarity
  Coupler** (`conduit_coupler`) expose their RU buffers, so other tech mods can
  both **read and feed** the grid.
- **Soft dependency:** compiled against the TR Energy API as `modCompileOnly` and
  gated by `FabricLoader.isModLoaded(...)`, so it activates only when a
  1.21.x-compatible Team Reborn Energy is installed and is **completely inert
  otherwise**.
- **Implementation:** `com.echoes.compat.TeamRebornEnergyCompat` /
  `ResonanceEnergyBridge`. Version pinned via `tr_energy_version` in
  `gradle.properties` (currently 4.0.1).

This lets the Coupler act as a two-way gateway between this mod's grid and any
other Reborn-Energy-compatible system, in addition to its wired↔wireless role.

## Trinkets

Listed as a suggested dependency for planned wearable gear (e.g. a future
**Silence Cloak** trinket — see [`roadmap.md`](../roadmap.md)). Nothing in the
current build requires it; it is forward-looking.

## Recipe viewers (JEI / EMI)

Not yet integrated. The custom **`crushing`** recipe type won't appear in JEI/EMI
recipe lookups until dedicated plugin support is added (tracked on the roadmap).
Standard crafting, smelting, and blasting recipes show up normally.
