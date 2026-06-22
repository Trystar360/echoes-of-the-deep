#!/usr/bin/env python3
"""Generate the mod's custom GUI bitmap font (echoes:gui).

Renders a crisp 1-bit pixel font from LiberationMono-Bold into a Minecraft bitmap-font
atlas (16 columns, 8x8 cells) covering printable ASCII (0x20-0x7E) and Latin-1
supplement (0xA0-0xFF, so '·' and '×' resolve), plus the matching font provider JSON.

  -> src/main/resources/assets/echoes/textures/font/gui_ascii.png
  -> src/main/resources/assets/echoes/font/gui.json
"""
from PIL import Image, ImageFont, ImageDraw

CELL = 8                # px per glyph cell (no scaling: height matches)
COLS = 16
TTF = "/usr/share/fonts/truetype/liberation/LiberationMono-Bold.ttf"
ATLAS = "src/main/resources/assets/echoes/textures/font/gui_ascii.png"
JSON = "src/main/resources/assets/echoes/font/gui.json"

# code-point blocks laid out as rows of 16
BLOCKS = list(range(0x20, 0x80)) + list(range(0xA0, 0x100))   # 96 + 96 = 192 cells
ROWS = (len(BLOCKS) + COLS - 1) // COLS


def render():
    font = ImageFont.truetype(TTF, 9)
    img = Image.new("RGBA", (COLS * CELL, ROWS * CELL), (0, 0, 0, 0))
    for i, cp in enumerate(BLOCKS):
        ch = chr(cp)
        if not ch.isprintable():
            continue
        cell = Image.new("L", (CELL, CELL), 0)
        d = ImageDraw.Draw(cell)
        # render slightly up-left so the 9px glyph sits in the 8px cell on the baseline
        try:
            d.text((0, -1), ch, fill=255, font=font)
        except Exception:
            continue
        # 1-bit threshold -> crisp white pixels
        for y in range(CELL):
            for x in range(CELL):
                if cell.getpixel((x, y)) >= 110:
                    cx, cy = (i % COLS) * CELL + x, (i // COLS) * CELL + y
                    img.putpixel((cx, cy), (255, 255, 255, 255))
    img.save(ATLAS)
    print("wrote", ATLAS, img.size)


def write_json():
    import json
    chars = []
    for r in range(ROWS):
        row = ""
        for c in range(COLS):
            idx = r * COLS + c
            row += chr(BLOCKS[idx]) if idx < len(BLOCKS) else " "
        chars.append(row)
    provider = {
        "providers": [
            # explicit advances for whitespace (empty bitmap cells would otherwise collapse)
            {"type": "space", "advances": {" ": 4, " ": 4}},
            {"type": "bitmap", "file": "echoes:font/gui_ascii.png",
             "ascent": 7, "height": 8, "chars": chars}
        ]
    }
    open(JSON, "w").write(json.dumps(provider, ensure_ascii=False, indent=2) + "\n")
    print("wrote", JSON)


if __name__ == "__main__":
    import os
    os.makedirs("src/main/resources/assets/echoes/textures/font", exist_ok=True)
    os.makedirs("src/main/resources/assets/echoes/font", exist_ok=True)
    render()
    write_json()
