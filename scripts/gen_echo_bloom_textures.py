#!/usr/bin/env python3
"""Generates the 16x16 source sprites for the Echo Bloom resource crop:
  block/echo_bloom_stage0..3  (crop-cross sprites, growing teal glow)
  item/echo_bloom_seeds       (three teal-tipped seeds)
  item/echo_essence           (crystallised teal shard)
Style matches the mod's deep-resonance palette (deep dark ground, patinated
bronze stems, teal glow). Run from the repo root, then run
scripts/textures_32x.py to produce the 32x textures that ship in the jar.
"""
import struct, zlib

def write_png(path, w, h, px):
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        for x in range(w):
            raw += bytes(px[y*w+x])
    def chunk(t, d):
        return struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t+d) & 0xffffffff)
    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n")
        f.write(chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)))
        f.write(chunk(b"IDAT", zlib.compress(bytes(raw), 9)))
        f.write(chunk(b"IEND", b""))

CLEAR = (0, 0, 0, 0)
STEM_D = (52, 40, 20); STEM = (110, 82, 40); STEM_L = (160, 124, 66)
TEAL_DD = (16, 82, 78); TEAL_D = (32, 150, 140); TEAL = (86, 226, 212); TEAL_L = (190, 250, 244)

W = H = 16
def canvas():
    return [CLEAR] * (W*H)
def S(px, x, y, c):
    if 0 <= x < W and 0 <= y < H:
        px[y*W+x] = c + (255,)

def stem(px, x, y0, y1):
    for y in range(y0, y1 + 1):
        S(px, x, y, STEM)

def leaf(px, x, y, dx):
    S(px, x, y, STEM_L); S(px, x + dx, y, STEM); S(px, x + dx, y - 1, STEM_D)

def bud(px, cx, cy, r, glow):
    for dy in range(-r, r + 1):
        for dx in range(-r, r + 1):
            if dx*dx + dy*dy <= r*r:
                S(px, cx+dx, cy+dy, glow)
    S(px, cx, cy, TEAL_L)

# ---- stage 0: two sprouts
px = canvas()
stem(px, 4, 12, 15); leaf(px, 5, 13, 1)
stem(px, 11, 13, 15); leaf(px, 10, 14, -1)
write_png("src/main/resources/assets/echoes/textures/block/echo_bloom_stage0.png", W, H, px)

# ---- stage 1: sprouts with small buds
px = canvas()
stem(px, 4, 8, 15); leaf(px, 5, 11, 1); bud(px, 4, 7, 1, TEAL_DD)
stem(px, 11, 10, 15); leaf(px, 10, 12, -1); bud(px, 11, 9, 1, TEAL_DD)
write_png("src/main/resources/assets/echoes/textures/block/echo_bloom_stage1.png", W, H, px)

# ---- stage 2: taller, brighter buds
px = canvas()
stem(px, 4, 6, 15); leaf(px, 5, 9, 1); bud(px, 4, 5, 2, TEAL_D)
stem(px, 11, 8, 15); leaf(px, 10, 10, -1); bud(px, 11, 7, 2, TEAL_D)
write_png("src/main/resources/assets/echoes/textures/block/echo_bloom_stage2.png", W, H, px)

# ---- stage 3: mature — full teal blooms with bright cores
px = canvas()
stem(px, 4, 5, 15); leaf(px, 5, 9, 1); bud(px, 4, 3, 2, TEAL)
stem(px, 11, 7, 15); leaf(px, 10, 10, -1); bud(px, 11, 5, 2, TEAL)
# sparkles around mature blooms
S(px, 1, 2, TEAL_L); S(px, 7, 4, TEAL_L); S(px, 9, 3, TEAL_L); S(px, 14, 6, TEAL_L)
write_png("src/main/resources/assets/echoes/textures/block/echo_bloom_stage3.png", W, H, px)

# ---- seeds item: three teardrop seeds, dark hull with teal tip
px = canvas()
for (cx, cy) in ((4, 6), (8, 10), (12, 5)):
    S(px, cx, cy-2, TEAL)                      # tip
    S(px, cx-1, cy-1, STEM_D); S(px, cx, cy-1, STEM); S(px, cx+1, cy-1, STEM_D)
    S(px, cx-1, cy, STEM_D);   S(px, cx, cy, STEM_L); S(px, cx+1, cy, STEM_D)
    S(px, cx, cy+1, STEM_D)
write_png("src/main/resources/assets/echoes/textures/item/echo_bloom_seeds.png", W, H, px)

# ---- essence item: faceted teal crystal shard
px = canvas()
shard = [
    (8, 2, TEAL_L), (7, 3, TEAL_L), (8, 3, TEAL), (9, 3, TEAL_L),
    (6, 4, TEAL), (7, 4, TEAL), (8, 4, TEAL_L), (9, 4, TEAL), (10, 4, TEAL_D),
    (6, 5, TEAL), (7, 5, TEAL_L), (8, 5, TEAL), (9, 5, TEAL), (10, 5, TEAL_D),
    (5, 6, TEAL_D), (6, 6, TEAL), (7, 6, TEAL), (8, 6, TEAL), (9, 6, TEAL_D), (10, 6, TEAL_D),
    (5, 7, TEAL_D), (6, 7, TEAL), (7, 7, TEAL), (8, 7, TEAL_D), (9, 7, TEAL_D),
    (6, 8, TEAL_D), (7, 8, TEAL), (8, 8, TEAL_D),
    (6, 9, TEAL_DD), (7, 9, TEAL_D),
    (7, 10, TEAL_DD),
]
for (x, y, c) in shard:
    S(px, x, y, c)
S(px, 3, 4, TEAL_L); S(px, 12, 9, TEAL_L)  # glints
write_png("src/main/resources/assets/echoes/textures/item/echo_essence.png", W, H, px)

print("echo bloom textures written")
