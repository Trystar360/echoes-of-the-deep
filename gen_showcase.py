#!/usr/bin/env python3
"""Generate the Octaves of the One "Great Work" showcase / tutorial world.

Emits an `echoes_showcase` datapack (mcfunctions) that builds a walkable
feature-hall: a lit corridor of pedestals — one per block/item — grouped into
wings that mirror the Great Work advancement tree, each labelled with a wall
sign, plus a welcome atrium and a few static working-build demos.

Run from the repo root:  python3 gen_showcase.py
  -> showcase/datapack/...               (source of truth, committed)
The build pipeline (build_showcase.sh) copies it into a server world, lets the
`minecraft:load` tag build it, then packages the result as a save.

All NBT here is in the exact form the 1.21.4 server accepts (verified): sign
`messages` are four single-quoted JSON-text strings; item frames summon with
`Item:{id,count}`.
"""
from __future__ import annotations

import json
import os

MOD = "echoes"
DP = os.path.join("showcase", "datapack")
FUNC = os.path.join(DP, "data", "echoes_showcase", "function")
LANG = json.load(open(os.path.join(
    "src/main/resources/assets", MOD, "lang/en_us.json"), encoding="utf-8"))

# ── geometry ──────────────────────────────────────────────────────────────
FLOOR_Y = -61          # floor blocks (top surface at -60; players stand at -60)
PED_Y = -60            # pedestal block
DISP_Y = -59           # showcase block / item frame
SIGN_Y = -58           # wall sign (just above the display)
WALL_Z = -5            # north wall (stations hang on it)
STN_Z = -4             # pedestal / display / sign column, in front of the wall
SOUTH_Z = 5            # south wall
AISLE = (-3, 4)        # walkable z band
DX = 3                 # spacing between stations along +X
X0 = 0                 # first station x

def disp_name(kind, id_):
    return LANG.get(f"{'block' if kind=='block' else 'item'}.{MOD}.{id_}", id_.replace("_", " ").title())

def desc(id_):
    return LANG.get(f"tooltip.{MOD}.desc.{id_}", "")

# ── wings: (banner, [(kind, id), ...]) ────────────────────────────────────
def stars():
    return [("item", f"octave_star_{i}") for i in range(1, 7)]

WINGS = [
    ("I · Refine & Generate", [
        ("item", "raw_echocite"), ("item", "echocite_dust"), ("item", "echo_ingot"),
        ("block", "resonant_coil"), ("block", "stillness_core"),
        ("block", "octave_coil"), ("block", "storm_caller")]),
    ("II · The Wired Grid", [
        ("block", "wave_conduit"), ("block", "dense_wave_conduit"), ("block", "octave_conduit"),
        ("block", "resonance_cell"), ("block", "greater_resonance_cell"),
        ("block", "compressor"), ("block", "transmuter"), ("block", "balancer")]),
    ("III · Radiation", [
        ("block", "growth_radiator"), ("block", "warmth_radiator"), ("block", "polarity_field")]),
    ("IV · Wireless Transport", [
        ("block", "wave_relay"), ("block", "wave_amplifier"), ("block", "wave_filter"),
        ("block", "wave_splitter"), ("block", "wave_repeater"), ("block", "wave_coupler"),
        ("block", "wave_chest"), ("block", "signal_relay"),
        ("item", "wave_tuner"), ("item", "wave_atlas")]),
    ("V · The Octave Climb", [
        ("item", "silentite_crystal"), ("item", "drum_core"), ("item", "octave_seed"),
        ("item", "radiant_dust"), ("item", "radiant_ingot")]),
    ("VI · Transmutation", [
        ("block", "transmutation_table"), ("item", "transmutation_tablet"),
        ("item", "light_mote"), ("item", "tonic_mote"), ("item", "mediant_mote"),
        ("item", "dominant_mote"), ("item", "harmonic_mote")] + stars()),
    ("VII · The Octave Grove", [
        ("block", "lumewood_log"), ("block", "lumewood_planks"), ("block", "lumewood_stairs"),
        ("block", "lumewood_slab"), ("block", "lume_lantern"), ("block", "verdant_loam"),
        ("block", "echocite_bricks"), ("block", "lumebloom")]),
    ("VIII · Flight & Gear", [
        ("item", "resonant_pickaxe"), ("item", "resonant_axe"), ("item", "resonant_sword"),
        ("item", "resonant_shovel"), ("item", "resonant_hoe"),
        ("item", "resonant_thrusters"), ("item", "light_meter")]),
]

# ── command emit helpers ──────────────────────────────────────────────────
def jtext(s, color=None):
    s = s.replace("\\", "\\\\").replace('"', '\\"')
    return ('{"text":"%s","color":"%s"}' % (s, color)) if color else '{"text":"%s"}' % s

def sign_cmd(x, y, z, lines, colors=None):
    colors = (list(colors or []) + [None] * 4)[:4]   # pad so zip never truncates
    lines = (list(lines) + ["", "", "", ""])[:4]
    # each message is a single-quoted SNBT string holding a JSON text component;
    # apostrophes in the text must be escaped so they don't close the SNBT string.
    msgs = ",".join("'%s'" % jtext(l, c).replace("'", "\\'") for l, c in zip(lines, colors))
    return ('setblock %d %d %d minecraft:oak_wall_sign[facing=south]'
            '{front_text:{messages:[%s],has_glowing_text:1b,color:"white"}}' % (x, y, z, msgs))

def frame_cmd(x, y, z, item):
    return ('summon minecraft:item_frame %d %d %d '
            '{Facing:3b,Fixed:1b,Invisible:0b,Item:{id:"%s:%s",count:1}}' % (x, y, z, MOD, item))

def wrap(text, width=15, maxlines=3):
    out, line = [], ""
    for w in text.replace("—", "-").split():
        if len(line) + len(w) + 1 > width:
            out.append(line); line = w
            if len(out) == maxlines:
                break
        else:
            line = (line + " " + w).strip()
    if line and len(out) < maxlines:
        out.append(line)
    return out[:maxlines]

# ── build ─────────────────────────────────────────────────────────────────
def build():
    os.makedirs(os.path.join(FUNC, "build"), exist_ok=True)
    stations = sum(len(w[1]) for w in WINGS)
    length = (stations + len(WINGS) + 6) * DX + 12
    xend = X0 + length

    lib = []
    lib.append("# --- shell: floor, walls, lighting (idempotent) ---")
    lib.append(f"forceload add -8 {WALL_Z-2} {xend} {SOUTH_Z+2}")
    lib.append(f"fill -8 {FLOOR_Y} {WALL_Z-1} {xend} {FLOOR_Y} {SOUTH_Z+1} echoes:echocite_bricks")
    lib.append(f"fill -8 {FLOOR_Y+1} {AISLE[0]} {xend} {FLOOR_Y+1} {AISLE[1]} air")
    # walls
    lib.append(f"fill -8 {FLOOR_Y+1} {WALL_Z} {xend} {FLOOR_Y+5} {WALL_Z} echoes:echocite_bricks")
    lib.append(f"fill -8 {FLOOR_Y+1} {SOUTH_Z} {xend} {FLOOR_Y+5} {SOUTH_Z} echoes:echocite_bricks")
    # lume-lantern light strip along both walls every 6
    for z in (WALL_Z, SOUTH_Z):
        lib.append(f"fill -6 {FLOOR_Y+5} {z} {xend-2} {FLOOR_Y+5} {z} echoes:lume_lantern replace echoes:echocite_bricks")
    open(os.path.join(FUNC, "build", "shell.mcfunction"), "w").write("\n".join(lib) + "\n")

    out = []
    out.append("# === Octaves of the One — the Great Work showcase ===")
    out.append("gamerule doDaylightCycle false")
    out.append("gamerule doWeatherCycle false")
    out.append("gamerule doMobSpawning false")
    out.append("gamerule doTileDrops false")
    out.append("gamerule keepInventory true")
    out.append("weather clear 1000000")
    out.append("time set 2000")
    out.append("difficulty peaceful")
    out.append("function echoes_showcase:build/shell")

    # atrium (west end)
    ax = X0 - 6
    out.append(f"setblock {ax} {DISP_Y} {STN_Z} minecraft:lectern")
    out.append(sign_cmd(ax, SIGN_Y, STN_Z,
                        ["OCTAVES", "OF THE ONE", "The Great Work", "walk east ->"],
                        ["aqua", "aqua", "gold", "gray"]))
    out.append(sign_cmd(ax + 2, SIGN_Y, STN_Z,
                        ["How to read", "this hall", "name + lore", "on each sign"],
                        ["gold", "gold", "gray", "gray"]))
    out.append(f"setworldspawn {ax} {FLOOR_Y+1} 0")
    out.append(f"spawnpoint @a {ax} {FLOOR_Y+1} 0")

    x = X0
    for banner, items in WINGS:
        # wing banner on the wall
        out.append(sign_cmd(x, SIGN_Y + 1, STN_Z, ["= WING =", banner], ["gold", "aqua"]))
        x += DX
        for kind, id_ in items:
            name = disp_name(kind, id_)
            dtext = desc(id_)
            if dtext.startswith(name):                 # avoid repeating the name
                dtext = dtext[len(name):].lstrip(" —-:·")
            lines = [name] + wrap(dtext)
            out.append(f"setblock {x} {PED_Y} {STN_Z} echoes:echocite_brick_slab")
            if kind == "block":
                out.append(f"setblock {x} {DISP_Y} {STN_Z} echoes:{id_}")
            else:
                out.append(frame_cmd(x, DISP_Y, STN_Z, id_))
            out.append(sign_cmd(x, SIGN_Y, STN_Z, lines, ["aqua", "white", "white", "white"]))
            x += DX
        x += DX  # gap between wings

    # a couple of static demo builds in the south bay
    out.append("function echoes_showcase:build/demos")
    out.append('tellraw @a {"text":"The Great Work hall is built. Walk east through the wings.","color":"aqua"}')
    out.append("say SHOWCASE_BUILD_DONE")
    out.append("forceload remove all")
    open(os.path.join(FUNC, "build", "all.mcfunction"), "w").write("\n".join(out) + "\n")

    # demos (static arrangements that read clearly)
    d = []
    dz = SOUTH_Z + 3
    d.append(f"forceload add 8 {dz-1} 40 {dz+3}")
    d.append(f"fill 8 {FLOOR_Y} {dz} 40 {FLOOR_Y} {dz+2} echoes:echocite_bricks")
    # ambient capture: coil ringed by note blocks
    d.append(f"setblock 10 {FLOOR_Y+1} {dz+1} echoes:resonant_coil")
    for ddx, ddz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        d.append(f"setblock {10+ddx} {FLOOR_Y+1} {dz+1+ddz} minecraft:note_block")
    d.append(sign_cmd(10, FLOOR_Y+3, dz, ["Ambient Capture", "note blocks", "charge the Coil", "with sound"],
                      ["aqua", "white", "white", "gray"]))
    # growth radiator over wheat
    d.append(f"fill 18 {FLOOR_Y} {dz} 20 {FLOOR_Y} {dz+2} minecraft:farmland")
    d.append(f"fill 18 {FLOOR_Y+1} {dz} 20 {FLOOR_Y+1} {dz+2} minecraft:wheat[age=7]")
    d.append(f"setblock 19 {FLOOR_Y+4} {dz+1} echoes:growth_radiator")
    d.append(sign_cmd(18, FLOOR_Y+3, dz, ["Growth Radiator", "pours Light", "as life - grows", "nearby crops"],
                      ["aqua", "white", "white", "gray"]))
    # wireless: two relays + chest
    d.append(f"setblock 28 {FLOOR_Y+1} {dz+1} echoes:wave_relay")
    d.append(f"setblock 34 {FLOOR_Y+1} {dz+1} echoes:wave_relay")
    d.append(f"setblock 34 {FLOOR_Y+1} {dz+2} minecraft:chest")
    d.append(sign_cmd(28, FLOOR_Y+3, dz, ["Wireless", "same channel =", "beam items &", "Light, no wire"],
                      ["aqua", "white", "white", "gray"]))
    d.append("forceload remove all")
    open(os.path.join(FUNC, "build", "demos.mcfunction"), "w").write("\n".join(d) + "\n")

    # reset
    open(os.path.join(FUNC, "reset.mcfunction"), "w").write(
        f"forceload add -8 {WALL_Z-2} {xend} {SOUTH_Z+4}\n"
        f"fill -8 {FLOOR_Y} -8 {xend} {FLOOR_Y+6} {SOUTH_Z+4} air\n"
        f"kill @e[type=item_frame]\nforceload remove all\n")

    # pack.mcmeta + load tag
    open(os.path.join(DP, "pack.mcmeta"), "w").write(json.dumps({
        "pack": {"pack_format": 61,
                 "description": "Octaves of the One - the Great Work showcase world"}}, indent=2) + "\n")
    tagdir = os.path.join(DP, "data", "minecraft", "tags", "function")
    os.makedirs(tagdir, exist_ok=True)
    open(os.path.join(tagdir, "load.json"), "w").write(
        json.dumps({"values": ["echoes_showcase:build/all"]}, indent=2) + "\n")

    print(f"wrote datapack -> {DP}")
    print(f"  {stations} stations across {len(WINGS)} wings; corridor ~{length} blocks")


if __name__ == "__main__":
    build()
