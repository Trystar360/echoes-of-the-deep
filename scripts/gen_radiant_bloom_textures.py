#!/usr/bin/env python3
"""Generates the 16x16 source sprites for the Radiant Bloom (tier-2 crop):
  block/radiant_bloom_stage0..3  (crop-cross sprites, gold-white glow)
  item/radiant_bloom_seeds       (three gold-tipped seeds)
  item/radiant_essence           (brilliant gold-teal crystal)
Palette: same deep-resonance bronze stems as the Echo Bloom, but the glow is
wound a full octave higher — warm gold with teal cores. Run from the repo
root, then scripts/textures_32x.py to produce the 32x textures for the jar.
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
GOLD_DD = (94, 66, 14); GOLD_D = (176, 132, 32); GOLD = (240, 196, 64); GOLD_L = (255, 240, 170)
TEAL = (86, 226, 212)

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

def bud(px, cx, cy, r, glow, core):
    for dy in range(-r, r + 1):
        for dx in range(-r, r + 1):
            if dx*dx + dy*dy <= r*r:
                S(px, cx+dx, cy+dy, glow)
    S(px, cx, cy, core)

# ---- stage 0: two sprouts
px = canvas()
stem(px, 4, 12, 15); leaf(px, 5, 13, 1)
stem(px, 11, 13, 15); leaf(px, 10, 14, -1)
write_png("src/main/resources/assets/echoes/textures/block/radiant_bloom_stage0.png", W, H, px)

# ---- stage 1: sprouts with small gold buds
px = canvas()
stem(px, 4, 8, 15); leaf(px, 5, 11, 1); bud(px, 4, 7, 1, GOLD_DD, GOLD_D)
stem(px, 11, 10, 15); leaf(px, 10, 12, -1); bud(px, 11, 9, 1, GOLD_DD, GOLD_D)
write_png("src/main/resources/assets/echoes/textures/block/radiant_bloom_stage1.png", W, H, px)

# ---- stage 2: taller, brighter buds
px = canvas()
stem(px, 4, 6, 15); leaf(px, 5, 9, 1); bud(px, 4, 5, 2, GOLD_D, GOLD)
stem(px, 11, 8, 15); leaf(px, 10, 10, -1); bud(px, 11, 7, 2, GOLD_D, GOLD)
write_png("src/main/resources/assets/echoes/textures/block/radiant_bloom_stage2.png", W, H, px)

# ---- stage 3: mature — full radiant blooms, gold petals with teal cores
px = canvas()
stem(px, 4, 5, 15); leaf(px, 5, 9, 1); bud(px, 4, 3, 2, GOLD, TEAL)
stem(px, 11, 7, 15); leaf(px, 10, 10, -1); bud(px, 11, 5, 2, GOLD, TEAL)
S(px, 1, 2, GOLD_L); S(px, 7, 4, GOLD_L); S(px, 9, 3, GOLD_L); S(px, 14, 6, GOLD_L)
write_png("src/main/resources/assets/echoes/textures/block/radiant_bloom_stage3.png", W, H, px)

# ---- seeds item: three teardrop seeds, dark hull with gold tip
px = canvas()
for (cx, cy) in ((4, 6), (8, 10), (12, 5)):
    S(px, cx, cy-2, GOLD)                      # tip
    S(px, cx-1, cy-1, STEM_D); S(px, cx, cy-1, STEM); S(px, cx+1, cy-1, STEM_D)
    S(px, cx-1, cy, STEM_D);   S(px, cx, cy, STEM_L); S(px, cx+1, cy, STEM_D)
    S(px, cx, cy+1, STEM_D)
write_png("src/main/resources/assets/echoes/textures/item/radiant_bloom_seeds.png", W, H, px)

# ---- essence item: brilliant gold crystal with teal heart
px = canvas()
shard = [
    (8, 2, GOLD_L), (7, 3, GOLD_L), (8, 3, GOLD), (9, 3, GOLD_L),
    (6, 4, GOLD), (7, 4, GOLD), (8, 4, GOLD_L), (9, 4, GOLD), (10, 4, GOLD_D),
    (6, 5, GOLD), (7, 5, GOLD_L), (8, 5, TEAL), (9, 5, GOLD), (10, 5, GOLD_D),
    (5, 6, GOLD_D), (6, 6, GOLD), (7, 6, TEAL), (8, 6, TEAL), (9, 6, GOLD_D), (10, 6, GOLD_D),
    (5, 7, GOLD_D), (6, 7, GOLD), (7, 7, GOLD), (8, 7, GOLD_D), (9, 7, GOLD_D),
    (6, 8, GOLD_D), (7, 8, GOLD), (8, 8, GOLD_D),
    (6, 9, GOLD_DD), (7, 9, GOLD_D),
    (7, 10, GOLD_DD),
]
for (x, y, c) in shard:
    S(px, x, y, c)
S(px, 3, 4, GOLD_L); S(px, 12, 9, GOLD_L)  # glints
write_png("src/main/resources/assets/echoes/textures/item/radiant_essence.png", W, H, px)

print("radiant bloom textures written")
