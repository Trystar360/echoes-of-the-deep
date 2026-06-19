# Botanicals & crops proposal — the Verdant Octave

Goal: add a **Mystical-Agriculture–style** botanical suite — an **ore/material crop
economy** (plus food, decorative plants, and new wood types in later passes) — built on a
single base crop that grows **Light, the universal One**, which **gains energy to climb
the octaves**: the more Light energy you pour into a Mote, the higher its octave and the
heavier the "element" it becomes. That charged-Light ladder is the crafting currency for
the whole tree, and it caps off in a **transmutation economy** (an EMC analogue). Names
follow the locked scheme in [`naming-proposal.md`](naming-proposal.md): id == display
name, both Russell-themed **and** functional, grouped into small consistent families.

> Status: **proposal — decisions locked (see end), awaiting final go.** No Java/assets
> land until then. It reuses systems already shipped (Growth Radiator, Verdant Loam,
> Compressor, **Transmuter** = "raising matter an octave", **Octave Seed** = "each
> octave's inert-gas rest point", the Lumewood/Lumebloom/Lume Lantern garden set, and the
> echocite/drumstone/silentite/echo/radiant material chains).

## The cosmology this is built on (Russell's octave wave)

Walter Russell's elements are not a list but a **wave**. Light springs from a still
magnetic centre and is **wound up** by *generation* (charging, compression, the
centripetal "in-breath") into ever-denser, higher-octave matter — then **unwound** by
*radiation* (discharging, expansion, the centrifugal "out-breath") back toward stillness.
Each octave begins and ends at an **inert gas** (the rest point / throne), rises through
generative **tones** to a **balanced crest**, and falls again. Carbon sits at the crest of
the great wave — the most balanced, mature element. Past the crest, the heavy radioactive
octaves are **over-charged**: they give their Light back. Add energy → climb toward the
crest; overshoot → it radiates back to the still centre. The suite makes that arc playable,
and names the tiers as **tones of the octave** (Russell's "nine octaves of tones").

```
 generation (add Light energy →)                         (→ radiation gives it back)
 stillness ──► Tonic ──► Mediant ──► Dominant ──► ⟨CREST: Harmonic⟩ ──► over-octave ──► back to stillness
 (Light Mote)   O1        O2          O3            O4 (balance)         radiates Light    (Zero-Point)
```

## Design conventions (the families)

- **The currency is charged Light → "… Mote".** A Mote is a packet of Light; you raise
  its octave by feeding it **energy in the Transmuter** (an **Octave Seed** is the
  inert-gas "rest" catalyst that lets the octave close). The tiers are named as the
  **tones of a rising octave** — the major triad (Tonic · Mediant · Dominant) resolving to
  **Harmonic** (the crest = harmony = Russell's balance). Each step costs more Light:
  *"more energy → higher octave element."*
- **Plant block → "… Sprout"** — the growing crop (staged like wheat). Seeds are
  **"… Seed(s)"**. So a crop is *Iron Seed → Iron Sprout → drops iron*.
- **Decorative plants → the "Lume-"/garden family** (slot beside Lumebloom / Lume Lantern).
- **Wood → "…wood" full sets** exactly like Lumewood, themed to the wave:
  **Hushwood** = the inert/rest octave (silence), **Sunwood** = the Harmonic crest.
- Reuse existing material names for crop drops (Raw Echocite, Drumstone Shard, etc.) —
  no parallel "essence" vocabulary for resources.

## Additions (id | display name | type | what it does)

### Base crop & the octave ladder (Light gaining energy)
The spine. The crop grows raw Light; the **grid's energy** (via the Transmuter) winds it
up the octaves. Each tone is a heavier "element" of Light and gates the tiers below it.

| id | display name | type | octave / tone | what it does |
| --- | --- | --- | --- | --- |
| `mote_seed` | Mote Seed | item (seed) | — | Plant on **Verdant Loam** to grow the base crop. Rare drop from grass/fern; also 4 Light Mote → 1 Seed. |
| `mote_sprout` | Mote Sprout | crop block | — | 7-stage crop; glows faintly when ripe. Harvest → **1 Light Mote** (+ chance of a Mote Seed). |
| `light_mote` | Light Mote | item | **O0 — the still source** | Raw, uncharged Light: the universal One, straight off the crop. The base currency (the silent tonic-before-the-strike). |
| `tonic_mote` | Tonic Mote | item | **O1 — the tonic** | Light wound once: the first tone struck. Light Mote charged in the Transmuter (+ Octave Seed). |
| `mediant_mote` | Mediant Mote | item | **O2 — the mediant** | Charged again; the chord's middle tone. |
| `dominant_mote` | Dominant Mote | item | **O3 — the dominant** | Charged again; the strong tone, nearing the crest. |
| `harmonic_mote` | Harmonic Mote | item | **O4 — the crest (harmony / balance)** | The triad resolved: the mature, balanced apex of the wave; begins to radiate. **Bridges into the existing Radiant Dust chain** (4 Harmonic Mote → Radiant Dust), so the trees are one tree. |
| `light_mote_block` | Light Mote Block | block | — | Décor/storage: 9 Light Mote, glows softly (a soft-light cousin of the Lume Lantern); also a bulk Light top-up fed to the Compressor/Transmuter. |

> **Over-octave (endgame):** charging a Harmonic Mote past the crest pushes it onto the
> radiative down-slope — it **gives its Light back** to the grid and decays to a Light
> Mote, looping to the planned **Zero-Point Well** (Phase V). Russell's two-way universe,
> closed. (Kept as the radiate-back endgame; no separate 5th craftable tier.)

### Soil tiers (the grow-gate, built on Verdant Loam)
Higher-octave crops need soil wound to a matching tone.

| id | display name | type | octave | what it does |
| --- | --- | --- | --- | --- |
| `verdant_loam` | Verdant Loam *(exists)* | block (soil) | O0–O1 | Living soil; grows base/low crops. |
| `attuned_loam` | Attuned Loam | block (soil) | ≤ O3 | Verdant Loam + 4 Mediant Mote. |
| `radiant_loam` | Radiant Loam | block (soil) | ≤ O4 | Attuned Loam + 4 Harmonic Mote. |

A seed planted on too-low a soil octave simply won't advance (tooltip + particle hint).
**Growth Radiator** and Verdant Loam's aura accelerate any octave.

### Resource / material crops (ores renewable, octave-gated) — **the v1 content**
Pattern per crop: `<x>_seed` (item) + `<x>_sprout` (crop block) → drops the material's
existing harvest unit. Seed recipe = a **Mote of the gating tone + a sample of the
material** — so heavier materials literally require higher-octave Light.

| Octave gate | Crops (`<x>`) | Seed recipe | Sprout drops |
| --- | --- | --- | --- |
| **O1 · Tonic · Verdant Loam** | `iron`, `copper`, `coal`, `redstone`, `echocite` | Tonic Mote + the material | Raw form (raw iron/copper, coal, redstone, **Raw Echocite**) |
| **O2 · Mediant · Attuned Loam** | `gold`, `lapis`, `quartz`, `drumstone` | Mediant Mote + the material | Raw/gem form (incl. **Drumstone Shard**) |
| **O3 · Dominant · Attuned Loam** | `diamond`, `emerald`, `amethyst` | Dominant Mote + the material | Gem |
| **O4 · Harmonic · Radiant Loam** | `silentite`, `radiant`, `netherite_scrap` | Harmonic Mote + the material | Crystal/scrap (incl. **Silentite Crystal**; the `radiant` crop grows **Radiant Dust**) |

The mod's own ores map onto the wave: **Echocite** is the tonic (O1), **Drumstone** the
mediant (O2), **Silentite** the inert/Deep-Dark rest (O4, paradoxically the rarest because
stillness is hardest to reach), **Radiant** the crest itself. *Balance:* sprouts drop
small amounts, gated behind charged Motes (each octave costs grid Light) and Growth-
Radiator cost — so the early ore game still matters; crops are the **late-game renewable**,
not a shortcut.

### Food crops — *deferred to a later pass (documented for continuity)*
| id | display name | type | what it does |
| --- | --- | --- | --- |
| `grain_seeds` | Resonant Grain Seeds | item | Plant → `resonant_grain` sprout. |
| `resonant_grain` | Resonant Grain | crop block | Wheat-analogue; harvest → **Resonant Grain** (item). |
| `resonant_bread` | Resonant Bread | food item | 3 Resonant Grain. The staple. |
| `glowgourd` | Glowgourd | crop block (gourd) | A glowing melon/gourd; carves into **Glowgourd Slice**. |
| `glowgourd_slice` | Glowgourd Slice | food item | High stack-restore; brief faint glow. |
| `stillmint` | Stillmint | crop block (herb) | The "calming herb" of the inert/rest octave. Harvest → **Stillmint Leaf**. |
| `still_tea` | Still Tea | food item | Stillmint + water bottle + sugar. **Hook:** short Regeneration + Saturation. |

### Decorative plants (Lume/garden palette) — *deferred to a later pass*
| id | display name | type | what it does |
| --- | --- | --- | --- |
| `lumecap` | Lumecap | plant block (fungus) | Glowing fungus; grows on Lumewood/loam in dim light. Décor + Glowing tie-in. |
| `chime_lily` | Chime Lily | flower | Décor; mills to a **dye** (covers a hard-to-farm vanilla colour). |
| `octave_orchid` | Octave Orchid | flower | Décor; **dye** output — pairs with the 16 Wave channel colours. |
| `verdant_fern` | Verdant Fern | shrub | Grove ground-cover; compostable décor. |

### Wood types (full Lumewood-parity sets, themed to the wave)
| id family | display name | type | what it does |
| --- | --- | --- | --- |
| `hushwood_*` | Hushwood (set) | wood set | **v1 — committed full set** (log/stripped/wood/stripped wood/planks/stairs/slab/fence/gate/door/trapdoor/button/pressure plate/sign/hanging sign/leaves/sapling). The **inert/rest octave**: deep-Dark indigo, matte, sound-dampening; ties to Silentite. Worldgen tree near Deep Dark. |
| `sunwood_*` | Sunwood (set) | wood set | **Fast-follow.** The **Harmonic crest octave**: golden, light-emitting. Its sapling matures **only under a Growth Radiator** — i.e. it must literally be *charged up an octave* with added energy. |

## The transmutation economy — an EMC analogue: **Bound Light**

Russell's whole premise is that **all matter is condensed Light**. So the mod already has
a native EMC: every item carries a **Light Value** — the Light wound into it to exist.
This is the ProjectE/Equivalent-Exchange "EMC," reskinned so it isn't a bolt-on number but
the literal cosmology. **(Locked: the full set ships, including the free⇄bound bridge.)**

- **Free Light** = the grid's energy (RU; Coils generate, Cells store, Conduits carry) —
  *Light in motion / radiating.*
- **Bound Light** = an item's **Light Value** — *Light at rest in form.*
- A **Transmutation Table** is the **balanced-interchange altar** where the two trade: one
  side **unwinds** matter into Bound Light (radiation), the other **rewinds** Bound Light
  into matter (generation). Russell's two-way universe, made a workstation. (Distinct from
  the **Transmuter**, which raises *one* item *one* octave with *free* Light.)

### Light Value — the currency (EMC)
The **octave Mote ladder is the denomination scale** (×4 per octave — the same curve as
the energy climb), so Motes are literally the *coins* of Bound Light and the system is one
economy, not two:

| Token | Light Value (LV) | role |
| --- | --- | --- |
| (base unit) | 1 LV | cobblestone/dirt ≈ 1 LV; the floor |
| **Light Mote** | 64 LV | the smallest Light "coin" the Table can crystallize |
| **Tonic Mote** | 256 LV | ×4 |
| **Mediant Mote** | 1,024 LV | ×4 |
| **Dominant Mote** | 4,096 LV | ×4 |
| **Harmonic Mote** | 16,384 LV | ×4 — the crest coin |

Example item values (tunable, vanilla-anchored): iron ingot ≈ 256, gold ≈ 2,048, diamond
≈ 8,192. You can **withdraw** stored Bound Light as physical Motes (the Table makes change
in denominations) and **deposit** Motes or any item to top it up — so the base crop, the
octave climb, and EMC are the same thing seen three ways.

### The pieces (all in scope)
| id | display name | type | what it does |
| --- | --- | --- | --- |
| `transmutation_table` | Transmutation Table | block (+ screen) | The workstation. **Attune** an item by placing one (records its tone in your personal ledger — ProjectE-style "learning"); thereafter **condense** it from your stored Bound Light, or **dissolve** items to add Light Value. Large Bound-Light pool; searchable/sortable GUI of attuned tones. |
| `transmutation_tablet` | Transmutation Tablet | item (portable) | The portable Table — same per-player pool & ledger from your inventory; right-click in-world for quick transmutes (cobble↔stone, sand↔glass, rotate a material through its tier). |
| `light_value` | Light Value (LV) | stat / data | The EMC number: **Bound Light**, per-item (data-driven JSON map, pack-overridable) with a blacklist for unique/exploit items. Shown in tooltips (hold a key) and read by the Light Meter. |
| `octave_star` (I–VI) | Octave Star | item (tiered) | Portable **Bound-Light battery** (the "Klein Star"): six tiers of capacity; carry your Light between bases. (Distinct from the **Cell** family, which stores *free* Light/RU.) |
| `tone_collector` | Tone Collector | block | Passively **winds ambient light into Bound Light** (the EE "Energy Collector"): faster in sunlight / near the glowing garden. Slow & capped — a trickle. |
| `condenser` | Condenser | block (+ screen) | Set a target (an attuned item) + feed Bound Light → **auto-produces** it repeatedly (the EE "Condenser"). The duplication engine, gated late. |
| `codex_of_tones` | Codex of Tones | item (endgame) | Attunes you to **every** tone at once (the "Tome of Knowledge"). Capstone behind the Harmonic tier. |
| `interchange_coil` | Interchange Coil | block | **The bridge (locked in):** converts *free* Light (grid RU) ⇄ *Bound* Light at a **steep, hard-capped** rate — the literal generation↔radiation interchange. Gated and throttled (see balance). |

### Balance & anti-exploit (this system is famous for trivializing progression)
- **Gate it late:** the Table unlocks at the **Harmonic (O4)** tier; Tablet, Stars,
  Condenser, Codex, and the Interchange Coil later still.
- **Attune from a real sample first** — you must legitimately obtain each item once before
  condensing it (ore/structure progression still has to happen).
- **Capped sources:** Collectors trickle and are capped; the **Interchange Coil** ratio is
  steep and hard-capped per tick, so a Coil farm can't become infinite diamonds.
- **Data-driven values + blacklist:** no LV for unique/exploit items (spawn eggs, bedrock,
  command blocks); **audit every A→B→A path for net-free LV** (the classic EMC bug) before
  shipping.
- All numbers are named constants / JSON so a pack author can retune or disable the economy.

### Recipe sketches (transmutation)
- `transmutation_table` ← a ring of **Harmonic Mote** + **Radiant Ingot** corners around an
  **Octave Seed** (the inert-gas rest at the centre of interchange) on an Echocite base.
- `transmutation_tablet` ← **Harmonic Mote** + the Table's "face" item + a **Light Meter**.
- `octave_star` I ← **Harmonic Mote** ring + **Resonance Cell** core; higher tiers re-craft
  the lower Star with more Harmonic Motes.
- `tone_collector` / `condenser` ← Lumewood/glass + Radiant Ingots + an Octave Coil core.
- `interchange_coil` ← **Octave Coil** + Harmonic Motes + a **Balancer** (the device that
  already equalizes the grid) — fittingly, the bridge is built from the balancer.

## Progression at a glance

```
Mote Seed ─► Mote Sprout ─► Light Mote (O0, raw Light, the universal One)
                                  │  feed Light energy in the Transmuter (+ Octave Seed)
                                  ▼
        Tonic (O1) ─► Mediant (O2) ─► Dominant (O3) ─► Harmonic (O4, crest) ─► [over-octave: radiates back → Zero-Point]
            │              │                                  │
            ▼              ▼                                  ▼
   O1 crops + Verdant   O2/O3 crops + Attuned Loam    O4 crops + Radiant Loam, Hushwood (v1)
                                                              │  unlocks ↓
                                              Transmutation Table ⇄ Bound Light (EMC) ⇄ any attuned item
                                              (Tablet · Octave Stars · Tone Collector · Condenser · Interchange Coil)
```

The whole tree is gated by **how much energy you've wound into the Light** — the
Mystical-Agriculture climb reskinned to Russell's octave wave — capping off in the
**transmutation economy**, where Light and matter become freely interchangeable
(ProjectE-style EMC, reskinned as **Bound Light**).

## Recipe sketches (crops & climb)

- **Base / climb**
  - `4 Light Mote` (2×2) → `1 Mote Seed`; Mote Sprout drops Light Mote (+ seed chance).
  - **Transmuter (octave climb)** — consumes grid **Light** + an **Octave Seed**, raises
    one octave: `Light Mote → Tonic → Mediant → Dominant → Harmonic`. Cost scales per step
    (~1k → 4k → 16k → 64k Light) so "more energy = higher octave."
  - `4 Harmonic Mote → 1 Radiant Dust` (joins the existing transmutation chain).
  - `9 Light Mote ↔ Light Mote Block` (storage; the "compress the base crop" rung — copy
    an existing `data/echoes/recipe/compressor/*.json`).
- **Soil:** `Verdant Loam + 4 Mediant Mote → Attuned Loam`; `Attuned Loam + 4 Harmonic Mote → Radiant Loam`
- **Resource seed (example):** `Tonic Mote + Iron Ingot → Iron Seed`;
  `Dominant Mote + Diamond → Diamond Seed`; `Harmonic Mote + Silentite Crystal → Silentite Seed`

## Integration (reuse, don't duplicate)

- **The octave climb is energy, not just crafting:** it runs on the **Transmuter**
  ("raising matter an octave") drawing **Light** from the grid, with the **Octave Seed** as
  the inert-gas rest catalyst — both already in the mod.
- **Growth speed:** Growth Radiator (grows crops, ~300 Light/grow, 4×2 radius) and
  **Verdant Loam** accelerate any octave; **Sunwood**'s sapling *requires* a Growth
  Radiator to mature — turning an existing block into the high-octave gate.
- **EMC reuses energy/storage parts:** Octave Stars are built on **Resonance Cells**, the
  Interchange Coil on the **Octave Coil** + **Balancer**, and Light Value is read by the
  **Light Meter** — the EMC layer is wired into the existing grid, not bolted beside it.
- **Crop/Sapling wiring** copies an existing CropBlock/SaplingBlock + its
  blockstate/models/loot_table/recipe/tags exactly before adding new ones.
- **Lang:** every block/item gets `block|item.echoes.*` **and** a
  `tooltip.echoes.desc.<id>` line, in the existing flavour voice.

## Decisions (locked)

1. **Octave-ladder names → Russell tones:** `Light → Tonic → Mediant → Dominant →
   Harmonic Mote` (the major triad resolving to harmony = the balanced crest).
2. **Transmutation economy → the full set, including the bridge:** Table, Tablet, Light
   Value, Octave Stars, Tone Collector, Condenser, Codex of Tones, **and the Interchange
   Coil** (free⇄bound Light) — with the balance caps above.
3. **Wood scope → Hushwood first**, Sunwood as a fast-follow.
4. **First implementation pass → resource/ore crops** (+ the always-core base crop, octave
   ladder, and EMC economy, + Hushwood). **Food, decorative/dye, and mob-essence crops are
   deferred** to later passes (documented above for continuity).

## Still to confirm (minor — sensible defaults assumed)

- **Octave tiers:** 4 craftable tones (O1–O4) + the radiate-back over-octave endgame
  (assumed; no 5th craftable tier).
- **Light Value sourcing:** auto-derive from recipes with **hand overrides**, base scale
  cobble = 1 LV, ×4 per octave (assumed). Say the word to curate fully by hand instead.
- **Mob-essence crops:** deferred; revisit after v1.

## Implementation order & checklist (runs only after final go)

**v1 = base crop + octave ladder + resource/ore crops + Hushwood + the EMC economy.**

- [ ] Register the base crop, the Mote octave ladder (O0–O4 + block), soil tiers, and the
      resource crops in `ModBlocks`/`ModItems`/`ModBlockEntities`/`ModWorldGen`
- [ ] Add the **Transmuter octave-climb recipe(s)** (Light cost per step + Octave Seed);
      copy the existing compressor JSON schema only for the storage-block recipe
- [ ] **Transmutation economy:** data-driven **Light Value** map (+ blacklist) and a
      per-player ledger (NBT); `transmutation_table` block + screen, `transmutation_tablet`
      item + screen; LV in tooltips/Light Meter; **Octave Stars, Tone Collector, Condenser,
      Codex, Interchange Coil**. Audit for free-LV loops; verify the bridge cap.
- [ ] **Hushwood** full set — per-block file checklist (blockstate/model/loot/tags:
      mineable·axe, logs, planks, leaves, saplings; flammability; strippable map) — verify
      all ~18 files exist before building
- [ ] Extend `gen_textures.py` / `gen_phase2_assets.py` for all sprites, **per-stage crop
      frames** (stage0..N), the Mote ladder (rising glow per tone), and the Hushwood
      textures — same pixel style, no Pillow
- [ ] `lang/en_us.json`: name + `tooltip.echoes.desc.<id>` for every new id
- [ ] Regenerate wiki (`scripts/build_wiki_site.py`, `gen_wiki_*`) **and** edit the prose
      (Blocks.md, Items-and-Gear.md, Ores-and-Worldgen.md, Crafting-and-Progression.md)
- [ ] Verify: `./gradlew build` (toolchain JDK 21) **and** a dedicated-server boot with no
      registry/recipe/tag/worldgen errors (baseline ~1430 recipes) before un-drafting
- [ ] **Later passes:** food crops → decorative/dye crops → Sunwood → mob-essence crops
</content>
