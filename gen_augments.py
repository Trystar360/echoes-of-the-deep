#!/usr/bin/env python3
"""Generate the three Compressor augment item textures, in the mod's deep-resonance
style (deep-dark inset tile, patinated bronze bezel, an accent-coloured glyph).

  acceleration_coil  — TEAL  speed chevrons (faster, costs more Light)
  efficiency_damper   — AMBER damper bar   (less Light per craft)
  yield_resonator     — AMETH radiating dots (better byproduct odds)

Run:  python3 gen_augments.py  ->  src/main/resources/assets/echoes/textures/item/*.png
"""
from PIL import Image

OUT = "src/main/resources/assets/echoes/textures/item"

DEEP   = [(7, 13, 15), (14, 24, 26), (22, 36, 38), (33, 52, 54), (48, 70, 72)]
TEAL   = [(6, 34, 36), (16, 82, 78), (32, 150, 140), (86, 226, 212), (200, 255, 248)]
AMBER  = [(40, 24, 10), (92, 56, 24), (156, 98, 40), (214, 152, 66), (252, 216, 152)]
AMETH  = [(24, 14, 42), (54, 36, 88), (102, 70, 150), (160, 120, 214), (224, 198, 252)]
BRONZE = [(22, 19, 17), (48, 41, 33), (84, 71, 55), (128, 110, 84), (190, 172, 138)]
FACE   = (12, 19, 21)


def px(im, x, y, c, a=255):
    if 0 <= x < 16 and 0 <= y < 16:
        im.putpixel((x, y), (c[0], c[1], c[2], a))


def base_tile(accent):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    # rounded-ish dark tile
    for y in range(16):
        for x in range(16):
            if (x in (0, 15) and y in (0, 15)):
                continue                      # clipped corners
            im.putpixel((x, y), (*DEEP[1], 255))
    # bronze bezel ring (inset 1px)
    for i in range(1, 15):
        px(im, i, 1, BRONZE[3]); px(im, i, 14, BRONZE[2])
        px(im, 1, i, BRONZE[3]); px(im, 14, i, BRONZE[2])
    # corner rune flecks
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        px(im, x, y, BRONZE[4])
    # dark inset face
    for y in range(3, 13):
        for x in range(3, 13):
            im.putpixel((x, y), (*FACE, 255))
    # faint accent floor glow
    for y in range(3, 13):
        for x in range(3, 13):
            if (x + y) % 2 == 0:
                px(im, x, y, accent[0], 60)
    return im


def glow(im, x, y, accent):
    px(im, x, y, accent[4])
    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        px(im, x + dx, y + dy, accent[3])
    for dx, dy in ((1, 1), (-1, 1), (1, -1), (-1, -1)):
        px(im, x + dx, y + dy, accent[2], 200)


def acceleration_coil():
    im = base_tile(TEAL)
    # three rising chevrons (speed)
    for cy, c in ((11, TEAL[2]), (8, TEAL[3]), (5, TEAL[4])):
        for k in range(4):
            px(im, 6 + k, cy + k - 1, c)
            px(im, 10 - k, cy + k - 1, c)
    glow(im, 8, 5, TEAL)
    return im


def efficiency_damper():
    im = base_tile(AMBER)
    # a damper: bracket over a descending bar (less in)
    for x in range(5, 11):
        px(im, x, 5, AMBER[3])
    px(im, 5, 6, AMBER[3]); px(im, 10, 6, AMBER[3])
    for y in range(7, 12):
        px(im, 7, y, AMBER[2]); px(im, 8, y, AMBER[4])
    px(im, 7, 11, AMBER[1]); px(im, 8, 11, AMBER[2])  # damped tail
    glow(im, 8, 8, AMBER)
    return im


def yield_resonator():
    im = base_tile(AMETH)
    # radiating dots from a bright core (more out)
    glow(im, 8, 8, AMETH)
    for (x, y) in ((8, 4), (8, 12), (4, 8), (12, 8), (5, 5), (11, 5), (5, 11), (11, 11)):
        px(im, x, y, AMETH[3])
    for (x, y) in ((8, 5), (8, 11), (5, 8), (11, 8)):
        px(im, x, y, AMETH[2], 200)
    return im


def main():
    out = {
        "acceleration_coil": acceleration_coil(),
        "efficiency_damper": efficiency_damper(),
        "yield_resonator": yield_resonator(),
    }
    for name, im in out.items():
        im.save(f"{OUT}/{name}.png")
        print("wrote", f"{OUT}/{name}.png")


if __name__ == "__main__":
    main()
