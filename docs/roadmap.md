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
flora, and new wood types. Full proposal (awaiting sign-off) in
[`docs/botanicals-proposal.md`](botanicals-proposal.md). Depends only on shipped
systems (Compressor, Transmuter, Verdant Loam, Growth Radiator, transmutation chain).

Decisions are locked (tones, full EMC set, Hushwood-first, resource crops as v1); see the
proposal's "Decisions (locked)" section.

- ⬜ **Mote base loop** *(v1)* — **Mote Seed** → **Mote Sprout** crop → **Light Mote** (O0,
  raw Light, the universal One), the binder used in every seed/soil/recipe.
- ⬜ **Octave climb (energy → higher octave)** *(v1)* — feed grid **Light** into a Mote in
  the **Transmuter** (+ **Octave Seed** as the inert-gas rest) to wind it up the octaves,
  named as tones: **Tonic (O1) → Mediant (O2) → Dominant (O3) → Harmonic (O4, crest)** —
  the triad resolving to harmony; each step costs more energy, and the Harmonic tier
  bridges into the existing Radiant Dust chain.
- ⬜ **Soil tiers** *(v1)* — **Attuned / Radiant Loam** on Verdant Loam, gating crop octave.
- ⬜ **Resource crops (ores & materials)** *(v1)* — a `<Material> Sprout` per resource (incl.
  the mod's own Echocite=O1/Drumstone=O2/Silentite=O4/**Radiant**=crest), renewable but
  gated behind charged Motes + Growth-Radiator cost.
- ⬜ **Wood types** — **Hushwood** *(v1, inert/rest octave; full set, worldgen)*; **Sunwood**
  *(fast-follow; Harmonic crest, golden/glowing; sapling matures only under a Growth Radiator)*.
- ⬜ **Transmutation economy (EMC = Bound Light)** *(v1)* — every item carries a **Light
  Value** (Russell's "matter is condensed Light"). A **Transmutation Table** + portable
  **Transmutation Tablet** is the balanced-interchange altar: dissolve matter → Bound
  Light, condense Bound Light → any *attuned* item. The Mote ladder is the value scale
  (×4/octave). Full set ships: **Octave Stars** (portable Bound-Light batteries), **Tone
  Collector** (winds ambient light into Bound Light), **Condenser** (auto-duplicate),
  **Codex of Tones** (attune-all), and the **Interchange Coil** (free⇄bound Light bridge —
  steep, hard-capped). Gated to the Harmonic tier with attune-from-sample + capped sources
  to protect ore progression.
- ⬜ **Later passes** — **food crops** (Resonant Grain→Bread, Glowgourd, Stillmint→Still
  Tea), **decorative/dye flora** (Lumecap, Chime Lily, Octave Orchid, Verdant Fern), then
  **mob-essence crops** (Deep-Dark themed).
- ⬜ **Transmutation economy (EMC = Bound Light)** — every item carries a **Light Value**
  (the Light condensed into it; Russell's "matter is condensed Light"). A
  **Transmutation Table** (and portable **Transmutation Tablet**) is the balanced-
  interchange altar: dissolve matter → Bound Light, condense Bound Light → any *attuned*
  item. The Mote octave ladder is the value scale (×4/octave). Optional ProjectE-shaped
  extras: **Octave Stars** (portable Bound-Light batteries), **Tone Collector** (winds
  ambient light into Bound Light), **Condenser** (auto-duplicate), **Codex of Tones**
  (attune-all), and an optional **Interchange Coil** (free⇄bound Light bridge). Gated to
  the Radiant tier with attune-from-sample + capped sources to protect ore progression.

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
