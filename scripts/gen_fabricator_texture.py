#!/usr/bin/env python3
"""Generates the 16x16 source sprite for the Resonant Fabricator front face,
in the mod's deep-resonance style (patinated bronze bezel, deep-dark panel,
teal glow — here a 3x3 crafting-grid motif with a faint ripple echo).
Run from the repo root, then run scripts/textures_32x.py to produce the
32x texture that ships in the jar.
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

DEEP = (13, 22, 24); DEEP2 = (23, 38, 40)
BRONZE_D = (66, 44, 20); BRONZE = (140, 96, 44); BRONZE_L = (196, 150, 84)
TEAL = (86, 226, 212); TEAL_D = (32, 150, 140); TEAL_DD = (16, 82, 78)

W = H = 16
px = [DEEP + (255,)] * (W*H)
def S(x, y, c): px[y*W+x] = c + (255,)

for i in range(16):
    S(i, 0, BRONZE_L); S(i, 15, BRONZE_D); S(0, i, BRONZE_L); S(15, i, BRONZE_D)
S(0, 0, DEEP); S(15, 0, DEEP); S(0, 15, DEEP); S(15, 15, DEEP)
for i in range(1, 15):
    S(i, 1, BRONZE); S(i, 14, BRONZE_D); S(1, i, BRONZE); S(14, i, BRONZE_D)
for y in range(2, 14):
    for x in range(2, 14):
        S(x, y, DEEP2 if (x + y) % 2 == 0 else DEEP)
for gy in (3, 7, 11):
    for gx in (3, 7, 11):
        for dy in range(2):
            for dx in range(2):
                S(gx + dx, gy + dy, TEAL_D)
        S(gx, gy, TEAL)
for (x, y) in [(12, 12), (11, 13), (13, 11), (10, 12), (12, 10)]:
    S(x, y, TEAL_DD)

write_png("src/main/resources/assets/echoes/textures/block/fabricator.png", 16, 16, px)
print("fabricator.png (16x16) written — now run scripts/textures_32x.py")
