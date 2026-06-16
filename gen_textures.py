#!/usr/bin/env python3
"""Procedural 16x16 pixel-art for Echoes of the Deep — cohesive "deep resonance"
overhaul (no PIL).

Shared mystique (every sprite speaks the same language):
  * Deep-dark base — near-black blue-teal deepslate, faint sculk veins.
  * Patinated BRONZE bezels with verdigris in the crevices and rune-etched corners.
  * TEAL resonance light with a soft bloom halo; the recurring motif is concentric
    sound-wave RIPPLES emanating from a glowing core.
  * Accent energies: AMETHYST for the dimensional gear, AMBER for the percussive
    gear — used sparingly so teal stays the through-line.
  * Top-left light source, beveled edges, a gentle dark vignette so it all reads
    as something ancient pulled up "from the deep".
"""
import struct, zlib, os, math, json

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
    def over(self, x, y, c):
        if not (0 <= x < self.w and 0 <= y < self.h): return
        if len(c) == 3: c = (c[0], c[1], c[2], 255)
        r, g, b, a = c
        if a == 0: return
        br, bg, bb, ba = self.get(x, y)
        af = a / 255.0
        nr = int(r * af + br * (1 - af)); ng = int(g * af + bg * (1 - af)); nb = int(b * af + bb * (1 - af))
        self.set(x, y, (nr, ng, nb, max(a, ba)))
    def rect(self, x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, c)

# ---------------------------------------------------------------- prng / noise / math
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
def lum(c):
    return 0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]

# ---------------------------------------------------------------- palettes (dark -> light)
DEEP   = [(7, 13, 15), (14, 24, 26), (22, 36, 38), (33, 52, 54), (48, 70, 72)]
DEEPER = [(4, 8, 10), (9, 16, 18), (15, 26, 28), (23, 38, 40), (34, 52, 55)]
TEAL   = [(6, 34, 36), (16, 82, 78), (32, 150, 140), (86, 226, 212), (200, 255, 248)]
AMETH  = [(24, 14, 42), (54, 36, 88), (102, 70, 150), (160, 120, 214), (224, 198, 252)]
AMBER  = [(40, 24, 10), (92, 56, 24), (156, 98, 40), (214, 152, 66), (252, 216, 152)]
BRONZE = [(22, 19, 17), (48, 41, 33), (84, 71, 55), (128, 110, 84), (190, 172, 138)]
GREY   = [(18, 20, 24), (38, 42, 48), (68, 72, 80), (104, 108, 116), (152, 156, 164)]
BONE   = [(58, 56, 52), (94, 90, 82), (132, 128, 116), (170, 166, 152), (216, 212, 198)]
VERDI  = (40, 96, 88)
FACE   = (12, 19, 21)  # dark inset face behind machine motifs

OUT = "src/main/resources/assets/echoes/textures"
for d in ("block", "item", "gui"):
    os.makedirs(os.path.join(OUT, d), exist_ok=True)

# Animation: GLOW scales every emissive draw (bloom/ripple/core). Frame builders
# re-run with different GLOW values to make the resonance breathe.
GLOW = 1.0
PULSE = [1.0, 0.9, 0.74, 0.66, 0.74, 0.9]   # smooth loop, ~1.1s at frametime 3
FRAMETIME = 3

# ---------------------------------------------------------------- shared toolkit
def vignette(c, strength=46):
    cx, cy = 7.5, 7.5
    for y in range(c.h):
        for x in range(c.w):
            if c.get(x, y)[3] == 0: continue
            d = math.hypot(x - cx, y - cy) / 10.6
            if d > 0.5:
                c.over(x, y, (0, 0, 0, int(min(strength, (d - 0.5) * strength * 2.2))))

def ambient(c, alpha=26):
    for y in range(c.h):
        for x in range(c.w):
            if c.get(x, y)[3] and x + y < 7:
                c.over(x, y, (255, 255, 255, alpha))

def sculk_bg(c, seed, base=DEEP, accent=TEAL, veins=True):
    for y in range(16):
        for x in range(16):
            n = hash_noise(x, y, seed) * 0.7 + hash_noise(x // 2, y // 2, seed + 7) * 0.3
            idx = 0 if n < 0.22 else 1 if n < 0.5 else 2 if n < 0.8 else 3
            c.set(x, y, base[idx])
    nx = rng(seed + 51)
    if veins:
        for _ in range(7):  # glowing sculk flecks
            x, y = nx() % 16, nx() % 16
            c.over(x, y, (accent[2][0], accent[2][1], accent[2][2], 120))
            if nx() % 3 == 0:
                c.set(x, y, accent[3])
        for _ in range(3):  # verdigris in low crevices
            x, y = 1 + nx() % 14, 1 + nx() % 14
            c.over(x, y, (VERDI[0], VERDI[1], VERDI[2], 150))
    ambient(c)

def bloom(c, accent, alpha=72, reach=2, thresh=175):
    alpha = int(alpha * GLOW)
    a3 = accent[3]
    bright = [(x, y) for y in range(c.h) for x in range(c.w)
              if c.get(x, y)[3] > 0 and lum(c.get(x, y)) > thresh]
    for (x, y) in bright:
        for dy in range(-reach, reach + 1):
            for dx in range(-reach, reach + 1):
                dist = abs(dx) + abs(dy)
                if dist == 0 or dist > reach: continue
                nx, ny = x + dx, y + dy
                if 0 <= nx < c.w and 0 <= ny < c.h:
                    cur = c.get(nx, ny)
                    if cur[3] < 50 or lum(cur) < 80:
                        c.over(nx, ny, (a3[0], a3[1], a3[2], max(0, int(alpha / dist))))

def ripples(c, cx, cy, accent, rmax=6.0, x0=0, y0=0, x1=15, y1=15, alpha=235, phase=0):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if c.get(x, y)[3] == 0: continue
            d = math.hypot(x - cx, y - cy)
            if d > rmax: continue
            ring = int(round(d)) + phase
            t = max(0.0, 1 - d / rmax)
            g = 0.55 + 0.45 * GLOW
            if ring % 2 == 0:
                col = lerp(accent[1], accent[4], t)
                if (x - cx) + (y - cy) < -1: col = lerp(col, accent[4], 0.4)
                c.over(x, y, (col[0], col[1], col[2], int(alpha * (0.35 + 0.65 * t) * g)))
            else:
                col = lerp((10, 14, 16), accent[2], t * 0.5)
                c.over(x, y, (col[0], col[1], col[2], int(120 * t * g)))

def core(c, cx, cy, accent, r=1):
    body = lerp(accent[2], accent[4], GLOW)
    center = lerp(accent[3], (235, 255, 250), GLOW)
    for dy in range(-r, r + 1):
        for dx in range(-r, r + 1):
            if dx * dx + dy * dy <= r * r + 1:
                c.set(int(cx) + dx, int(cy) + dy, body)
    c.set(int(cx), int(cy), center)

def bezel(c, metal=BRONZE, accent=TEAL, t=2, runes=True):
    dark, mid, lite = metal[0], metal[2], metal[3]
    c.rect(0, 0, 15, 15, mid)
    for i in range(t):
        for x in range(16):
            c.set(x, i, lerp(lite, mid, i / t)); c.set(x, 15 - i, lerp(dark, mid, i / t))
        for y in range(16):
            c.set(i, y, lerp(lite, mid, i / t)); c.set(15 - i, y, lerp(dark, mid, i / t))
    c.set(0, 0, metal[4]); c.set(15, 15, dark)
    # verdigris weathering along the frame
    nx = rng(909)
    for _ in range(6):
        e = nx() % 4
        x = nx() % 16; y = nx() % 16
        if e == 0: y = 0
        elif e == 1: y = 15
        elif e == 2: x = 0
        else: x = 15
        c.over(x, y, (VERDI[0], VERDI[1], VERDI[2], 120))
    if runes:  # tiny etched accent rune at each corner
        for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
            c.over(rx, ry, (accent[3][0], accent[3][1], accent[3][2], 200))
            c.over(rx + 1, ry + 1, (0, 0, 0, 90))

def glyph(c, accent, pts, hi=None):
    """Etched accent rune: dark under-shadow then lit accent then highlights."""
    for (x, y) in pts:
        c.over(x, y + 1, (0, 0, 0, 110))
    for (x, y) in pts:
        c.set(x, y, accent[3])
    for (x, y) in (hi or []):
        c.set(x, y, accent[4])

def face_inset(c, color=FACE):
    c.rect(3, 3, 12, 12, color)

def solids(c, thresh=160):
    return [(x, y) for y in range(c.h) for x in range(c.w) if c.get(x, y)[3] >= thresh]
def outline(c, color=(0, 0, 0), alpha=170, thresh=160):
    for (x, y) in solids(c, thresh):
        for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            nx, ny = x + dx, y + dy
            if 0 <= nx < c.w and 0 <= ny < c.h and c.get(nx, ny)[3] < 10:
                c.over(nx, ny, (color[0], color[1], color[2], alpha))

def gem(c, cx, cy, ramp, r=2):
    for dy in range(-r - 1, r + 2):
        for dx in range(-r - 1, r + 2):
            if abs(dx) + abs(dy) == r + 1:
                c.over(cx + dx, cy + dy, (ramp[3][0], ramp[3][1], ramp[3][2], 70))
    for dy in range(-r, r + 1):
        for dx in range(-r, r + 1):
            if abs(dx) + abs(dy) <= r:
                col = ramp[3] if dx + dy < 0 else ramp[1] if dx + dy > 0 else ramp[2]
                c.set(cx + dx, cy + dy, col)
    c.set(cx, cy - r, ramp[4]); c.set(cx - 1, cy - 1, ramp[4])
    c.over(cx + r, cy, (ramp[0][0], ramp[0][1], ramp[0][2], 180))

# ================================================================ ORES
def ore(name, base, accent, seed, npts=3):
    c = C()
    sculk_bg(c, seed, base=base, accent=accent)
    pts = [(5, 6), (11, 10), (7, 12), (12, 4), (3, 11)]
    nx = rng(seed + 31)
    big = pts[0]
    for i in range(npts):
        px, py = pts[i]
        px = max(2, min(13, px + (nx() % 3 - 1))); py = max(2, min(13, py + (nx() % 3 - 1)))
        gem(c, px, py, accent, r=2 if i == 0 else (nx() % 2 + 1))
    ripples(c, big[0], big[1], accent, rmax=5.5, alpha=70, phase=1)  # faint resonance hum
    bloom(c, accent, alpha=55, reach=2, thresh=170)
    vignette(c, 40)
    write_png(f"{OUT}/block/{name}.png", 16, 16, c.px)

ore("echocite_ore", DEEP, TEAL, 101, 3)
ore("deepslate_echocite_ore", DEEPER, TEAL, 102, 3)
ore("drumstone_ore", DEEPER, AMBER, 103, 3)
ore("silentite_ore", DEEPER, AMETH, 104, 2)

# ================================================================ MACHINES / DEVICES
# Each "front" is a build_*() that returns a canvas, so it can be re-rendered at
# several GLOW levels into an animated strip (the resonance breathes).
def emitter_face(c, accent, rmax=5.4, phase=0, bg=FACE):
    """The archetypal resonance face: dark inset, concentric ripples, glowing core."""
    face_inset(c, bg)
    ripples(c, 7.5, 7.5, accent, rmax=rmax, x0=3, y0=3, x1=12, y1=12, phase=phase)
    core(c, 7, 7, accent, r=1)

def build_resonator():
    c = C(); bezel(c, BRONZE, TEAL)
    emitter_face(c, TEAL, rmax=5.6)
    bloom(c, TEAL, alpha=80); vignette(c, 30)
    return c

def build_resonant_relay():
    c = C(); bezel(c, BRONZE, TEAL)
    emitter_face(c, TEAL, rmax=5.4)
    for (x, y) in [(7, 3), (8, 12), (3, 8), (12, 7)]:  # four broadcast pips
        c.set(x, y, TEAL[4])
    bloom(c, TEAL, alpha=78); vignette(c, 30)
    return c

def build_resonant_amplifier():
    c = C(); bezel(c, BRONZE, AMBER)
    emitter_face(c, TEAL, rmax=5.4)            # teal core (resonance through-line)
    glyph(c, AMBER, [(4, 7), (5, 6), (4, 8), (10, 7), (11, 6), (10, 8)],
          hi=[(5, 6), (11, 6)])                # amber boost chevrons
    bloom(c, TEAL, alpha=70); bloom(c, AMBER, alpha=40, reach=1); vignette(c, 30)
    return c

def build_echo_repeater():
    c = C(); bezel(c, BRONZE, AMETH)
    face_inset(c)
    ripples(c, 7.5, 7.5, AMETH, rmax=5.4, x0=3, y0=3, x1=12, y1=12)  # dimensional swirl
    core(c, 7, 7, TEAL, r=1)                   # teal heart inside amethyst rings
    bloom(c, AMETH, alpha=70); bloom(c, TEAL, alpha=45, reach=1); vignette(c, 30)
    return c

def build_tuning_conduit():
    c = C(); bezel(c, BRONZE, TEAL, t=1)
    face_inset(c, (14, 22, 24))
    c.rect(7, 1, 8, 14, None)                  # rune channel carrying resonance edge-to-edge
    for x in range(1, 15):
        col = lerp(TEAL[1], TEAL[3], 1 - abs(7.5 - x) / 7.5)
        c.over(x, 7, col); c.over(x, 8, lerp(col, TEAL[1], 0.4))
    for y in range(1, 15):
        col = lerp(TEAL[1], TEAL[3], 1 - abs(7.5 - y) / 7.5)
        c.over(7, y, col); c.over(8, y, lerp(col, TEAL[1], 0.4))
    core(c, 7, 7, TEAL, r=1)
    bloom(c, TEAL, alpha=70); vignette(c, 28)
    return c

def build_harmonic_filter():
    c = C(); bezel(c, BRONZE, TEAL)
    face_inset(c)
    for x in range(3, 13):                     # engraved sieve lattice
        for y in range(3, 13):
            if x % 2 == 0 or y % 2 == 0:
                c.over(x, y, (BRONZE[2][0], BRONZE[2][1], BRONZE[2][2], 150))
    for x in range(4, 13, 3):                  # glowing filter nodes
        for y in range(4, 13, 3):
            c.set(x, y, TEAL[3]); c.over(x, y - 1, (TEAL[2][0], TEAL[2][1], TEAL[2][2], 120))
    bloom(c, TEAL, alpha=55, reach=1); vignette(c, 30)
    return c

def build_resonant_splitter():
    c = C(); bezel(c, BRONZE, TEAL)
    face_inset(c)
    for x in range(3, 8):                       # one shaft in
        c.set(x, 7, TEAL[2]); c.set(x, 8, TEAL[2])
    for k in range(4):                          # forking to two
        c.set(8 + k, 7 - k, TEAL[3]); c.set(8 + k, 8 + k, TEAL[3])
    for (x, y) in [(3, 7), (11, 3), (11, 12)]:
        c.set(x, y, TEAL[4])
    bloom(c, TEAL, alpha=60); vignette(c, 30)
    return c

def build_conduit_coupler():
    c = C(); bezel(c, BRONZE, TEAL)
    face_inset(c)
    ripples(c, 4.5, 7.5, TEAL, rmax=4.2, x0=3, y0=3, x1=7, y1=12)   # wireless half
    c.rect(9, 3, 12, 12, BRONZE[1])                                  # wired plate
    for y in range(3, 13): c.over(10, y, TEAL[2])
    for x in range(9, 13): c.over(x, 7, TEAL[3]); c.over(x, 8, TEAL[2])
    c.over(8, 3, (0, 0, 0, 120)); c.over(8, 12, (0, 0, 0, 120))      # seam
    core(c, 4, 7, TEAL, r=0); c.set(10, 7, TEAL[4])
    bloom(c, TEAL, alpha=62); vignette(c, 30)
    return c

def build_note_relay():
    c = C(); bezel(c, BRONZE, TEAL)
    face_inset(c)
    c.rect(8, 4, 8, 11, TEAL[2])                # stem
    for (x, y) in [(5, 10), (6, 10), (7, 10), (5, 11), (6, 11), (7, 11), (5, 12), (6, 12), (7, 12)]:
        c.set(x, y, TEAL[3])                    # note head
    c.set(9, 4, TEAL[3]); c.set(10, 5, TEAL[3]); c.set(10, 6, TEAL[3]); c.set(9, 6, TEAL[3])  # flag
    c.set(6, 10, TEAL[4]); c.set(8, 4, TEAL[4])
    bloom(c, TEAL, alpha=62, reach=2); vignette(c, 30)
    return c

def build_resonant_chest():
    c = C()
    c.rect(0, 0, 15, 15, BRONZE[1])             # ancient dark casket body, thin bronze rim
    c.rect(1, 1, 14, 14, (16, 24, 26))
    for x in range(1, 15):
        c.set(x, 1, BRONZE[3]); c.set(x, 14, BRONZE[0])
    for y in range(1, 15):
        c.set(1, y, BRONZE[2]); c.set(14, y, BRONZE[0])
    c.rect(1, 2, 14, 4, (22, 32, 34))           # lid quarter + bronze seam band
    c.rect(1, 5, 14, 5, BRONZE[3]); c.rect(1, 6, 14, 6, BRONZE[1])
    c.rect(3, 2, 3, 13, BRONZE[2]); c.rect(12, 2, 12, 13, BRONZE[1])   # corner straps
    c.rect(6, 4, 9, 9, BRONZE[3])               # central rune lock plate
    c.set(6, 4, BRONZE[4]); c.rect(6, 9, 9, 9, BRONZE[0])
    c.set(7, 6, TEAL[4]); c.set(8, 7, TEAL[3]); c.set(8, 6, TEAL[3]); c.set(7, 7, TEAL[3])
    c.over(7, 8, (TEAL[2][0], TEAL[2][1], TEAL[2][2], 150))
    for x in (2, 13): c.over(x, 5, (TEAL[3][0], TEAL[3][1], TEAL[3][2], 120))
    bloom(c, TEAL, alpha=55, reach=2, thresh=170); vignette(c, 32)
    return c

def build_crusher():
    c = C(); bezel(c, BRONZE, TEAL)
    c.rect(2, 2, 13, 13, (16, 22, 24))
    for y in range(2, 5):                        # hopper maw
        c.rect(3 + (y - 2), y, 12 - (y - 2), y, (10, 14, 16))
    for x in range(3, 13):                        # interlocking teeth
        h = 2 if x % 2 == 0 else 3
        for k in range(h): c.set(x, 6 + k, BONE[3 if k == 0 else 2])
        c.set(x, 6, BONE[4])
        h2 = 3 if x % 2 == 0 else 2
        for k in range(h2): c.set(x, 11 - k, BONE[2 if k == 0 else 1])
    for x in range(3, 13):                        # teal grind glow
        c.over(x, 9, (TEAL[3][0], TEAL[3][1], TEAL[3][2], 165))
        c.over(x, 8, (TEAL[2][0], TEAL[2][1], TEAL[2][2], 95))
        c.over(x, 10, (TEAL[2][0], TEAL[2][1], TEAL[2][2], 80))
    bloom(c, TEAL, alpha=58, reach=1); vignette(c, 30)
    return c

# Shared bronze casing — the SIDE and TOP/BOTTOM of every machine, so the whole
# family reads as one material; only the glowing front differs.
def device_side():
    c = C()
    for y in range(16):
        for x in range(16):
            n = hash_noise(x, y, 711)
            c.set(x, y, BRONZE[1] if n < 0.42 else BRONZE[2] if n < 0.85 else BRONZE[3])
    for x in range(16): c.set(x, 0, BRONZE[3]); c.set(x, 15, BRONZE[0])
    for y in range(16): c.set(0, y, BRONZE[2]); c.set(15, y, BRONZE[0])
    for y in range(2, 14): c.over(4, y, (0, 0, 0, 70)); c.over(11, y, (0, 0, 0, 70))  # panel seams
    for y in range(3, 13):                       # faint inner teal vein
        c.over(7, y, (TEAL[1][0], TEAL[1][1], TEAL[1][2], 70))
        c.over(8, y, (TEAL[1][0], TEAL[1][1], TEAL[1][2], 50))
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        c.set(rx, ry, BRONZE[4]); c.over(rx + 1, ry + 1, (0, 0, 0, 90))
    nx = rng(712)
    for _ in range(3): c.over(1 + nx() % 14, 1 + nx() % 14, (VERDI[0], VERDI[1], VERDI[2], 120))
    vignette(c, 26)
    write_png(f"{OUT}/block/device_side.png", 16, 16, c.px)
device_side()

def device_top():
    c = C()
    for y in range(16):
        for x in range(16):
            n = hash_noise(x, y, 733)
            c.set(x, y, BRONZE[1] if n < 0.42 else BRONZE[2] if n < 0.85 else BRONZE[3])
    for x in range(16): c.set(x, 0, BRONZE[3]); c.set(x, 15, BRONZE[0])
    for y in range(16): c.set(0, y, BRONZE[2]); c.set(15, y, BRONZE[0])
    for y in range(16):                          # centred resonance ring stamp
        for x in range(16):
            if abs(math.hypot(x - 7.5, y - 7.5) - 4) < 0.6:
                c.over(x, y, (TEAL[2][0], TEAL[2][1], TEAL[2][2], 70))
    c.over(7, 7, (TEAL[3][0], TEAL[3][1], TEAL[3][2], 130)); c.over(8, 8, (TEAL[2][0], TEAL[2][1], TEAL[2][2], 90))
    for (rx, ry) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        c.set(rx, ry, BRONZE[4]); c.over(rx + 1, ry + 1, (0, 0, 0, 90))
    vignette(c, 26)
    write_png(f"{OUT}/block/device_top.png", 16, 16, c.px)
device_top()

# Render the breathing fronts as vertical animation strips + .mcmeta.
ANIMATED = {
    "resonator": build_resonator, "resonant_relay": build_resonant_relay,
    "resonant_amplifier": build_resonant_amplifier, "echo_repeater": build_echo_repeater,
    "tuning_conduit": build_tuning_conduit, "harmonic_filter": build_harmonic_filter,
    "resonant_splitter": build_resonant_splitter, "conduit_coupler": build_conduit_coupler,
    "note_relay": build_note_relay, "resonant_chest": build_resonant_chest, "crusher": build_crusher,
}
def emit_block(name, builder):
    global GLOW
    n = len(PULSE)
    strip = [(0, 0, 0, 0)] * (16 * 16 * n)
    for i, g in enumerate(PULSE):
        GLOW = g
        c = builder()
        for y in range(16):
            for x in range(16):
                strip[(i * 16 + y) * 16 + x] = c.get(x, y)
    GLOW = 1.0
    write_png(f"{OUT}/block/{name}.png", 16, 16 * n, strip)
    with open(f"{OUT}/block/{name}.png.mcmeta", "w") as f:
        json.dump({"animation": {"frametime": FRAMETIME}}, f)
        f.write("\n")
for _nm, _fn in ANIMATED.items():
    emit_block(_nm, _fn)

# ================================================================ ITEMS
def chunk_item(name, rock, accent, seed):
    c = C(); nx = rng(seed); cx, cy = 8, 8
    for y in range(2, 15):
        for x in range(2, 15):
            d = math.hypot(x - cx, y - cy) + hash_noise(x, y, seed) * 2.2 - 1.0
            if d <= 5.4:
                n = hash_noise(x, y, seed + 3)
                idx = 1 if n < 0.34 else 2 if n < 0.72 else 3
                if (x - cx) + (y - cy) < -3: idx = min(4, idx + 1)
                if (x - cx) + (y - cy) > 4: idx = max(0, idx - 1)
                c.set(x, y, rock[idx])
    for (gx, gy) in [(6, 6), (10, 9), (8, 11)]:    # accent veins
        gx2 = gx + (nx() % 3 - 1); gy2 = gy + (nx() % 3 - 1)
        c.set(gx2, gy2, accent[3]); c.set(gx2, gy2 - 1, accent[4])
        c.over(gx2 + 1, gy2, (accent[2][0], accent[2][1], accent[2][2], 200))
    bloom(c, accent, alpha=55, reach=2, thresh=150)
    outline(c)
    write_png(f"{OUT}/item/{name}.png", 16, 16, c.px)
chunk_item("raw_echocite", DEEP, TEAL, 201)
chunk_item("resonant_slag", GREY, TEAL, 202)

def crystal_item(name, ramp):
    c = C()
    shape = [(8,1),(7,2),(8,2),(9,2),(6,3),(7,3),(8,3),(9,3),(10,3),
             (5,4),(6,4),(7,4),(8,4),(9,4),(10,4),(11,4),
             (5,5),(6,5),(7,5),(8,5),(9,5),(10,5),(11,5),
             (6,6),(7,6),(8,6),(9,6),(10,6),(6,7),(7,7),(8,7),(9,7),(10,7),
             (7,8),(8,8),(9,8),(7,9),(8,9),(9,9),(8,10),(8,11)]
    cx = 8
    for (x, y) in shape:
        col = ramp[3] if x < cx - 1 else ramp[1] if x > cx + 1 else ramp[2]
        c.set(x, y, col)
    for y in range(2, 9): c.set(cx, y, ramp[4])
    c.set(7, 3, ramp[4]); c.set(6, 4, (235, 255, 250)); c.set(8, 1, ramp[4])
    bloom(c, ramp, alpha=55, reach=2, thresh=150)
    outline(c, thresh=150)
    write_png(f"{OUT}/item/{name}.png", 16, 16, c.px)
crystal_item("silentite_crystal", AMETH)

def shard_item(name, ramp):
    c = C()
    rows = {3:(10,12),4:(9,12),5:(8,12),6:(7,11),7:(6,10),8:(5,10),9:(5,9),10:(4,8),11:(4,7),12:(5,6)}
    for y, (x0, x1) in rows.items():
        for x in range(x0, x1 + 1):
            f = (x - x0) - (x1 - x)
            c.set(x, y, ramp[3] if f < -1 else ramp[1] if f > 1 else ramp[2])
    for (x, y) in [(11,4),(10,5),(9,6),(8,7),(7,8),(6,9)]: c.set(x, y, ramp[4])
    c.set(12, 3, ramp[4]); c.set(10, 4, (235, 255, 250))
    bloom(c, ramp, alpha=50, reach=2, thresh=150)
    outline(c, thresh=150)
    write_png(f"{OUT}/item/{name}.png", 16, 16, c.px)
shard_item("drumstone_shard", AMBER)

def ingot(name, ramp, glow_accent=None):
    c = C(); top, bot = 5, 11
    for y in range(top, bot + 1):
        off = (y - top); x0 = 2 + off // 2; x1 = 12 + off // 2
        for x in range(x0, x1 + 1):
            if y == top: col = ramp[4]
            elif x == x0: col = ramp[3]
            elif x == x1 or y == bot: col = ramp[1]
            else: col = ramp[2]
            c.set(x, y, col)
    for x in range(2, 13): c.set(x, top - 1, ramp[3])
    c.rect(3, 4, 12, 4, ramp[4]); c.set(4, 4, (240, 248, 245))
    if glow_accent:                              # etched resonance rune on the face
        for (x, y) in [(6, 7), (7, 8), (8, 7), (9, 8)]:
            c.over(x, y, (glow_accent[3][0], glow_accent[3][1], glow_accent[3][2], 200))
        bloom(c, glow_accent, alpha=45, reach=1, thresh=150)
    for x in range(2, 14): c.over(x + 3, bot + 1, (0, 0, 0, 70))
    write_png(f"{OUT}/item/{name}.png", 16, 16, c.px)
ingot("echo_ingot", BONE, glow_accent=TEAL)
ingot("dull_ingot", GREY)                        # intentionally inert (no glow)

def dust_item(name, ramp, seed):
    c = C(); nx = rng(seed)
    heights = [0,1,2,3,4,5,5,6,6,5,5,4,3,2,1,0]; base = 13
    for x in range(16):
        for k in range(heights[x] + (nx() % 2)):
            y = base - k; n = hash_noise(x, y, seed)
            idx = 1 if n < 0.3 else 2 if n < 0.62 else 3 if n < 0.85 else 4
            c.set(x, y, ramp[idx])
    for _ in range(8):
        x = 2 + nx() % 12; y = 4 + nx() % 6
        c.over(x, y, (ramp[3][0], ramp[3][1], ramp[3][2], 230))
    for _ in range(4):
        c.set(2 + nx() % 12, base - (nx() % 5), ramp[4])
    bloom(c, ramp, alpha=50, reach=2, thresh=170)
    for x in range(16):
        if c.get(x, base)[3] > 0: c.over(x, base + 1, (0, 0, 0, 90))
    write_png(f"{OUT}/item/{name}.png", 16, 16, c.px)
dust_item("echocite_dust", TEAL, 221)
dust_item("echo_dust", TEAL, 222)

def drum_core():
    c = C(); cx, cy = 8, 8
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - cx, y - cy)
            if d <= 6.3:
                ring = int(d)
                if ring >= 6: col = BRONZE[1]
                elif ring == 5: col = BRONZE[3]
                elif ring == 4: col = BRONZE[2]
                elif ring == 3: col = AMBER[2]
                elif ring == 2: col = AMBER[3]
                elif ring == 1: col = AMBER[3]
                else: col = AMBER[4]
                if (x - cx) + (y - cy) < -3: col = lerp(col, (250, 240, 220), 0.25)
                if (x - cx) + (y - cy) > 4: col = shade(col, 0.7)
                c.set(x, y, col)
    c.set(7, 7, AMBER[4])
    for ang in range(0, 360, 90):
        c.set(int(cx + 5.4 * math.cos(math.radians(ang))), int(cy + 5.4 * math.sin(math.radians(ang))), BRONZE[4])
    bloom(c, AMBER, alpha=45, reach=1, thresh=170)
    outline(c)
    write_png(f"{OUT}/item/drum_core.png", 16, 16, c.px)
drum_core()

def frequency_tuner():
    c = C(); r = BRONZE
    c.rect(5, 2, 5, 8, r[3]); c.rect(10, 2, 10, 8, r[3])   # prongs
    c.rect(5, 8, 10, 9, r[2])                              # bridge
    c.rect(7, 9, 8, 14, r[3])                              # handle
    c.set(5, 2, TEAL[4]); c.set(10, 2, TEAL[4])            # resonating tips
    c.over(5, 3, (TEAL[3][0], TEAL[3][1], TEAL[3][2], 200))
    c.over(10, 3, (TEAL[3][0], TEAL[3][1], TEAL[3][2], 200))
    c.set(7, 11, TEAL[3])                                  # rune on the grip
    bloom(c, TEAL, alpha=55, reach=2, thresh=150)
    outline(c, thresh=150)
    write_png(f"{OUT}/item/frequency_tuner.png", 16, 16, c.px)
frequency_tuner()

def channel_atlas():
    c = C()
    c.rect(3, 2, 12, 14, DEEP[2])                          # dark tome cover
    c.rect(3, 2, 4, 14, DEEP[0])                           # spine
    for y in range(2, 15): c.over(4, y, (BRONZE[2][0], BRONZE[2][1], BRONZE[2][2], 160))
    c.rect(11, 3, 12, 13, BONE[3])                         # page edge
    c.set(7, 7, TEAL[4]); c.set(8, 8, TEAL[3]); c.set(8, 7, TEAL[3]); c.set(7, 8, TEAL[3])  # rune sigil
    c.set(6, 6, TEAL[2]); c.set(9, 9, TEAL[2])
    bloom(c, TEAL, alpha=50, reach=2, thresh=150)
    outline(c, thresh=150)
    write_png(f"{OUT}/item/channel_atlas.png", 16, 16, c.px)
channel_atlas()

# ================================================================ CRUSHER GUI (256x256) — deep restyle
def gui():
    W = H = 256
    px = [(0, 0, 0, 0)] * (W * H)
    def s(x, y, c):
        if 0 <= x < W and 0 <= y < H: px[y * W + x] = (c[0], c[1], c[2], 255) if len(c) == 3 else c
    def rect(x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1): s(x, y, c)
    PANEL = (26, 32, 34); LITE = (58, 74, 74); DARK = (10, 14, 16); MID = (18, 24, 26); SDARK = (8, 11, 12)
    rect(0, 0, 175, 165, PANEL)
    rect(0, 0, 175, 2, LITE); rect(0, 0, 2, 165, LITE)
    rect(0, 163, 175, 165, DARK); rect(173, 0, 175, 165, DARK)
    def slot(ix, iy):
        rect(ix - 1, iy - 1, ix + 16, iy + 16, MID)
        rect(ix - 1, iy - 1, ix + 16, iy - 1, SDARK); rect(ix - 1, iy - 1, ix - 1, iy + 16, SDARK)
        rect(ix - 1, iy + 16, ix + 16, iy + 16, LITE); rect(ix + 16, iy - 1, ix + 16, iy + 16, LITE)
    slot(56, 35); slot(116, 35)
    for r in range(3):
        for col in range(9): slot(8 + col * 18, 84 + r * 18)
    for col in range(9): slot(8 + col * 18, 142)
    def arrow(ox, oy, fill):
        for yy in range(oy + 5, oy + 11):
            for xx in range(ox, ox + 15): s(xx, yy, fill)
        for k in range(8):
            for yy in range(oy + 2 + k, oy + 14 - k): s(ox + 14 + k, yy, fill)
    arrow(79, 32, SDARK)
    rect(20, 20, 28, 52, SDARK); rect(21, 21, 27, 51, (10, 16, 18))
    for yy in range(40, 51):
        for xx in range(21, 28): s(xx, yy, lerp(TEAL[1], TEAL[3], (51 - yy) / 11))
    def sprite_arrow(ox, oy):
        for yy in range(oy + 5, oy + 11):
            for xx in range(ox, ox + 15): s(xx, yy, lerp(TEAL[3], TEAL[1], (yy - (oy + 5)) / 5))
        for k in range(8):
            for yy in range(oy + 2 + k, oy + 14 - k): s(ox + 14 + k, yy, lerp(TEAL[3], TEAL[2], k / 8))
        for xx in range(ox, ox + 15): s(xx, oy + 5, TEAL[4])
    sprite_arrow(176, 0)
    write_png(f"{OUT}/gui/crusher.png", W, H, px)
gui()

print("textures generated:")
for d in ("block", "item", "gui"):
    p = os.path.join(OUT, d)
    print(f"  {d}: {len(os.listdir(p))} files")
