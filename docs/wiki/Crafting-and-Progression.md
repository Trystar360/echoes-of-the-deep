# Crafting & Progression

[← Home](Home.md)

Every recipe in the mod, shown as crafting-grid widgets, and the order they unlock
in. All recipes are reachable in survival — there are no creative-only items
(verified by an obtainability checker).

> Recipe images are generated from the real textures by
> [`scripts/gen_wiki_recipes.py`](https://github.com/Trystar360/echoes-of-the-deep/blob/main/scripts/gen_wiki_recipes.py);
> vanilla ingredients use compact pixel-art stand-ins. The text key under each
> "▸ layout" toggle is the source of truth.

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

| | Recipe | Notes |
| --- | --- | --- |
| <img src="images/recipes/echo_ingot_from_smelting.png" height="58"> | Raw Echocite → **Echo Ingot** | smelt or blast · 0.7 xp · 200t |
| <img src="images/recipes/echo_ingot_from_dust_smelting.png" height="58"> | Echocite Dust → **Echo Ingot** | the dust route after crushing |
| <img src="images/recipes/crusher_echocite.png" height="58"> | Raw Echocite → **2× Echocite Dust** | **Compressor** · +15% Resonant Slag |
| <img src="images/recipes/dull_ingot_from_smelting.png" height="58"> | Resonant Slag → **Dull Ingot** | smelt · 0.3 xp |

## Core blocks

### Generative Coil
<img src="images/recipes/resonator.png" width="230">

<details><summary>▸ layout</summary>

```
i e i     i = iron ingot
e c e     e = echocite dust
i e i     c = echo ingot
```
Also craftable with a **Drum Core** in the centre instead of an Echo Ingot.
</details>

### Wave Conduit (makes 4)
<img src="images/recipes/tuning_conduit.png" width="230">

<details><summary>▸ layout</summary>

```
e r e     e = echocite dust   r = redstone
```
Alt: `d r d` with **Dull Ingots** in place of dust.
</details>

### Dense Wave Conduit (makes 2)
<img src="images/recipes/dense_conduit.png" width="230">

<details><summary>▸ layout</summary>

Shapeless: 3× **Wave Conduit** + **Echo Ingot**.
</details>

### Accumulator
<img src="images/recipes/resonance_capacitor.png" width="230">

<details><summary>▸ layout</summary>

```
e e e     e = echo ingot
e R e     R = redstone block
e e e
```
</details>

### Stillness Core
<img src="images/recipes/stillness_core.png" width="230">

<details><summary>▸ layout</summary>

```
e s e     e = echo ingot
s R s     s = silentite crystal
e s e     R = redstone block
```
</details>

### Compressor
<img src="images/recipes/crusher.png" width="230">

<details><summary>▸ layout</summary>

```
C I C     C = cobblestone
I e I     I = iron ingot
C I C     e = echo ingot
```
</details>

### Transmuter
<img src="images/recipes/attunement_furnace.png" width="230">

<details><summary>▸ layout</summary>

```
. e .     e = echo ingot
e F e     F = furnace
. e .
```
</details>

## Radiation & field blocks

### Radiator
<img src="images/recipes/radiator.png" width="230">

<details><summary>▸ layout</summary>

```
e g e     e = echo ingot
g R g     g = glowstone
e g e     R = redstone block
```
</details>

### Warmth Radiator
<img src="images/recipes/warmth_radiator.png" width="230">

<details><summary>▸ layout</summary>

```
e b e     e = echo ingot
b R b     b = blaze powder
e b e     R = redstone block
```
</details>

### Polarity Field
<img src="images/recipes/polarity_field.png" width="230">

<details><summary>▸ layout</summary>

```
e r e     e = echo ingot
r I r     r = redstone
e r e     I = iron block
```
</details>

### Balancer
<img src="images/recipes/balancer.png" width="230">

<details><summary>▸ layout</summary>

```
e c e     e = echo ingot
e R e     c = comparator
e c e     R = redstone block
```
</details>

## Wireless family

The **Wave Relay** is the root; every other channel gadget is a relay upgrade.

### Wave Relay (makes 2)
<img src="images/recipes/resonant_relay.png" width="230">

<details><summary>▸ layout</summary>

```
. R .     R = redstone
e I e     e = echocite dust
. R .     I = iron ingot
```
</details>

### Amplitude Coil
<img src="images/recipes/resonant_amplifier.png" width="230">

<details><summary>▸ layout</summary>

```
e r e     e = echo ingot   r = redstone
r R r     R = wave relay
e r e
```
</details>

### Octave Repeater
<img src="images/recipes/echo_repeater.png" width="230">

<details><summary>▸ layout</summary>

```
e p e     e = echo ingot   p = ender pearl
p R p     R = wave relay
e p e
```
Alt: **Silentite crystals** in place of ender pearls.
</details>

### Relay upgrades (shapeless)

| | Result | Ingredients |
| --- | --- | --- |
| <img src="images/recipes/conduit_coupler.png" width="200"> | **Polarity Coupler** | Wave Relay + Wave Conduit + Echo Ingot |
| <img src="images/recipes/note_relay.png" width="200"> | **Tone Relay** | Wave Relay + note block |
| <img src="images/recipes/resonant_chest.png" width="200"> | **Locked Potential Vault** | Wave Relay + chest |
| <img src="images/recipes/harmonic_filter.png" width="200"> | **Harmonic Filter** | Wave Relay + hopper + iron ingot |
| <img src="images/recipes/resonant_splitter.png" width="200"> | **Interchange Splitter** | Wave Relay + comparator |

## Tools & gear

### Centrifugal Thrusters
<img src="images/recipes/resonance_thrusters.png" width="230">

<details><summary>▸ layout</summary>

```
d . d     d = drum core
e p e     e = echo ingot
e . e     p = redstone block
```
</details>

### Drum Core
<img src="images/recipes/drum_core.png" width="230">

<details><summary>▸ layout</summary>

```
. s .     s = drumstone shard
s i s     i = iron ingot
. s .
```
</details>

### Resonant tools

Standard vanilla tool shapes with **Echo Ingot** heads and stick handles. See
[Items & Gear](Items-and-Gear.md) for their (deliberately over-tuned) stats.

<img src="images/recipes/resonant_pickaxe.png" width="170"> <img src="images/recipes/resonant_sword.png" width="170"> <img src="images/recipes/resonant_axe.png" width="170">
<img src="images/recipes/resonant_shovel.png" width="170"> <img src="images/recipes/resonant_hoe.png" width="170">

## Handheld tools

| | Result | Ingredients |
| --- | --- | --- |
| <img src="images/recipes/resonance_meter.png" width="200"> | **Light Meter** | Echo Ingot + redstone + comparator |
| <img src="images/recipes/frequency_tuner.png" width="200"> | **Octave Tuner** | 2 Echo Ingot + 2 redstone |
| <img src="images/recipes/channel_atlas.png" width="200"> | **Octave Atlas** | book + Echo Dust |
| <img src="images/recipes/echo_dust.png" width="200"> | **Echo Dust** | Echocite Dust + Glowstone Dust |
