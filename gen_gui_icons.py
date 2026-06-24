#!/usr/bin/env python3
"""Generate small icon sprites for the GUI: tab glyphs (info / config / function) and
per-block header emblems (compressor, transmuter, filter, transmute, info), as dark-ink
line-art on transparent, in the parchment-book style.

  -> assets/echoes/textures/gui/sprites/widget/icon_*.png   (12x12 tab glyphs)
  -> assets/echoes/textures/gui/sprites/widget/emblem_*.png (16x16 header emblems)
"""
import json
import os
from PIL import Image, ImageDraw

DIR = "src/main/resources/assets/echoes/textures/gui/sprites/widget"
INK = (52, 36, 20, 255)
ACC = (124, 46, 28, 255)     # burgundy accent


def new(s):
    im = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    return im, ImageDraw.Draw(im)


def save(name, im):
    os.makedirs(DIR, exist_ok=True)
    im.save(f"{DIR}/{name}.png")
    # icons are not stretched — plain (stretch) scaling is fine
    open(f"{DIR}/{name}.png.mcmeta", "w").write(
        json.dumps({"gui": {"scaling": {"type": "stretch"}}}, indent=2) + "\n")
    print("wrote", name, im.size)


# ---- 12x12 tab glyphs ----
def icon_info():
    im, d = new(12)
    d.ellipse([1, 1, 10, 10], outline=INK)
    d.rectangle([5, 3, 6, 4], fill=ACC)            # dot
    d.rectangle([5, 6, 6, 9], fill=ACC)            # stem
    save("icon_info", im)


def gear(im, d, cx, cy, r, col):
    d.ellipse([cx - r, cy - r, cx + r, cy + r], outline=col)
    for dx, dy in ((0, -r - 1), (0, r), (-r - 1, 0), (r, 0),
                   (-r, -r), (r - 1, -r), (-r, r - 1), (r - 1, r - 1)):
        d.point((cx + dx, cy + dy), fill=col)
    d.ellipse([cx - 1, cy - 1, cx + 1, cy + 1], fill=col)


def icon_config():
    im, d = new(12)
    gear(im, d, 6, 6, 3, INK)
    save("icon_config", im)


def icon_function():
    im, d = new(12)
    gear(im, d, 5, 6, 3, INK)
    d.line([8, 6, 10, 6], fill=ACC)                # arrow shaft
    d.line([8, 4, 10, 6], fill=ACC)
    d.line([8, 8, 10, 6], fill=ACC)
    save("icon_function", im)


# ---- 16x16 header emblems ----
def emblem_compressor():
    im, d = new(16)
    d.rectangle([3, 7, 12, 8], fill=INK)           # anvil bar
    for x in range(6, 10):                         # down arrows (compress)
        d.line([x, 1, x, 4], fill=ACC)
    d.line([5, 3, 8, 5], fill=ACC); d.line([10, 3, 8, 5], fill=ACC)
    d.line([3, 10, 12, 10], fill=INK)
    save("emblem_compressor", im)


def emblem_transmuter():
    im, d = new(16)
    d.polygon([8, 1, 11, 7, 9, 7, 11, 12, 5, 12, 7, 7, 5, 7], outline=ACC)  # flame
    d.point((8, 9), fill=ACC)
    save("emblem_transmuter", im)


def emblem_filter():
    im, d = new(16)
    d.polygon([2, 3, 13, 3, 9, 8, 9, 13, 6, 13, 6, 8], outline=INK)   # funnel
    save("emblem_filter", im)


def emblem_transmute():
    im, d = new(16)
    d.line([8, 1, 8, 14], fill=ACC); d.line([1, 8, 14, 8], fill=ACC)  # 4-point star
    d.line([4, 4, 11, 11], fill=INK); d.line([11, 4, 4, 11], fill=INK)
    d.ellipse([6, 6, 9, 9], fill=ACC)
    save("emblem_transmute", im)


def emblem_info():
    im, d = new(16)
    for r in (6, 4, 2):                            # concentric resonance rings
        d.ellipse([8 - r, 8 - r, 8 + r, 8 + r], outline=INK if r != 4 else ACC)
    save("emblem_info", im)


def emblem_config():
    im, d = new(16)
    gear(im, d, 8, 8, 5, INK)
    d.ellipse([6, 6, 9, 9], fill=ACC)
    save("emblem_config", im)


for fn in (icon_info, icon_config, icon_function,
           emblem_compressor, emblem_transmuter, emblem_filter, emblem_transmute, emblem_info,
           emblem_config):
    fn()
