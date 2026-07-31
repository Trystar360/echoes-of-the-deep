#!/usr/bin/env python3
"""Generates the 16x16 source sprites for the pattern cards:
  item/blank_pattern    (pale encoding card with an empty 3x3 grid etched on it)
  item/encoded_pattern  (same card, grid cells lit resonance teal — a saved layout)
Palette: warm parchment card, bronze edge, teal glow when encoded — matching the
mod's deep-resonance look. Run from the repo root, then scripts/textures_32x.py
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
CARD_D = (128, 104, 64)   # card edge (dark parchment-bronze)
CARD   = (214, 190, 142)  # card face (parchment)
CARD_L = (240, 224, 186)  # highlight
ETCH   = (92, 74, 44)     # etched grid lines
TEAL_D = (22, 94, 88)
TEAL   = (86, 226, 212)   # resonance teal (encoded cells)
TEAL_L = (190, 255, 248)

W = H = 16
def canvas(): return [CLEAR] * (W*H)
def S(px, x, y, c):
    if 0 <= x < W and 0 <= y < H:
        px[y*W+x] = c + (255,)

def card(px, cell_fill):
    # 12x12 card, slight tilt-free portrait, 2px margin
    x0, y0, x1, y1 = 2, 2, 13, 13
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            edge = x in (x0, x1) or y in (y0, y1)
            S(px, x, y, CARD_D if edge else CARD)
    # corner notches (highlight)
    S(px, x0 + 1, y0 + 1, CARD_L); S(px, x1 - 1, y0 + 1, CARD_L)
    # etched 3x3 grid in the middle: cells at (4..6, 7..9, 10..12) both axes
    for gy in range(3):
        for gx in range(3):
            cx, cy = 4 + gx * 3, 4 + gy * 3
            for dy in range(2):
                for dx in range(2):
                    S(px, cx + dx, cy + dy, cell_fill(gx, gy))
            S(px, cx + 2, cy, ETCH); S(px, cx, cy + 2, ETCH)  # grid seams
    # fold mark bottom-right
    S(px, 12, 12, ETCH); S(px, 11, 12, ETCH); S(px, 12, 11, ETCH)

# Blank: all cells parchment-dark etch
px = canvas()
card(px, lambda gx, gy: CARD_D if (gx + gy) % 2 else CARD)
write_png("src/main/resources/assets/echoes/textures/item/blank_pattern.png", W, H, px)

# Encoded: a saved layout — cells lit teal in a recognisable pattern (like a
# shaped recipe: top row + centre column), with bright cores
layout = {(0,0),(1,0),(2,0),(1,1),(1,2)}
def enc(gx, gy):
    if (gx, gy) in layout:
        return TEAL if (gx + gy) % 2 == 0 else TEAL_D
    return CARD_D if (gx + gy) % 2 else CARD
px = canvas()
card(px, enc)
# sparkle core on the middle cell
S(px, 7, 7, TEAL_L)
write_png("src/main/resources/assets/echoes/textures/item/encoded_pattern.png", W, H, px)

print("wrote item/blank_pattern.png, item/encoded_pattern.png (16x16 sources)")
