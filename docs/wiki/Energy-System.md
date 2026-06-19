# Energy System

[← Home](Home.md)

**Light** is the mod's energy. Internally it is tracked as **RU** (Resonance
Units) on the `ResonanceNode` capability; the UI always shows it as Light. This
page covers the **wired** grid. For the wireless side, see
[Wireless Transport](Wireless-Transport.md).

## Node roles

Every Light-aware block exposes a `ResonanceNode` with one or more **roles**
(a bitmask, so a block can be several at once):

| Role | Meaning | Examples |
| --- | --- | --- |
| **PROVIDER** | supplies Light to the network | Generative Coil, Stillness Core |
| **CONSUMER** | draws Light from the network | Compressor, Transmuter, Radiators, Polarity Field |
| **STORAGE** | banks Light | Accumulator, Coil, Stillness Core, Coupler |
| **CONDUIT** | carries Light, no buffer | Wave Conduit, Dense Wave Conduit |

(`com.echoes.energy.NodeRole`, bit values PROVIDER=1, CONSUMER=2, STORAGE=4,
CONDUIT=8.)

## Storage

`ResonanceStorage` is a simple bounded buffer (clamped to `[0, capacity]`). Any
storage node is **comparator-readable**: the comparator output is the fill ratio
scaled to **0–15**. Default capacities:

| Block | Capacity |
| --- | --- |
| Generative Coil (Resonator) | 10,000 RU |
| Stillness Core | 50,000 RU |
| Accumulator (Capacitor) | 250,000 RU |
| Compressor / Transmuter (buffer) | 1,000 RU each |
| Radiator / Warmth Radiator / Polarity Field (buffer) | 3,000 RU each |

## The wired network

A **network** is the set of nodes joined by Wave Conduits (and the blocks those
conduits touch). Each tick the network does a single balanced distribution:

1. **Pull** available Light from PROVIDERs, then from STORAGE on any shortfall.
2. **Share** it among CONSUMERs by **demand**. Under scarcity this is a
   **largest-remainder fair distribution**: each consumer gets a proportional
   share `(demand / totalDemand)`, and leftover whole units go to the most-starved
   consumers first. No consumer starves while another is overfilled.
3. **Top up** STORAGE with any surplus, filling the **lowest-fill** storage first
   so banks charge evenly.

### Throughput

Light moves only as fast as the conduits allow:

| Conduit | Throughput |
| --- | --- |
| **Wave Conduit** (`tuning_conduit`) | 1,000 RU/t |
| **Dense Wave Conduit** (`dense_conduit`) | 16,000 RU/t (×16) |

Use Dense conduit to feed many or hungry consumers without huge conduit bundles.

### Performance

The network is maintained **incrementally** by `ResonanceNetworkManager`:
placing or breaking a conduit **merges** or **splits** networks on the spot —
there is **no per-tick flood fill**. The only expensive path is a connectivity
re-check when a conduit is removed (it might split one network into two).

Large networks are **staggered**: a network with more than **256** conduits
ticks its distribution every **4 ticks** instead of every tick, so big builds
never threaten the server tick.

### Persistence

The grid's conduit topology is saved to a `PersistentState`
(`ResonanceNetworkState`), so **networks survive a server restart** rather than
going dark until a conduit is replaced.

## Special network blocks

- **Balancer** — nudges every STORAGE node on its network toward the **same fill
  ratio** (≈2,000 RU/t, every 10 ticks) so no Accumulator hoards; the grid
  "breathes" evenly.
- **Polarity Coupler** (Conduit Coupler) — joins the wired grid as a STORAGE node
  while its buffer doubles as a **wireless RU endpoint**, formally bridging the
  wired and wireless transport systems. See [Wireless Transport](Wireless-Transport.md).

## Reading the grid

Craft a **Light Meter** and right-click a device for a live readout: role,
stored / capacity Light, current demand, and conduit throughput. Comparators give
a coarse (0–15) fill signal off any storage node.
