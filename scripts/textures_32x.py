#!/usr/bin/env python3
"""32x texture upgrade pass for Echoes of the Deep.

Faithful-32 style: each 16x16 sprite becomes 32x32. Instead of blurry
interpolation, we re-derive each source pixel as a 2x2 block with:
  * deterministic per-pixel grain (keeps the hand-made pixel-art feel at 2x)
  * luminance modulation biased by the original palette (no hue drift)
  * crisp 1px inner edge shading along hard color boundaries (depth)
  * alpha edges stay sharp (no halo)
Animated strips (16x96 etc.) are handled frame-by-frame.
Usage: python3 scripts/textures_32x.py
"""
import struct, zlib, os, sys, glob, math

OUT_DIRS = ["src/main/resources/assets/echoes/textures/block",
            "src/main/resources/assets/echoes/textures/item"]

# ---------------------------------------------------------------- PNG io
def read_png(path):
    d = open(path, "rb").read()
    assert d[:8] == b"\x89PNG\r\n\x1a\n", path
    pos, w, h, ctype, idat = 8, 0, 0, None, b""
    while pos < len(d):
        ln = struct.unpack(">I", d[pos:pos+4])[0]
        t = d[pos+4:pos+8]
        data = d[pos+8:pos+8+ln]
        if t == b"IHDR":
            w, h, bit, ctype = struct.unpack(">IIBB", data[:10])
            assert bit == 8 and ctype == 6, f"{path}: unsupported png fmt"
        elif t == b"IDAT":
            idat += data
        pos += 12 + ln
    raw = zlib.decompress(idat)
    px = [(0,0,0,0)] * (w*h)
    stride = w*4
    prev = bytearray(stride)
    i = 0
    for y in range(h):
        f = raw[y*(stride+1)]
        line = bytearray(raw[y*(stride+1)+1:(y+1)*(stride+1)])
        if f == 1:
            for x in range(4, stride): line[x] = (line[x] + line[x-4]) & 255
        elif f == 2:
            for x in range(stride): line[x] = (line[x] + prev[x]) & 255
        elif f == 3:
            for x in range(stride):
                a = line[x-4] if x >= 4 else 0
                line[x] = (line[x] + ((a + prev[x]) >> 1)) & 255
        elif f == 4:
            for x in range(stride):
                a = line[x-4] if x >= 4 else 0
                b = prev[x]
                c = prev[x-4] if x >= 4 else 0
                p = a + b - c
                pa, pb, pc = abs(p-a), abs(p-b), abs(p-c)
                pr = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[x] = (line[x] + pr) & 255
        for x in range(w):
            px[i] = (line[4*x], line[4*x+1], line[4*x+2], line[4*x+3]); i += 1
        prev = line
    return w, h, px

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

# ---------------------------------------------------------------- helpers
def lum(c): return 0.2126*c[0] + 0.7152*c[1] + 0.0722*c[2]

def noise(x, y, seed):
    n = (x*374761393 + y*668265263 + seed*1442695040888963407) & 0xFFFFFFFFFFFFFFFF
    n = (n ^ (n >> 30)) * 0xBF58476D1CE4E5B9 & 0xFFFFFFFFFFFFFFFF
    n = (n ^ (n >> 27)) * 0x94D049BB133111EB & 0xFFFFFFFFFFFFFFFF
    return ((n ^ (n >> 31)) & 0xFFFF) / 65535.0

def upgrade(w, h, px, seed=7):
    """16x16 -> 32x32 faithful re-render."""
    W, H = w*2, h*2
    out = [(0,0,0,0)] * (W*H)
    def src(x, y):
        x = min(max(x, 0), w-1); y = min(max(y, 0), h-1)
        return px[y*w+x]
    for sy in range(h):
        for sx in range(w):
            c = src(sx, sy)
            if c[3] == 0:
                continue  # transparent stays transparent (2x2 block left as 0)
            # hard boundary detection (big color jump to a neighbor)
            e_right = abs(lum(c) - lum(src(sx+1, sy))) > 40
            e_down  = abs(lum(c) - lum(src(sx, sy+1))) > 40
            L = lum(c)
            for dy in range(2):
                for dx in range(2):
                    gx, gy = sx*2+dx, sy*2+dy
                    n = noise(gx, gy, seed)
                    # grain: +-6% luminance modulation, stronger on mid-tones
                    strength = 0.06 if 30 < L < 220 else 0.03
                    mod = 1.0 + (n - 0.5) * 2 * strength
                    r = min(255, int(c[0]*mod)); g = min(255, int(c[1]*mod)); b = min(255, int(c[2]*mod))
                    # inner edge shading: darken the subpixel touching a hard boundary
                    if dx == 1 and e_right: r,g,b = int(r*0.82), int(g*0.82), int(b*0.82)
                    if dy == 1 and e_down:  r,g,b = int(r*0.82), int(g*0.82), int(b*0.82)
                    # highlight opposite edge for beveled depth
                    if dx == 0 and e_right: r,g,b = min(255,int(r*1.10)), min(255,int(g*1.10)), min(255,int(b*1.10))
                    out[gy*W+gx] = (r, g, b, c[3])
    return W, H, out

# ---------------------------------------------------------------- main
changed, skipped = 0, 0
for d in OUT_DIRS:
    for p in sorted(glob.glob(f"{d}/*.png")):
        try:
            w, h, px = read_png(p)
        except Exception as ex:
            print(f"SKIP {p}: {ex}"); skipped += 1; continue
        if w == 16 and h % 16 == 0:
            W, H, out = upgrade(w, h, px)
            write_png(p, W, H, out)
            changed += 1
        elif w == 32:
            skipped += 1
        else:
            print(f"ODD  {p}: {w}x{h} (left alone)"); skipped += 1
print(f"upgraded {changed} textures to 32x, skipped {skipped}")
