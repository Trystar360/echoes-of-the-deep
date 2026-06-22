#!/usr/bin/env python3
"""Generate the custom GUI button sprites (Obsidian & Gold), 9-sliced so they stretch to
any button size. Three states: normal / highlighted (hover) / disabled.

  -> assets/echoes/textures/gui/sprites/widget/button{,_highlighted,_disabled}.png (+ .mcmeta)
"""
import json
import os
from PIL import Image

DIR = "src/main/resources/assets/echoes/textures/gui/sprites/widget"
SIZE = 20
BORDER = 3


def lerp(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def button(fill, edge_hi, edge_lo, border_full=None):
    im = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    for y in range(SIZE):
        for x in range(SIZE):
            im.putpixel((x, y), (*fill, 255))
    # 1px outer black frame
    for i in range(SIZE):
        im.putpixel((i, 0), (0, 0, 0, 255)); im.putpixel((i, SIZE - 1), (0, 0, 0, 255))
        im.putpixel((0, i), (0, 0, 0, 255)); im.putpixel((SIZE - 1, i), (0, 0, 0, 255))
    # inner bevel: gold top/left, shadow bottom/right (or a full accent border on hover)
    hi = border_full or edge_hi
    lo = border_full or edge_lo
    for i in range(1, SIZE - 1):
        im.putpixel((i, 1), (*hi, 255)); im.putpixel((1, i), (*hi, 255))
        im.putpixel((i, SIZE - 2), (*lo, 255)); im.putpixel((SIZE - 2, i), (*lo, 255))
    return im


def write(name, im):
    os.makedirs(DIR, exist_ok=True)
    im.save(f"{DIR}/{name}.png")
    meta = {"gui": {"scaling": {"type": "nine_slice", "width": SIZE, "height": SIZE, "border": BORDER}}}
    open(f"{DIR}/{name}.png.mcmeta", "w").write(json.dumps(meta, indent=2) + "\n")
    print("wrote", name)


GOLD = (138, 110, 42)
GOLD_HI = (232, 184, 75)
SHADOW = (0, 0, 0)

write("button", button((26, 28, 34), GOLD, SHADOW))
write("button_highlighted", button((36, 38, 46), GOLD_HI, SHADOW, border_full=GOLD_HI))
write("button_disabled", button((18, 19, 23), (58, 61, 70), SHADOW))
