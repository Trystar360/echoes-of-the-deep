# Crafting & Progression

[← Home](Home.md)

Every recipe in the mod, and the order they unlock in. All recipes are reachable
in survival — there are no creative-only items (verified by an obtainability
checker). Recipe ingredient ids are `echoes:*` unless prefixed `minecraft:`.

## The tech-tree spine

```
Echocite Ore ──mine──▶ Raw Echocite
                         │
              ┌──────────┴───────────┐
         smelt/blast              Compressor (crush)
              │                        │
              ▼                        ▼
         Echo Ingot ◀──smelt──  Echocite Dust ×2  (+ ~15% Resonant Slag)
              │                        │                     │
              │                        │                  smelt
   ┌──────────┼───────────┐           +Glowstone Dust        ▼
   ▼          ▼           ▼            ▼                  Dull Ingot
 Machines  Conduits     Tools      Echo Dust                 │
 Wireless  Thrusters   (Echo mat)  (Octave Atlas)        Wave Conduit (alt)
```

**Echo Ingot** gates almost everything. Side lines: **Drum Core** (from Drumstone)
feeds the Coil and Thrusters; **Silentite Crystal** (Deep Dark) feeds the
Stillness Core and an alternate Octave Repeater; **Dull Ingot** (from Slag) is a
cheap conduit material.

## Smelting & crushing

| Recipe | Input | Output | Notes |
| --- | --- | --- | --- |
| Smelt / blast | Raw Echocite | Echo Ingot | 0.7 xp, 200 ticks (blast faster) |
| Smelt / blast | Echocite Dust | Echo Ingot | dust route after crushing |
| Smelt / blast | Resonant Slag | Dull Ingot | 0.3 xp |
| **Crushing** (Compressor) | Raw Echocite | **2× Echocite Dust** | energy 200, 120 ticks, +15% Resonant Slag |

## Core blocks (crafting table)

**Generative Coil** — `resonator` (also craftable with a Drum Core in place of the centre Echo Ingot)
```
i e i     i = iron ingot
e c e     e = echocite dust
i e i     c = echo ingot  (or drum core)
```

**Wave Conduit** — `tuning_conduit` → makes **4** (alt: `d r d` with Dull Ingots)
```
e r e     e = echocite dust   r = redstone
```

**Dense Wave Conduit** — `dense_conduit` → makes **2** (shapeless)
```
3× Wave Conduit + Echo Ingot
```

**Accumulator** — `resonance_capacitor`
```
e e e     e = echo ingot
e R e     R = redstone block
e e e
```

**Stillness Core** — `stillness_core`
```
e s e     e = echo ingot
s R s     s = silentite crystal
e s e     R = redstone block
```

**Compressor** — `crusher`
```
C I C     C = cobblestone
I e I     I = iron ingot
C I C     e = echo ingot
```

**Transmuter** — `attunement_furnace`
```
. e .     e = echo ingot
e F e     F = furnace
. e .
```

## Radiation & field blocks

**Radiator** — `radiator`
```
e g e     e = echo ingot
g R g     g = glowstone
e g e     R = redstone block
```

**Warmth Radiator** — `warmth_radiator`
```
e b e     e = echo ingot
b R b     b = blaze powder
e b e     R = redstone block
```

**Polarity Field** — `polarity_field`
```
e r e     e = echo ingot
r I r     r = redstone
e r e     I = iron block
```

**Balancer** — `balancer`
```
e c e     e = echo ingot
e R e     c = comparator
e c e     R = redstone block
```

## Wireless family

**Wave Relay** — `resonant_relay` → makes **2**
```
. R .     R = redstone
e I e     e = echocite dust
. R .     I = iron ingot
```

**Amplitude Coil** — `resonant_amplifier`
```
e r e     e = echo ingot
r R r     r = redstone
e r e     R = wave relay
```

**Octave Repeater** — `echo_repeater` (alt: silentite crystals in place of ender pearls)
```
e p e     e = echo ingot
p R p     p = ender pearl
e p e     R = wave relay
```

Shapeless relay upgrades:

| Result | Ingredients |
| --- | --- |
| **Polarity Coupler** (`conduit_coupler`) | Wave Relay + Wave Conduit + Echo Ingot |
| **Tone Relay** (`note_relay`) | Wave Relay + note block |
| **Locked Potential Vault** (`resonant_chest`) | Wave Relay + chest |
| **Harmonic Filter** (`harmonic_filter`) | Wave Relay + hopper + iron ingot |
| **Interchange Splitter** (`resonant_splitter`) | Wave Relay + comparator |

## Tools & gear

**Centrifugal Thrusters** — `resonance_thrusters`
```
d . d     d = drum core
e p e     e = echo ingot
e . e     p = redstone block
```

**Resonant tools** use the standard vanilla tool shapes with **Echo Ingot** as
the head material and sticks as handles (Pickaxe, Axe, Shovel, Sword, Hoe). See
[Items & Gear](Items-and-Gear.md) for their stats.

**Drum Core** — `drum_core`
```
. s .     s = drumstone shard
s i s     i = iron ingot
. s .
```

## Handheld tools

| Result | Recipe |
| --- | --- |
| **Light Meter** (`resonance_meter`) | Echo Ingot + redstone + comparator (shapeless) |
| **Octave Tuner** (`frequency_tuner`) | 2 Echo Ingot (vertical) + 2 redstone (bottom corners) |
| **Octave Atlas** (`channel_atlas`) | book + Echo Dust (shapeless) |
| **Echo Dust** (`echo_dust`) | Echocite Dust + Glowstone Dust (shapeless) |
