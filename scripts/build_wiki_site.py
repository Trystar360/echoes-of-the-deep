#!/usr/bin/env python3
"""Build the Echoes of the Deep / Octaves of the One wiki as a static site.

Reads the mod's own data — textures, recipe JSON, and the en_us lang file — and
emits a themed, fully cross-linked HTML site into docs/site/:

  docs/site/index.html          home + live search
  docs/site/<id>.html           one page per block / item (icon, description,
                                 stats, crafting recipe, "used in" backlinks)
  docs/site/guide-<slug>.html   concept pages (energy, wireless, cosmology, ...)
  docs/site/style.css           deep-resonance dark theme
  docs/site/assets/icons/*.png  flat item/block icons (upscaled real textures)

Crafting recipes render as real HTML grids: every ingredient cell links to that
item's page. Pure-Python PNG I/O (no Pillow), matching gallery.py / montage.py.
"""
import struct, zlib, os, json, glob, html

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(ROOT, "src/main/resources/assets/echoes/textures")
RECIPES = os.path.join(ROOT, "src/main/resources/data/echoes/recipe")
LANG = os.path.join(ROOT, "src/main/resources/assets/echoes/lang/en_us.json")
SITE = os.path.join(ROOT, "docs/site")
ICONS = os.path.join(SITE, "assets/icons")
VICONS = os.path.join(ICONS, "vanilla")


# ───────────────────────── PNG I/O ─────────────────────────
def read_png(path):
    d = open(path, "rb").read(); i = 8; w = h = 0; idat = b""
    while i < len(d):
        ln = struct.unpack(">I", d[i:i+4])[0]; typ = d[i+4:i+8]; data = d[i+8:i+8+ln]
        if typ == b"IHDR":
            w, h = struct.unpack(">II", data[:8])
        elif typ == b"IDAT":
            idat += data
        elif typ == b"IEND":
            break
        i += 12 + ln
    raw = zlib.decompress(idat); px = [(0, 0, 0, 0)]*(w*h); pos = 0
    for y in range(h):
        pos += 1
        for x in range(w):
            px[y*w+x] = (raw[pos], raw[pos+1], raw[pos+2], raw[pos+3]); pos += 4
    return w, h, px


def write_png(path, w, h, rows):
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        for x in range(w):
            raw += bytes(rows[y][x])

    def ch(t, dd):
        return struct.pack(">I", len(dd)) + t + dd + struct.pack(">I", zlib.crc32(t+dd) & 0xffffffff)
    open(path, "wb").write(b"\x89PNG\r\n\x1a\n" +
                           ch(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)) +
                           ch(b"IDAT", zlib.compress(bytes(raw), 9)) + ch(b"IEND", b""))


_ITEMS = {os.path.basename(p)[:-4] for p in glob.glob(f"{TEX}/item/*.png")}
_BLOCKS = {os.path.basename(p)[:-4] for p in glob.glob(f"{TEX}/block/*.png")}
MODELS = os.path.join(ROOT, "src/main/resources/assets/echoes/models")
TEXKEYS = ["layer0", "all", "texture", "front", "top", "cross", "fan",
           "side", "particle", "end", "0", "bottom", "up"]


def _read_model(rel):
    p = os.path.join(MODELS, rel + ".json")
    return json.load(open(p)) if os.path.exists(p) else None


def resolve_texture_file(name):
    """Follow item-model → parent chain, gather echoes texture refs, return a PNG path.

    Handles derived blocks (stairs/slab/fence/wood) that carry no dedicated sprite
    by reading the texture their block model points at."""
    textures = {}
    rel = f"item/{name}"
    for _ in range(8):
        d = _read_model(rel)
        if d is None:
            break
        for k, v in d.get("textures", {}).items():
            textures.setdefault(k, v)
        parent = d.get("parent", "")
        if isinstance(parent, str) and parent.startswith("echoes:"):
            rel = parent.split(":", 1)[1]
        else:
            break
    cands = [textures[k] for k in TEXKEYS if k in textures] + list(textures.values())
    for v in cands:
        if isinstance(v, str) and v.startswith("echoes:"):
            f = os.path.join(TEX, v.split(":", 1)[1] + ".png")
            if os.path.exists(f):
                return f
    # last resort: a same-named block/item sprite
    for sub in (f"item/{name}", f"block/{name}"):
        f = os.path.join(TEX, sub + ".png")
        if os.path.exists(f):
            return f
    return None


def frame0_file(path):
    w, h, px = read_png(path)
    return [[px[y*w+x] for x in range(16)] for y in range(16)]


def upscale(grid16, scale):
    n = 16 * scale
    out = [[(0, 0, 0, 0)]*n for _ in range(n)]
    for y in range(16):
        for x in range(16):
            c = grid16[y][x]
            for sy in range(scale):
                for sx in range(scale):
                    out[y*scale+sy][x*scale+sx] = c
    return out


# ───────────────────── vanilla pixel-art (16x16) ─────────────────────
def blank():
    return [[(0, 0, 0, 0)]*16 for _ in range(16)]


def rect(g, x0, y0, x1, y1, c):
    for y in range(max(0, y0), min(16, y1+1)):
        for x in range(max(0, x0), min(16, x1+1)):
            g[y][x] = c


def speckle(base, dark, light):
    g = blank(); rect(g, 1, 1, 14, 14, base)
    for y in range(1, 15):
        for x in range(1, 15):
            n = (x*7 + y*13) % 5
            if n == 0:
                g[y][x] = dark
            elif n == 1:
                g[y][x] = light
    for x in range(1, 15):
        g[1][x] = light; g[14][x] = dark
    for y in range(1, 15):
        g[y][1] = light; g[y][14] = dark
    return g


def dustpile(c, hi):
    g = blank()
    for (x, y) in [(4, 11), (5, 12), (6, 11), (7, 13), (8, 12), (9, 13), (10, 11), (11, 12),
                   (5, 10), (7, 11), (9, 11), (8, 10), (6, 13), (10, 13), (4, 13), (11, 10)]:
        g[y][x] = c; g[y-1][x] = hi
    return g


def iron_ingot():
    g = blank(); sil = (205, 205, 210, 255); dk = (120, 120, 128, 255); hi = (235, 235, 240, 255)
    for y in range(6, 12):
        o = y - 6
        rect(g, 3 + o//2, y, 12 - (5-(y-6))//2, y, sil)
    rect(g, 4, 6, 11, 7, hi); rect(g, 4, 10, 12, 11, dk); return g


def stick():
    g = blank(); br = (140, 100, 55, 255); dk = (110, 76, 40, 255)
    for i in range(11):
        x = 4 + i//2; y = 12 - i
        if 0 <= x < 16 and 0 <= y < 16:
            g[y][x] = br; g[y][x+1] = dk
    return g


def comparator():
    g = speckle((175, 175, 175, 255), (150, 150, 150, 255), (200, 200, 200, 255))
    rect(g, 2, 9, 13, 13, (170, 170, 170, 255))
    g[5][4] = (230, 60, 60, 255); g[5][11] = (230, 60, 60, 255)
    g[3][8] = (180, 40, 40, 255); g[4][8] = (230, 60, 60, 255); return g


def note_block():
    g = speckle((120, 80, 52, 255), (95, 62, 40, 255), (140, 96, 62, 255))
    rect(g, 6, 6, 9, 9, (40, 30, 22, 255)); g[6][9] = (40, 30, 22, 255); g[5][9] = (40, 30, 22, 255); return g


def furnace():
    g = speckle((130, 130, 130, 255), (105, 105, 105, 255), (155, 155, 155, 255))
    rect(g, 5, 8, 10, 12, (35, 35, 38, 255)); rect(g, 6, 10, 9, 11, (120, 70, 30, 255)); return g


def ender_pearl():
    g = blank(); base = (20, 110, 100, 255); hi = (120, 210, 195, 255); dk = (12, 70, 64, 255)
    for y in range(16):
        for x in range(16):
            r = ((x-7.5)**2 + (y-7.5)**2) ** 0.5
            if r <= 6.2:
                g[y][x] = dk if r > 5 else base
    rect(g, 5, 5, 7, 7, hi); return g


def book():
    g = blank(); rect(g, 3, 2, 12, 13, (150, 95, 55, 255)); rect(g, 3, 2, 4, 13, (90, 55, 30, 255))
    rect(g, 5, 3, 12, 12, (225, 220, 205, 255)); rect(g, 9, 2, 10, 13, (180, 60, 60, 255)); return g


def chest():
    g = blank(); rect(g, 2, 3, 13, 13, (150, 100, 55, 255)); rect(g, 2, 3, 13, 4, (110, 72, 38, 255))
    rect(g, 2, 7, 13, 8, (95, 62, 32, 255)); rect(g, 7, 7, 8, 9, (70, 70, 72, 255))
    for x in range(2, 14):
        g[3][x] = (185, 130, 75, 255)
    return g


def hopper():
    g = blank(); gray = (108, 108, 114, 255); dk = (40, 40, 44, 255)
    rect(g, 2, 3, 13, 5, gray); rect(g, 3, 4, 12, 4, dk)   # rim + opening
    for i, y in enumerate(range(6, 11)):
        rect(g, 3+i, y, 12-i, y, gray)                      # funnel
    rect(g, 7, 11, 8, 13, gray)                             # spout
    return g


def lightning_rod():
    g = blank(); cu = (196, 110, 72, 255); dk = (150, 80, 52, 255); hi = (228, 152, 112, 255)
    rect(g, 7, 3, 8, 13, cu)                                # rod
    g[2][7] = hi; g[1][7] = hi                              # pointed tip
    g[3][8] = dk; rect(g, 7, 13, 8, 13, dk)                # shading
    rect(g, 6, 6, 9, 7, dk)                                 # band
    return g


VANILLA = {
    "iron_ingot": iron_ingot, "stick": stick, "comparator": comparator, "note_block": note_block,
    "furnace": furnace, "ender_pearl": ender_pearl, "book": book, "chest": chest, "hopper": hopper,
    "lightning_rod": lightning_rod,
    "cobblestone": lambda: speckle((128, 128, 128, 255), (95, 95, 95, 255), (165, 165, 165, 255)),
    "redstone": lambda: dustpile((200, 25, 25, 255), (255, 90, 90, 255)),
    "glowstone_dust": lambda: dustpile((220, 200, 70, 255), (255, 240, 150, 255)),
    "blaze_powder": lambda: dustpile((235, 150, 30, 255), (255, 210, 90, 255)),
    "redstone_block": lambda: speckle((165, 22, 22, 255), (120, 12, 12, 255), (210, 45, 45, 255)),
    "iron_block": lambda: speckle((205, 205, 210, 255), (170, 170, 176, 255), (235, 235, 240, 255)),
    "glowstone": lambda: speckle((215, 190, 95, 255), (180, 150, 70, 255), (250, 235, 150, 255)),
    "dirt": lambda: speckle((122, 86, 58, 255), (96, 66, 44, 255), (142, 102, 70, 255)),
    "bone_meal": lambda: dustpile((232, 232, 222, 255), (255, 255, 255, 255)),
}
VANILLA_NAMES = {
    "iron_ingot": "Iron Ingot", "stick": "Stick", "comparator": "Redstone Comparator",
    "note_block": "Note Block", "furnace": "Furnace", "ender_pearl": "Ender Pearl",
    "book": "Book", "chest": "Chest", "cobblestone": "Cobblestone", "redstone": "Redstone Dust",
    "glowstone_dust": "Glowstone Dust", "blaze_powder": "Blaze Powder",
    "redstone_block": "Block of Redstone", "iron_block": "Block of Iron", "glowstone": "Glowstone",
    "hopper": "Hopper", "dirt": "Dirt", "bone_meal": "Bone Meal", "lightning_rod": "Lightning Rod",
}


# ───────────────────── data: names, categories, stats ─────────────────────
LANGD = json.load(open(LANG))


def disp(rid):
    ns, name = rid.split(":", 1)
    if ns == "echoes":
        return LANGD.get(f"block.echoes.{name}", LANGD.get(f"item.echoes.{name}", name))
    return VANILLA_NAMES.get(name, name.replace("_", " ").title())


def desc(name):
    return LANGD.get(f"tooltip.echoes.desc.{name}", DESC_EXTRA.get(name, ""))


CATEGORIES = [
    ("Ores & Materials", "The raw resources and the items you smelt and crush them into.",
     ["echocite_ore", "deepslate_echocite_ore", "drumstone_ore", "silentite_ore",
      "raw_echocite", "echocite_dust", "echo_ingot", "echo_dust", "resonant_slag",
      "dull_ingot", "drumstone_shard", "drum_core", "silentite_crystal"]),
    ("Energy Core", "Generate, store, and carry Light across the wired grid.",
     ["stillness_core", "resonant_coil", "storm_caller", "resonance_cell",
      "wave_conduit", "dense_wave_conduit", "balancer"]),
    ("Octave Tier", "The higher octave: charged Radiant matter and its big generators, banks, and conduits.",
     ["octave_seed", "radiant_dust", "radiant_ingot", "octave_coil", "octave_conduit",
      "greater_resonance_cell"]),
    ("Machines", "Light-powered processing.",
     ["compressor", "transmuter"]),
    ("Transmutation", "The EMC economy, reskinned as Bound Light: dissolve matter, bank its Light Value, withdraw it as Mote coins.",
     ["transmutation_table", "light_mote", "tonic_mote", "mediant_mote",
      "dominant_mote", "harmonic_mote"]),
    ("Radiation & Fields", "Pour Light back into the world.",
     ["growth_radiator", "warmth_radiator", "polarity_field"]),
    ("Wireless", "Move items, fluids, Light, and redstone over channels — no conduits.",
     ["wave_relay", "wave_amplifier", "wave_filter", "wave_splitter",
      "wave_repeater", "wave_coupler", "wave_chest", "signal_relay",
      "wave_tuner", "wave_atlas"]),
    ("Tools & Gear", "Handheld diagnostics, flight, and over-tuned tools.",
     ["light_meter", "resonant_thrusters", "resonant_pickaxe", "resonant_axe",
      "resonant_shovel", "resonant_sword", "resonant_hoe"]),
    ("Lumewood & Garden", "A glowing custom tree, its building set, and the soil and flora that grow it.",
     ["lumewood_sapling", "lumewood_log", "lumewood_wood", "lumewood_planks",
      "lumewood_stairs", "lumewood_slab", "lumewood_fence", "lumewood_fence_gate",
      "lumewood_trapdoor", "lumewood_leaves", "lumebloom", "lume_lantern", "verdant_loam"]),
    ("Building Blocks", "Luminous masonry that matches the resonance palette.",
     ["echocite_bricks", "echocite_brick_stairs", "echocite_brick_slab"]),
]
CAT_OF = {rid: cat for cat, _, ids in CATEGORIES for rid in ids}
ORDER = [rid for _, _, ids in CATEGORIES for rid in ids]

STATS = {
    "stillness_core": [("Role", "Provider · Storage"), ("Capacity", "50,000 Light"), ("Generation", "+4 / tick")],
    "resonant_coil": [("Role", "Provider · Storage"), ("Capacity", "10,000 Light"), ("Charges from", "ambient sound + mob deaths")],
    "resonance_cell": [("Role", "Storage"), ("Capacity", "250,000 Light"), ("Readout", "comparator")],
    "wave_conduit": [("Role", "Conduit"), ("Throughput", "1,000 / tick")],
    "dense_wave_conduit": [("Role", "Conduit"), ("Throughput", "16,000 / tick (×16)")],
    "balancer": [("Role", "Network utility"), ("Effect", "evens storage fill (~2,000/t)")],
    "compressor": [("Role", "Consumer"), ("Buffer", "1,000 Light"), ("Does", "ore-doubling + ~15% slag")],
    "transmuter": [("Role", "Consumer"), ("Buffer", "1,000 Light"), ("Does", "fuel-free vanilla smelting")],
    "growth_radiator": [("Role", "Consumer"), ("Buffer", "3,000 Light"), ("Effect", "grows crops, ~300/grow, 4×2 radius")],
    "warmth_radiator": [("Role", "Consumer"), ("Buffer", "3,000 Light"), ("Effect", "cooks drops, melts ice (~4 blocks)")],
    "polarity_field": [("Role", "Consumer"), ("Buffer", "3,000 Light"), ("Modes", "Attract / Repel, radius 6")],
    "wave_relay": [("Role", "Wireless transport"), ("Budget", "8 items · 1 bucket · 1,000 Light / sender")],
    "wave_amplifier": [("Role", "Channel modifier"), ("Effect", "×2 throughput each (×16 cap)")],
    "wave_filter": [("Role", "Channel modifier"), ("Effect", "3×3 item whitelist")],
    "wave_splitter": [("Role", "Channel modifier"), ("Effect", "round-robin ↔ fill-first")],
    "wave_repeater": [("Role", "Channel modifier"), ("Effect", "cross-dimension channels")],
    "wave_coupler": [("Role", "Storage · bridge"), ("Effect", "wired ↔ wireless gateway")],
    "wave_chest": [("Role", "Wireless storage"), ("Slots", "27, native on a channel")],
    "signal_relay": [("Role", "Wireless redstone"), ("Effect", "broadcast / receive redstone")],
    "wave_tuner": [("Type", "Tool"), ("Use", "copy/paste channel; sneak-use opens config")],
    "wave_atlas": [("Type", "Tool"), ("Use", "list devices per channel")],
    "light_meter": [("Type", "Tool"), ("Use", "read role, stored Light, demand, flow")],
    "resonant_thrusters": [("Type", "Gear"), ("Capacity", "1,000,000 Light"), ("Flight", "~8 / tick, fall-immune")],
    "echocite_ore": [("Type", "Ore"), ("Where", "Overworld, y −20…60"), ("Drops", "Raw Echocite")],
    "deepslate_echocite_ore": [("Type", "Ore"), ("Where", "Overworld deepslate"), ("Drops", "Raw Echocite")],
    "drumstone_ore": [("Type", "Ore"), ("Where", "Overworld, y −48…24"), ("Drops", "Drumstone Shard")],
    "silentite_ore": [("Type", "Ore"), ("Where", "Deep Dark, y −58…−8"), ("Drops", "Silentite Crystal")],
}
for t in ["resonant_pickaxe", "resonant_axe", "resonant_shovel", "resonant_sword", "resonant_hoe"]:
    STATS[t] = [("Type", "Tool — Echo material"), ("Stats", "speed 12 · 4000 durability · ench 22"), ("Mines", "anything")]
STATS.update({
    "octave_seed": [("Type", "Catalyst"), ("Opens", "the Radiant transmutation chain")],
    "radiant_dust": [("Type", "Charged matter"), ("Smelt to", "Radiant Ingot")],
    "radiant_ingot": [("Type", "Charged matter"), ("Builds", "the higher-octave tier")],
    "octave_coil": [("Role", "Provider · Storage"), ("Capacity", "300,000 Light"), ("Generation", "strong, tunable")],
    "octave_conduit": [("Role", "Conduit"), ("Throughput", "64,000 / tick (4× Dense)")],
    "greater_resonance_cell": [("Role", "Storage"), ("Capacity", "2,000,000 Light"), ("Built from", "Radiant Ingots + Resonance Cell")],
    "verdant_loam": [("Role", "Growth soil"), ("Effect", "pulses Light to grow nearby plants (tunable)")],
    "lumebloom": [("Type", "Flora"), ("Effect", "glows; grants Glowing on contact")],
    "lume_lantern": [("Type", "Décor"), ("Light", "full-bright")],
    "lumewood_leaves": [("Type", "Leaves"), ("Light", "emits Light")],
    "lumewood_log": [("Type", "Log"), ("From", "the Octave Grove feature in forests")],
    "storm_caller": [("Role", "Provider · Storage"), ("Capacity", "400,000 Light"),
                     ("Per strike", "40,000 Light"), ("Needs", "open sky during storms")],
    "transmutation_table": [("Role", "Transmutation"), ("Stores", "Bound Light (EMC)"),
                            ("Dissolve", "matter → Light Value (1 / ~10 ticks)"),
                            ("Withdraw", "Mote coins (×4 per octave)")],
    "light_mote": [("Light Value", "64"), ("Octave", "O0 — the still source"), ("Source", "Transmutation Table")],
    "tonic_mote": [("Light Value", "256"), ("Octave", "O1 — the tonic"), ("Source", "Transmutation Table")],
    "mediant_mote": [("Light Value", "1,024"), ("Octave", "O2 — the mediant"), ("Source", "Transmutation Table")],
    "dominant_mote": [("Light Value", "4,096"), ("Octave", "O3 — the dominant"), ("Source", "Transmutation Table")],
    "harmonic_mote": [("Light Value", "16,384"), ("Octave", "O4 — the crest (balance)"), ("Source", "Transmutation Table")],
})

DESC_EXTRA = {
    "octave_seed": "The rest point of the next octave — catalyst that opens the Radiant chain.",
    "radiant_dust": "Charged matter from the Octave Seed; smelt or blast into a Radiant Ingot.",
    "radiant_ingot": "The charged metal that builds the higher-octave bank, coil, and conduit.",
    "octave_coil": "A strong baseline generator — 300,000-Light buffer with a tunable rate.",
    "octave_conduit": "A higher-octave conduit — 64,000 Light/t, 4× the Dense Wave Conduit.",
    "greater_resonance_cell": "Block-of-light tier storage: banks 2,000,000 Light.",
    "verdant_loam": "Ticking soil that pulses Light upward to grow nearby plants.",
    "lumebloom": "A glowing flower; brushing it grants the Glowing effect.",
    "lume_lantern": "A full-bright décor block of woven Light.",
    "lumewood_log": "The glowing log of the Lumewood tree, grown from the Octave Grove.",
    "lumewood_wood": "Six-sided Lumewood log for building.",
    "lumewood_planks": "Glowing planks — the base of the Lumewood building set.",
    "lumewood_leaves": "Luminous leaves that emit Light.",
    "lumewood_sapling": "Plant and grow it into a glowing Lumewood tree.",
    "echocite_bricks": "Luminous masonry that matches the resonance palette.",
    "storm_caller": "A spire that calls lightning during storms and banks the windfall as Light.",
}

# For items with no crafting recipe, an accurate "how to obtain" note that overrides
# the generic "mined or found" fallback.
_MOTE_SOURCE = ('<p class="muted">Withdrawn from a '
                '<a href="transmutation_table.html">Transmutation Table</a> '
                'by spending its banked Bound Light (Light Value).</p>')
SOURCE_NOTE = {
    "light_mote": _MOTE_SOURCE, "tonic_mote": _MOTE_SOURCE, "mediant_mote": _MOTE_SOURCE,
    "dominant_mote": _MOTE_SOURCE, "harmonic_mote": _MOTE_SOURCE,
}


# ───────────────────── recipes ─────────────────────
def norm_ing(v):
    if isinstance(v, str):
        return v
    return v.get("item", v.get("tag", v.get("id", "")))


def load_recipes():
    """Return (by_result: id->[recipe], uses: id->set(result_id))."""
    by_result = {}
    uses = {}
    for path in sorted(glob.glob(f"{RECIPES}/**/*.json", recursive=True)):
        d = json.load(open(path))
        t = d.get("type", "")
        r = d.get("result")
        if isinstance(r, str):
            rid, rcount = r, 1
        elif isinstance(r, dict):
            rid, rcount = r.get("id", ""), r.get("count", 1)
        else:
            continue
        rid = rid.split(":", 1)[1] if rid.startswith("echoes:") else rid
        rec = {"count": rcount}
        ings = []
        if t in ("minecraft:crafting_shaped",):
            cells = [None]*9
            key = d["key"]
            for ri, row in enumerate(d["pattern"]):
                for ci, c in enumerate(row):
                    if c != " ":
                        cells[ri*3+ci] = norm_ing(key[c])
            rec.update(kind="craft", cells=cells)
            ings = [c for c in cells if c]
        elif t == "minecraft:crafting_shapeless":
            cells = [None]*9
            for i, v in enumerate(d["ingredients"][:9]):
                cells[i] = norm_ing(v)
            rec.update(kind="craft", cells=cells)
            ings = [c for c in cells if c]
        elif t in ("minecraft:smelting", "minecraft:blasting"):
            ing = norm_ing(d["ingredient"])
            rec.update(kind=("blast" if "blasting" in t else "smelt"), input=ing)
            ings = [ing]
        elif t == "echoes:crushing":
            ing = norm_ing(d["ingredient"])
            sec = d.get("secondary")
            rec.update(kind="crush", input=ing,
                       secondary=(norm_ing(sec) if sec else None),
                       secondary_chance=d.get("secondaryChance"))
            ings = [ing]
        else:
            continue
        by_result.setdefault(rid, []).append(rec)
        for ing in ings:
            short = ing.split(":", 1)[1] if ing.startswith("echoes:") else None
            if short:
                uses.setdefault(short, set()).add(rid)
    return by_result, uses


# ───────────────────── icons ─────────────────────
def gen_icons():
    os.makedirs(ICONS, exist_ok=True); os.makedirs(VICONS, exist_ok=True)
    for name in ORDER:
        f = resolve_texture_file(name)
        if not f:
            print("  ! no texture resolved for", name)
            write_png(f"{ICONS}/{name}.png", 64, 64, [[(40, 60, 64, 255)]*64 for _ in range(64)])
            continue
        write_png(f"{ICONS}/{name}.png", 64, 64, upscale(frame0_file(f), 4))
    for name, fn in VANILLA.items():
        write_png(f"{VICONS}/{name}.png", 64, 64, upscale(fn(), 4))


def icon_src(rid, depth):
    """rid may be 'echoes:x', 'minecraft:x', or a bare echoes name."""
    up = "../" * depth
    if rid.startswith("minecraft:"):
        return f"{up}assets/icons/vanilla/{rid.split(':',1)[1]}.png"
    name = rid.split(":", 1)[1] if ":" in rid else rid
    if name in ORDER:
        return f"{up}assets/icons/{name}.png"
    return f"{up}assets/icons/vanilla/{name}.png"


def is_ours(rid):
    name = rid.split(":", 1)[1] if ":" in rid else rid
    return (rid.startswith("echoes:") or ":" not in rid) and name in ORDER


# ───────────────────── HTML ─────────────────────
def page(title, body, depth, active=""):
    up = "../" * depth
    nav = []
    for cat, _, ids in CATEGORIES:
        nav.append(f'<div class="nav-cat">{html.escape(cat)}</div>')
        for rid in ids:
            cls = "nav-item active" if rid == active else "nav-item"
            nav.append(f'<a class="{cls}" href="{up}{rid}.html">'
                       f'<img src="{icon_src(rid, depth)}" alt="">{html.escape(disp("echoes:"+rid))}</a>')
    guides = [("guide-getting-started", "Getting Started"), ("guide-energy", "Energy System"),
              ("guide-wireless", "Wireless Transport"), ("guide-ambient", "Ambient Capture"),
              ("guide-worldgen", "Ores & Worldgen"), ("guide-cosmology", "Cosmology"),
              ("guide-reference", "Reference")]
    gnav = "".join(f'<a class="nav-item" href="{up}{s}.html">{html.escape(t)}</a>' for s, t in guides)
    return f"""<!DOCTYPE html>
<html lang="en"><head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>{html.escape(title)} · Octaves of the One</title>
<link rel="stylesheet" href="{up}style.css">
<link rel="icon" href="{up}assets/icons/resonant_coil.png">
</head><body>
<input type="checkbox" id="navtoggle" hidden>
<header class="topbar">
  <label for="navtoggle" class="burger">☰</label>
  <a class="brand" href="{up}index.html"><img src="{up}assets/icons/stillness_core.png" alt="">Octaves of the One</a>
  <input id="search" class="search" type="search" placeholder="Search blocks &amp; items…" autocomplete="off">
</header>
<div class="layout">
  <nav class="sidebar">
    <div class="nav-cat">Guides</div>
    <a class="nav-item" href="{up}index.html">Home</a>
    {gnav}
    {''.join(nav)}
  </nav>
  <main class="content">{body}</main>
</div>
<div id="results" class="results" hidden></div>
<script src="{up}search.js" data-depth="{depth}"></script>
</body></html>"""


def slot(rid, depth, result=False, count=1):
    cls = "slot result" if result else "slot"
    if rid is None:
        return '<div class="slot empty"></div>'
    name = disp(rid if ":" in rid else "echoes:"+rid)
    img = f'<img src="{icon_src(rid, depth)}" alt="{html.escape(name)}" title="{html.escape(name)}">'
    badge = f'<span class="count">{count}</span>' if count and count > 1 else ""
    if is_ours(rid):
        short = rid.split(":", 1)[1] if ":" in rid else rid
        inner = f'<a href="{"../"*depth}{short}.html">{img}</a>'
    else:
        # vanilla ingredient → link out to the Minecraft Wiki so every cell is clickable
        page_title = name.replace(" ", "_")
        inner = (f'<a class="ext" href="https://minecraft.wiki/w/{page_title}" '
                 f'target="_blank" rel="noopener" title="{html.escape(name)} — Minecraft Wiki">{img}</a>')
    return f'<div class="{cls}">{inner}{badge}</div>'


def render_recipe(rec, depth):
    if rec["kind"] == "craft":
        cells = "".join(slot(c, depth) for c in rec["cells"])
        res = slot(_self_id[0], depth, result=True, count=rec.get("count", 1))
        return f'<div class="recipe"><div class="grid3">{cells}</div>'\
               f'<div class="arrow">▶</div>{res}</div>'
    else:
        label = {"smelt": "Smelting", "blast": "Blasting", "crush": "Crushing"}[rec["kind"]]
        inp = slot(rec["input"], depth)
        res = slot(_self_id[0], depth, result=True, count=rec.get("count", 1))
        extra = ""
        if rec["kind"] == "crush" and rec.get("secondary"):
            pct = int(round(rec.get("secondary_chance", 0)*100))
            extra = f'<div class="byproduct">+ {slot(rec["secondary"], depth)}'\
                    f'<span class="chance">{pct}%</span></div>'
        acls = "arrow flame" if rec["kind"] != "crush" else "arrow"
        return f'<div class="recipe"><span class="rtag">{label}</span>{inp}'\
               f'<div class="{acls}">▶</div>{res}{extra}</div>'


_self_id = [None]  # set per page so render_recipe knows the result item


def entry_page(rid, by_result, uses):
    _self_id[0] = rid
    name = disp("echoes:"+rid)
    d = desc(rid)
    cat = CAT_OF.get(rid, "")
    stats = STATS.get(rid, [])
    statrows = "".join(f'<tr><th>{html.escape(k)}</th><td>{html.escape(v)}</td></tr>' for k, v in stats)
    recs = by_result.get(rid, [])
    recipe_html = "".join(f'<div class="recipe-wrap">{render_recipe(r, 0)}</div>' for r in recs) \
        or SOURCE_NOTE.get(rid, '<p class="muted">Obtained from the world (mined or found), not crafted.</p>')
    used = sorted(uses.get(rid, []))
    used_html = ""
    if used:
        chips = "".join(
            f'<a class="chip" href="{u}.html"><img src="{icon_src(u,0)}" alt="">{html.escape(disp("echoes:"+u))}</a>'
            for u in used)
        used_html = f'<section><h2>Used to craft</h2><div class="chips">{chips}</div></section>'
    body = f"""
<div class="crumbs"><a href="index.html">Home</a> › <span>{html.escape(cat)}</span></div>
<article class="entry">
  <div class="entry-head">
    <div class="entry-icon"><img src="{icon_src(rid,0)}" alt="{html.escape(name)}"></div>
    <div>
      <h1>{html.escape(name)}</h1>
      {f'<p class="lead">{html.escape(d)}</p>' if d else ''}
      <code class="idtag">echoes:{rid}</code>
    </div>
  </div>
  {f'<table class="stats">{statrows}</table>' if statrows else ''}
  <section><h2>Recipe</h2>{recipe_html}</section>
  {used_html}
</article>"""
    return page(name, body, 0, active=rid)


def card(rid, depth=0):
    name = disp("echoes:"+rid)
    return (f'<a class="card" href="{"../"*depth}{rid}.html" data-name="{html.escape(name.lower())}" data-id="{rid}">'
            f'<img src="{icon_src(rid, depth)}" alt=""><span>{html.escape(name)}</span></a>')


def index_page():
    secs = []
    for cat, blurb, ids in CATEGORIES:
        cards = "".join(card(r) for r in ids)
        secs.append(f'<section class="catsec" id="{cat.lower().replace(" & ","-").replace(" ","-")}">'
                    f'<h2>{html.escape(cat)}</h2><p class="muted">{html.escape(blurb)}</p>'
                    f'<div class="grid">{cards}</div></section>')
    hero = """
<section class="hero">
  <h1>Octaves of the One</h1>
  <p>A Fabric tech mod for Minecraft 1.21.4 themed on Walter Russell's two-way universe of
     <em>rhythmic balanced interchange</em>. Draw <strong>Light</strong> from the still centre,
     wind it through the octaves, and spend it across a wired and wireless grid.</p>
  <div class="hero-links">
    <a class="btn" href="guide-getting-started.html">Getting Started</a>
    <a class="btn ghost" href="guide-energy.html">Energy System</a>
    <a class="btn ghost" href="guide-wireless.html">Wireless</a>
  </div>
</section>"""
    return page("Home", hero + "".join(secs), 0)


# ───────────────────── guides ─────────────────────
def guides_data():
    return {
        "guide-getting-started": ("Getting Started", f"""
<h1>Getting Started</h1>
<p>From first ore to your first powered machine.</p>
<ol class="steps">
<li><b>Mine Echocite.</b> {linkico('echocite_ore')} generates across the Overworld and drops
{linkico('raw_echocite')} (use a stone pickaxe or better).</li>
<li><b>Make Echo Ingots.</b> Smelt {linkico('raw_echocite')} → {linkico('echo_ingot')}, or crush it in a
{linkico('compressor')} for <b>2×</b> {linkico('echocite_dust')} (then smelt). Echo Ingot is the core material.</li>
<li><b>Build the core.</b> Craft a {linkico('resonant_coil')}, a line of {linkico('wave_conduit')}, and a
{linkico('compressor')}. Connected by conduit, they form one network.</li>
<li><b>Feed it sound.</b> A {linkico('resonant_coil')} charges from nearby note blocks, anvils, bells,
and mob deaths — see <a href="guide-ambient.html">Ambient Capture</a>.</li>
<li><b>Read the grid.</b> Craft a {linkico('light_meter')} and right-click any device.</li>
</ol>
<p>Next: skip conduits with the <a href="guide-wireless.html">wireless</a> family, bank surplus in an
{linkico('resonance_cell')}, and craft {linkico('resonant_thrusters')} for flight.</p>"""),

        "guide-energy": ("Energy System", f"""
<h1>Energy System</h1>
<p><b>Light</b> (tracked internally as RU) is the mod's energy. Every device exposes a node with one
or more roles: <b>Provider</b>, <b>Consumer</b>, <b>Storage</b>, or <b>Conduit</b>.</p>
<h2>The wired grid</h2>
<p>Conduits join devices into a <b>network</b>. Each tick it pulls Light from providers (then storage),
shares it among consumers by a <em>largest-remainder fair distribution</em> (no starvation), and tops up
storage lowest-first. Networks merge/split incrementally on place/break — no per-tick flood fill — and
persist across restarts.</p>
<h2>Throughput</h2>
<p>{linkico('wave_conduit')} carries 1,000/t; {linkico('dense_wave_conduit')} carries 16,000/t. Use a
{linkico('balancer')} to even storage, and a {linkico('wave_coupler')} to bridge to a wireless channel.</p>
<table class="stats">
<tr><th>{linkico('resonant_coil')} Resonant Coil</th><td>10,000 Light · ambient capture</td></tr>
<tr><th>{linkico('stillness_core')} Stillness Core</th><td>50,000 Light · +4/t baseline</td></tr>
<tr><th>{linkico('resonance_cell')} Resonance Cell</th><td>250,000 Light</td></tr>
</table>"""),

        "guide-wireless": ("Wireless Transport", f"""
<h1>Wireless Transport</h1>
<p>Move items, fluids, Light, and redstone over <b>16 channels</b> (one per dye colour) — no conduits.
Tune any device: right-click cycles mode (Receive → Send → Disabled), sneak-click steps the channel,
or use a dye to jump to a colour.</p>
<h2>The family</h2>
<div class="chips">
{''.join(linkchip(x) for x in ['wave_relay','wave_amplifier','wave_filter','wave_splitter','wave_repeater','wave_coupler','wave_chest','signal_relay','wave_tuner','wave_atlas'])}
</div>
<h2>Budget</h2>
<p>Per channel, per tick: <b>8 items · 1 bucket · 1,000 Light</b> per sender, capped at 64 / 8 / 16,000,
all scaled by {linkico('wave_amplifier')} amplifiers (×2 each, ×16 cap). A channel needs ≥2 devices.</p>"""),

        "guide-ambient": ("Ambient Capture", f"""
<h1>Ambient Capture</h1>
<p>A {linkico('resonant_coil')} winds <b>sound</b> into Light. Mob deaths within 8 blocks add 25 Light;
mapped world sounds charge the nearest coil from a data-driven, reloadable table.</p>
<table class="stats">
<tr><th>Note block (harp/bass)</th><td>8</td></tr><tr><th>Note block (bell)</th><td>12</td></tr>
<tr><th>Bell use</th><td>10</td></tr><tr><th>Anvil land</th><td>40</td></tr>
<tr><th>Explosion</th><td>40</td></tr><tr><th>Beacon activate</th><td>100</td></tr>
<tr><th>Thunder</th><td>2,000</td></tr>
</table>
<p>Extend it with a datapack override of <code>data/echoes/resonance_sources.json</code>.</p>"""),

        "guide-worldgen": ("Ores & Worldgen", f"""
<h1>Ores &amp; Worldgen</h1>
<table class="stats">
<tr><th>{linkico('echocite_ore')} Echocite</th><td>Overworld (stone + deepslate), y −20…60 · drops {linkico('raw_echocite')}</td></tr>
<tr><th>{linkico('drumstone_ore')} Drumstone</th><td>Overworld, y −48…24 · drops {linkico('drumstone_shard')}</td></tr>
<tr><th>{linkico('silentite_ore')} Silentite</th><td>Deep Dark only, y −58…−8 · drops {linkico('silentite_crystal')}</td></tr>
</table>
<p>All three need the matching pickaxe tier and respect Fortune.</p>"""),

        "guide-cosmology": ("Cosmology", """
<h1>Cosmology &amp; Lore</h1>
<blockquote>“The universe is a play of Light. All motion springs from a still magnetic centre of zero
and returns to it, in equal giving and regiving.”</blockquote>
<p>The mod maps <b>Walter Russell's two-way universe</b> onto its tech tree: <b>Light</b> is the one
substance; <b>generation</b> (compression/charging) and <b>radiation</b> (expansion/discharging) are the
paired halves of <em>rhythmic balanced interchange</em>; and the 16 wireless channels are <b>octaves</b>.
The framing is flavour, not physics — internally the namespace stays <code>echoes</code> for
save-compatibility, and the display names are the “Octaves of the One” reskin.</p>"""),

        "guide-reference": ("Reference", f"""
<h1>Reference</h1>
<table class="stats">
<tr><th>Minecraft</th><td>1.21.4 (Fabric)</td></tr>
<tr><th>Energy unit</th><td>Light (RU internally)</td></tr>
<tr><th>Wireless channels</th><td>16 (dye colours)</td></tr>
<tr><th>Conduit throughput</th><td>1,000 /t · Dense 16,000 /t</td></tr>
<tr><th>Capacities</th><td>Coil 10k · Core 50k · Resonance Cell 250k · Thrusters 1M</td></tr>
<tr><th>Echo tools</th><td>speed 12 · 4000 durability · ench 22 · mines anything</td></tr>
</table>
<p>This site is generated from the mod's own textures, recipes, and lang file by
<code>scripts/build_wiki_site.py</code>.</p>"""),
    }


def linkico(rid):
    name = disp("echoes:"+rid)
    return (f'<a class="inlineico" href="{rid}.html"><img src="{icon_src(rid,0)}" alt="">'
            f'{html.escape(name)}</a>')


def linkchip(rid):
    return (f'<a class="chip" href="{rid}.html"><img src="{icon_src(rid,0)}" alt="">'
            f'{html.escape(disp("echoes:"+rid))}</a>')


# ───────────────────── assets: css + search ─────────────────────
CSS = """
:root{--bg:#0c1014;--panel:#141b21;--panel2:#1b242c;--line:#26333d;--ink:#dfe9ee;
--mut:#8aa0ac;--teal:#43d0c9;--teal2:#7be7e0;--bronze:#c8a063;--slot:#2a3640;--slotin:#10161b;}
*{box-sizing:border-box}
body{margin:0;background:var(--bg);color:var(--ink);
font:15px/1.6 system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif}
a{color:var(--teal);text-decoration:none}a:hover{color:var(--teal2)}
img{image-rendering:pixelated}
.topbar{position:sticky;top:0;z-index:20;display:flex;align-items:center;gap:14px;
padding:10px 18px;background:linear-gradient(180deg,#0e151b,#0c1014);border-bottom:1px solid var(--line)}
.brand{display:flex;align-items:center;gap:10px;font-weight:700;color:var(--ink);font-size:17px;letter-spacing:.2px}
.brand img{width:26px;height:26px}
.search{margin-left:auto;width:min(360px,46vw);padding:8px 12px;border-radius:9px;
border:1px solid var(--line);background:var(--slotin);color:var(--ink)}
.search:focus{outline:none;border-color:var(--teal)}
.burger{display:none;font-size:20px;cursor:pointer;color:var(--mut)}
.layout{display:grid;grid-template-columns:268px 1fr;align-items:start}
.sidebar{position:sticky;top:57px;height:calc(100vh - 57px);overflow-y:auto;
padding:14px 10px 40px;background:var(--panel);border-right:1px solid var(--line)}
.nav-cat{margin:16px 8px 6px;font-size:11px;letter-spacing:.14em;text-transform:uppercase;color:var(--bronze)}
.nav-item{display:flex;align-items:center;gap:9px;padding:6px 9px;border-radius:8px;color:var(--ink);font-size:13.5px}
.nav-item img{width:20px;height:20px}
.nav-item:hover{background:var(--panel2)}
.nav-item.active{background:#10312f;color:var(--teal2);box-shadow:inset 2px 0 0 var(--teal)}
.content{padding:30px clamp(18px,4vw,56px);max-width:1040px}
.hero{padding:30px 0 10px;border-bottom:1px solid var(--line);margin-bottom:26px}
.hero h1{font-size:40px;margin:0 0 10px;background:linear-gradient(90deg,var(--teal2),var(--bronze));
-webkit-background-clip:text;background-clip:text;color:transparent}
.hero p{max-width:680px;color:var(--mut)}
.hero-links{margin-top:18px;display:flex;gap:12px;flex-wrap:wrap}
.btn{padding:10px 18px;border-radius:10px;background:var(--teal);color:#04201e;font-weight:600}
.btn.ghost{background:transparent;border:1px solid var(--line);color:var(--ink)}
.btn:hover{filter:brightness(1.08)}
.catsec{margin:34px 0}
.catsec h2{font-size:22px;margin:0 0 4px}
.muted{color:var(--mut);margin:.2em 0 1em}
.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(150px,1fr));gap:12px}
.card{display:flex;flex-direction:column;align-items:center;gap:10px;padding:16px 10px;
background:var(--panel);border:1px solid var(--line);border-radius:12px;color:var(--ink);text-align:center;
transition:transform .12s,border-color .12s,background .12s}
.card:hover{transform:translateY(-3px);border-color:var(--teal);background:var(--panel2)}
.card img{width:46px;height:46px}
.card span{font-size:13px;line-height:1.3}
.crumbs{color:var(--mut);font-size:13px;margin-bottom:14px}
.entry-head{display:flex;gap:20px;align-items:center;margin-bottom:18px}
.entry-icon{flex:0 0 auto;width:96px;height:96px;display:grid;place-items:center;
background:radial-gradient(circle at 50% 40%,#13343` 0,var(--panel) 70%);
border:1px solid var(--line);border-radius:16px}
.entry-icon img{width:72px;height:72px}
.entry h1{margin:0 0 4px;font-size:30px}
.lead{margin:0 0 8px;color:var(--ink)}
.idtag{font-size:12px;color:var(--mut);background:var(--slotin);padding:3px 8px;border-radius:6px}
h2{font-size:19px;border-bottom:1px solid var(--line);padding-bottom:6px;margin:30px 0 14px}
.stats{border-collapse:collapse;width:100%;max-width:560px;margin:4px 0 8px}
.stats th{text-align:left;color:var(--mut);font-weight:500;padding:7px 14px 7px 0;
vertical-align:top;white-space:nowrap;border-bottom:1px solid var(--line)}
.stats td{padding:7px 0;border-bottom:1px solid var(--line)}
.stats .inlineico,.stats img{vertical-align:middle}
.recipe-wrap{margin:10px 0}
.recipe{display:inline-flex;align-items:center;gap:14px;padding:14px 18px;
background:var(--panel);border:1px solid var(--line);border-radius:14px;flex-wrap:wrap}
.rtag{font-size:11px;letter-spacing:.1em;text-transform:uppercase;color:var(--bronze);align-self:center}
.grid3{display:grid;grid-template-columns:repeat(3,1fr);gap:5px;
padding:6px;background:var(--slotin);border-radius:8px}
.slot{position:relative;width:46px;height:46px;display:grid;place-items:center;border-radius:6px;
background:var(--slot);box-shadow:inset 2px 2px 0 #0006,inset -2px -2px 0 #ffffff14}
.slot.empty{background:#1a232b;box-shadow:inset 2px 2px 0 #0004}
.slot img{width:34px;height:34px}
.slot a{display:grid;place-items:center;width:100%;height:100%}
.slot.result{background:#10312f;box-shadow:inset 0 0 0 2px var(--teal),inset 2px 2px 0 #0006}
.count{position:absolute;right:3px;bottom:1px;font-size:13px;font-weight:700;
color:#fff;text-shadow:1px 1px 0 #000}
.arrow{color:var(--teal);font-size:22px}
.arrow.flame{color:var(--bronze)}
.byproduct{display:flex;align-items:center;gap:4px;color:var(--mut)}
.chance{font-size:12px}
.chips{display:flex;flex-wrap:wrap;gap:8px}
.chip{display:inline-flex;align-items:center;gap:7px;padding:6px 12px 6px 8px;border-radius:999px;
background:var(--panel);border:1px solid var(--line);color:var(--ink);font-size:13px}
.chip:hover{border-color:var(--teal)}.chip img{width:22px;height:22px}
.inlineico{display:inline-flex;align-items:center;gap:5px}.inlineico img{width:20px;height:20px}
.steps{padding-left:20px}.steps li{margin:8px 0}
blockquote{border-left:3px solid var(--bronze);margin:0 0 14px;padding:6px 16px;color:var(--mut);font-style:italic}
.results{position:fixed;inset:57px 0 0;background:rgba(8,11,14,.97);z-index:30;
padding:24px clamp(18px,6vw,80px);overflow-y:auto}
.results .grid{margin-top:10px}
@media(max-width:820px){
.layout{grid-template-columns:1fr}.burger{display:block}
.sidebar{position:fixed;top:57px;left:0;width:280px;z-index:25;transform:translateX(-100%);transition:transform .2s}
#navtoggle:checked ~ .layout .sidebar{transform:none}
.hero h1{font-size:30px}}
"""

# tiny fix: the radial-gradient hex above must be valid
CSS = CSS.replace("#13343`", "#133431")

SEARCH_JS = """
(function(){
var depth=+document.currentScript.dataset.depth||0, up='../'.repeat(depth);
var input=document.getElementById('search'), box=document.getElementById('results');
var DATA=window.__INDEX__||[];
function render(q){
  q=q.trim().toLowerCase();
  if(!q){box.hidden=true;box.innerHTML='';return;}
  var hits=DATA.filter(function(d){return d.n.indexOf(q)>=0||d.id.indexOf(q)>=0||d.c.indexOf(q)>=0;}).slice(0,60);
  box.innerHTML='<div class="grid">'+hits.map(function(d){
    return '<a class="card" href="'+up+d.id+'.html"><img src="'+up+'assets/icons/'+d.id+'.png" alt=""><span>'+d.label+'</span></a>';
  }).join('')+'</div>'+(hits.length?'':'<p class="muted">No matches.</p>');
  box.hidden=false;
}
if(input){input.addEventListener('input',function(){render(input.value);});
input.addEventListener('keydown',function(e){if(e.key==='Escape'){input.value='';render('');}});}
})();
"""


def build_search_index():
    arr = []
    for rid in ORDER:
        nm = disp("echoes:"+rid)
        arr.append({"id": rid, "label": nm, "n": nm.lower(), "c": CAT_OF.get(rid, "").lower()})
    return "window.__INDEX__=" + json.dumps(arr, separators=(",", ":")) + ";\n"


# ───────────────────── main ─────────────────────
def main():
    os.makedirs(SITE, exist_ok=True)
    gen_icons()
    by_result, uses = load_recipes()
    open(f"{SITE}/style.css", "w").write(CSS)
    open(f"{SITE}/search.js", "w").write(build_search_index() + SEARCH_JS)
    open(f"{SITE}/index.html", "w").write(index_page())
    for rid in ORDER:
        open(f"{SITE}/{rid}.html", "w").write(entry_page(rid, by_result, uses))
    for slug, (title, bodyhtml) in guides_data().items():
        open(f"{SITE}/{slug}.html", "w").write(page(title, bodyhtml, 0))
    # .nojekyll so GitHub Pages serves the folder as-is
    open(f"{SITE}/.nojekyll", "w").write("")
    print(f"built site: {len(ORDER)} entry pages + {len(guides_data())} guides + index → {SITE}")


if __name__ == "__main__":
    main()
