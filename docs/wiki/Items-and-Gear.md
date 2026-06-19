# Items & Gear

[← Home](Home.md)

Materials, tools, flight, and handheld diagnostics. Handheld wireless tools (the
**Wave Tuner** and **Wave Atlas**) are documented on
[Wireless Transport](Wireless-Transport.md).

## Materials

| | In-world name | Id | Source | Use |
| :-: | --- | --- | --- | --- |
| <img src="images/icons/raw_echocite.png" width="28"> | Raw Echocite | `raw_echocite` | mining Echocite Ore | smelt → Echo Ingot, or crush → dust |
| <img src="images/icons/echocite_dust.png" width="28"> | Echocite Dust | `echocite_dust` | crushing raw echocite (×2) | smelt → Echo Ingot; crafting |
| <img src="images/icons/echo_ingot.png" width="28"> | **Echo Ingot** | `echo_ingot` | smelt raw echocite **or** dust | **the core crafting material** |
| <img src="images/icons/echo_dust.png" width="28"> | Echo Dust | `echo_dust` | Echocite Dust + Glowstone Dust | crafts the Wave Atlas |
| <img src="images/icons/resonant_slag.png" width="28"> | Resonant Slag | `resonant_slag` | ~15% byproduct of crushing | smelt → Dull Ingot |
| <img src="images/icons/dull_ingot.png" width="28"> | Dull Ingot | `dull_ingot` | smelt Resonant Slag | cheap alternate conduit material |
| <img src="images/icons/drumstone_shard.png" width="28"> | Drumstone Shard | `drumstone_shard` | mining Drumstone Ore | crafts the Drum Core |
| <img src="images/icons/drum_core.png" width="28"> | Drum Core | `drum_core` | 4 shards + iron | alt Coil membrane; **Thrusters** |
| <img src="images/icons/silentite_crystal.png" width="28"> | Silentite Crystal | `silentite_crystal` | mining Silentite Ore (Deep Dark) | Stillness Core; alt Wave Repeater |

**Echo Ingot** is the spine of the tech tree — nearly every machine, conduit,
tool, and wireless device needs it. The **Dull Ingot** and **Drum Core** lines
are cheaper/alternate paths so the loop has redundancy. See
[Crafting & Progression](Crafting-and-Progression.md).

## <img src="images/icons/resonant_thrusters.png" width="28" align="top"> Resonant Thrusters — `echoes:resonant_thrusters`

*Centrifugal radiation = expansion.* **Sound-powered flight** with no client mod
— fully server-side. Portable Light lives on the item via a `stored_ru` data
component.

- **Capacity:** 1,000,000 RU (a huge reserve).
- **Flight:** hold *use* to fly in the direction you look. **Sprint** = faster,
  **sneak** = hover / brake. Costs only **~8 RU/t** to fly, so the reserve lasts.
- **Fall-damage immunity** while you carry a charged set.
- **Recharge:** right-click a **Resonant Coil**, **Resonance Cell**, or **Polarity
  Coupler** to pull Light into the thrusters.
- Tooltip shows current charge (`Light: x / y`).

Crafted from **Echo Ingots**, a **Redstone Block**, and two **Drum Cores**.

## Resonant tools

<img src="images/icons/resonant_pickaxe.png" width="40"> <img src="images/icons/resonant_axe.png" width="40"> <img src="images/icons/resonant_shovel.png" width="40"> <img src="images/icons/resonant_sword.png" width="40"> <img src="images/icons/resonant_hoe.png" width="40">

A full set — **Pickaxe, Axe, Shovel, Sword, Hoe** — on the custom **Echo** tool
material (`ModItems.ECHO_MATERIAL`). Deliberately over-tuned (the cosmology
justifies it):

| Property | Value |
| --- | --- |
| Mining speed | **12.0** (faster than netherite) |
| Mining level | mines **anything** |
| Durability | **4,000** |
| Enchantability | **22** (very high) |
| Repair material | `#echoes:resonant_repair` tag (Echo Ingot) |

Each tool adds its usual attack-damage/speed profile on top. The constants live
in `ModItems.ECHO_MATERIAL` and the individual `Resonant*Item` classes for easy
re-balancing. Crafted from **Echo Ingots + sticks** in the vanilla tool shapes.

## Diagnostics & tuning (handheld)

### <img src="images/icons/light_meter.png" width="28" align="top"> Light Meter — `echoes:light_meter`
Right-click any Light device to print its **role, stored / capacity Light,
demand, and conduit throughput** in chat. Light is otherwise invisible, so this is
your primary debugging tool. Crafted from an Echo Ingot + redstone + comparator.

### <img src="images/icons/wave_tuner.png" width="28" align="top"> Wave Tuner — `echoes:wave_tuner`
Copy/paste a wireless **channel** between devices: sneak + right-click a device to
copy its octave, right-click another to paste. Batch-tune without dyes.

### <img src="images/icons/wave_atlas.png" width="28" align="top"> Wave Atlas — `echoes:wave_atlas`
Right-click a device to print its channel roster (device / give / regive / shaper
counts); right-click the air for an overview of every active channel.

Both wireless tools are detailed on [Wireless Transport](Wireless-Transport.md).
