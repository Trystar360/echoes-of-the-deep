# Transmutation & Light Values

[← Home](Home.md)

The mod's endgame economy: **matter is condensed Light** (Russell), so every item carries a
**Light Value** — its *Bound Light*. The **Transmutation Table** and portable **Tablet**
are your personal Bound-Light account; you **dissolve** matter to bank its value, **withdraw**
it as **Mote** coins, and **condense** anything you've attuned back out of the pool.

## Your Bound-Light account

Bound Light is stored **per player**, not per block — a single ledger (`TransmutationState`)
that both the Table and the Tablet read. Your banked Light and your *attuned* items (the
ones you've "learned" by dissolving) follow you across the world and survive restarts. Open
the same account from a placed **Transmutation Table** or the pocket **Transmutation Tablet**.

## The three actions

| Action | What it does |
| --- | --- |
| **Dissolve** | Put an item in the input slot and dissolve it: its full Light Value (× the stack count) is **banked**, and its tone is **attuned** so you can re-create it later. |
| **Withdraw** | Pay the pool out as physical **Mote** coins (five tones, below). Useful as a tradeable, stackable currency. |
| **Condense** | Set a *ghost template* of an item you've attuned, then condense **×1** or **×64** — each copy costs that item's Light Value from your pool. |

## The Mote ladder

Motes are the denomination coins of Bound Light — each tone is Light wound one octave
higher (**×4 per octave**). The triad resolves to harmony:

| Mote | Tone | Light Value |
| --- | --- | --- |
| **Light Mote** | the universal One | 64 |
| **Tonic Mote** | octave 1 | 256 |
| **Mediant Mote** | the chord's middle | 1,024 |
| **Dominant Mote** | nearing the crest | 4,096 |
| **Harmonic Mote** | the resolved crest | 16,384 |

Withdraw and re-dissolve are exact inverses, so Motes are a loss-free way to carry value.

## Octave Stars

**Octave Stars** are portable Bound-Light batteries in six tiers (I–VI), each **×4** the
last (100,000 → 102,400,000). *Use* charges a Star from your account; *sneak-use* pours it
back. They're tradeable between players — a way to hand someone raw Bound Light.

## How Light Values are decided

Values come from two layers:

1. **Seeds** — a small hand-authored set in
   [`light_values.json`](https://github.com/Trystar360/echoes-of-the-deep/blob/main/src/main/resources/data/echoes/light_values.json):
   primitives that aren't craftable from anything cheaper (ores, mob drops, plants), plus
   explicit overrides and a blacklist. Seeds are **authoritative**.
2. **Derived** — every other item, **vanilla or modded**, gets a value by propagating the
   seeds through the **entire recipe graph**: an item's value is the cheapest
   `sum(inputs) / outputCount` over all recipes that make it, iterated to a fixed point.

Because derivation always takes the **minimum** and **floors** the division, you can never
craft *up* in value — there's no recipe loop that profits. This protects ore progression
while still giving sensible values to thousands of items for free. Reloads (`/reload`) and
server start recompute the table; modpacks override values with their own datapack copy of
the JSON.

> **Footguns.** Dissolving a container (e.g. a full shulker box) values it at the container
> only — its contents are lost. Items with damage/enchantments dissolve at their base value;
> enchantments are not refunded.

## Recipes & unlock

The **Transmutation Table** is built from Radiant Ingots, Echocite Bricks, and an Octave
Seed — so it sits naturally at the top of the [octave climb](Crafting-and-Progression.md).
The advancement **"Balanced Interchange"** marks the moment you build it; **"The Resolved
Crest"** and **"Carry the Light"** are the challenge capstones. See
[The Great Work](The-Great-Work.md).
