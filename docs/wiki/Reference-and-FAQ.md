# Reference & FAQ

[← Home](Home.md)

## Constants

### Generation & storage

| Block | Role | Number |
| --- | --- | --- |
| Resonant Coil | Provider + Storage | 10,000 buffer; charges from sound |
| Stillness Core | Provider | 4 Light/t; 50,000 buffer |
| Octave Coil | Provider | 24 Light/t (tunable); 300,000 buffer |
| Storm Caller | Provider + Storage | 40,000/strike; 400,000 buffer |
| Resonance Cell | Storage | 250,000 |
| Greater Resonance Cell | Storage | 2,000,000 |

### Throughput

| Conduit | Light/t |
| --- | --- |
| Wave Conduit | 1,000 |
| Dense Wave Conduit | 16,000 |
| Octave Conduit | 64,000 |

### Wireless (per channel, before Amplifiers)

| Cargo | Per sender | Cap | Amplify |
| --- | --- | --- | --- |
| Items | 8/t | 64/t | ×2 each, ×16 max |
| Fluids | 1 bucket/t | 8 buckets/t | ×2 each, ×16 max |
| Light | 1,000/t | 16,000/t | ×2 each, ×16 max |

Channels: **16** (one per dye colour).

### Ambient capture

| Source | RU |
| --- | --- |
| Mob death | 25 |
| Note block / bell / anvil / explosion / beacon / thunder | data-driven (8 / 12 / 40 / 40 / 100 / 2,000) |

Capture radius: **8 blocks** to the nearest Resonant Coil.

### Light Values (Bound Light)

| Mote | Value | Octave Star | Capacity |
| --- | --- | --- | --- |
| Light | 64 | Star I | 100,000 |
| Tonic | 256 | Star II | 400,000 |
| Mediant | 1,024 | Star III | 1,600,000 |
| Dominant | 4,096 | Star IV | 6,400,000 |
| Harmonic | 16,384 | Star V | 25,600,000 |
| | | Star VI | 102,400,000 |

## FAQ

**My Resonant Coil isn't charging.** It needs **sound** within 8 blocks (note blocks, mob
deaths, anvils…). For passive Light, use a **Stillness Core**. See
[Ambient Capture](Ambient-Capture.md).

**A machine isn't running even though I have power.** Check the conduit line actually
connects, and read the machine with the **Light Meter** — it shows stored Light and demand.
Under scarcity, Light is split fairly across all consumers. Also check the device's
**redstone** mode in its Frequency Tuner config.

**How do I see how much Light something holds?** The **Light Meter** (right-click), or a
**comparator** next to any storage/generator block.

**Two relays won't talk.** They must share the same **channel** (dye colour) and be in the
**same dimension** (or have a **Wave Repeater** on the channel). One must **Send**, the
other **Receive**. Check the **Channel Atlas**.

**I lost my shulker box / enchanted tool in the Transmutation Table.** Dissolving values an
item at its **base** Light Value — container contents and enchantments aren't counted. Don't
dissolve full containers. See [Transmutation](Transmutation.md).

**Are my saves safe across updates?** Yes — the namespace stays `echoes` and energy is
tracked internally as RU; the "Octaves of the One" names are a display reskin.

**Where are the recipes?** The [HTML wiki](https://trystar360.github.io/echoes-of-the-deep/)
shows every recipe as a clickable grid; [Crafting & Progression](Crafting-and-Progression.md)
gives the flow; in-game, follow the [Great Work](The-Great-Work.md) advancements.

## Identifiers

Mod id: **`echoes`**. Block/item ids are `echoes:<snake_case>` of the display name (e.g.
*Resonant Coil* → `echoes:resonant_coil`). The creative tab is **Octaves of the One**.

## Links

- [HTML wiki](https://trystar360.github.io/echoes-of-the-deep/) · [Repository](https://github.com/Trystar360/echoes-of-the-deep)
- Design docs: [`cosmology.md`](../cosmology.md) · [`wireless_transport.md`](../wireless_transport.md) · [`roadmap.md`](../roadmap.md)
