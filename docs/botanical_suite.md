# The Verdant Octave — a botanical & crop suite

> *Light is wound into a seed, and the seed gives it back as a harvest. The garden
> is the slowest octave of the grid — generation by growth, radiation by bloom.*

This document is the **design plan** for a full botanical octave: food crops,
decorative flora, new wood types, and a Mystical-Agriculture–style **resource-crop**
economy built on a single base essence that is **compressed** and reused across the
whole tree. It extends the existing **Octave Grove** (Lumewood, Lumebloom, Verdant
Loam) rather than replacing it, and it plugs into the systems already in the mod —
the **Light/Resonance** grid, the **Compressor**, the **Transmuter**, the **Growth
Radiator**, and the **transmutation chain** (Octave Seed → Radiant Dust → Radiant
Ingot).

Nothing here is built yet. This is the spec; statuses use the repo legend
✅ done · 🔜 next · ⬜ planned. Today everything is ⬜ unless noted.

---

## 1. Design pillars

1. **One base crop feeds everything.** Like Mystical Agriculture's Inferium, a single
   crop drops the binding essence used in *every* seed, soil, tool, and tier-up recipe.
   Grow that one crop and you can bootstrap the entire suite. (See §3.)
2. **Compression is the spine.** The base essence is the literal "base crop that is
   compressed and used for different recipes." It compresses two independent ways —
   **density** (9→1 storage ladder) and **octave** (potency tier-up, charged in the
   Transmuter) — and both ladders run through the existing **Compressor** so the
   roadmap's "custom Compressor recipes" finally have a payload. (See §4.)
3. **It maps to the cosmology.** Growth *is* generation (Light wound into matter);
   bloom and harvest *are* radiation (Light given back). Octaves of essence are
   literally Russell's octaves. The garden is the grid's slowest, most living loop.
4. **Reuse what exists.** Verdant Loam, the Growth Radiator, the Compressor, the
   Transmuter, the Octave Seed, and Radiant Dust/Ingot are all already in the mod. The
   suite slots into them; the only genuinely new *systems* are the crop block, the soil
   tiers, and the auto-harvester.
5. **Survival-craftable, end to end.** Every item is reachable from the base crop, and
   the obtainability checker (already in the build) must stay green.

---

## 2. The progression at a glance

```
                 plant on grass / Verdant Loam
  Mote Seed  ──────────────────────────────────►  Lumen Essence  (the base crop)
                                                        │
        ┌───────────────────────────────────────────────┼───────────────────────────┐
        │ DENSITY ladder (Compressor / 3×3)              │ OCTAVE ladder (Transmuter) │
        ▼                                                ▼                            │
  9 Essence → Essence Block          Lumen → Resonant → Radiant → Brilliant → Zenith  │
  (storage / décor / bulk fuel)      (1st     2nd        3rd        4th         5th)   │
                                       octave essences; higher octave = stronger seeds)│
        │                                                                              │
        ▼                                                                              ▼
  Seed Frame (core) + 4 Lumen Essence + a resource  ─────────►  Resource Seed  (grows that resource)
                                                                       │
   needs the matching SOIL TIER to grow ◄───────────────────  Verdant Loam → Attuned → Radiant → Zenith Loam
                                                                       │
                                                                       ▼
                                              Resource Essence → (smelt / craft) → the resource
```

Three crop families ride this same machinery: **resource crops** (§5), **food crops**
(§6), and **decorative flora** (§7). **Wood types** (§8) are their own grove-grown
family. **Tools & automation** (§9) tie it back to the grid.

---

## 3. The base crop — Lumen Essence

The heart of the suite, modelled on Inferium.

| Thing | Name | Notes |
| --- | --- | --- |
| Seed item | **Mote Seed** | Drops from tall grass/foliage (low %) and from the Octave Grove feature, so the player can start with zero prior tech. Cheap fallback craft: 4 Lumen Essence in a square → 1 Mote Seed (self-seeding once you have any). |
| Crop block | **Lumen Sprout** | A 7-stage crop (like wheat). Glows faintly at maturity (theme: ripe Light). Harvest yields **1 Lumen Essence + occasional Mote Seed**. |
| Base essence | **Lumen Essence** | The universal binder. Used in every seed, every soil tier, the watering can, growth accelerators, and the octave tier-ups. *This is the "base crop that is compressed."* |

**Lumen Essence is consumed everywhere**, so the loop is "grow more base crop to do
anything," exactly like Inferium. Growth is sped by **Verdant Loam** and the **Growth
Radiator** (both already in the mod), giving the existing blocks a real job.

---

## 4. Compression — the two ladders

This is the requested mechanic: a base crop that is compressed and reused.

### 4a. Density ladder (storage & bulk)
Pure 9↔1 compression, reversible, via a 3×3 grid **and** as fast **Compressor** recipes:

```
9 Lumen Essence            → 1 Lumen Essence Block      (and back, 1 → 9)
```

The **Lumen Essence Block** is a glowing décor/storage block (a soft-light cousin of
the Lume Lantern) and a **bulk Light fuel**: fed to the Compressor/Transmuter it tops
up their internal Light buffer, closing the food→energy loop. Optional second rung —
**Compressed Lumen Block** (9 blocks) — for storage hoarders.

### 4b. Octave ladder (potency / tier-up)
The progression spine, mirroring Inferium → Prudentium → Tertium → Imperium →
Supremium, but renamed to **octaves** because that *is* the cosmology. Each tier is
the previous essence "raised an octave" — charged in the **Transmuter** (the
"raising matter an octave" machine already in the mod) with an **Octave Seed** catalyst.

| Octave | Essence | Charge recipe (in the Transmuter) | Theme |
| --- | --- | --- | --- |
| 1st | **Lumen Essence** | — (base crop) | the audible base tone |
| 2nd | **Resonant Essence** | 4 Lumen Essence + Octave Seed | the grid's own note |
| 3rd | **Radiant Essence** | 4 Resonant Essence + Octave Seed | folds into the existing **Radiant Dust** tier |
| 4th | **Brilliant Essence** | 4 Radiant Essence + Octave Seed | overtone |
| 5th | **Zenith Essence** | 4 Brilliant Essence + Octave Seed | the still apex (ties to Phase V Zero-Point) |

Higher-octave essence is required for higher-tier **resource seeds** and **soil tiers**,
so the player climbs the octave ladder to grow rarer resources — the classic MA gate.
The 3rd octave deliberately *is* Radiant, so the botanical tree and the existing
transmutation chain are one tree, not two.

---

## 5. Resource crops — ores & materials

The Mystical-Agriculture core: a crop per resource that grows a **resource essence**,
which smelts/crafts into the resource. This is the requested **ores & materials** set.

### 5a. How a resource seed is made
```
Seed Frame (core)  +  4 Lumen Essence  +  1 of the resource   → 1 Resource Seed
```
- **Seed Frame** = the reusable crafting core (Lumen Essence + Drumstone Shard +
  Echo Dust), analogous to MA's Tier Crafting Seed. Higher tiers use a **Resonant /
  Radiant Seed Frame** built from the matching octave essence.
- The seed's **tier** sets which **soil tier** it must be planted on to mature (see §5c).

### 5b. The crops
Each grows a **<Resource> Essence**; **8 essence → 1 unit** of the resource (ingot,
gem, dust). Grouped by tier (= octave/soil gate):

| Tier (octave) | Vanilla resource crops | Mod resource crops |
| --- | --- | --- |
| 1 — Lumen | Coal, Copper, Iron, Redstone, Nether Quartz | **Echocite** (raw_echocite) |
| 2 — Resonant | Gold, Lapis, Glowstone, Amethyst | **Drumstone** (shards) |
| 3 — Radiant | Diamond, Emerald, Sculk/XP | **Silentite** (crystals), **Radiant** (grows Radiant Dust directly) |
| 4 — Brilliant | Quartz/Netherrack bulk, Blaze, Ender | **Dull-metal / Resonant Slag** |
| 5 — Zenith | **Netherite scrap**, Nether Star (rare, slow) | — |

> **Why the mod's own ores grow too:** the Echocite/Drumstone/Silentite/Radiant crops
> turn the whole resonance economy renewable, which is the point of an MA-style suite —
> and the **Radiant crop** literally grows **Radiant Dust**, feeding the octave ladder
> back into itself.

### 5c. Soil tiers (the grow-gate)
Higher-octave seeds need richer soil — built straight on **Verdant Loam** (already in
the mod) so it gains a tier ladder instead of a one-off:

| Soil | Built from | Grows up to |
| --- | --- | --- |
| **Verdant Loam** (✅ exists) | — | tier 1 crops |
| **Attuned Loam** | Verdant Loam + 4 Resonant Essence | tier ≤ 2 |
| **Radiant Loam** | Attuned Loam + 4 Radiant Essence | tier ≤ 3 |
| **Brilliant Loam** | Radiant Loam + 4 Brilliant Essence | tier ≤ 4 |
| **Zenith Loam** | Brilliant Loam + 4 Zenith Essence | tier ≤ 5 |

A seed planted on too-low a tier simply won't advance past stage 0 (clear feedback via
tooltip/particles). Verdant Loam's existing Light-pulse aura and the Growth Radiator
both accelerate any tier.

### 5d. Mob-essence crops (small, themed set)
MA's "mob chunk" crops, kept short and on-theme (sound / Deep Dark):
- **Sculk crop** → Sculk + XP motes · **Hush crop** → Echo Shards / Sculk catalyst bits
- **Spectre crop** → Phantom Membrane · **Cinder crop** → Blaze Powder
Built on a **Soulium-equivalent** frame (Silentite-based **Hush Frame**), gated to
tier ≥ 3 so mob farming is mid/late, not free.

---

## 6. Food crops

Edible botanicals, several with light buffs that fit the Light theme. All compostable.

| Crop | Item(s) | Effect / use |
| --- | --- | --- |
| **Chime Grain** | grain → **Resonant Bread** | wheat-analogue; the staple. |
| **Lumeberry Bush** | **Lumeberry** | sweet-berry-style bush; eating grants brief **Glowing** + small heal. |
| **Tuneroot** | **Tuneroot** (raw/roasted) | carrot/potato-analogue; roast in any furnace. |
| **Echo Gourd** | **Echo Gourd** slices | melon-analogue; high stack-restore, low saturation. |
| **Stillfruit** | **Stillfruit** | rare grove fruit; restores hunger **and** grants short Saturation + Regeneration (the "still centre" food). |
| **Glowcap** | **Glowcap** (mushroom) | grows on Lumewood/loam in dim light; into **Glowcap Stew** (suspicious-stew-style, Night Vision). |
| **Hush Pepper** | **Hush Pepper** | Deep-Dark crop; into spicy food that briefly **mutes** your sound emissions (mini Phase VI tie-in). |

**Cooking combos** (give the crops synergy, not just parallel foods):
- **Lumen Pie** = Chime Grain + Lumeberry + sugar — strong, long saturation.
- **Resonant Stew** = Tuneroot + Echo Gourd + any mushroom — bowl food, brief Haste.
- **Grove Salad** = three different food crops — fills a lot, light saturation.

---

## 7. Decorative flora

Build-and-garden material, extending Lumebloom/Lume Lantern into a full set.

- **Flowers (placeable, some glow):** Chime Lily, Octave Orchid, **Star-of-Zero**
  (white, faint glow), Verdant Fern, Prism Petal (refracts light into a tiny rainbow
  particle), plus the existing **Lumebloom**.
- **Dye crops:** a handful of flowers that mill into **botanical dyes** (covering the
  colours not easily farmable in vanilla), so the 16 wireless **octave/channel** dyes
  become fully grow-able — nice synergy with the relay family.
- **Tall / vine flora:** **Resonant Reeds** (sugar-cane-analogue on water/loam, yields a
  papery **Frond** for décor & books), **Bloomvine** (glowing climbing vine), **Grove
  Moss / Lumemoss** carpet (a glowing moss-carpet + mossy variants).
- **Arrangement blocks:** flower-pot variants, **Lumemoss** blocks/carpets, leaf
  **hedges** (from each wood's leaves), and **Petal Blocks** (flower-block décor like
  the moss/azalea family) to make the grove buildable.

---

## 8. Wood types — the grove's trees

Beyond **Lumewood** (✅), three more full wood sets, each a complete vanilla-parity
family: **log, stripped log, wood, stripped wood, planks, stairs, slab, fence, gate,
trapdoor, door, button, pressure plate, sign/hanging sign, leaves, sapling.** Each tree
generates as a worldgen feature and/or grows from its sapling on grass/loam.

| Wood | Palette / vibe | Hook into the mod |
| --- | --- | --- |
| **Lumewood** (✅) | teal, glowing | the original Octave Grove tree |
| **Drumwood** | warm amber, hollow "knock" | percussive: planks act as a tonal block (note-block-ish), ties to Drumstone |
| **Hushwood** | deep-dark indigo, matte | **sound-dampening leaves** (muffle vibrations — Phase VI / sculk synergy); grows in/near Deep Dark |
| **Sunwood** (Radiant) | golden, emits bright light | the radiant-tier tree; planks glow full-bright, ties to the Radiant octave |

**Wood as a crop, too:** a tier-1 **Heartwood crop** grows **Bark Essence** → any-log
(a generic wood source for players without a grove), and per-wood saplings remain the
"real" way to get each species. This satisfies both "wood types" and "wood crops."

---

## 9. Tools & automation

Tie the garden back to the grid so it automates with existing systems.

- **Lumewater Can** — a watering-can: right-click tilled soil/crops to boost growth
  chance in a small area; holds "charges" of Light, recharged at a Resonator/Cell.
- **Harvest Resonator** *(new block entity)* — RU-powered auto-harvester: on a tick
  budget, breaks mature crops in a radius, replants, and pushes drops into an adjacent
  inventory via the **Transfer API** (same pattern as the Crusher). The garden's answer
  to the existing machine family; bounded tick cost like everything else.
- **Reuse:** **Growth Radiator** (✅ accelerates crops) + **Verdant Loam** (✅ growth
  aura) are the in-place accelerators; the **Compressor** runs the density ladder; the
  **Transmuter** runs the octave ladder. No new energy plumbing required.
- **Fertilized Essence** — a bone-meal analogue (Lumen Essence + bone meal) that, used
  on the **Lumen Sprout**, can pop an extra essence — the MA "Fertilized Essence" QoL.

---

## 10. Worldgen & getting started

- **Mote Seed** drops sparsely from breaking grass/ferns and generates as a sprinkle in
  the **Octave Grove** feature, so a fresh world can bootstrap the base crop with no
  prerequisites.
- New trees (**Drumwood/Hushwood/Sunwood**) attach via `BiomeModifications` like the
  existing Lumewood grove — Drumwood in plains/forest, Hushwood biased to Deep Dark
  adjacency, Sunwood rare/grove-only.
- Decorative flora scatters lightly in the grove biome so it reads as a distinct,
  glowing garden region.

---

## 11. Art & data pipeline

Follows the repo's existing tooling so the suite stays one cohesive style:
- **Textures** procedurally generated by extending `gen_textures.py` /
  `gen_phase2_assets.py` (the "deep resonance" palette: teal Light bloom, bronze
  bezels, amber percussion, indigo hush, gold radiance). Crops get 7-stage strips;
  glowing items get the bloom treatment.
- **Crops/soil/blocks** registered through the existing `ModBlocks` / `ModItems` /
  `ModBlockEntities` / `ModWorldGen` registries; recipes as JSON (3×3, smelting, and
  the custom **Compressor**/**Transmuter** recipe types already defined).
- **Wiki** auto-generates from textures + recipes + lang via the existing
  `scripts/build_wiki_site.py`, so every new crop/seed/essence gets a clickable page
  for free.
- **Obtainability checker** must stay green: every essence, seed, soil, food, flower,
  and wood item reachable from the base **Lumen Essence**.

---

## 12. Suggested build order (incremental PRs)

Each step is independently shippable and survival-complete on its own.

1. **Base loop** — Mote Seed, Lumen Sprout crop, Lumen Essence, Lumen Essence Block
   (density ladder + Compressor recipes), Seed Frame. *Now the spine exists.*
2. **Octave ladder** — Resonant/Radiant/Brilliant/Zenith essences via the Transmuter;
   wire Radiant into the existing Radiant Dust tier.
3. **Soil tiers** — Attuned/Radiant/Brilliant/Zenith Loam on top of Verdant Loam.
4. **Resource crops** — tier-1 set first (Iron/Copper/Coal/Redstone/Echocite), then
   higher tiers; `8 essence → resource`.
5. **Automation** — Harvest Resonator + Lumewater Can + Fertilized Essence.
6. **Food crops** — staples first (Chime Grain, Tuneroot, Lumeberry), then combos.
7. **Decorative flora** — flowers, dye crops, moss/petal blocks.
8. **Wood types** — Drumwood, then Hushwood, then Sunwood; Heartwood crop.
9. **Mob-essence crops** — last, gated behind tier 3+.

> **Roadmap placement:** this whole document is **Phase II½ — The Verdant Octave**,
> a breadth expansion of the Octave Grove. It depends only on systems already shipped
> (Compressor, Transmuter, Verdant Loam, Growth Radiator, transmutation chain).
</content>
</invoke>
