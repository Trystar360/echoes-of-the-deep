#!/usr/bin/env python3
"""Procedural 16x16 pixel-art textures for Echoes of the Deep (no PIL).

Design language:
  - Resonance theme colour = cyan-teal, glowing. Drumstone = warm amber.
    Silentite = amethyst purple. Metals are teal-tinted steel vs. plain grey.
  - Light source top-left: highlights on top/left edges, shadow bottom/right.
  - Ores: faceted gem clusters embedded in stone/deepslate with a soft glow.
  - Machines: heavy metal frames, rivets, glowing emitter cores.
  - Items: clean silhouettes on transparent backgrounds, beveled shading.
"""
import struct, zlib, os, math

# ---------------------------------------------------------------- PNG (RGBA)
def write_png(path, w, h, px):
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        for x in range(w):
            r, g, b, a = px[y * w + x]
            raw += bytes((r, g, b, a))
    def chunk(t, d):
        return struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t + d) & 0xffffffff)
    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n")
        f.write(chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)))
        f.write(chunk(b"IDAT", zlib.compress(bytes(raw), 9)))
        f.write(chunk(b"IEND", b""))

# ---------------------------------------------------------------- canvas
class C:
    def __init__(self, w=16, h=16):
        self.w, self.h = w, h
        self.px = [(0, 0, 0, 0)] * (w * h)
    def set(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h and c is not None:
            if len(c) == 3: c = (c[0], c[1], c[2], 255)
            self.px[y * self.w + x] = c
    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.px[y * self.w + x]
        return (0, 0, 0, 0)
    def over(self, x, y, c):  # alpha-blend c onto existing
        if not (0 <= x < self.w and 0 <= y < self.h): return
        if len(c) == 3: c = (c[0], c[1], c[2], 255)
        r, g, b, a = c
        if a == 0: return
        br, bg, bb, ba = self.get(x, y)
        af = a / 255.0
        nr = int(r * af + br * (1 - af)); ng = int(g * af + bg * (1 - af)); nb = int(b * af + bb * (1 - af))
        na = max(a, ba)
        self.set(x, y, (nr, ng, nb, na))
    def rect(self, x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, c)

# ---------------------------------------------------------------- prng / noise
def rng(seed):
    s = seed & 0x7fffffff
    def nxt():
        nonlocal s
        s = (s * 1103515245 + 12345) & 0x7fffffff
        return s
    return nxt

def hash_noise(x, y, seed):
    n = (x * 374761393 + y * 668265263 + seed * 69069) & 0xffffffff
    n = (n ^ (n >> 13)) * 1274126177 & 0xffffffff
    return ((n ^ (n >> 16)) & 0xffff) / 65535.0

def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3])

def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

# ---------------------------------------------------------------- palettes (dark -> light ramps)
STONE     = [(78,78,84),(102,102,108),(124,124,130),(140,140,146),(158,158,164)]
DEEPSLATE = [(38,38,44),(52,52,60),(68,68,78),(84,84,94),(100,100,112)]
TEAL      = [(7,38,40),(13,74,72),(28,134,128),(58,214,200),(150,255,244)]
AMBER     = [(58,34,14),(112,68,26),(170,104,40),(214,150,64),(248,210,128)]
PURPLE    = [(36,22,56),(70,44,104),(116,76,164),(168,118,222),(216,184,250)]
STEEL     = [(36,54,56),(64,92,94),(104,142,142),(150,196,192),(208,240,236)]
GREY      = [(52,54,58),(82,84,90),(116,118,124),(150,152,158),(198,200,206)]
IRON      = [(58,58,64),(92,92,98),(126,126,132),(160,160,166),(206,206,212)]

OUT = "src/main/resources/assets/echoes/textures"
for d in ("block", "item", "gui"):
    os.makedirs(os.path.join(OUT, d), exist_ok=True)

# ---------------------------------------------------------------- shared builders
def stone_bg(c, ramp, seed, cracks=True):
    for y in range(16):
        for x in range(16):
            n = hash_noise(x, y, seed)
            n2 = hash_noise(x // 2, y // 2, seed + 7) * 0.5
            v = n * 0.7 + n2
            idx = 1 if v < 0.30 else 2 if v < 0.62 else 3 if v < 0.86 else 4
            c.set(x, y, ramp[idx])
    if cracks:
        nx = rng(seed + 99)
        for _ in range(2):
            x = 2 + nx() % 12; y = 1 + nx() % 6
            for _ in range(4 + nx() % 4):
                c.set(x, y, ramp[0])
                x += (nx() % 3) - 1; y += 1
    # subtle top-left ambient light
    for y in range(16):
        for x in range(16):
            if x + y < 6:
                c.over(x, y, (255, 255, 255, 26))

def gem(c, cx, cy, ramp, r=2, glow=True):
    """A faceted crystal: bright top-left facet, dark bottom-right, sparkle."""
    if glow:
        for dy in range(-r - 1, r + 2):
            for dx in range(-r - 1, r + 2):
                d = abs(dx) + abs(dy)
                if d == r + 1:
                    c.over(cx + dx, cy + dy, (ramp[3][0], ramp[3][1], ramp[3][2], 60))
    for dy in range(-r, r + 1):
        for dx in range(-r, r + 1):
            if abs(dx) + abs(dy) <= r:
                if dx + dy < 0:        # upper-left facet (lit)
                    col = ramp[3]
                elif dx + dy > 0:      # lower-right facet (shadow)
                    col = ramp[1]
                else:
                    col = ramp[2]
                c.set(cx + dx, cy + dy, col)
    c.set(cx, cy - r, ramp[4])         # top highlight
    c.set(cx - 1, cy - 1, ramp[4])     # sparkle
    # outline a couple shadow edges for definition
    c.over(cx + r, cy, (ramp[0][0], ramp[0][1], ramp[0][2], 180))
    c.over(cx, cy + r, (ramp[0][0], ramp[0][1], ramp[0][2], 180))

def rivet(c, x, y, ramp):
    c.set(x, y, ramp[3]); c.set(x, y, ramp[4])
    c.over(x + 1, y + 1, (0, 0, 0, 90))

def solids(c, thresh=170):
    return [(x, y) for y in range(c.h) for x in range(c.w) if c.get(x, y)[3] >= thresh]

def outline(c, color=(0, 0, 0), alpha=160, thresh=170):
    """Dark 1px outline around the solid silhouette. Snapshot-based so it can't
    cascade across the canvas (the classic read-while-write flood-fill bug)."""
    for (x, y) in solids(c, thresh):
        for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < c.w and 0 <= ny < c.h and c.get(nx, ny)[3] < 10:
                c.over(nx, ny, (color[0], color[1], color[2], alpha))

def glow(c, ramp, alpha=55, thresh=200):
    """Soft coloured halo one pixel out from the brightest pixels."""
    snap = [(x, y) for (x, y) in solids(c, thresh)]
    for (x, y) in snap:
        for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1), (-1, -1), (1, 1)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < c.w and 0 <= ny < c.h and c.get(nx, ny)[3] < 10:
                c.over(nx, ny, (ramp[3][0], ramp[3][1], ramp[3][2], alpha))

def frame(c, ramp, t=2):
    dark, mid, lite = ramp[0], ramp[2], ramp[3]
    c.rect(0, 0, 15, 15, mid)
    # bevel: light top/left, dark bottom/right
    for i in range(t):
        for x in range(16):
            c.set(x, i, lerp(lite, mid, i / t)); c.set(x, 15 - i, lerp(dark, mid, i / t))
        for y in range(16):
            c.set(i, y, lerp(lite, mid, i / t)); c.set(15 - i, y, lerp(dark, mid, i / t))
    c.set(0, 0, lite); c.set(15, 15, dark)

# ================================================================ ORES
def ore(name, base, gemramp, seed, npts=3):
    c = C()
    stone_bg(c, base, seed)
    pts = [(4, 5), (11, 9), (7, 12), (12, 3), (3, 11)]
    nx = rng(seed + 31)
    for i in range(npts):
        px, py = pts[i]
        px = max(2, min(13, px + (nx() % 3 - 1)))
        py = max(2, min(13, py + (nx() % 3 - 1)))
        gem(c, px, py, gemramp, r=2 if i == 0 else (nx() % 2 + 1))
    # a few loose specks
    for _ in range(5):
        x = nx() % 16; y = nx() % 16
        if c.get(x, y)[:3] in (base[1], base[2], base[3]):
            c.over(x, y, (gemramp[3][0], gemramp[3][1], gemramp[3][2], 130))
    write_png(f"{OUT}/block/{name}.png", 16, 16, c.px)

ore("echocite_ore", STONE, TEAL, 101, 3)
ore("deepslate_echocite_ore", DEEPSLATE, TEAL, 102, 3)
ore("drumstone_ore", DEEPSLATE, AMBER, 103, 3)
ore("silentite_ore", DEEPSLATE, PURPLE, 104, 2)

# ================================================================ RESONATOR (drum/emitter)
def resonator():
    c = C()
    frame(c, IRON, t=2)
    # dark inset membrane
    c.rect(3, 3, 12, 12, (24, 26, 30))
    cx, cy = 7.5, 7.5
    for y in range(3, 13):
        for x in range(3, 13):
            d = math.hypot(x - cx, y - cy)
            if d <= 5.2:
                ring = int(d) % 2
                t = max(0.0, 1 - d / 5.2)
                if ring == 0:
                    col = lerp(TEAL[1], TEAL[4], t)
                else:
                    col = lerp((18, 22, 26), TEAL[2], t * 0.6)
                # top-left gets a brighter sheen
                if x - cx + y - cy < -2: col = lerp(col, TEAL[4], 0.35)
                c.set(x, y, col)
    c.set(7, 7, TEAL[4]); c.set(8, 7, TEAL[4]); c.set(7, 8, TEAL[3])
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        rivet(c, rx, ry, IRON)
    write_png(f"{OUT}/block/resonator.png", 16, 16, c.px)
resonator()

# ================================================================ TUNING CONDUIT (energy node)
def conduit():
    c = C()
    # dark metal plate
    for y in range(16):
        for x in range(16):
            n = hash_noise(x, y, 55)
            c.set(x, y, IRON[1] if n < 0.5 else IRON[2])
    frame(c, IRON, t=1)
    # connection stubs to each edge
    c.rect(6, 0, 9, 5, IRON[2]); c.rect(6, 10, 9, 15, IRON[2])
    c.rect(0, 6, 5, 9, IRON[2]); c.rect(10, 6, 15, 9, IRON[2])
    # glowing teal cross + core
    c.rect(7, 1, 8, 14, TEAL[2]); c.rect(1, 7, 14, 8, TEAL[2])
    c.rect(7, 1, 8, 14, None)
    for x in range(1, 15): c.over(x, 7, TEAL[3]); c.over(x, 8, TEAL[2])
    for y in range(1, 15): c.over(7, y, TEAL[3]); c.over(8, y, TEAL[2])
    # bright core
    c.rect(6, 6, 9, 9, TEAL[3])
    c.set(7, 7, TEAL[4]); c.set(8, 8, TEAL[2]); c.set(7, 8, TEAL[4]); c.set(8, 7, TEAL[3])
    write_png(f"{OUT}/block/tuning_conduit.png", 16, 16, c.px)
conduit()

# ================================================================ CRUSHER (grinder face)
def crusher():
    c = C()
    frame(c, IRON, t=2)
    # inner chamber
    c.rect(2, 2, 13, 13, (40, 42, 48))
    # hopper mouth (top, dark trapezoid)
    for y in range(2, 5):
        x0 = 3 + (y - 2); x1 = 12 - (y - 2)
        c.rect(x0, y, x1, y, (22, 24, 28))
    # two rows of interlocking crushing teeth
    teeth = GREY
    for x in range(3, 13):
        # upper teeth point down
        h = 2 if x % 2 == 0 else 3
        for k in range(h):
            c.set(x, 6 + k, teeth[3 if k == 0 else 2])
        c.set(x, 6, teeth[4])
        # lower teeth point up (offset)
        h2 = 3 if x % 2 == 0 else 2
        for k in range(h2):
            c.set(x, 11 - k, teeth[2 if k == 0 else 1])
    # teal glow seeping from the grinding gap
    for x in range(3, 13):
        c.over(x, 9, (TEAL[3][0], TEAL[3][1], TEAL[3][2], 150))
        c.over(x, 8, (TEAL[2][0], TEAL[2][1], TEAL[2][2], 90))
        c.over(x, 10, (TEAL[2][0], TEAL[2][1], TEAL[2][2], 70))
    # corner rivets + warning notches
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        rivet(c, rx, ry, IRON)
    write_png(f"{OUT}/block/crusher.png", 16, 16, c.px)
crusher()

# ================================================================ RESONANT RELAY (wireless broadcaster)
def relay():
    c = C()
    frame(c, IRON, t=2)
    # dark inset face
    c.rect(3, 3, 12, 12, (22, 26, 30))
    cx, cy = 7.5, 7.5
    # concentric broadcast arcs radiating from a bright core (suggests wireless)
    for y in range(3, 13):
        for x in range(3, 13):
            d = math.hypot(x - cx, y - cy)
            ring = int(round(d))
            if d <= 5.0 and ring % 2 == 0:
                t = max(0.0, 1 - d / 5.0)
                col = lerp(TEAL[1], TEAL[4], t)
                # brighten the upper-left for the top-left light source
                if (x - cx) + (y - cy) < -1:
                    col = lerp(col, TEAL[4], 0.35)
                c.over(x, y, (col[0], col[1], col[2], 235))
    # bright emitter core
    c.rect(7, 7, 8, 8, TEAL[4])
    c.set(7, 7, (235, 255, 250)); c.set(8, 8, TEAL[3])
    # four broadcast pips at the cardinal edges
    for (px, py) in [(7, 1), (8, 14), (1, 8), (14, 7)]:
        c.set(px, py, TEAL[4]); c.over(px, py + 1, (TEAL[2][0], TEAL[2][1], TEAL[2][2], 160))
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        rivet(c, rx, ry, IRON)
    write_png(f"{OUT}/block/resonant_relay.png", 16, 16, c.px)
relay()

# ================================================================ ITEMS
def ingot(name, ramp):
    c = C()
    # parallelogram ingot
    top, bot = 5, 11
    for y in range(top, bot + 1):
        off = (y - top)
        x0 = 2 + off // 2
        x1 = 12 + off // 2
        for x in range(x0, x1 + 1):
            if y == top: col = ramp[4]
            elif x == x0: col = ramp[3]
            elif x == x1 or y == bot: col = ramp[1]
            else: col = ramp[2]
            c.set(x, y, col)
    # top face (lighter slab)
    for x in range(2, 13):
        c.set(x, top - 1, ramp[3]); c.set(x + 1, top - 1, ramp[3])
    c.rect(3, 4, 12, 4, ramp[4])
    c.set(4, 4, (255, 255, 255)); c.set(5, 4, ramp[4])
    # dark outline bottom
    for x in range(2, 14):
        c.over(x + 3, bot + 1, (0, 0, 0, 70))
    write_png(f"{OUT}/item/{name}.png", 16, 16, c.px)

ingot("echo_ingot", STEEL)
ingot("dull_ingot", GREY)

def crystal_item(name, ramp, tall=True):
    c = C()
    # faceted gem, pointed top & bottom
    shape = [
        (8, 1), (7, 2), (8, 2), (9, 2),
        (6, 3), (7, 3), (8, 3), (9, 3), (10, 3),
        (5, 4), (6, 4), (7, 4), (8, 4), (9, 4), (10, 4), (11, 4),
        (5, 5), (6, 5), (7, 5), (8, 5), (9, 5), (10, 5), (11, 5),
        (6, 6), (7, 6), (8, 6), (9, 6), (10, 6),
        (6, 7), (7, 7), (8, 7), (9, 7), (10, 7),
        (7, 8), (8, 8), (9, 8),
        (7, 9), (8, 9), (9, 9),
        (8, 10), (8, 11),
    ]
    cx = 8
    for (x, y) in shape:
        if x < cx - 1: col = ramp[3]      # left facet lit
        elif x > cx + 1: col = ramp[1]    # right facet shadow
        else: col = ramp[2]
        c.set(x, y, col)
    # central highlight ridge + sparkle
    for y in range(2, 9): c.set(cx, y, ramp[4])
    c.set(7, 3, ramp[4]); c.set(6, 4, (255, 255, 255)); c.set(8, 1, ramp[4])
    glow(c, ramp, alpha=50, thresh=150)
    outline(c, color=(max(0, ramp[0][0]-12), max(0, ramp[0][1]-12), max(0, ramp[0][2]-12)), thresh=150)
    write_png(f"{OUT}/item/{name}.png", 16, 16, c.px)

crystal_item("silentite_crystal", PURPLE)

def chunk_item(name, rockramp, gemramp, seed):
    """Raw ore chunk: irregular rock blob with embedded crystal bits."""
    c = C()
    nx = rng(seed)
    # blob mask via radial noise
    cx, cy = 8, 8
    for y in range(2, 15):
        for x in range(2, 15):
            d = math.hypot(x - cx, y - cy) + hash_noise(x, y, seed) * 2.2 - 1.0
            if d <= 5.4:
                n = hash_noise(x, y, seed + 3)
                idx = 1 if n < 0.33 else 2 if n < 0.7 else 3
                # top-left lighter
                if (x - cx) + (y - cy) < -3: idx = min(4, idx + 1)
                if (x - cx) + (y - cy) > 4: idx = max(0, idx - 1)
                c.set(x, y, rockramp[idx])
    # embedded gem bits
    for (gx, gy) in [(6, 6), (10, 9), (8, 11)]:
        gx2 = gx + (nx() % 3 - 1); gy2 = gy + (nx() % 3 - 1)
        c.set(gx2, gy2, gemramp[3]); c.set(gx2, gy2 - 1, gemramp[4])
        c.over(gx2 + 1, gy2, (gemramp[2][0], gemramp[2][1], gemramp[2][2], 200))
        c.over(gx2, gy2 + 1, (gemramp[1][0], gemramp[1][1], gemramp[1][2], 200))
    outline(c)
    write_png(f"{OUT}/item/{name}.png", 16, 16, c.px)

chunk_item("raw_echocite", STONE, TEAL, 201)
chunk_item("resonant_slag", [(40,38,40),(64,60,60),(86,80,78),(108,100,96),(130,120,114)], TEAL, 202)

def shard_item(name, ramp):
    c = C()
    # a chunky angular crystal shard running lower-left to upper-right
    rows = {        # y : (x0, x1) span of the shard body
        3: (10, 12), 4: (9, 12), 5: (8, 12), 6: (7, 11),
        7: (6, 10), 8: (5, 10), 9: (5, 9), 10: (4, 8),
        11: (4, 7), 12: (5, 6),
    }
    for y, (x0, x1) in rows.items():
        for x in range(x0, x1 + 1):
            # lit facet on the upper-left half, shadow on lower-right
            f = (x - x0) - (x1 - x)
            col = ramp[3] if f < -1 else ramp[1] if f > 1 else ramp[2]
            c.set(x, y, col)
    # bright central ridge + tip highlights + sparkle
    for (x, y) in [(11, 4), (10, 5), (9, 6), (8, 7), (7, 8), (6, 9)]:
        c.set(x, y, ramp[4])
    c.set(12, 3, ramp[4]); c.set(5, 11, ramp[1]); c.set(10, 4, (255, 255, 255))
    glow(c, ramp, alpha=45, thresh=150)
    outline(c, thresh=150)
    write_png(f"{OUT}/item/{name}.png", 16, 16, c.px)
shard_item("drumstone_shard", AMBER)

def dust_item(name, ramp, seed, sparkle=True):
    c = C()
    nx = rng(seed)
    # mounded heap at the bottom
    heights = [0,1,2,3,4,5,5,6,6,5,5,4,3,2,1,0]
    base = 13
    for x in range(16):
        h = heights[x] + (nx() % 2)
        for k in range(h):
            y = base - k
            n = hash_noise(x, y, seed)
            idx = 1 if n < 0.3 else 2 if n < 0.62 else 3 if n < 0.85 else 4
            c.set(x, y, ramp[idx])
    # scattered granules above
    for _ in range(8):
        x = 2 + nx() % 12; y = 4 + nx() % 6
        c.over(x, y, (ramp[3][0], ramp[3][1], ramp[3][2], 230))
    if sparkle:
        for _ in range(4):
            x = 2 + nx() % 12; y = base - (nx() % 5)
            c.set(x, y, ramp[4])
    # base shadow line
    for x in range(16):
        if c.get(x, base)[3] > 0:
            c.over(x, base + 1, (0, 0, 0, 90))
    write_png(f"{OUT}/item/{name}.png", 16, 16, c.px)

dust_item("echocite_dust", TEAL, 221)
dust_item("echo_dust", [(20,60,70),(40,150,150),(80,220,210),(160,255,240),(230,255,250)], 222)

def core_item(name):
    c = C()
    cx, cy = 8, 8
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - cx, y - cy)
            if d <= 6.3:
                ring = int(d)
                if ring >= 6: col = IRON[1]
                elif ring == 5: col = IRON[3]
                elif ring == 4: col = IRON[2]
                elif ring == 3: col = TEAL[1]
                elif ring == 2: col = TEAL[2]
                elif ring == 1: col = TEAL[3]
                else: col = TEAL[4]
                if (x - cx) + (y - cy) < -3: col = lerp(col, (255,255,255), 0.25)
                if (x - cx) + (y - cy) > 4: col = shade(col, 0.7)
                c.set(x, y, col)
    c.set(7, 7, TEAL[4]); c.set(8, 8, TEAL[2])
    # rim rivets
    for ang in range(0, 360, 90):
        x = int(cx + 5.4 * math.cos(math.radians(ang)))
        y = int(cy + 5.4 * math.sin(math.radians(ang)))
        c.set(x, y, IRON[4])
    outline(c)
    write_png(f"{OUT}/item/{name}.png", 16, 16, c.px)
core_item("drum_core")

# ================================================================ CRUSHER GUI (256x256)
def gui():
    W = H = 256
    px = [(0, 0, 0, 0)] * (W * H)
    def s(x, y, c):
        if 0 <= x < W and 0 <= y < H:
            px[y * W + x] = (c[0], c[1], c[2], 255) if len(c) == 3 else c
    def rect(x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1): s(x, y, c)
    PANEL=(198,198,198); LITE=(255,255,255); DARK=(85,85,85); MID=(139,139,139)
    SDARK=(55,55,55)
    # main panel 176x166
    rect(0,0,175,165,PANEL)
    rect(0,0,175,2,LITE); rect(0,0,2,165,LITE)
    rect(0,163,175,165,DARK); rect(173,0,175,165,DARK)
    s(175,0,PANEL); s(0,165,PANEL)
    def slot(ix, iy):
        rect(ix-1,iy-1,ix+16,iy+16,MID)
        rect(ix-1,iy-1,ix+16,iy-1,SDARK); rect(ix-1,iy-1,ix-1,iy+16,SDARK)
        rect(ix-1,iy+16,ix+16,iy+16,LITE); rect(ix+16,iy-1,ix+16,iy+16,LITE)
    slot(56,35)   # input
    slot(116,35)  # output
    for r in range(3):
        for col in range(9):
            slot(8+col*18, 84+r*18)
    for col in range(9):
        slot(8+col*18, 142)
    # arrow track (empty, engraved) around x 79..101 y 33..49
    def arrow(ox, oy, fill):
        # shaft
        for yy in range(oy+5, oy+11):
            for xx in range(ox, ox+15):
                s(xx, yy, fill)
        # head
        for k in range(8):
            for yy in range(oy+2+k, oy+14-k):
                s(ox+14+k, yy, fill)
    arrow(79, 32, SDARK)  # engraved background
    # RU gauge frame (left of input)
    rect(20,20,28,52,SDARK); rect(21,21,27,51,(24,28,30))
    # teal fill in gauge bottom
    for yy in range(40,51):
        for xx in range(21,28):
            t=(51-yy)/11
            s(xx,yy, lerp(TEAL[1],TEAL[3],t))
    # ----- filled progress arrow sprite at (176,0) 24x16 -----
    def sprite_arrow(ox, oy):
        for yy in range(oy+5, oy+11):
            for xx in range(ox, ox+15):
                t=(yy-(oy+5))/5
                s(xx, yy, lerp(TEAL[3], TEAL[1], t))
        for k in range(8):
            for yy in range(oy+2+k, oy+14-k):
                s(ox+14+k, yy, lerp(TEAL[3], TEAL[2], k/8))
        # top highlight
        for xx in range(ox, ox+15): s(xx, oy+5, TEAL[4])
    sprite_arrow(176, 0)
    write_png(f"{OUT}/gui/crusher.png", W, H, px)
gui()

print("textures generated:")
for d in ("block", "item", "gui"):
    p = os.path.join(OUT, d)
    print(f"  {d}: {len(os.listdir(p))} files")
