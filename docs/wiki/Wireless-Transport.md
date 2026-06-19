# Wireless Transport

[← Home](Home.md) · See also the full design doc: [`docs/wireless_transport.md`](../wireless_transport.md)

> *Sound needs no wire. Tune two stones to the same note and they remember each
> other across any distance.*

The wireless system moves **items, fluids, and Light (RU)** over **channels**
(octaves) with no conduit runs. It's the thematic counterpart to the wired
[Energy System](Energy-System.md), and it's deliberately **cheap and early** —
the first logistics tool you reach for, like hoppers in vanilla.

## Channels (octaves)

There are **16 channels**, one per **dye colour**. A device only resonates with
other devices on the **same** channel. Tuning controls are shared across every
gadget:

| Interaction | Effect |
| --- | --- |
| Right-click (empty hand) | Cycle **mode**: Receive → Send → Disabled |
| Sneak + right-click (empty hand) | Step the channel forward one colour |
| Right-click with any **dye** | Jump straight to that colour's channel |

In-world the modes read **Regive (receive)**, **Give (send)**, and **Stilled**.
A comparator next to a device reads `0` when disabled, otherwise a coarse channel
indicator (1–15).

## The Wave Relay (the flagship)

**Wave Relay** — `echoes:resonant_relay`. Place it against any container, tank,
machine, or Coil — it "wraps" the block on its facing side. Then:

- **Send** relays pull cargo *out* of the block they face and broadcast it on the channel.
- **Receive** relays pull cargo *off* the channel and push it *into* the block they face.

Items and fluids ride the Fabric **Transfer API**, so **vanilla chests, barrels,
furnaces, and any modded inventory/tank work out of the box**. RU bridges the
relay to the `ResonanceNode` grid, so a Coil's stored Light can be beamed to a
distant Compressor with no conduit run. Crafts **2 at a time**.

## The channel-gadget family

All share the tuning controls above and the `WirelessDevice` interface, so the
router treats them uniformly.

### Throughput & routing
- **Amplitude Coil** (`resonant_amplifier`) — each one on a channel **doubles**
  that channel's per-tick budget (**×2 per coil, capped ×16**). The way to make a
  channel "fat".
- **Harmonic Filter** (`harmonic_filter`) — a **3×3 ghost-slot** screen sets the
  channel's item **whitelist** (samples aren't consumed). Fluids are filtered by
  their **bucket item** (a water bucket whitelists water). Enables wireless
  sorting; item transport is constrained to the union of every filter's whitelist.
- **Interchange Splitter** (`resonant_splitter`) — toggles delivery between
  **balanced round-robin** across receivers and **fill-first**.

### Range & cost
- **Octave Repeater** (`echo_repeater`) — pools its channel across **every
  dimension** it appears in. Without one, a channel stays within a single
  dimension. (Craftable two ways: with ender pearls, or with Silentite crystals.)
- **Hush Cost** — an opt-in tax (`WirelessNetworkManager.HUSH_COST`, **off by
  default**): when on, broadcasting drains a little Light per sender from the
  channel's energy providers, tying logistics back into the energy economy. Free
  on channels with no energy provider.

### Quality of life
- **Locked Potential Vault** (`resonant_chest`) — a **27-slot** storage block
  natively on a channel (no separate relay). A *passive* buffer: senders fill it,
  receivers drain it, but it never shuffles with other passive stores.
- **Octave Tuner** (`frequency_tuner`, handheld) — copy/paste a channel between
  devices without dyes.
- **Octave Atlas** (`channel_atlas`, handheld) — print a device's channel roster,
  or an overview of every active channel.

### Cross-system glue
- **Polarity Coupler** (`conduit_coupler`) — joins the **wired** Wave Conduit grid
  as a STORAGE node while its buffer doubles as a wireless **RU endpoint**,
  formally bridging the two transport systems (Send = wired→channel,
  Receive = channel→wired). Also the Team Reborn Energy bridge point — see
  [Compatibility](Compatibility.md).
- **Tone Relay** (`note_relay`) — wireless **redstone bus**: Send broadcasts the
  redstone power it receives onto the channel; Receive emits the channel's
  strongest broadcast.

## Transfer budget (bounded by design)

Every channel has a per-tick budget that scales with the number of senders but is
**hard-capped**, so a large build never threatens the server tick:

| Cargo | Per sender / tick | Base cap / channel |
| --- | --- | --- |
| Items | 8 | 64 |
| Fluids | 1 bucket (1,000 mB) | 8 buckets |
| Light (RU) | 1,000 | 16,000 |

All three caps are multiplied by the **Amplitude Coil** multiplier (×2 each,
capped ×16). A channel needs at least **2 devices** to tick.

## Implementation notes

- `com.echoes.wireless.WirelessDevice` — the router-facing interface: channel,
  mode, item/fluid/energy endpoints, and modifier hooks (amplifier, repeater,
  round-robin, whitelist, redstone).
- `com.echoes.wireless.WirelessNetworkManager` — server-global router keyed by
  `GlobalPos` (so repeaters span dimensions). Ticks on `END_SERVER_TICK`, skipping
  channels with fewer than two devices.
- `com.echoes.wireless.RelayMode` — Receive / Send / Disabled.
- `AbstractChannelDeviceBlock` / `AbstractChannelDeviceBlockEntity` — shared
  tuning, ticker-based registration, and NBT; each concrete device overrides only
  the hooks it needs.

The roster is in-memory and rebuilt from the world as chunks load — every device
carries its channel/mode in NBT and re-registers on load, so the wireless side
**self-heals** across restarts.
