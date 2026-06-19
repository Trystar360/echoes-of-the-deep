# Botanicals & crops proposal — the Verdant Octave

Goal: add a **Mystical-Agriculture–style** botanical suite — food crops, decorative
plants, new wood types, and an **ore/material crop economy** — built on a single
**base crop whose harvest is compressed and reused as a crafting currency** across the
whole tree. Names follow the locked scheme in [`naming-proposal.md`](naming-proposal.md):
id == display name, both Russell-themed **and** obviously functional, grouped into small
consistent families.

> Status: **proposal — awaiting sign-off.** No Java/assets land until approved. This
> doc is the spec; it reuses systems already shipped (Growth Radiator, Verdant Loam,
> Compressor, Transmuter, Octave Seed, the Lumewood/Lumebloom/Lume Lantern garden set,
> and the echocite/drumstone/silentite/echo/radiant material chains).

## Design conventions (the crop families)

- **Base currency → "… Mote"** — `Light Mote → Condensed Mote → Radiant Mote` (3 tiers,
  the inferium/prudentium/tertium analogue). The harvest of the base crop; the universal
  binder in seed, soil, and tier recipes. *"Radiant" deliberately ties into the existing
  Radiant Dust/Ingot tier so the trees are one tree.*
- **Plant block → "… Sprout"** — the growing crop (staged like wheat). Seeds are
  **"… Seed(s)"**. So a crop is *Iron Seed → Iron Sprout → drops iron*.
- **Decorative plants → the "Lume-"/garden family** (slot beside Lumebloom / Lume Lantern).
- **Wood → "…wood" full sets** exactly like Lumewood (Hushwood, Sunwood).
- Reuse existing material names for crop drops (Raw Echocite, Drumstone Shard, etc.) —
  no parallel "essence" vocabulary for resources.

## Additions (id | display name | type | what it does)

### Base crop & the compression currency
| id | display name | type | what it does |
| --- | --- | --- | --- |
| `mote_seed` | Mote Seed | item (seed) | Plant on **Verdant Loam** to grow the base crop. Rare drop from grass/fern; also 4 Light Mote → 1 Seed. |
| `mote_sprout` | Mote Sprout | crop block | 7-stage crop; glows faintly when ripe. Harvest → **1 Light Mote** (+ chance of a Mote Seed). |
| `light_mote` | Light Mote | item | **Tier-1 currency.** The universal binder used in every seed/soil/tier recipe below. |
| `condensed_mote` | Condensed Mote | item | **Tier-2.** 9 Light Mote (Compressor or 3×3), reversible. Gates mid-tier crops. |
| `radiant_mote` | Radiant Mote | item | **Tier-3.** 9 Condensed Mote, reversible. Gates high-tier crops; bridges to Radiant Dust. |
| `mote_block` | Mote Block | block | Décor/storage: 9 Light Mote, glows softly (a soft-light cousin of the Lume Lantern); also a bulk Light top-up when fed to the Compressor/Transmuter. |

### Soil tiers (the grow-gate, built on Verdant Loam)
| id | display name | type | what it does |
| --- | --- | --- | --- |
| `attuned_loam` | Attuned Loam | block (soil) | Verdant Loam + 4 Condensed Mote. Required to mature **tier-2** crops. |
| `radiant_loam` | Radiant Loam | block (soil) | Attuned Loam + 4 Radiant Mote. Required to mature **tier-3** crops. |

A seed planted on too-low a soil tier simply won't advance (tooltip + particle hint).
**Growth Radiator** and Verdant Loam's existing aura accelerate any tier.

### Resource / material crops (the ores-renewable set)
Pattern per crop: `<x>_seed` (item) + `<x>_sprout` (crop block) → drops the material's
existing harvest unit. Seed recipe = a **Mote tier + a sample of the material**.

| Tier (gate) | Crops (id `<x>` = …) | Seed recipe | Sprout drops |
| --- | --- | --- | --- |
| 1 · Light Mote · Verdant Loam | `iron`, `copper`, `coal`, `redstone`, `echocite` | 4 Light Mote + the material | Raw form (raw iron/copper, coal, redstone, **Raw Echocite**) |
| 2 · Condensed · Attuned Loam | `gold`, `lapis`, `diamond`, `drumstone`, `quartz` | 4 Condensed Mote + the material | Raw/gem form (incl. **Drumstone Shard**) |
| 3 · Radiant · Radiant Loam | `emerald`, `silentite`, `radiant`, `netherite_scrap` | 4 Radiant Mote + the material | Gem/crystal/scrap (incl. **Silentite Crystal**, **Radiant Dust**) |

*Balance:* sprouts drop small amounts (raw forms / 1 unit + low seed-return), gated
behind compressed Motes + Growth-Radiator Light cost, so the early ore game still
matters — crops are the **late-game renewable**, not a shortcut. (Table above is the
intended roster; exact list confirmable at sign-off.)

### Food crops
| id | display name | type | what it does |
| --- | --- | --- | --- |
| `grain_seeds` | Resonant Grain Seeds | item | Plant → `resonant_grain` sprout. |
| `resonant_grain` | Resonant Grain | crop block | Wheat-analogue; harvest → **Resonant Grain** (item). |
| `resonant_bread` | Resonant Bread | food item | 3 Resonant Grain. The staple. |
| `glowgourd` | Glowgourd | crop block (gourd) | A glowing melon/gourd; carves into **Glowgourd Slice**. |
| `glowgourd_slice` | Glowgourd Slice | food item | High stack-restore; brief faint glow. |
| `stillmint` | Stillmint | crop block (herb) | The "calming herb." Harvest → **Stillmint Leaf**. |
| `still_tea` | Still Tea | food item | Stillmint + water bottle + sugar. **Gameplay hook:** short Regeneration + Saturation (a "centred" food). |

### Decorative plants (Lume/garden palette)
| id | display name | type | what it does |
| --- | --- | --- | --- |
| `lumecap` | Lumecap | plant block (fungus) | Glowing fungus; grows on Lumewood/loam in dim light. Décor + Glowing tie-in. |
| `chime_lily` | Chime Lily | flower | Décor; mills to a **dye** (covers a hard-to-farm vanilla colour). |
| `octave_orchid` | Octave Orchid | flower | Décor; **dye** output — pairs with the 16 Wave channel colours. |
| `verdant_fern` | Verdant Fern | shrub | Grove ground-cover; compostable décor. |

### Wood types (full Lumewood-parity sets)
| id family | display name | type | what it does |
| --- | --- | --- | --- |
| `hushwood_*` | Hushwood (set) | wood set | **Committed full set** (log/stripped/wood/stripped wood/planks/stairs/slab/fence/gate/door/trapdoor/button/pressure plate/sign/hanging sign/leaves/sapling). Deep-Dark indigo; matte; ties to Silentite. Generated as a worldgen tree near Deep Dark. |
| `sunwood_*` | Sunwood (set) | wood set | Second full set, golden and **light-emitting**; its sapling only matures **under a Growth Radiator** (the "magic sapling" gate). Ties to the Radiant tier. |

## Progression at a glance

```
Mote Seed ─► Mote Sprout ─► Light Mote ──(9→1 Compressor)──► Condensed Mote ──(9→1)──► Radiant Mote
   (on Verdant Loam)            │                                  │                        │
                                ▼                                  ▼                        ▼
                 tier-1 resource/food/décor seeds   tier-2 crops + Attuned Loam   tier-3 crops + Radiant Loam
                 + Hushwood worldgen                                                + Sunwood (under a Growth Radiator)
```

Light Mote is consumed *everywhere* (seeds, soil tiers, tier-ups), so "grow more base
crop to do anything" is the loop — exactly the Mystical-Agriculture feel, reskinned to
Light and octaves.

## Recipe sketches

- **Base / currency**
  - `4 Light Mote` (2×2) → `1 Mote Seed`
  - **Compressor** `9 Light Mote → 1 Condensed Mote`; `9 Condensed Mote → 1 Radiant Mote`
    *(copy an existing `data/echoes/recipe/compressor/*.json` exactly — custom type)*
  - 3×3 `9 Light Mote → Mote Block` and reverse `1 → 9` (and the same for the Mote ladder)
- **Soil:** `Verdant Loam + 4 Condensed Mote → Attuned Loam`; `Attuned Loam + 4 Radiant Mote → Radiant Loam`
- **Resource seed (example):** `4 Light Mote + Iron Ingot → Iron Seed`;
  `4 Condensed Mote + Diamond → Diamond Seed`; `4 Radiant Mote + Silentite Crystal → Silentite Seed`
- **Food:** `3 Resonant Grain → Resonant Bread`; `Stillmint Leaf + Water Bottle + Sugar → Still Tea`
- **Décor:** `Chime Lily → dye`; standard wood-set recipes for Hushwood/Sunwood

## Integration (reuse, don't duplicate)

- **Growth speed:** Growth Radiator (already grows crops, ~300 Light/grow, 4×2 radius)
  and **Verdant Loam** are the in-place accelerators; **Sunwood**'s sapling *requires* a
  Growth Radiator to mature — turning an existing block into a gate.
- **Compression** runs through the existing **Compressor** (custom recipe type), giving
  the roadmap's "custom Compressor recipes" a real payload.
- **Crop/Sapling wiring** copies an existing CropBlock/SaplingBlock + its
  blockstate/models/loot_table/recipe/tags exactly before adding new ones.
- **Lang:** every block/item gets `block|item.echoes.*` **and** a
  `tooltip.echoes.desc.<id>` line, in the existing flavour voice.

## Open questions (please confirm at sign-off)

1. **Base names:** `Light Mote / Condensed Mote / Radiant Mote` + crop **Mote Sprout** —
   good, or prefer "First Light / Seed of the One" framing?
2. **Resource-crop roster:** the three-tier list above — trim or extend? (e.g., include
   Glowstone, Amethyst, Blaze, Ender?)
3. **Wood scope:** ship **Hushwood** first (lower build risk), with **Sunwood** as a
   fast-follow, or both in one pass?
4. **Mob-essence crops** (Sculk/Phantom/Blaze, Deep-Dark themed) — in scope now or defer?

## Implementation checklist (runs only after approval)

- [ ] Register base crop, Mote currency (×3 + block), soil tiers, resource crops, food,
      décor, and wood sets in `ModBlocks`/`ModItems`/`ModBlockEntities`/`ModWorldGen`
- [ ] Extend `gen_textures.py` / `gen_phase2_assets.py` for all sprites, **per-stage crop
      frames** (stage0..N), and wood-set textures — same pixel style, no Pillow
- [ ] Compressor recipes copied from the existing schema; 3×3/smelting/loot JSON
- [ ] Per-wood file checklist (blockstate/model/loot/tags: mineable·axe, logs, planks,
      leaves, saplings; flammability; strippable map) — verify all ~18 files per set
- [ ] `lang/en_us.json`: name + `tooltip.echoes.desc.<id>` for every new id
- [ ] Regenerate wiki (`scripts/build_wiki_site.py`, `gen_wiki_*`) **and** edit the prose
      (Blocks.md, Items-and-Gear.md, Ores-and-Worldgen.md, Crafting-and-Progression.md)
- [ ] Verify: `./gradlew build` (toolchain JDK 21) **and** a dedicated-server boot with no
      registry/recipe/tag/worldgen errors (baseline ~1430 recipes) before un-drafting
</content>
