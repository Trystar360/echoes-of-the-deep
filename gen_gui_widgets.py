#!/usr/bin/env python3
"""Generate the parchment / wood / leather GUI sprites (9-sliced) — an ornate, hand-drawn
book aesthetic: rounded carved-wood frames, a title plaque banner, dark-leather slot wells,
and wood buttons.

  -> assets/echoes/textures/gui/sprites/widget/panel.png   (rounded parchment + wood frame)
  -> assets/echoes/textures/gui/sprites/widget/banner.png  (wood title plaque)
  -> assets/echoes/textures/gui/sprites/widget/slot.png    (dark leather well)
  -> assets/echoes/textures/gui/sprites/widget/button{,_highlighted,_disabled}.png
"""
import json
import os
from PIL import Image, ImageDraw

DIR = "src/main/resources/assets/echoes/textures/gui/sprites/widget"

PARCH  = (228, 210, 170)
EDGE   = (198, 176, 132)
WOOD_D = (62, 42, 24)
WOOD   = (120, 86, 50)
WOOD_L = (160, 122, 76)
OUTLN  = (34, 22, 12)


def h(x, y, s=0):
    n = (x * 374761393 + y * 668265263 + s * 69069) & 0xffffffff
    n = (n ^ (n >> 13)) * 1274126177 & 0xffffffff
    return (n >> 8) & 0xff


def grain(im, rect, base, amp, s):
    x0, y0, x1, y1 = rect
    px = im.load()
    for y in range(y0, y1):
        for x in range(x0, x1):
            if px[x, y][3] == 0:
                continue
            d = (h(x, y, s) - 128) * amp // 128
            r, g, b, a = px[x, y]
            px[x, y] = (max(0, min(255, r + d)), max(0, min(255, g + d)), max(0, min(255, b + d)), a)


def save(name, im, border):
    os.makedirs(DIR, exist_ok=True)
    im.save(f"{DIR}/{name}.png")
    meta = {"gui": {"scaling": {"type": "nine_slice",
                                "width": im.width, "height": im.height, "border": border}}}
    open(f"{DIR}/{name}.png.mcmeta", "w").write(json.dumps(meta, indent=2) + "\n")
    print("wrote", name, im.size, "border", border)


def save_hslice(name, im, lr, tb):
    """A horizontally-stretched plaque: big left/right border, small top/bottom."""
    os.makedirs(DIR, exist_ok=True)
    im.save(f"{DIR}/{name}.png")
    meta = {"gui": {"scaling": {"type": "nine_slice", "width": im.width, "height": im.height,
                                "border": {"left": lr, "right": lr, "top": tb, "bottom": tb}}}}
    open(f"{DIR}/{name}.png.mcmeta", "w").write(json.dumps(meta, indent=2) + "\n")
    print("wrote", name, im.size, "border lr", lr, "tb", tb)


def panel():
    S, B, R = 48, 10, 5
    im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    # nested rounded rectangles: outline -> wood -> dark groove -> parchment lip -> field
    d.rounded_rectangle([0, 0, S - 1, S - 1], R, fill=OUTLN)
    d.rounded_rectangle([1, 1, S - 2, S - 2], R - 1, fill=WOOD)
    grain(im, (1, 1, S - 1, S - 1), WOOD, 16, 2)
    d.rounded_rectangle([2, 2, S - 3, S - 3], R - 1, outline=WOOD_L)        # wood highlight
    d.rounded_rectangle([B - 4, B - 4, S - 1 - (B - 4), S - 1 - (B - 4)], 3, fill=WOOD_D)   # inner groove
    d.rounded_rectangle([B - 3, B - 3, S - 1 - (B - 3), S - 1 - (B - 3)], 3, fill=EDGE)     # parchment lip
    d.rounded_rectangle([B - 2, B - 2, S - 1 - (B - 2), S - 1 - (B - 2)], 2, fill=PARCH)    # field
    grain(im, (B - 2, B - 2, S - (B - 2), S - (B - 2)), PARCH, 7, 3)
    # corner brackets (little carved studs)
    for (cx, cy) in ((5, 5), (S - 6, 5), (5, S - 6), (S - 6, S - 6)):
        d.rectangle([cx - 1, cy - 1, cx + 1, cy + 1], fill=WOOD_D)
        im.putpixel((cx, cy), (*WOOD_L, 255))
    save("panel", im, B)


def banner():
    # horizontal wood plaque with rounded ends, for the title bar
    W, Hh, B = 64, 16, 8
    im = Image.new("RGBA", (W, Hh), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rounded_rectangle([0, 1, W - 1, Hh - 2], 6, fill=OUTLN)
    d.rounded_rectangle([1, 2, W - 2, Hh - 3], 5, fill=WOOD)
    grain(im, (1, 2, W - 1, Hh - 2), WOOD, 14, 4)
    d.rounded_rectangle([2, 3, W - 3, Hh - 4], 4, outline=WOOD_L)
    d.rounded_rectangle([3, 4, W - 4, Hh - 5], 3, fill=WOOD_D)   # inset for the title text
    grain(im, (3, 4, W - 3, Hh - 4), WOOD_D, 8, 5)
    save_hslice("banner", im, 8, 4)   # stretch horizontally only (16px tall)


def slot():
    S, B = 18, 5
    LEA, LEA_D = (58, 30, 26), (34, 16, 14)
    im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    for y in range(S):
        for x in range(S):
            dd = min(x, y, S - 1 - x, S - 1 - y)
            c = (20, 10, 8) if dd == 0 else (96, 60, 40) if dd == 1 else (LEA if dd > 2 else LEA_D)
            im.putpixel((x, y), (*c, 255))
    grain(im, (2, 2, S - 2, S - 2), LEA, 10, 7)
    save("slot", im, B)


def button(face, rim_hi, rim_lo, grainy=True):
    S, B = 20, 4
    im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rounded_rectangle([0, 0, S - 1, S - 1], 3, fill=OUTLN)
    d.rounded_rectangle([1, 1, S - 2, S - 2], 2, fill=face)
    if grainy:
        grain(im, (1, 1, S - 1, S - 1), face, 10, 5)
    d.line([2, 1, S - 3, 1], fill=rim_hi)     # top highlight
    d.line([1, 2, 1, S - 3], fill=rim_hi)
    d.line([2, S - 2, S - 3, S - 2], fill=rim_lo)  # bottom shadow
    d.line([S - 2, 2, S - 2, S - 3], fill=rim_lo)
    return im, B


WOOD_HI, WOOD_LO = (176, 134, 84), (86, 58, 32)
im, b = button(WOOD, WOOD_HI, WOOD_LO);                       save("button", im, b)
im, b = button((158, 120, 70), (212, 178, 122), WOOD_LO);     save("button_highlighted", im, b)
im, b = button((98, 86, 68), (122, 110, 90), (72, 62, 48), grainy=False); save("button_disabled", im, b)
panel()
banner()
slot()
