# Wireless Transport

[← Home](Home.md) · full design doc: [`docs/wireless_transport.md`](../wireless_transport.md)

Tune two or more devices to the same **channel** and they resonate — beaming **items,
fluids, and Light** with no conduit between them. A channel *is* an octave in the cosmology.

## Channels (octaves)

There are **16 channels**, one per **dye colour**. Set a device's channel by sneak-clicking
it or using a **dye**, or copy one device's channel onto another with the **Frequency
Tuner** (copy, then paste). The **Channel Atlas** lists how many devices — and how many
givers / regivers / shapers — are active on each channel.

A channel only connects devices **within one dimension**, *unless* a **Wave Repeater** is on
it (then it pools across dimensions).

## Modes

Right-click the **Wave Relay** to cycle its mode (the cosmology names are in parentheses):

| Mode | Meaning |
| --- | --- |
| **Receive** *(Regive)* | Pulls cargo in from the channel. |
| **Send** *(Give)* | Pushes its inventory's cargo out onto the channel. |
| **Disabled** *(Stilled)* | Off. |

The relay rides the **Fabric Transfer API**, so it works with the block it faces — vanilla
chests, barrels, furnaces, and fluid tanks all just work.

## Throughput

A single sender/receiver pair moves a healthy trickle; more senders widen the pipe, and
**Amplifiers** multiply it, up to a hard ceiling so big builds can't stall the tick:

| Cargo | Per sender | Hard cap (before amplifiers) |
| --- | --- | --- |
| Items | 8 / tick | 64 / tick |
| Fluids | 1 bucket / tick | 8 buckets / tick |
| Light (RU) | 1,000 / tick | 16,000 / tick |

Each **Wave Amplifier** on the channel doubles the budget, up to **×16**.

## The channel-gadget family

| Device | What it adds to its channel |
| --- | --- |
| **Wave Relay** | The core node — send/receive items, fluids, and Light. |
| **Wave Amplifier** | Widens throughput (×2 each, ×16 cap). |
| **Wave Filter** | Restricts items to a whitelist via a 3×3 **ghost-slot** GUI (samples aren't consumed). A water bucket in the grid whitelists water. |
| **Wave Splitter** | Toggles **round-robin** (even split) vs. **fill-first** delivery. |
| **Wave Repeater** | Extends the channel **across dimensions**. |
| **Wave Coupler** | Bridges the **wired** RU grid onto a wireless channel (and to Team Reborn Energy). |
| **Wave Chest** | 27-slot storage that lives **on** a channel — a buffer with no block to face. |
| **Signal Relay** | A wireless **redstone** bus — the strongest broadcast reaches every device on the channel. |

## Hush Cost (optional)

By default, broadcasting is free — the base relay is cheap and powerful. An opt-in **Hush
Cost** can be enabled so cargo broadcasts drain a little Light per active sender from the
channel's energy providers, tying logistics back into the energy economy.

## Tips

- A channel only ticks when it has **at least two** devices, so idle channels are free.
- Devices carry their channel/mode in NBT and **re-register on load**, so wireless networks
  survive restarts with no setup.
- Use the **Wave Coupler** to feed a remote base's wired grid from a central generator bank.
