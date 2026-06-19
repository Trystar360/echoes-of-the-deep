# Ambient Capture

[← Home](Home.md)

A **Generative Coil** doesn't burn fuel — it **winds ambient sound into Light**.
Two sources feed it, both implemented as mixins that find the **nearest Coil** and
charge it.

## Mob deaths

A `LivingEntity#onDeath` mixin adds **25 RU** to the nearest Coil within **8
blocks** of the death. A mob farm next to a Coil is a steady trickle of Light.

## World sound

A `ServerWorld#playSound` mixin charges the nearest Coil whenever a mapped sound
plays. The mapping is **data-driven** and **reloadable** —
`data/echoes/resonance_sources.json`, read through a reload listener
(`ResonanceSources`), so **modpack authors can override or extend it** without
code.

### Default sound → Light table

| Sound | Light (RU) |
| --- | --- |
| Note block — harp | 8 |
| Note block — bass | 8 |
| Note block — bell | 12 |
| Bell use | 10 |
| Anvil land | 40 |
| Explosion | 40 |
| Beacon activate | 100 |
| **Thunder** | **2,000** |

Louder, rarer events pay far more — a thunderstorm near an exposed Coil is a
windfall, while a note-block loop is a reliable drip. Park Coils near anvils,
note-block contraptions, or automated bells for hands-off charging.

## Extending it

To add or rebalance sources, ship a datapack that overrides
`data/echoes/resonance_sources.json`:

```json
{
  "sources": {
    "minecraft:block.note_block.harp": 8,
    "minecraft:entity.lightning_bolt.thunder": 2000,
    "yourmod:some.custom.sound": 50
  }
}
```

Keys are `SoundEvent` ids; values are RU added to the nearest Coil when that sound
plays. Reload with `/reload`.
