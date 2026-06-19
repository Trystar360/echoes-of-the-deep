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

## The family (implemented)

A coherent system of channel gadgets, all sharing the same tuning controls (dye =
set channel, sneak = step channel) and the `WirelessDevice` interface so the
router treats them uniformly.

### Throughput & routing
- **Resonant Amplifier** — placed on a channel, each one doubles that channel's
  per-tick transfer budget (×2 per amplifier, capped ×16). The way to make a
  channel "fat".
- **Wave Filter** — right-click with an item to whitelist its type (empty hand
  clears). Item transport on the channel is then constrained to the union of every
  filter's whitelist. Enables wireless sorting.
- **Resonant Splitter** — toggles the channel between even round-robin sharing
  across receivers and fill-first delivery.

### Range & cost
- **Echo Repeater** — pools its channel across *every dimension* it appears in;
  without one, a channel stays within a single dimension.
- **Hush Cost** — opt-in (`WirelessNetworkManager.HUSH_COST`): when on, broadcasting
  cargo drains a little RU per sender from the channel's energy providers, tying
  logistics back into the energy economy. Off by default to keep the base relay
  cheap; free on channels with no energy provider.

### Quality of life
- **Frequency Tuner** (handheld) — sneak + right-click a device to copy its channel
  into the tuner; right-click another to paste it. Batch-tune without dyes.
- **Channel Atlas** (handheld) — right-click a device to print its channel roster
  (device/sender/receiver/modifier counts); right-click the air for an overview of
  every active channel.
- **Resonant Chest** — a 27-slot storage block natively on a channel (no separate
  relay). A *passive* buffer: senders fill it, receivers drain it, but it never
  shuffles with other passive stores.

### Cross-system glue
- **Conduit Coupler** — joins the wired Tuning Conduit grid as a STORAGE node while
  its buffer doubles as a wireless RU endpoint, formally bridging the two transport
  systems (Send = wired→channel, Receive = channel→wired).
- **Note Relay** — wireless redstone bus: Send broadcasts the redstone power it
  receives onto the channel; Receive emits the channel's strongest broadcast.

**Tuning constants** (`WirelessNetworkManager`): 8 items, 1 bucket, and 1000 RU per
*sender* per tick, base-capped at 64 items / 8 buckets / 16000 RU, all scaled by the
amplifier multiplier.

### Still open
- Per-target **priority** ordering (the Splitter only picks even vs. fill-first).
- A ghost-slot **filter GUI** and **fluid** filtering (the filter is item-type, set
  by clicking).
- Persisting the roster via a `PersistentState` (today it is rebuilt from NBT on
  chunk load, like the wired grid).

## Implementation notes

- `com.echoes.wireless.WirelessDevice` — the interface the router sees: channel,
  transport mode, item/fluid/energy endpoints, and the modifier hooks (amplifier,
  repeater, round-robin, item whitelist, redstone).
- `com.echoes.wireless.WirelessNetworkManager` — server-global router keyed by
  `GlobalPos` (so repeaters can span dimensions). Ticks on `END_SERVER_TICK`,
  skipping channels with fewer than two devices; per channel it groups by dimension
  (unless a repeater is present), then moves items (two-pass so passive chests don't
  shuffle), fluids, RU, and the redstone level.
- `com.echoes.wireless.RelayMode` — Receive / Send / Disabled.
- `com.echoes.block.AbstractChannelDeviceBlock` / `entity.AbstractChannelDeviceBlockEntity`
  — shared tuning, ticker-based registration, and NBT for every gadget; each concrete
  device overrides only the `WirelessDevice` hooks it needs.

Like `ResonanceNetworkManager`, the roster is in-memory and rebuilt from the world as
chunks load (every device's channel/mode lives in its block-entity NBT).
