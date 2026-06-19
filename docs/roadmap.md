# The Great Work — a map of projects

A cohesive plan for growing **Octaves of the One**, organized by Walter Russell's
cosmology so every addition reinforces the same idea: a *two-way universe* of
**rhythmic balanced interchange** — Light wound up from stillness (generation,
compression) and unwound back into it (radiation, expansion), through the octaves.

Status legend: ✅ done · 🔜 next · ⬜ planned · 🐞 bug to fix. Each project notes
its **concept** (the Russell tie) and **mechanic**.

---

## Phase 0 — Correctness & fixes
Known issues to clear first; these gate a clean release before more content lands.

- 🐞 **Inventory textures** — fix item/inventory sprites that render wrong (missing,
  mismatched, or mis-scaled icons in the hotbar and GUIs).
- 🐞 **Block breakability** — fix blocks that mine incorrectly: wrong harvest tool /
  mining level, off break speed, or missing/incorrect drops.
- ⬜ **Block descriptions** — a short lore + function tooltip on every block and item
  (hover text, optionally an in-world guidebook), so each block explains itself and
  the cosmology reads at a glance.

## Phase I — The Two-Way Universe (generation ↔ radiation)
Make the charge/discharge duality a real, paired mechanic. *Today only generation
(Coils, Stillness Core) exists; radiation is the missing half.*

- ✅ **Growth Radiator** — *radiation / centrifugal outpouring.* Consumes Light and pours it
  back into the world as **life**: accelerates crops & saplings in a radius, and glows.
- ✅ **Warmth Radiator** — radiates heat: cooks dropped items (vanilla smelting), melts
  snow & ice nearby, glows brightly (a powered campfire-of-Light).
- ✅ **Polarity Field** — *the two poles of one device.* Right-click toggles **Attract**
  (centripetal — pulls in items & XP) and **Repel** (centrifugal — throws mobs outward).
- ✅ **Balancer** — *rhythmic balanced interchange.* Nudges all storage on its network
  toward the same fill ratio so no Resonance Cell hoards; the grid breathes evenly.

*Phase I complete — the generation↔radiation duality is now a full, paired mechanic.*

## Phase II — The Octaves (tiered progression + the living garden)
Russell builds matter in nine octaves of tones; give the tech tree the same spine —
and a botanical octave the player can build, light, and tend.

- ✅ **Inert-gas Seed** — *each octave's rest point.* The **Octave Seed** (Silentite +
  Drum Core + Echo Dust) is the catalyst that opens the transmutation chain.
- ✅ **Transmutation chain** — the Seed feeds **Radiant Dust → Radiant Ingot** (smelt /
  blast), the charged matter that builds the higher-octave bank.
- ✅ **Greater Resonance Cell** — block-of-light tier storage (2,000,000 Light), built from
  a ring of Radiant Ingots around an Resonance Cell; full config GUI + comparator output.
- ✅ **Lumewood set** — a glowing custom tree (log/wood, planks, stairs, slab, fence,
  fence gate, trapdoor, luminous leaves, sapling) that grows from the **Octave Grove**
  feature in forests — a full building-material family that emits Light.
- ✅ **Verdant Loam** — a configurable growth block: a ticking soil that pulses Light
  upward to fertilize/grow nearby plants in a tunable radius and interval (config GUI).
- ✅ **Garden flora & décor** — **Lumebloom** (glowing flower, grants Glowing), **Lume
  Lantern** (full-bright décor block), and **Echocite Bricks** (+ stairs & slab) for a
  matching luminous masonry set.
- ✅ **Octave coil/conduit tiers** — the **Octave Coil** (a strong baseline generator,
  300,000-Light buffer, tunable generation rate, comparator output) and **Octave
  Conduit** (64,000 Light/t — 4× the Dense Conduit), both crafted from Radiant Ingots
  so the transmutation chain feeds directly into higher generation & throughput.

## Phase II½ — The Verdant Octave (botanical & crop suite)
The garden as the grid's slowest octave: a Mystical-Agriculture–style economy built on
one **base crop that is compressed and re-used everywhere**, plus food, decorative
flora, and new wood types. Full design plan in
[`docs/botanical_suite.md`](botanical_suite.md). Depends only on shipped systems
(Compressor, Transmuter, Verdant Loam, Growth Radiator, transmutation chain).

- ⬜ **Lumen Essence base loop** — the **Mote Seed** → **Lumen Sprout** crop → **Lumen
  Essence**, the universal binder used in every seed/soil/tier-up. Density compression
  (9↔1 **Lumen Essence Block**) runs through the **Compressor**.
- ⬜ **Octave essence ladder** — **Lumen → Resonant → Radiant → Brilliant → Zenith**,
  each "raised an octave" in the **Transmuter** (Octave Seed catalyst); the 3rd octave
  folds into the existing **Radiant Dust** tier so the trees are one tree.
- ⬜ **Soil tiers** — **Attuned / Radiant / Brilliant / Zenith Loam** on top of Verdant
  Loam, gating which crop tier can mature.
- ⬜ **Resource crops (ores & materials)** — a crop per resource growing a
  `<Resource> Essence` (8 essence → 1 unit), incl. the mod's own Echocite/Drumstone/
  Silentite/**Radiant** crops, making the resonance economy renewable.
- ⬜ **Food crops** — Chime Grain, Tuneroot, Lumeberry, Echo Gourd, Stillfruit, Glowcap,
  Hush Pepper, plus cooking combos.
- ⬜ **Decorative flora** — flower set, grow-able **dye crops** (the 16 octave colours),
  Resonant Reeds, Bloomvine, Lumemoss/petal blocks.
- ⬜ **Wood types** — **Drumwood**, **Hushwood** (sound-dampening), **Sunwood** (Radiant,
  glowing), each a full vanilla-parity set; plus a **Heartwood** wood-crop.
- ⬜ **Automation** — **Harvest Resonator** (RU auto-harvester via Transfer API),
  **Lumewater Can**, **Fertilized Essence**.
- ⬜ **Mob-essence crops** — a small Deep-Dark–themed set, gated to tier 3+.

## Phase III — The Wave (motion & fields)
The wave is the unit of the cosmos; lean into movement and area effects.

- ✅ **Resonant Thrusters** — look-direction flight, fall immunity. *(done)*
- 🔜 **Resonant Ring (worn flight)** — *shift the jetpack onto a worn ring.* Move
  flight off the held item and onto a Trinkets/curio **ring** themed to the modpack
  (a "Ring of Octaves"), so flight no longer occupies a hand or hotbar slot. Keeps
  look-direction flight + fall immunity, recharged from the grid; Trinkets is already
  a soft dependency. Supersedes the held Thrusters as the intended end form.
- ⬜ **Wearable thrusters (jump-to-fly)** — optional chest-slot variant with the
  classic jump-to-fly jetpack feel; sneak-dash / double-pulse.
- ⬜ **Wave Walker** boots — walk on water/lava (surface tension of the wave).
- ⬜ **Levity Field / Vortex Pad** — anti-gravity column and a launch/landing pad
  (the spiral vortex that winds bodies up and down).

## Phase IV — Mind & Light (knowledge, automation, readout)
Russell's universe is Mind knowing itself; this is the control & information layer.

- ✅ **Light Meter / Wave Atlas** — diagnostics. *(done)*
- ✅ **Device Configuration GUI** — right-click any functional device with the
  Frequency Tuner to open a shared config screen: wireless **channel/octave**,
  **redstone** behaviour (always / needs-signal / off-on-signal), **per-face I/O**,
  and block-specific **tuning** (Growth Radiator & Polarity radius, Balancer rate). Wired
  effects today: channel (wireless), redstone gating, and tuning; per-face I/O is
  persisted and surfaced for the network layer to honour next.
- ⬜ **Seer** — a screen/handheld that visualizes a whole network: nodes, flow, fill.
- ⬜ **Octave Programmer** — set channels/modes/filters across many devices at once
  (builds on the per-device Configuration GUI).
- ⬜ **Advancements** — a guided "Great Work" progression tree.

## Phase V — Stillness & Balance (storage & endgame)
- ✅ **Stillness Core** — baseline Light from the still centre. *(done)*
- ✅ **Greater Resonance Cell** — high-octave bank (2,000,000 Light) from Radiant Ingots.
  *(see Phase II)*
- ⬜ **Zero-Point Well** — endgame: stronger generation tied to a rare structure/cost.

## Phase VI — Silence & the Unstruck Tone (the Silentite path)
- ⬜ **Silence Cloak** — suppresses the wearer's sound emissions (ties into ambient
  capture & mob detection); Deep-Dark/Warden synergy.
- ⬜ **Hush Field** — a zone that silences mobs / dampens vibrations (sculk sensors).

## Phase VII — World & Exploration
- ✅ **Storm Caller** — *generation from the sky's discharge.* A conductive spire that,
  during thunderstorms, calls lightning to itself far more often than nature would and
  banks the windfall (40,000 Light/strike into a 400,000 buffer; PROVIDER + STORAGE,
  comparator output). Needs open sky; the self-struck bolts are **cosmetic** so it
  never ignites a build; tunable strike rate via the config GUI. Russell's high-potential
  discharge wound back into Light. (Ties into [Ambient Capture](wiki/Ambient-Capture.md).)
- ⬜ **Octave Geodes** — buried resonant geode structures (Silentite in the Deep Dark).
- ⬜ **Sounding Chamber** — a small dungeon/altar with loot and a tuned puzzle.

## Phase VIII — Polish & Integration
- ⬜ **True emissive textures** — optional Continuity/Indium glow for the cores.
- ⬜ **Recipe-viewer compat** — JEI/EMI for the custom Compressor recipes.
- ⬜ **Sound & particle pass** — a tonal hum on active machines; flight/charge FX.
- ⬜ **Config** — tune generation/throughput/Hush-Cost without editing code.

## Wiki & Identity (presentation)
- ⬜ **Consolidate names & theme, internally and externally** — unify the dual naming
  so internal ids and external display names line up (e.g. `echoes:resonant_coil` shows as
  "Resonant Coil"). Today the namespace stays `echoes` and ids keep their original
  names for save-compatibility while the display layer is the "Octaves of the One"
  reskin. Decide one canonical identity and reconcile registry ids, lang, docs, and the
  mod name — with a migration/remap plan so existing worlds still load.
- ⬜ **In-game-style 3D renders on the wiki** — block/item icons that read like the real
  Minecraft inventory render (correct dimetric projection + face shading/AO), replacing
  flat icons. The earlier hand-rolled isometric looked off; needs a proper block-model
  renderer (or a headless MC/Blockbench render step) and visual sign-off.
- ✅ **All recipe ingredients clickable on the wiki** — every cell links out: mod items
  to their own page, vanilla items to the Minecraft Wiki.

---

*Working order:* Phase 0 fixes first (correctness gates a clean release), then the
Resonant Ring (the intended flight end form), then Phase II for long-term depth, and
III–VIII as polish and breadth. Items are independent enough to reorder on request.
