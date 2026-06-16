# The Resonance Carrier — wireless transport for Echoes of the Deep

> *Sound needs no wire. Tune two stones to the same note and they remember each
> other across any distance.*

This document describes the wireless transport family. The **Resonant Relay** (the
flagship) is implemented; the rest is the planned roadmap.

## Design pillars

1. **Wireless, not magic.** It fits the mod's premise: sound is a physical resource
   that carries without conduits. A relay *broadcasts on a frequency*; every relay
   tuned to that frequency resonates and shares cargo. This is the thematic
   counterpart to the wired **Tuning Conduit** RU grid.
2. **Cheap and early.** A relay is two dust + an iron ingot + redstone. It is the
   first thing a player reaches for, the way hoppers are in vanilla — utility, not
   a tech-tree capstone. Power and convenience come from *combining* relays and from
   the upgrade tier, not from a steep recipe.
3. **One block, three cargoes.** Items, fluids, and Resonance (RU) all ride the same
   channel, so a single concept covers logistics that would otherwise need three
   separate pipe systems.
4. **Bounded by design.** Every channel has a per-tick transfer budget that scales
   with the number of senders but is hard-capped, so a large build never threatens
   the server tick. The roster is maintained incrementally (register on load,
   unregister on removal) — no per-tick world scans.

## The Resonant Relay (implemented)

Place it against any container, tank, machine, or Resonator — it "wraps" the block
on its facing side. Then tune it:

| Interaction                     | Effect                                            |
| ------------------------------- | ------------------------------------------------- |
| Right-click (empty hand)        | Cycle mode: **Receive → Send → Disabled**         |
| Sneak + right-click (empty hand)| Step the channel forward one colour               |
| Right-click with any **dye**    | Jump straight to that colour's channel (16 total) |

- **Send** relays pull cargo *out* of the block they face and broadcast it.
- **Receive** relays pull cargo *off* the channel and push it *into* the block they face.
- A comparator next to a relay reads `0` when disabled, otherwise a rough channel
  indicator (1–15).

Items and fluids move through the Fabric **Transfer API**, so vanilla chests,
barrels, furnaces, and any modded inventory/tank work out of the box. RU bridges the
relay to the existing `ResonanceNode` energy system, so a Resonator's stored RU can
be beamed to a distant Crusher with no conduit run.

**Tuning constants** (`WirelessNetworkManager`): 8 items, 1 bucket, and 1000 RU per
*sender* per tick, capped per channel at 64 items / 8 buckets / 16000 RU.

## The family (roadmap)

A coherent system of "similar utilities" that all build on the channel concept:

### Throughput & routing
- **Resonant Amplifier** — a relay upgrade (or adjacent block) that multiplies a
  channel's per-tick budget. The intended way to make a channel "fat".
- **Harmonic Filter** — a relay variant with a ghost-slot filter UI: only matching
  items/fluids broadcast or are accepted. Enables sorting systems on one channel.
- **Round-Robin Splitter / Priority Tuner** — pick the distribution policy
  (even split vs. fill nearest/highest-priority receiver first).

### Range & cost
- **Hush Cost** — broadcasting could consume a trickle of RU per tick scaled by
  distance/volume, tying logistics back into the energy economy (toggleable, off by
  default to keep the base relay "cheap").
- **Echo Repeater** — extends a channel across dimensions, or boosts an otherwise
  range-limited variant if range limits are introduced for balance.

### Quality of life
- **Frequency Tuner** (handheld) — right-click a relay to copy its channel, right-click
  another to paste it. Batch-tunes a build without dye juggling.
- **Channel Atlas** — a book/screen listing every relay on a channel, its mode, and
  live throughput, for debugging large networks.
- **Resonant Tank / Resonant Chest** — storage blocks that are *natively* on a channel
  (no separate relay needed), the wireless analogue of an Ender Chest.

### Cross-system glue
- **Conduit Coupler** — a block that injects a wireless channel's RU into the wired
  Tuning Conduit grid (and vice-versa), formally bridging the two transport systems.
- **Note Beacon link** — relays could broadcast a redstone/comparator signal on a
  channel, turning frequencies into a wireless redstone bus.

## Implementation notes

- `com.echoes.wireless.WirelessNetworkManager` — per-`ServerWorld` router, one roster
  list per channel; ticks on `END_WORLD_TICK`, skipping channels without both a sender
  and a receiver.
- `com.echoes.wireless.RelayMode` — Receive / Send / Disabled.
- `com.echoes.block.ResonantRelayBlock` (+ `entity.ResonantRelayBlockEntity`) — the
  block, its `FACING`, interactions, and a lightweight ticker that keeps the relay on
  the roster across chunk loads.

Like `ResonanceNetworkManager`, the roster is in-memory and rebuilt from the world as
chunks load (the relay's channel/mode live in its block-entity NBT). A shipping build
would back it with a `PersistentState`.
