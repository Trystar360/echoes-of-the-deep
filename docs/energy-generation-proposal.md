# Energy generation rework — standing-wave resonant arrays

Status: proposed → implementing on `claude/energy-generation-mechanics-jnbrb5`.

## The problem

Three of the four Light generators are "place block → flat RU/t":

| Generator | Old mechanic |
| --- | --- |
| Resonant Coil | captures RU from nearby world *sounds* — the good one |
| Stillness Core | flat 4 RU/t, always |
| Octave Coil | flat 24 RU/t × a "rate" slider, always |
| Storm Caller | flat 40,000 per self-called lightning bolt |

The flat generators ignore the mod's own sound/resonance/Russell theme. This rework makes
**all four** generators read their world and reward *building*, using one coherent idea.

## The unifying mechanic — standing-wave coupling

Russell: tones repeat in **octaves**, and the cosmos is **rhythmic balanced interchange** —
standing waves with nodes and antinodes. We make that literal.

Every generator gains an **Octave** dial (the existing `BlockConfig.octave`, already
GUI-/NBT-supported, previously used only by wireless devices — free to reuse on generators).

Generators tuned to the **same octave** within a **coupling radius (10 blocks)** form a
**resonant array**. But they only reinforce each other when placed on the **antinode
lattice** for that octave:

- **Resonant spacing** for octave *o* is `2 + o` blocks (octave 0 → 2, octave 8 → 10).
  Higher octave = higher pitch = wider standing wave.
- A partner contributes `align = max(0, 1 − |distance − spacing|)` — full weight when it
  sits exactly on an antinode, nothing when it's more than a block off. So a sloppy clump
  does almost nothing; a clean axis-aligned lattice at the right pitch does everything.
- Contributions sum into a **coupling** value, capped at **4.0** (≈ four well-placed
  partners). Each generator turns coupling into its own **resonance multiplier**.

This is read once every 40 ticks per generator (staggered, chunk-local scan — same cheap
pattern as ambient sound capture) and cached.

## Each generator's role in the array

| Generator | Base | Resonance role | Multiplier (lone → full array) |
| --- | --- | --- | --- |
| **Octave Coil** | 24 RU/t | **Workhorse** — scales hardest with a tuned lattice | ×1 → **×4** (24 → 96 RU/t) |
| **Stillness Core** | 4 RU/t | **Anchor** — humble output, but counts as a **×1.5 antinode** that empowers every partner. Build your lattice around it. | ×1 → ×2 (4 → 8 RU/t) |
| **Storm Caller** | 40,000/strike | **Amplified spire** — a tuned spire array catches a bigger discharge | ×1 → **×3** (40k → 120k/strike) |
| **Resonant Coil** | captured sound RU | **Sound converter** — an arrayed Coil converts ambient sound far more efficiently | ×1 → **×3** captured |

Design notes:
- The **Stillness Core anchor** is the cosmology payoff: "all motion springs from the still
  centre." The cheap early generator becomes structurally vital late — you anchor lattices on it.
- The **Octave Coil "rate" slider is removed** — the array *is* the new way to scale, and its
  ceiling (96 RU/t) matches the old max. No more boring power dial.
- The **Storm Caller keeps its strike-frequency dial** (that's behaviour, not a flat power
  slider) and *adds* the octave dial.
- A **lone generator still works at ×1**, so early game is unchanged; the depth is opt-in.

## Feedback (the mechanic is invisible otherwise)

- **Light Meter** on any generator now reports: `Resonance ×N.NN (k in-tune partners,
  spacing M)` — so players can see and tune their array.
- Generators in a strong array (multiplier ≥ ~1.5) emit occasional **note particles** — you
  can *see* a lattice ringing.

## Save compatibility

No new blocks, items, recipes, or registry ids. Pure behaviour + config-exposure change on
existing block entities, plus one shared helper class. Existing worlds load unchanged;
generators default to octave 0 (tight spacing-2 arrays) until tuned.

## Touched files

- New: `energy/ResonanceCoupler.java` (marker interface), `energy/ResonanceField.java` (scan + math).
- Edit: `ResonatorBlockEntity` (+ ticker in `ResonatorBlock`), `StillnessCoreBlockEntity`,
  `OctaveCoilBlockEntity`, `StormCallerBlockEntity`, `ResonanceMeterItem`.
- Lang: meter line + refreshed generator tooltips. Wiki: `Energy-System.md` generation section.
