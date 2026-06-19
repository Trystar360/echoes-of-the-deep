#!/usr/bin/env python3
"""Render crafting-grid / furnace recipe images (modpack-wiki style).

Reads every recipe JSON in data/echoes/recipe and draws it as a GUI-style
widget: a 3x3 crafting grid (or a 1->1 furnace/crusher slot pair) with an arrow
and the result slot + count. Item sprites come from the mod's own textures;
vanilla ingredients use compact pixel-art approximations defined here.

Output: docs/wiki/images/recipes/<recipe>.png

Pure-Python PNG I/O (no Pillow), matching gallery.py / montage.py.
"""
import struct, zlib, os, json, glob

TEX = "src/main/resources/assets/echoes/textures"
RECIPES = "src/main/resources/data/echoes/recipe"
OUT = "docs/wiki/images/recipes"

SC = 2                       # item sprite upscale -> 32px
ITEM = 16 * SC
SLOT = ITEM + 6             # slot box
GAP = 2
PANEL = (198, 198, 198, 255)
SLOT_BG = (139, 139, 139, 255)
EDGE_D = (85, 85, 85, 255)
EDGE_L = (255, 255, 255, 255)


# ---------- PNG ----------
def read_png(path):
    d = open(path, "rb").read(); i = 8; w = h = 0; idat = b""
    while i < len(d):
        ln = struct.unpack(">I", d[i:i+4])[0]; typ = d[i+4:i+8]; data = d[i+8:i+8+ln]
        if typ == b"IHDR":
            w, h = struct.unpack(">II", data[:8])
        elif typ == b"IDAT":
            idat += data
        elif typ == b"IEND":
            break
        i += 12 + ln
    raw = zlib.decompress(idat); px = [(0, 0, 0, 0)]*(w*h); pos = 0
    for y in range(h):
        pos += 1
        for x in range(w):
            px[y*w+x] = (raw[pos], raw[pos+1], raw[pos+2], raw[pos+3]); pos += 4
    return w, h, px


def write_png(path, w, h, px):
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        for x in range(w):
            raw += bytes(px[y*w+x])

    def ch(t, dd):
        return struct.pack(">I", len(dd)) + t + dd + struct.pack(">I", zlib.crc32(t+dd) & 0xffffffff)
    open(path, "wb").write(b"\x89PNG\r\n\x1a\n" +
                           ch(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)) +
                           ch(b"IDAT", zlib.compress(bytes(raw), 9)) + ch(b"IEND", b""))


# ---------- our textures ----------
_ITEMS = {os.path.basename(p)[:-4] for p in glob.glob(f"{TEX}/item/*.png")}
_BLOCKS = {os.path.basename(p)[:-4] for p in glob.glob(f"{TEX}/block/*.png")}


def our_sprite(name):
    kind = "item" if name in _ITEMS else "block"
    w, h, px = read_png(f"{TEX}/{kind}/{name}.png")
    return [[px[y*w+x] for x in range(16)] for y in range(16)]


# ---------- vanilla approximations (16x16) ----------
def blank():
    return [[(0, 0, 0, 0)]*16 for _ in range(16)]


def rect(g, x0, y0, x1, y1, c):
    for y in range(max(0, y0), min(16, y1+1)):
        for x in range(max(0, x0), min(16, x1+1)):
            g[y][x] = c


def speckle_block(base, dark, light):
    g = blank()
    rect(g, 1, 1, 14, 14, base)
    for y in range(1, 15):
        for x in range(1, 15):
            n = (x*7 + y*13) % 5
            if n == 0:
                g[y][x] = dark
            elif n == 1:
                g[y][x] = light
    # bevel
    for x in range(1, 15):
        g[1][x] = light; g[14][x] = dark
    for y in range(1, 15):
        g[y][1] = light; g[y][14] = dark
    return g


def dust(c, hi):
    g = blank()
    pts = [(4, 11), (5, 12), (6, 11), (7, 13), (8, 12), (9, 13), (10, 11), (11, 12),
           (5, 10), (7, 11), (9, 11), (8, 10), (6, 13), (10, 13), (4, 13), (11, 10)]
    for (x, y) in pts:
        g[y][x] = c
        g[y-1][x] = hi
    return g


def iron_ingot():
    g = blank()
    sil = (205, 205, 210, 255); dk = (120, 120, 128, 255); hi = (235, 235, 240, 255)
    # slanted bar
    for y in range(6, 12):
        off = (y - 6)
        rect(g, 3 + off // 2, y, 12 - (5 - (y - 6)) // 2, y, sil)
    rect(g, 4, 6, 11, 7, hi)
    rect(g, 4, 10, 12, 11, dk)
    return g


def stick():
    g = blank()
    br = (140, 100, 55, 255); dk = (110, 76, 40, 255)
    for i in range(11):
        x = 4 + i // 2; y = 12 - i
        if 0 <= x < 16 and 0 <= y < 16:
            g[y][x] = br; g[y][x+1] = dk
    return g


def comparator():
    g = speckle_block((175, 175, 175, 255), (150, 150, 150, 255), (200, 200, 200, 255))
    rect(g, 2, 9, 13, 13, (170, 170, 170, 255))
    g[5][4] = (230, 60, 60, 255); g[5][11] = (230, 60, 60, 255)  # side torches
    g[3][8] = (180, 40, 40, 255); g[4][8] = (230, 60, 60, 255)   # front torch
    return g


def note_block():
    g = speckle_block((120, 80, 52, 255), (95, 62, 40, 255), (140, 96, 62, 255))
    rect(g, 6, 6, 9, 9, (40, 30, 22, 255))
    g[6][9] = (40, 30, 22, 255); g[5][9] = (40, 30, 22, 255)
    return g


def furnace():
    g = speckle_block((130, 130, 130, 255), (105, 105, 105, 255), (155, 155, 155, 255))
    rect(g, 5, 8, 10, 12, (35, 35, 38, 255))     # dark front opening
    rect(g, 6, 10, 9, 11, (120, 70, 30, 255))    # ember hint
    return g


def ender_pearl():
    g = blank()
    base = (20, 110, 100, 255); hi = (120, 210, 195, 255); dk = (12, 70, 64, 255)
    for y in range(16):
        for x in range(16):
            dx = x - 7.5; dy = y - 7.5
            r = (dx*dx + dy*dy) ** 0.5
            if r <= 6.2:
                g[y][x] = dk if r > 5 else base
    rect(g, 5, 5, 7, 7, hi)
    return g


def book():
    g = blank()
    rect(g, 3, 2, 12, 13, (150, 95, 55, 255))
    rect(g, 3, 2, 4, 13, (90, 55, 30, 255))     # spine
    rect(g, 5, 3, 12, 12, (225, 220, 205, 255))  # pages
    rect(g, 9, 2, 10, 13, (180, 60, 60, 255))    # bookmark
    return g


def chest():
    g = blank()
    rect(g, 2, 3, 13, 13, (150, 100, 55, 255))
    rect(g, 2, 3, 13, 4, (110, 72, 38, 255))
    rect(g, 2, 7, 13, 8, (95, 62, 32, 255))      # seam
    rect(g, 7, 7, 8, 9, (70, 70, 72, 255))       # latch
    for x in range(2, 14):
        g[3][x] = (185, 130, 75, 255)
    return g


def cobblestone():
    return speckle_block((128, 128, 128, 255), (95, 95, 95, 255), (165, 165, 165, 255))


VANILLA = {
    "iron_ingot": iron_ingot, "stick": stick, "comparator": comparator,
    "note_block": note_block, "furnace": furnace, "ender_pearl": ender_pearl,
    "book": book, "chest": chest, "cobblestone": cobblestone,
    "redstone": lambda: dust((200, 25, 25, 255), (255, 90, 90, 255)),
    "glowstone_dust": lambda: dust((220, 200, 70, 255), (255, 240, 150, 255)),
    "blaze_powder": lambda: dust((235, 150, 30, 255), (255, 210, 90, 255)),
    "redstone_block": lambda: speckle_block((165, 22, 22, 255), (120, 12, 12, 255), (210, 45, 45, 255)),
    "iron_block": lambda: speckle_block((205, 205, 210, 255), (170, 170, 176, 255), (235, 235, 240, 255)),
    "glowstone": lambda: speckle_block((215, 190, 95, 255), (180, 150, 70, 255), (250, 235, 150, 255)),
}


def sprite_for(rid):
    ns, name = rid.split(":", 1)
    if ns == "echoes":
        return our_sprite(name)
    if name in VANILLA:
        return VANILLA[name]()
    g = blank(); rect(g, 2, 2, 13, 13, (160, 120, 200, 255)); return g  # fallback


# ---------- digit font (3x5) ----------
FONT = {
    "0": ["111", "101", "101", "101", "111"], "1": ["010", "110", "010", "010", "111"],
    "2": ["111", "001", "111", "100", "111"], "3": ["111", "001", "111", "001", "111"],
    "4": ["101", "101", "111", "001", "001"], "5": ["111", "100", "111", "001", "111"],
    "6": ["111", "100", "111", "101", "111"], "7": ["111", "001", "010", "010", "010"],
    "8": ["111", "101", "111", "101", "111"], "9": ["111", "101", "111", "001", "111"],
}


# ---------- canvas ----------
def new_canvas(w, h):
    return [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)], w, h


def fill(c, x0, y0, x1, y1, col):
    for y in range(max(0, y0), min(len(c), y1)):
        for x in range(max(0, x0), min(len(c[0]), x1)):
            c[y][x] = col


def draw_slot(c, ox, oy):
    fill(c, ox, oy, ox+SLOT, oy+SLOT, SLOT_BG)
    for x in range(ox, ox+SLOT):
        c[oy][x] = EDGE_D; c[oy+SLOT-1][x] = EDGE_L
    for y in range(oy, oy+SLOT):
        c[y][ox] = EDGE_D; c[y][ox+SLOT-1] = EDGE_L


def blit_sprite(c, ox, oy, sp):
    bx = ox + (SLOT - ITEM)//2; by = oy + (SLOT - ITEM)//2
    for y in range(16):
        for x in range(16):
            px = sp[y][x]
            if px[3] == 0:
                continue
            for sy in range(SC):
                for sx in range(SC):
                    c[by+y*SC+sy][bx+x*SC+sx] = (px[0], px[1], px[2], 255)


def draw_count(c, ox, oy, n):
    if n <= 1:
        return
    s = str(n); dscale = 2; dw = 3*dscale; gap = dscale
    total = len(s)*dw + (len(s)-1)*gap
    sx = ox + SLOT - 2 - total; sy = oy + SLOT - 2 - 5*dscale
    for ch in s:
        rows = FONT[ch]
        for ry in range(5):
            for rx in range(3):
                if rows[ry][rx] == "1":
                    for a in range(dscale):
                        for b in range(dscale):
                            yy = sy+ry*dscale+a; xx = sx+rx*dscale+b
                            c[yy+1][xx+1] = (0, 0, 0, 255)        # shadow
                            c[yy][xx] = (255, 255, 255, 255)
        sx += dw + gap


def draw_arrow(c, ox, oy, cy):
    col = (90, 90, 90, 255)
    fill(c, ox, cy-3, ox+16, cy+3, col)
    for i in range(8):
        fill(c, ox+16+i, cy-8+i, ox+16+i+1, cy+8-i, col)


# ---------- recipe -> image ----------
def grid_cells(data):
    cells = [None]*9
    if data["type"] == "minecraft:crafting_shaped":
        pat = data["pattern"]; key = data["key"]
        for r, row in enumerate(pat):
            for col, chx in enumerate(row):
                if chx != " ":
                    v = key[chx]
                    cells[r*3+col] = v if isinstance(v, str) else v.get("item", v.get("tag", ""))
    else:  # shapeless
        ings = data["ingredients"]
        for i, v in enumerate(ings[:9]):
            cells[i] = v if isinstance(v, str) else v.get("item", v.get("tag", ""))
    return cells


def result_of(data):
    r = data.get("result")
    if isinstance(r, str):
        return r, 1
    return r["id"], r.get("count", 1)


def render_crafting(data, out):
    cells = grid_cells(data)
    rid, rcount = result_of(data)
    PAD = 6; grid = 3*SLOT + 2*GAP
    arrow_w = 26
    W = PAD + grid + arrow_w + SLOT + PAD
    H = PAD + grid + PAD
    c, W, H = new_canvas(W, H)
    fill(c, 0, 0, W, H, PANEL)
    for i in range(9):
        ox = PAD + (i % 3)*(SLOT+GAP); oy = PAD + (i//3)*(SLOT+GAP)
        draw_slot(c, ox, oy)
        if cells[i]:
            blit_sprite(c, ox, oy, sprite_for(cells[i]))
    cy = H//2
    draw_arrow(c, PAD+grid+2, cy, cy)
    rx = PAD+grid+arrow_w; ry = (H-SLOT)//2
    draw_slot(c, rx, ry)
    blit_sprite(c, rx, ry, sprite_for(rid))
    draw_count(c, rx, ry, rcount)
    write_png(out, W, H, [px for row in c for px in row])


def render_furnace(data, out, kind):
    ing = data["ingredient"]
    ing = ing if isinstance(ing, str) else ing.get("item", "")
    rid, rcount = result_of(data)
    PAD = 6; arrow_w = 26
    W = PAD + SLOT + arrow_w + SLOT + PAD
    H = PAD + SLOT + PAD
    c, W, H = new_canvas(W, H)
    fill(c, 0, 0, W, H, PANEL)
    draw_slot(c, PAD, PAD)
    blit_sprite(c, PAD, PAD, sprite_for(ing))
    cy = H//2
    # flame tint for smelting/blasting
    fcol = (210, 120, 30, 255) if kind != "crushing" else (90, 90, 90, 255)
    fill(c, PAD+SLOT+2, cy-3, PAD+SLOT+2+16, cy+3, fcol)
    for i in range(8):
        fill(c, PAD+SLOT+2+16+i, cy-8+i, PAD+SLOT+2+16+i+1, cy+8-i, fcol)
    rx = PAD+SLOT+arrow_w; ry = PAD
    draw_slot(c, rx, ry)
    blit_sprite(c, rx, ry, sprite_for(rid))
    draw_count(c, rx, ry, rcount)
    write_png(out, W, H, [px for row in c for px in row])


def main():
    os.makedirs(OUT, exist_ok=True)
    n = 0
    for path in sorted(glob.glob(f"{RECIPES}/**/*.json", recursive=True)):
        data = json.load(open(path))
        t = data.get("type", "")
        rel = os.path.relpath(path, RECIPES)[:-5].replace("/", "_")
        out = f"{OUT}/{rel}.png"
        if t in ("minecraft:crafting_shaped", "minecraft:crafting_shapeless"):
            render_crafting(data, out); n += 1
        elif t in ("minecraft:smelting", "minecraft:blasting"):
            render_furnace(data, out, "smelt"); n += 1
        elif t == "echoes:crushing":
            render_furnace(data, out, "crushing"); n += 1
    print(f"wrote {n} recipe images to {OUT}")


if __name__ == "__main__":
    main()
