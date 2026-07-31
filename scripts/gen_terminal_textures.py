#!/usr/bin/env python3
"""Procedural 16x sprite for the Wave Terminal handheld.

A slate handheld slab with a glowing teal screen showing a mini channel-grid,
a violet channel dial in the corner, and bronze corner screws — matching the
device plating palette used by the other wireless gadgets.
"""
from PIL import Image

PLATE = (44, 42, 60)
PLATE_D = (32, 30, 46)
EDGE = (24, 22, 36)
BRONZE = (176, 128, 64)
BRONZE_D = (128, 92, 44)
TEAL = (64, 200, 176)
TEAL_D = (36, 128, 116)
TEAL_B = (140, 255, 232)
VIO = (108, 72, 198)
VIO_B = (168, 128, 255)

W = H = 16
img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
px = img.load()


def rect(x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = c


# body: rounded-ish slab (clip corners)
rect(2, 1, 13, 14, PLATE)
rect(1, 2, 14, 13, PLATE)
# darker base / grip band
rect(2, 12, 13, 14, PLATE_D)
rect(1, 12, 1, 13, PLATE_D)
rect(14, 12, 14, 13, PLATE_D)
# outline shade
for x, y in [(2, 1), (13, 1), (1, 2), (14, 2), (1, 13), (14, 13), (2, 14), (13, 14)]:
    px[x, y] = EDGE

# screen: inset teal panel
rect(3, 3, 12, 9, TEAL_D)
rect(4, 4, 11, 8, (18, 40, 44))
# channel grid: 4x2 mini "chest slots" glowing
for gy in (4, 6):
    for gx in (4, 6, 8, 10):
        px[gx, gy] = TEAL
        px[gx + 1, gy] = TEAL_B if (gx + gy) % 4 == 0 else TEAL
# scan line highlight
px[5, 4] = TEAL_B
px[9, 6] = TEAL_B

# violet channel dial (bottom-right of body)
for dx, dy in [(11, 11), (12, 11), (11, 12), (12, 12)]:
    px[dx, dy] = VIO
px[11, 11] = VIO_B

# bronze corner screws
for sx, sy in [(2, 2), (13, 2), (2, 11)]:
    px[sx, sy] = BRONZE
px[13, 11] = BRONZE_D

# antenna nub
px[7, 0] = BRONZE
px[8, 0] = BRONZE

img.save("src/main/resources/assets/echoes/textures/item/wave_terminal.png")
print("wrote wave_terminal.png 16x16")
