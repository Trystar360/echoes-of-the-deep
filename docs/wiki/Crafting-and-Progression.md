# Crafting & Progression

[← Home](Home.md)

The tech-tree flow, tier by tier. For **exact crafting grids**, the
[HTML wiki](https://trystar360.github.io/echoes-of-the-deep/) renders every recipe as a
clickable grid; the in-game **[Great Work](The-Great-Work.md)** advancements walk you
through the same order.

Everything is reachable in survival — no creative-only stubs.

## Tier 0 — Refining

```
Echocite Ore ──mine──▶ Raw Echocite ──smelt/blast──▶ Echo Ingot
                              │
                              └──crush (Compressor)──▶ Echocite Dust ×2 ──smelt──▶ Echo Ingot
                                                       (+ ~15% Resonant Slag ──smelt──▶ Dull Ingot)
Drumstone Ore ─▶ Drumstone Shard ×4 ─▶ Drum Core
Silentite Ore ─▶ Silentite Crystal
Echo Ingot + glowstone ─▶ Echo Dust
```

**Echo Ingot** is the spine: nearly everything is built from it.

## Tier 1 — The core grid

Built from Echo Ingot, Echocite Dust, iron, and redstone:

| Make | Role |
| --- | --- |
| **Resonant Coil** | generate Light from sound |
| **Resonance Cell** | bank Light |
| **Wave Conduit** | carry Light (also from Dull Ingot) |
| **Compressor** | double ore |
| **Transmuter** | fuelless smelting |

This is the minimum self-running loop: Coil → Conduit → Cell + machines. See
[Energy System](Energy-System.md).

## Tier 2 — Radiation & gear

The other half of the interchange, plus tools and flight:

- **Growth Radiator**, **Warmth Radiator**, **Polarity Field**, **Balancer**
- **Resonant tools** (Echo material) and the **Resonant Thrusters** (needs Drum Cores)
- **Dense Wave Conduit** (×16 throughput)

## Tier 3 — Wireless

The **Wave Relay** (Echocite Dust + iron + redstone) and its family — Amplifier, Filter,
Splitter, Repeater, Coupler, Chest, Signal Relay — plus the **Frequency Tuner** and
**Channel Atlas**. See [Wireless Transport](Wireless-Transport.md).

## Tier 4 — The octave climb

The gateway to the late game runs through the **Octave Seed**:

```
Silentite Crystal + Drum Core + Echo Dust ─▶ Octave Seed
Octave Seed + Echocite Dust ×4 ─▶ Radiant Dust ×4 ──smelt/blast──▶ Radiant Ingot
```

**Radiant Ingot** builds the high-octave tier:

| Make | Role |
| --- | --- |
| **Greater Resonance Cell** | 2,000,000-Light bank (ring of Radiant Ingots around a Resonance Cell) |
| **Octave Coil** | strong generator (Radiant ring around a Stillness Core) |
| **Octave Conduit** | 64,000 Light/t carrier |
| **Storm Caller** | lightning generator (lightning rod + Resonance Cell + Echo Ingots) |

## Tier 5 — The transmutation economy

```
Radiant Ingot + Echocite Bricks + Octave Seed ─▶ Transmutation Table
Echo Ingot + Radiant Ingot (etc.) ─▶ Transmutation Tablet, Octave Stars
```

Dissolve matter into Bound Light, withdraw **Motes**, and condense attuned items. The
full system — Light Values, the Mote ladder, Octave Stars — is on the
[Transmutation & Light Values](Transmutation.md) page.

## Progression map

```
Echocite ─▶ Echo Ingot ─▶ [Coil · Cell · Conduit · Compressor · Transmuter]
                              ├─▶ Radiation (Radiators · Polarity · Balancer)
                              ├─▶ Gear (Tools · Thrusters)
                              └─▶ Wireless (Relay family)
Deep Dark ─▶ Silentite ─▶ Octave Seed ─▶ Radiant Ingot
                              ├─▶ Greater Cell · Octave Coil/Conduit · Storm Caller
                              └─▶ Transmutation Table ─▶ Motes · Octave Stars
```
