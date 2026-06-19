#!/usr/bin/env python3
"""Render isometric 3D block icons (modpack-wiki style) from the real textures.

For each block we project a cube in 2:1 isometric, texture-mapping the top and
two visible side faces and shading them so the block reads as 3D. Directional
machines use their glowing front + the shared bronze device_side / device_top
casing; uniform blocks (ores, conduits, capacitor) use their single texture on
every face.

Output: docs/wiki/images/blocks3d/<name>.png  (transparent background)

Pure-Python PNG I/O (no Pillow), matching gallery.py / montage.py.
"""
import struct, zlib, os

SRC = "src/main/resources/assets/echoes/textures/block"
OUT = "docs/wiki/images/blocks3d"
K = 48           # px per iso unit (cube half-width); image is 2K wide
PAD = 8


def read_png(path):
    d = open(path, "rb").read()
    i = 8; w = h = 0; idat = b""
    while i < len(d):
        ln = struct.unpack(">I", d[i:i+4])[0]; typ = d[i+4:i+8]; data = d[i+8:i+8+ln]
        if typ == b"IHDR":
            w, h = struct.unpack(">II", data[:8])
        elif typ == b"IDAT":
            idat += data
        elif typ == b"IEND":
            break
        i += 12 + ln
    raw = zlib.decompress(idat)
    px = [(0, 0, 0, 0)] * (w * h); pos = 0
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

    open(path, "wb").write(
        b"\x89PNG\r\n\x1a\n"
        + ch(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
        + ch(b"IDAT", zlib.compress(bytes(raw), 9))
        + ch(b"IEND", b"")
    )


def frame0(name):
    """Top 16x16 frame of a (possibly animated) block texture."""
    w, h, px = read_png(f"{SRC}/{name}.png")
    return [[px[y*w+x] for x in range(16)] for y in range(16)]


def project(x, y, z):
    u = (x - z)
    v = (x + z) * 0.5 - y
    return (PAD + K + u * K, PAD + K + v * K)


# Each face: 3 corners O,A,B with s=0t=0, s=1t=0, s=0t=1, plus texture + shade.
def faces(front, side, top):
    return [
        # TOP (y=1): s->x, t->z
        ((0, 1, 0), (1, 1, 0), (0, 1, 1), top, 1.00),
        # FRONT (z=1): s->x, t->-y
        ((0, 1, 1), (1, 1, 1), (0, 0, 1), front, 0.80),
        # RIGHT (x=1): s->-z, t->-y
        ((1, 1, 1), (1, 1, 0), (1, 0, 1), side, 0.62),
    ]


def render(name, front, side, top):
    W = 2 * K + 2 * PAD
    H = int(1.5 * K) + 2 * PAD
    img = [(0, 0, 0, 0)] * (W * H)
    for O3, A3, B3, tex, shade in faces(front, side, top):
        O = project(*O3); A = project(*A3); B = project(*B3)
        e1 = (A[0]-O[0], A[1]-O[1]); e2 = (B[0]-O[0], B[1]-O[1])
        det = e1[0]*e2[1] - e1[1]*e2[0]
        if det == 0:
            continue
        xs = [O[0], A[0], B[0], O[0]+e1[0]+e2[0]]
        ys = [O[1], A[1], B[1], O[1]+e1[1]+e2[1]]
        for py in range(max(0, int(min(ys))), min(H, int(max(ys))+1)):
            for px_ in range(max(0, int(min(xs))), min(W, int(max(xs))+1)):
                dx = px_ + 0.5 - O[0]; dy = py + 0.5 - O[1]
                s = (dx*e2[1] - dy*e2[0]) / det
                t = (e1[0]*dy - e1[1]*dx) / det
                if 0 <= s < 1 and 0 <= t < 1:
                    c = tex[min(15, int(t*16))][min(15, int(s*16))]
                    if c[3] == 0:
                        continue
                    img[py*W+px_] = (int(c[0]*shade), int(c[1]*shade), int(c[2]*shade), 255)
    write_png(f"{OUT}/{name}.png", W, H, img)


# Directional machines: glowing front + shared bronze casing.
MACHINES = ["stillness_core", "resonant_coil", "growth_radiator", "warmth_radiator",
            "polarity_field", "balancer", "transmuter", "compressor",
            "wave_relay", "wave_repeater", "wave_coupler", "wave_chest",
            "signal_relay", "wave_amplifier", "wave_filter", "wave_splitter"]
# Uniform blocks: one texture on every face.
UNIFORM = ["echocite_ore", "deepslate_echocite_ore", "drumstone_ore", "silentite_ore",
           "wave_conduit", "dense_wave_conduit", "resonance_cell"]


def main():
    os.makedirs(OUT, exist_ok=True)
    side = frame0("device_side"); top = frame0("device_top")
    for m in MACHINES:
        render(m, frame0(m), side, top)
    for u in UNIFORM:
        tex = frame0(u)
        render(u, tex, tex, tex)
    print(f"wrote {len(MACHINES)+len(UNIFORM)} isometric block renders to {OUT}")


if __name__ == "__main__":
    main()
