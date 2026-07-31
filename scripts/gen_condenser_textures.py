#!/usr/bin/env python3
"""Generates the 16x16 source sprites for the Resonant Condenser:
  block/resonant_condenser       (side: dark device plating with an alchemical
                                  ring band — matter cycling around the core)
  block/resonant_condenser_top   (top: the condenser's target lens — a violet
                                  transmutation iris ringed in bronze)
Palette matches the mod's device family (device_side/device_top) with the
transmutation line's violet accents (motes/table). Run from the repo root,
then scripts/textures_32x.py to produce the 32x textures for the jar.
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

W = H = 16
CLEAR = (0, 0, 0, 0)
def canvas(): return [CLEAR] * (W*H)
def S(px, x, y, c):
    if 0 <= x < W and 0 <= y < H:
        px[y*W+x] = c + (255,)

# device-family plating
PLATE_D  = (24, 22, 34)   # deep seam
PLATE    = (44, 42, 60)   # casing
PLATE_L  = (66, 63, 88)   # bevel light
BRONZE_D = (94, 70, 40)
BRONZE   = (150, 116, 64) # machine trim
# transmutation violet (mote family)
VIO_D  = (38, 22, 74)
VIO    = (108, 72, 198)
VIO_L  = (176, 140, 255)
TEAL   = (86, 226, 212)   # resonance glint

def side():
    px = canvas()
    for y in range(H):
        for x in range(W):
            c = PLATE
            if x == 0 or y == 0: c = PLATE_L
            if x == W-1 or y == H-1: c = PLATE_D
            S(px, x, y, c)
    # bronze corner rivets
    for (x, y) in ((1,1),(14,1),(1,14),(14,14)): S(px, x, y, BRONZE)
    # alchemical ring band across the middle (matter cycling)
    cy = 8
    for x in range(2, 14):
        S(px, x, cy-2, VIO_D); S(px, x, cy+2, VIO_D)
    for x in range(3, 13):
        S(px, x, cy-1, VIO); S(px, x, cy+1, VIO)
    for x in range(4, 12):
        S(px, x, cy, VIO_L)
    # circulating glints (the "flow" of dissolved matter)
    S(px, 4, cy-1, TEAL); S(px, 11, cy+1, TEAL); S(px, 7, cy, TEAL)
    return px

def top():
    px = canvas()
    for y in range(H):
        for x in range(W):
            c = PLATE
            if x == 0 or y == 0: c = PLATE_L
            if x == W-1 or y == H-1: c = PLATE_D
            S(px, x, y, c)
    # bronze octagonal frame
    for i in range(2, 14):
        S(px, i, 2, BRONZE_D); S(px, i, 13, BRONZE_D)
        S(px, 2, i, BRONZE_D); S(px, 13, i, BRONZE_D)
    for i in range(3, 13):
        S(px, i, 3, BRONZE); S(px, i, 12, BRONZE)
        S(px, 3, i, BRONZE); S(px, 12, i, BRONZE)
    # transmutation iris: concentric violet rings into a bright pupil
    cx = cy = 7.5
    for y in range(4, 12):
        for x in range(4, 12):
            d2 = (x-cx)**2 + (y-cy)**2
            if d2 <= 12.5 and d2 > 6.5: S(px, x, y, VIO_D)
            elif d2 <= 6.5 and d2 > 2.5: S(px, x, y, VIO)
            elif d2 <= 2.5: S(px, x, y, VIO_L)
    S(px, 7, 7, TEAL)  # resonance spark at the pupil
    return px

write_png("src/main/resources/assets/echoes/textures/block/resonant_condenser.png", W, H, side())
write_png("src/main/resources/assets/echoes/textures/block/resonant_condenser_top.png", W, H, top())
print("wrote block/resonant_condenser.png + block/resonant_condenser_top.png (16x sources)")
