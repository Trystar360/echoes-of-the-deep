#!/usr/bin/env python3
"""Generates the 16x16 source sprites for the Abyssal Bloom (tier-3 crop):
  block/abyssal_bloom_stage0..3  (crop-cross sprites, deep-indigo void glow)
  item/abyssal_bloom_seeds       (three dark seeds with violet tips)
  item/abyssal_essence           (near-black crystal with a violet-teal core)
Palette: the same bronze stems as the lower tiers, but the glow is pulled down
into the deep — indigo petals with a faint teal ember, a bloom that drinks the
dark (it emits no light). Run from the repo root, then scripts/textures_32x.py
to produce the 32x textures for the jar.
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
STEM_D = (46, 36, 22); STEM = (96, 74, 40); STEM_L = (138, 110, 62)
INDIGO_DD = (16, 10, 38); INDIGO_D = (38, 22, 84); INDIGO = (74, 44, 148); INDIGO_L = (130, 92, 220)
EMBER = (58, 168, 158)  # dim teal ember (abyssal teal is muted, not bright)

W = H = 16
def canvas(): return [CLEAR] * (W*H)
def S(px, x, y, c):
    if 0 <= x < W and 0 <= y < H:
        px[y*W+x] = c + (255,)

def stem(px, x, y0, y1):
    for y in range(y0, y1 + 1):
        S(px, x, y, STEM)

def stage(px, buds, bloom=False):
    # two stems from the bottom
    stem(px, 4, 15 - 6, 15); stem(px, 11, 15 - 6, 15)
    S(px, 3, 14, STEM_D); S(px, 5, 13, STEM_L)
    S(px, 12, 14, STEM_D); S(px, 10, 13, STEM_L)
    for (bx, by, c) in buds:
        S(px, bx, by, c); S(px, bx + 1, by, c)
        S(px, bx, by + 1, c); S(px, bx + 1, by + 1, c)
    if bloom:
        # mature: a wide dark blossom at each stem top with an ember core
        for (cx, cy) in [(4, 4), (11, 4)]:
            for dx, dy in [(-1,0),(1,0),(0,-1),(0,1)]:
                S(px, cx + dx, cy + dy, INDIGO)
            S(px, cx, cy, EMBER)
            S(px, cx - 1, cy - 1, INDIGO_D); S(px, cx + 1, cy + 1, INDIGO_D)

# stage 0: sprouts only
px = canvas()
stem(px, 4, 12, 15); stem(px, 11, 13, 15)
write_png("src/main/resources/assets/echoes/textures/block/abyssal_bloom_stage0.png", W, H, px)

# stage 1: small dark buds
px = canvas()
stage(px, [(4, 8, INDIGO_D), (11, 9, INDIGO_D)])
write_png("src/main/resources/assets/echoes/textures/block/abyssal_bloom_stage1.png", W, H, px)

# stage 2: half-open indigo buds
px = canvas()
stage(px, [(4, 6, INDIGO), (11, 7, INDIGO)])
write_png("src/main/resources/assets/echoes/textures/block/abyssal_bloom_stage2.png", W, H, px)

# stage 3: full dark blossom, ember cores
px = canvas()
stage(px, [(4, 6, INDIGO), (11, 7, INDIGO)], bloom=True)
write_png("src/main/resources/assets/echoes/textures/block/abyssal_bloom_stage3.png", W, H, px)

# seeds: three dark seeds, violet tips
px = canvas()
for (sx, sy) in [(3, 10), (7, 12), (11, 9)]:
    S(px, sx, sy, STEM); S(px, sx + 1, sy, STEM)
    S(px, sx, sy + 1, STEM_D); S(px, sx + 1, sy + 1, STEM_D)
    S(px, sx, sy - 1, INDIGO_L); S(px, sx + 1, sy - 1, INDIGO)
write_png("src/main/resources/assets/echoes/textures/item/abyssal_bloom_seeds.png", W, H, px)

# essence: near-black crystal shard with a violet rim and dim teal core
px = canvas()
for y in range(2, 14):
    half = 2 if y < 4 else (4 if y < 11 else 3)
    for x in range(8 - half, 8 + half + 1):
        S(px, x, y, INDIGO_D)
for y in range(4, 11):
    for x in range(8 - 3, 8 + 3 + 1):
        S(px, x, y, INDIGO_DD)
# violet rim
for y in range(3, 12):
    S(px, 8 - 4, y, INDIGO); S(px, 8 + 4, y, INDIGO)
S(px, 8, 2, INDIGO_L); S(px, 8, 13, INDIGO)
# dim teal core
for (x, y) in [(8, 6), (7, 7), (8, 7), (9, 7), (8, 8)]:
    S(px, x, y, EMBER)
write_png("src/main/resources/assets/echoes/textures/item/abyssal_essence.png", W, H, px)

print("wrote abyssal bloom stages 0-3, seeds, essence (16x16 sources)")
