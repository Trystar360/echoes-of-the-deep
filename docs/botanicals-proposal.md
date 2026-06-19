# Botanicals & crops proposal — the Verdant Octave

Goal: add a **Mystical-Agriculture–style** botanical suite — food crops, decorative
plants, new wood types, and an **ore/material crop economy** — built on a single base
crop that grows **Light, the universal One**, which **gains energy to climb the octaves**:
the more Light energy you pour into a Mote, the higher its octave, and the higher its
octave the heavier the "element" it becomes. That charged-Light ladder is the crafting
currency for the whole tree. Names follow the locked scheme in
[`naming-proposal.md`](naming-proposal.md): id == display name, both Russell-themed
**and** obviously functional, grouped into small consistent families.

> Status: **proposal — awaiting sign-off.** No Java/assets land until approved. This
> doc is the spec; it reuses systems already shipped (Growth Radiator, Verdant Loam,
> Compressor, **Transmuter** = "raising matter an octave", **Octave Seed** = "each
> octave's inert-gas rest point", the Lumewood/Lumebloom/Lume Lantern garden set, and
> the echocite/drumstone/silentite/echo/radiant material chains).

## The cosmology this is built on (Russell's octave wave)

Walter Russell's elements are not a list but a **wave**. Light springs from a still
magnetic centre and is **wound up** by *generation* (charging, compression, the
centripetal "in-breath") into ever-denser, higher-octave matter — then **unwound** by
*radiation* (discharging, expansion, the centrifugal "out-breath") back toward stillness.
Each octave begins and ends at an **inert gas** (the rest point / throne), rises through
generative tones to a **balanced crest**, and falls again. Carbon sits at the crest of
the great wave — the most balanced, mature element (the cube-sphere). Past the crest, the
heavy radioactive octaves (radium, uranium) are **over-charged**: they give their Light
back. Add energy → climb toward the crest; overshoot → it radiates back to the still
centre. The suite makes that exact arc playable.

```
 generation (add Light energy →)                         (→ radiation gives it back)
 stillness ──► O1 ──► O2 ──► O3 ──►  ⟨CREST: balance⟩ ──► over-octave ──► back to stillness
 (inert/rest)  spark  tone   bright    Radiant (mature)    radiates Light    (Zero-Point)
```

## Design conventions (the crop families)

- **The currency is charged Light → "… Mote".** A Mote is a packet of Light; you raise
  its octave by feeding it **energy in the Transmuter** (an **Octave Seed** is the
  inert-gas "rest" catalyst that lets the octave close). Each step up costs more Light —
  *"more energy → higher octave element."* The Mote ladder is the universal binder used
  in every seed, soil, and recipe below.
- **Plant block → "… Sprout"** — the growing crop (staged like wheat). Seeds are
  **"… Seed(s)"**. So a crop is *Iron Seed → Iron Sprout → drops iron*.
- **Decorative plants → the "Lume-"/garden family** (slot beside Lumebloom / Lume Lantern).
- **Wood → "…wood" full sets** exactly like Lumewood, themed to the octave wave:
  **Hushwood** = the inert/rest octave (silence), **Sunwood** = the Radiant crest.
- Reuse existing material names for crop drops (Raw Echocite, Drumstone Shard, etc.) —
  no parallel "essence" vocabulary for resources.

## Additions (id | display name | type | what it does)

### Base crop & the octave ladder (Light gaining energy)
The spine. The crop grows raw Light; the **grid's energy** (via the Transmuter) winds it
up the octaves. Each tier is a heavier "element" of Light and gates the tiers below it.

| id | display name | type | octave | what it does |
| --- | --- | --- | --- | --- |
| `mote_seed` | Mote Seed | item (seed) | — | Plant on **Verdant Loam** to grow the base crop. Rare drop from grass/fern; also 4 Light Mote → 1 Seed. |
| `mote_sprout` | Mote Sprout | crop block | — | 7-stage crop; glows faintly when ripe. Harvest → **1 Light Mote** (+ chance of a Mote Seed). |
| `light_mote` | Light Mote | item | **O0 — the still source** | Raw, uncharged Light: the universal One, straight off the crop. The base currency. |
| `sparked_mote` | Sparked Mote | item | **O1 — first tone** | Light wound once. Light Mote charged in the Transmuter (+ Octave Seed). The first generative spark (hydrogen-like). |
| `resonant_mote` | Resonant Mote | item | **O2 — the note** | Charged again; matter "finds its note" (ties to Resonance). |
| `brilliant_mote` | Brilliant Mote | item | **O3 — rising** | Charged again; nearing the crest. |
| `radiant_mote` | Radiant Mote | item | **O4 — the crest (balance)** | The mature, balanced apex of the wave; begins to radiate. **Bridges into the existing Radiant Dust chain** (4 Radiant Mote → Radiant Dust), so the trees are one tree. |
| `light_mote_block` | Light Mote Block | block | — | Décor/storage: 9 Light Mote, glows softly (a soft-light cousin of the Lume Lantern); also a bulk Light top-up fed to the Compressor/Transmuter. |

> **Over-octave (endgame, flavour now):** charging a Radiant Mote past the crest pushes
> it onto the radiative down-slope — it **gives its Light back** to the grid and decays to
> a Light Mote, looping to the planned **Zero-Point Well** (Phase V). Russell's two-way
> universe, closed.

### Soil tiers (the grow-gate, built on Verdant Loam)
Higher-octave crops need soil wound to a matching octave.

| id | display name | type | octave | what it does |
| --- | --- | --- | --- | --- |
| `verdant_loam` | Verdant Loam *(exists)* | block (soil) | O0–O1 | Living soil; grows base/low crops. |
| `attuned_loam` | Attuned Loam | block (soil) | ≤ O2 | Verdant Loam + 4 Resonant Mote. |
| `radiant_loam` | Radiant Loam | block (soil) | ≤ O4 | Attuned Loam + 4 Radiant Mote. |

A seed planted on too-low a soil octave simply won't advance (tooltip + particle hint).
**Growth Radiator** and Verdant Loam's aura accelerate any octave.

### Resource / material crops (ores renewable, octave-gated)
Pattern per crop: `<x>_seed` (item) + `<x>_sprout` (crop block) → drops the material's
existing harvest unit. Seed recipe = a **Mote of the gating octave + a sample of the
material** — so heavier materials literally require higher-octave Light.

| Octave gate | Crops (`<x>`) | Seed recipe | Sprout drops |
| --- | --- | --- | --- |
| **O1 · Sparked · Verdant Loam** | `iron`, `copper`, `coal`, `redstone`, `echocite` | Sparked Mote + the material | Raw form (raw iron/copper, coal, redstone, **Raw Echocite**) |
| **O2 · Resonant · Attuned Loam** | `gold`, `lapis`, `quartz`, `drumstone` | Resonant Mote + the material | Raw/gem form (incl. **Drumstone Shard**) |
| **O3 · Brilliant · Attuned Loam** | `diamond`, `emerald`, `amethyst` | Brilliant Mote + the material | Gem |
| **O4 · Radiant · Radiant Loam** | `silentite`, `radiant`, `netherite_scrap` | Radiant Mote + the material | Crystal/scrap (incl. **Silentite Crystal**; the `radiant` crop grows **Radiant Dust**) |

The mod's own ores map onto the wave: **Echocite** is the base tone (O1), **Drumstone**
the percussive note (O2), **Silentite** the inert/Deep-Dark rest (O4, paradoxically the
rarest because stillness is hardest to reach), **Radiant** the crest itself. *Balance:*
sprouts drop small amounts, gated behind charged Motes (each octave costs grid Light) and
Growth-Radiator cost — so the early ore game still matters; crops are the **late-game
renewable**, not a shortcut. (Roster confirmable at sign-off.)

### Food crops
| id | display name | type | what it does |
| --- | --- | --- | --- |
| `grain_seeds` | Resonant Grain Seeds | item | Plant → `resonant_grain` sprout. |
| `resonant_grain` | Resonant Grain | crop block | Wheat-analogue; harvest → **Resonant Grain** (item). |
| `resonant_bread` | Resonant Bread | food item | 3 Resonant Grain. The staple. |
| `glowgourd` | Glowgourd | crop block (gourd) | A glowing melon/gourd; carves into **Glowgourd Slice**. |
| `glowgourd_slice` | Glowgourd Slice | food item | High stack-restore; brief faint glow. |
| `stillmint` | Stillmint | crop block (herb) | The "calming herb" of the inert/rest octave. Harvest → **Stillmint Leaf**. |
| `still_tea` | Still Tea | food item | Stillmint + water bottle + sugar. **Gameplay hook:** short Regeneration + Saturation (a "centred" food). |

### Decorative plants (Lume/garden palette)
| id | display name | type | what it does |
| --- | --- | --- | --- |
| `lumecap` | Lumecap | plant block (fungus) | Glowing fungus; grows on Lumewood/loam in dim light. Décor + Glowing tie-in. |
| `chime_lily` | Chime Lily | flower | Décor; mills to a **dye** (covers a hard-to-farm vanilla colour). |
| `octave_orchid` | Octave Orchid | flower | Décor; **dye** output — pairs with the 16 Wave channel colours. |
| `verdant_fern` | Verdant Fern | shrub | Grove ground-cover; compostable décor. |

### Wood types (full Lumewood-parity sets, themed to the wave)
| id family | display name | type | what it does |
| --- | --- | --- | --- |
| `hushwood_*` | Hushwood (set) | wood set | **Committed full set** (log/stripped/wood/stripped wood/planks/stairs/slab/fence/gate/door/trapdoor/button/pressure plate/sign/hanging sign/leaves/sapling). The **inert/rest octave**: deep-Dark indigo, matte, sound-dampening; ties to Silentite. Worldgen tree near Deep Dark. |
| `sunwood_*` | Sunwood (set) | wood set | The **Radiant crest octave**: golden, light-emitting. Its sapling matures **only under a Growth Radiator** — i.e. it must literally be *charged up an octave* with added energy. Second full set. |

## Progression at a glance

```
Mote Seed ─► Mote Sprout ─► Light Mote (O0, raw Light, the universal One)
                                  │  feed Light energy in the Transmuter (+ Octave Seed)
                                  ▼
        Sparked (O1) ─► Resonant (O2) ─► Brilliant (O3) ─► Radiant (O4, crest) ─► [over-octave: radiates back → Zero-Point]
            │                │                                   │
            ▼                ▼                                   ▼
   O1 crops + Verdant   O2 crops + Attuned Loam         O4 crops + Radiant Loam,
   Hushwood worldgen                                    Sunwood (matures under a Growth Radiator)
```

The whole tree is gated by **how much energy you've wound into the Light** — exactly the
Mystical-Agriculture climb, but reskinned to Russell's octave wave.

## Recipe sketches

- **Base / climb**
  - `4 Light Mote` (2×2) → `1 Mote Seed`; Mote Sprout drops Light Mote (+ seed chance).
  - **Transmuter (octave climb)** — consumes grid **Light** + an **Octave Seed**, raises
    one octave: `Light Mote → Sparked → Resonant → Brilliant → Radiant`. Cost scales per
    step (e.g. ~1k → 4k → 16k → 64k Light) so "more energy = higher octave." *(Copy an
    existing `data/echoes/recipe/compressor/*.json` only for the storage-block recipe;
    the octave climb is a Transmuter recipe.)*
  - `4 Radiant Mote → 1 Radiant Dust` (joins the existing transmutation chain).
  - `9 Light Mote ↔ Light Mote Block` (storage; the "compress the base crop" rung).
- **Soil:** `Verdant Loam + 4 Resonant Mote → Attuned Loam`; `Attuned Loam + 4 Radiant Mote → Radiant Loam`
- **Resource seed (example):** `Sparked Mote + Iron Ingot → Iron Seed`;
  `Brilliant Mote + Diamond → Diamond Seed`; `Radiant Mote + Silentite Crystal → Silentite Seed`
- **Food:** `3 Resonant Grain → Resonant Bread`; `Stillmint Leaf + Water Bottle + Sugar → Still Tea`
- **Décor / wood:** `Chime Lily → dye`; standard wood-set recipes for Hushwood/Sunwood

## Integration (reuse, don't duplicate)

- **The octave climb is energy, not just crafting:** it runs on the **Transmuter**
  ("raising matter an octave") drawing **Light** from the grid, with the **Octave Seed**
  as the inert-gas rest catalyst — both already in the mod. This is the literal
  "add energy → higher octave" mechanic.
- **Growth speed:** Growth Radiator (grows crops, ~300 Light/grow, 4×2 radius) and
  **Verdant Loam** accelerate any octave; **Sunwood**'s sapling *requires* a Growth
  Radiator to mature — turning an existing block into the high-octave gate.
- **Crop/Sapling wiring** copies an existing CropBlock/SaplingBlock + its
  blockstate/models/loot_table/recipe/tags exactly before adding new ones.
- **Lang:** every block/item gets `block|item.echoes.*` **and** a
  `tooltip.echoes.desc.<id>` line, in the existing flavour voice.

## Open questions (please confirm at sign-off)

1. **Octave-ladder names:** `Light → Sparked → Resonant → Brilliant → Radiant Mote` (a
   brightness/energy gradient that doubles as octave numbers) — good, or prefer numbered
   `First/Second/Third-Octave Mote`, or Russell tone-names (do/re/mi)?
2. **How many octaves are crafting tiers?** 4 charged tiers (O1–O4) shown; add a 5th
   over-octave material, or keep the over-octave as the radiate-back endgame only?
3. **Resource-crop roster & octave mapping** — trim or extend (Glowstone? Blaze? Ender?);
   is the Echocite=O1 / Drumstone=O2 / Silentite=O4 / Radiant=crest mapping right?
4. **Wood scope:** Hushwood first (lower build risk), Sunwood fast-follow, or both at once?
5. **Mob-essence crops** (Sculk/Phantom/Blaze, Deep-Dark themed) — in scope now or defer?

## Implementation checklist (runs only after approval)

- [ ] Register base crop, the Mote octave ladder (O0–O4 + block), soil tiers, resource
      crops, food, décor, and wood sets in
      `ModBlocks`/`ModItems`/`ModBlockEntities`/`ModWorldGen`
- [ ] Add the **Transmuter octave-climb recipe(s)** (Light cost per step + Octave Seed);
      copy the existing compressor JSON schema only for the storage-block recipe
- [ ] Extend `gen_textures.py` / `gen_phase2_assets.py` for all sprites, **per-stage crop
      frames** (stage0..N), the Mote ladder (rising glow per octave), and wood-set
      textures — same pixel style, no Pillow
- [ ] Per-wood file checklist (blockstate/model/loot/tags: mineable·axe, logs, planks,
      leaves, saplings; flammability; strippable map) — verify all ~18 files per set
- [ ] `lang/en_us.json`: name + `tooltip.echoes.desc.<id>` for every new id
- [ ] Regenerate wiki (`scripts/build_wiki_site.py`, `gen_wiki_*`) **and** edit the prose
      (Blocks.md, Items-and-Gear.md, Ores-and-Worldgen.md, Crafting-and-Progression.md)
- [ ] Verify: `./gradlew build` (toolchain JDK 21) **and** a dedicated-server boot with no
      registry/recipe/tag/worldgen errors (baseline ~1430 recipes) before un-drafting
</content>
