---
name: add-mod-feature
description: >-
  Use when the user describes an idea, feature, content suite, block, item, crop,
  machine, tool, ore, food, or mechanic they want added to the Echoes of the Deep
  Minecraft Fabric mod (e.g. "add a botanicals suite", "I want a teleport staff",
  "make a new ore that..."). Turns a loose idea into a coherent, on-theme, correctly
  wired implementation: plan → sign-off → code + procedural assets + recipes + lang
  → wiki → build/server verification → draft PR. Invoke this BEFORE writing any code
  for a new feature so the result matches the mod's naming scheme, conventions, and
  build pipeline.
---

# Add a feature to Echoes of the Deep

You are implementing a new idea into this Fabric mod **coherently** — it must match the
existing naming scheme, code patterns, procedural-asset pipeline, lore, and balance, and
it must build + load on a dedicated server before it's done.

MC 1.21.4 · Fabric loader 0.16.10 · fabric-api 0.119.2 · Yarn 1.21.4+build.8 ·
namespace `echoes` · group `com.echoes`. Lore = Walter Russell cosmology (Light energy,
octaves, resonance, stillness, polarity — see `docs/cosmology.md`).

## The workflow — follow it in order

### 1. Clarify the idea
Restate what the user wants in one or two sentences. Ask **only** the questions whose
answers change the implementation (scope, progression tier, whether it ties into an
existing system). If the idea is small and unambiguous, skip straight to planning.

### 2. Plan first — always produce a proposal before mass edits
For anything bigger than a single block/item, write a short design doc at
`docs/<feature>-proposal.md`, mirroring `docs/naming-proposal.md`:
- A table: **id | display name | type | what it does** for every new block/item.
- How it slots into existing progression and which existing things it reuses.
- Recipe sketches and any balance notes (Light cost, drop rates, tiers).
Present it and get sign-off **before** writing Java/assets. This repo's owner prefers
plan-then-build. (For a trivial one-block change, a short inline plan is fine.)

### 3. Name it the repo's way (read `docs/naming-proposal.md` first)
Registry **id == display name**, and the name is **both** on-theme **and** obvious about
function. Established families — reuse them, don't invent parallels:
- Generators → **Coil** · storage → **Cell** · wired transport → **Conduit**
- Whole wireless system → **Wave ___** · wireless redstone → **Signal Relay**
- Machines/fields → plain function words (Compressor, Transmuter, Growth Radiator, Balancer)
- Ores/materials/garden keep clear themed names (Echocite, Drumstone, Silentite, Lumewood…)
Pick a small consistent family for a new suite and apply it uniformly.
> Note: Java **class/field names still use legacy names** (e.g. field `FREQUENCY_TUNER`
> registers id `"wave_tuner"`; `RadiatorBlock` backs `growth_radiator`). Don't be thrown by
> this. For NEW content, name the Java symbol to match the new id to avoid future drift.

### 4. Implement — match these exact patterns

**Register blocks** in `src/main/java/com/echoes/registry/ModBlocks.java` via the
`register(name, factory, settings)` helper (it also creates the `BlockItem`):
```java
public static final Block MY_THING = register("my_thing",
        Block::new, AbstractBlock.Settings.create().strength(3.0f).requiresTool());
```
Crops/plants/trees use vanilla bases already imported here: `CropBlock`, `SaplingBlock`,
`SaplingGenerator`, `LeavesBlock`, `FlowerBlock`, `PillarBlock` (logs), `SlabBlock`,
`StairsBlock`, `FenceBlock`, `FenceGateBlock`, `TrapdoorBlock`, `WoodType`/`BlockSetType`.
Study how `lumewood_*`, `verdant_loam`, and `lumebloom` are wired and copy that exactly.
Custom behavior → add a class in `src/main/java/com/echoes/block/` (see existing ones).

**Register items** in `ModItems.java` the same way: `register("my_item", Item::new, new Item.Settings())`.
Food → `new Item.Settings().food(new FoodComponent.Builder()....build())`.

**Show it in the creative tab:** add to `ModItemGroups.java` (group `Octaves of the One`).
Block entities → `ModBlockEntities.java`; screens → `ModScreens.java`; worldgen (trees,
ore features, biome modifiers) → `ModWorldGen.java` + `data/echoes/worldgen/...`.

**Recipes** live in `src/main/resources/data/echoes/recipe/`:
- Vanilla shaped/shapeless/smelting — standard `minecraft:crafting_shaped` etc. (see
  `resonant_coil.json`, `octave_seed.json`).
- The mod's machine recipe is the **custom type `echoes:crushing`** (the Compressor;
  class `com.echoes.recipe.CrushingRecipe`, registered in `ModRecipes.java`). Recipes go in
  `data/echoes/recipe/compressor/`. Copy an existing one exactly — fields:
  `ingredient`, `result{id,count}`, `energy`, `processingTime`, optional `secondary`+`secondaryChance`.
  Don't assume vanilla schema for it. If you need a NEW machine process, add a new recipe
  type class + serializer and register it in `ModRecipes.java`.

**Lang** — every block/item needs BOTH a name key and a description key in
`src/main/resources/assets/echoes/lang/en_us.json`:
```
"block.echoes.my_thing": "My Thing",
"tooltip.echoes.desc.my_thing": "One-line flavor that says what it does."
```
Match the existing terse, lore-tinged tooltip voice (~64 desc keys already exist).

**Tags & loot** — blocks need a loot table (`data/echoes/loot_table/blocks/<id>.json`) and
usually mineable/tool tags. Wood sets need logs/planks/leaves/saplings tags + strippable +
flammability. **Missing one file silently fails server load** — checklist every file.

### 5. Generate assets procedurally — DO NOT hand-author PNGs, there is NO Pillow
All textures and most JSON models are produced by pure-Python generators. Extend them; run them.
- **Textures:** `gen_textures.py` (helpers: `write_png`, `shade`, `lerp`, `bloom`, `ore`,
  `cross`, `bezel`, `glyph`, `core`, `ripples`, …) and `gen_phase2_assets.py`. Crops need
  one frame per growth stage. Match the existing teal/bronze sculk palette.
- **Models/blockstates/loot/recipes via helpers** in `gen_phase2_assets.py`:
  `bs` (blockstate), `bmodel`/`imodel` (block/item models), `loot`, `self_drop`,
  `slab_drop`, `cube_all`, `pillar`, `stairs`, `slab`, `fence`, `fence_gate`, `trapdoor`,
  `cross` (crops/flowers), `shaped`, `shapeless`, `smelt`. Add your block to the right
  helper list and re-run the script.
After editing a generator, **run it** (`python3 gen_textures.py`, `python3 gen_phase2_assets.py`)
and confirm the new files appear under `src/main/resources/assets/echoes/` and `.../data/echoes/`.

### 6. Update the wiki (both generated AND hand-written)
- **Generated:** `docs/site/` + `docs/wiki/images/` come from `scripts/build_wiki_site.py`,
  `scripts/gen_wiki_recipes.py`, `gen_wiki_icons.py`, `gen_wiki_blocks3d.py`, plus
  `gallery.py`/`montage.py`. Re-run the relevant ones so new ids/recipes/textures show up.
- **Hand-written prose:** edit the matching `docs/wiki/*.md` (Blocks.md, Items-and-Gear.md,
  Ores-and-Worldgen.md, Crafting-and-Progression.md, Getting-Started.md as relevant).

### 7. Verify — not done until this passes
```
./gradlew build            # must SUCCEED (toolchain auto-selects JDK 21; no -D override)
```
Then boot a **dedicated server** and confirm it reaches "Done", parses recipes/tags/biome
modifiers, and logs no registry/missing-texture/missing-model errors. Server-load catches
data errors a client run misses. Fix everything it reports.

### 8. Ship
Develop on the assigned feature branch. Commit with clear messages. Push with
`git push -u origin <branch>`. Open a **draft** PR (via the GitHub MCP tools) if none exists.
Keep GitHub comments frugal.

## Pitfalls (these have actually bitten this repo)
- **Scripted renames mangling Java identifiers** — only ever match whole tokens
  (`\bword\b` / id-boundary regex), never naive `str.replace`.
- **Wood sets** are the #1 source of silent server-load failures: ~18 files per set
  (blockstates, models, item models, loot tables, tags, strippable, flammability). Checklist them.
- **Custom recipe type** is `echoes:crushing`, not a vanilla type — copy an existing
  `compressor/*.json`.
- **Balance vs. the ore economy** — if material/resource crops are too cheap they trivialize
  the echocite/drumstone/silentite progression the mod is built on. Gate them behind cost/tiers.
- **Don't commit machine-specific config** — JDK comes from the Gradle toolchain in
  `build.gradle`; never reintroduce `org.gradle.java.home`.
- **Regenerate, don't hand-edit, generated files** — anything under `docs/site/`,
  `docs/wiki/images/`, and most `assets/`/`data/` JSON is produced by a script; change the
  script and re-run, or your edit gets overwritten next regeneration.

## Quick reference — where things live
- Registries: `src/main/java/com/echoes/registry/Mod{Blocks,Items,BlockEntities,ItemGroups,Screens,WorldGen,Components}.java`
- Block logic: `src/main/java/com/echoes/block/` · items: `.../item/` · recipes: `.../recipe/`
- Assets: `src/main/resources/assets/echoes/{blockstates,models/{block,item},items,textures/{block,item},lang/en_us.json}`
- Data: `src/main/resources/data/echoes/{recipe,loot_table,tags,worldgen,resonance_sources.json}`
- Generators: `gen_textures.py`, `gen_phase2_assets.py`, `gallery.py`, `montage.py`, `scripts/gen_wiki_*.py`, `scripts/build_wiki_site.py`
- Lore/design: `docs/cosmology.md`, `docs/naming-proposal.md`, `docs/roadmap.md`, `docs/wiki/*.md`
