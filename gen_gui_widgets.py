#!/usr/bin/env python3
"""Generate the parchment / wood / leather GUI sprites (9-sliced so they stretch to any
panel or button size). Warm, hand-drawn tech-mod book aesthetic.

  -> assets/echoes/textures/gui/sprites/widget/panel.png   (parchment + wood frame)
  -> assets/echoes/textures/gui/sprites/widget/slot.png    (dark leather well)
  -> assets/echoes/textures/gui/sprites/widget/button{,_highlighted,_disabled}.png
  (each with a .mcmeta nine_slice)
"""
import json
import os
from PIL import Image

DIR = "src/main/resources/assets/echoes/textures/gui/sprites/widget"


def h(x, y, s=0):
    n = (x * 374761393 + y * 668265263 + s * 69069) & 0xffffffff
    n = (n ^ (n >> 13)) * 1274126177 & 0xffffffff
    return (n >> 8) & 0xff


def mott(base, x, y, amp, s=0):
    d = (h(x, y, s) - 128) * amp // 128
    return tuple(max(0, min(255, base[i] + d)) for i in range(3))


def save(name, im, border):
    os.makedirs(DIR, exist_ok=True)
    im.save(f"{DIR}/{name}.png")
    meta = {"gui": {"scaling": {"type": "nine_slice",
                                "width": im.width, "height": im.height, "border": border}}}
    open(f"{DIR}/{name}.png.mcmeta", "w").write(json.dumps(meta, indent=2) + "\n")
    print("wrote", name, im.size, "border", border)


# ---- parchment panel: cream centre, wood frame, dark studs ----
def panel():
    S, B = 48, 9
    PARCH = (228, 210, 170)
    WOOD_D = (74, 50, 28)
    WOOD = (120, 86, 50)
    WOOD_L = (158, 120, 74)
    EDGE = (198, 176, 132)
    im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    for y in range(S):
        for x in range(S):
            d = min(x, y, S - 1 - x, S - 1 - y)   # distance to edge
            if d == 0:
                c = (40, 26, 14)                  # outer line
            elif d < 3:
                c = mott(WOOD_D, x, y, 16, 1)      # dark frame
            elif d < B - 2:
                # wood plank with horizontal grain
                base = WOOD_L if ((x + y) // 2) % 3 == 0 else WOOD
                c = mott(base, x, y, 14, 2)
            elif d < B:
                c = EDGE                           # parchment lip
            else:
                c = mott(PARCH, x, y, 8, 3)        # parchment field
            im.putpixel((x, y), (*c, 255))
    # corner studs
    for (sx, sy) in ((4, 4), (S - 5, 4), (4, S - 5), (S - 5, S - 5)):
        for dx in range(-1, 2):
            for dy in range(-1, 2):
                im.putpixel((sx + dx, sy + dy), (54, 36, 20, 255))
        im.putpixel((sx, sy), (150, 116, 70, 255))
    save("panel", im, B)


# ---- dark leather slot well ----
def slot():
    S, B = 18, 5
    LEA = (58, 30, 26)
    LEA_D = (34, 16, 14)
    im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    for y in range(S):
        for x in range(S):
            d = min(x, y, S - 1 - x, S - 1 - y)
            if d == 0:
                c = (20, 10, 8)
            elif d == 1:
                c = (96, 60, 40)                   # stitch/highlight rim
            else:
                c = mott(LEA if d > 2 else LEA_D, x, y, 10, 7)
            im.putpixel((x, y), (*c, 255))
    save("slot", im, B)


# ---- wood / parchment buttons ----
def button(face, rim_hi, rim_lo, grain=True):
    S, B = 20, 4
    im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    for y in range(S):
        for x in range(S):
            d = min(x, y, S - 1 - x, S - 1 - y)
            if d == 0:
                c = (34, 22, 12)
            elif d == 1:
                c = rim_hi if (y < S // 2) else rim_lo
            else:
                base = face
                if grain and (x + (y // 2)) % 4 == 0:
                    base = tuple(max(0, base[i] - 12) for i in range(3))
                c = mott(base, x, y, 8, 5)
            im.putpixel((x, y), (*c, 255))
    return im, B


WOOD = (132, 94, 54)
WOOD_HI = (176, 134, 84)
WOOD_LO = (86, 58, 32)

im, b = button(WOOD, WOOD_HI, WOOD_LO);              save("button", im, b)
im, b = button((158, 120, 70), (210, 176, 120), WOOD_LO); save("button_highlighted", im, b)
im, b = button((96, 84, 66), (120, 108, 88), (70, 60, 46), grain=False); save("button_disabled", im, b)

panel()
slot()
