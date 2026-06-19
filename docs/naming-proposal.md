# Naming scheme proposal — one cohesive ecosystem

Goal: make every block/item's **id** and **display name** the same word, chosen so
it is **both** Walter-Russell-themed **and** immediately clear what the thing does.
Names are grouped into consistent families so the tech tree reads as one system.

> Status: **proposal — awaiting sign-off.** Nothing is renamed in-game until approved.
> The rename mechanism is already proven (full build + server load: 1430 recipes parsed).

## Design conventions
- **Generators → "Coil"** · **Storage → "Cell"** · **Wired transport → "Conduit"**
- **Wireless system → "Wave ___"** (one obvious family) · **Wireless redstone → "Signal Relay"**
- **Machines/fields → plain function words** (Compressor, Furnace, Radiator, Field)
- Keep names that are already clear + on-theme (ores, materials, garden set, tools where good).

## Proposed changes (id → id, and display name)

| Current id | Current name | → New id | → New name |
| --- | --- | --- | --- |
| `resonator` | Generative Coil | `resonant_coil` | **Resonant Coil** |
| `resonance_capacitor` | Accumulator | `resonance_cell` | **Resonance Cell** |
| `greater_accumulator` | Greater Accumulator | `greater_resonance_cell` | **Greater Resonance Cell** |
| `tuning_conduit` | Wave Conduit | `wave_conduit` | **Wave Conduit** |
| `dense_conduit` | Dense Wave Conduit | `dense_wave_conduit` | **Dense Wave Conduit** |
| `crusher` | Compressor | `compressor` | **Compressor** |
| `attunement_furnace` | Transmuter | `transmuter` | **Transmuter** |
| `radiator` | Radiator | `growth_radiator` | **Growth Radiator** |
| `resonant_relay` | Wave Relay | `wave_relay` | **Wave Relay** |
| `resonant_amplifier` | Amplitude Coil | `wave_amplifier` | **Wave Amplifier** |
| `harmonic_filter` | Harmonic Filter | `wave_filter` | **Wave Filter** |
| `resonant_splitter` | Interchange Splitter | `wave_splitter` | **Wave Splitter** |
| `echo_repeater` | Octave Repeater | `wave_repeater` | **Wave Repeater** |
| `conduit_coupler` | Polarity Coupler | `wave_coupler` | **Wave Coupler** |
| `resonant_chest` | Locked Potential Vault | `wave_chest` | **Wave Chest** |
| `note_relay` | Tone Relay | `signal_relay` | **Signal Relay** |
| `frequency_tuner` | Octave Tuner | `wave_tuner` | **Wave Tuner** |
| `channel_atlas` | Octave Atlas | `wave_atlas` | **Wave Atlas** |
| `resonance_meter` | Light Meter | `light_meter` | **Light Meter** |
| `resonance_thrusters` | Centrifugal Thrusters | `resonant_thrusters` | **Resonant Thrusters** |

## Kept as-is (already clear + on-theme)
- **Generation:** Stillness Core, Octave Coil, Storm Caller
- **Transport:** Octave Conduit
- **Fields:** Warmth Radiator, Polarity Field, Balancer
- **Tools:** Resonant Pickaxe / Axe / Shovel / Sword / Hoe
- **Ores & materials:** Echocite Ore (+Deepslate), Drumstone Ore, Silentite Ore, Raw Echocite,
  Echocite Dust, Echo Ingot, Echo Dust, Resonant Slag, Dull Ingot, Drumstone Shard, Drum Core,
  Silentite Crystal, Octave Seed, Radiant Dust, Radiant Ingot
- **Garden & building:** Lumewood set (log/wood/planks/stairs/slab/fence/gate/trapdoor/leaves/sapling),
  Lumebloom, Lume Lantern, Verdant Loam, Echocite Bricks (+stairs/slab)

## The resulting ecosystem at a glance
- **Coils** generate Light — Stillness Core (rest) · Resonant Coil (sound) · Octave Coil (radiant) · Storm Caller (lightning)
- **Cells** store it — Resonance Cell · Greater Resonance Cell
- **Conduits** carry it — Wave · Dense Wave · Octave
- **Wave ___** is the whole wireless system — Relay, Amplifier, Filter, Splitter, Repeater, Coupler, Chest, Tuner, Atlas — plus **Signal Relay** for wireless redstone
- **Machines** process — Compressor (crush) · Resonant Furnace (smelt)
- **Radiators & Fields** emit — Growth Radiator · Warmth Radiator · Polarity Field · Balancer

## Decisions (locked)
1. Storage → **Resonance Cell** / **Greater Resonance Cell** (cohesive "Cell" family).
2. Smelter → **Transmuter** (keep the thematic name; raising matter an octave).
3. Wireless tools → **Wave Tuner** / **Wave Atlas** (folded into the Wave family).

## Implementation checklist (runs only after you approve the doc)
- [ ] Rename registry ids in code (block/item registration) per the table
- [ ] Move + rename asset files: models, blockstates, textures, recipes, loot tables, tags, advancements
- [ ] Update `lang/en_us.json` so each `*.name` matches the new display name
- [ ] Sweep for hardcoded id references (data-gen, JEI/REI compat, code lookups)
- [ ] Regenerate the wiki from the new ids/names
- [ ] Verify: full `build` + dedicated-server load (recipe/tag/registry parse), then open PR
