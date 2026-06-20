#!/usr/bin/env python3
"""Generate per-block / per-item icon PNGs for the wiki from the real textures.

Reads each sprite's first 16x16 frame (textures may be animated vertical strips),
upscales it with nearest-neighbour to a crisp icon, and writes it to
docs/wiki/images/icons/<name>.png. Also copies the two existing montages into
docs/wiki/images/ so the wiki is self-contained.

Pure-Python PNG I/O (no Pillow), matching gallery.py / montage.py.
"""
import struct, zlib, os, shutil

SRC = "src/main/resources/assets/echoes/textures"
OUT = "docs/wiki/images"
ICONS = f"{OUT}/icons"
SCALE = 4  # 16px -> 64px


def read_png(path):
    d = open(path, "rb").read()
    assert d[:8] == b"\x89PNG\r\n\x1a\n", path
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
        pos += 1  # filter byte (assumed 0)
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


def icon(name, kind):
    """Upscale the top 16x16 frame of a sprite to a SCALE*16 transparent icon."""
    w, h, px = read_png(f"{SRC}/{kind}/{name}.png")
    S = SCALE; W = 16 * S
    out = [(0, 0, 0, 0)] * (W * W)
    for y in range(16):
        for x in range(16):
            c = px[y*w+x]
            for sy in range(S):
                for sx in range(S):
                    out[(y*S+sy)*W + (x*S+sx)] = c
    write_png(f"{ICONS}/{name}.png", W, W, out)


BLOCKS = ["echocite_ore", "deepslate_echocite_ore", "drumstone_ore", "silentite_ore",
          "stillness_core", "resonant_coil", "wave_conduit", "dense_wave_conduit",
          "resonance_cell", "compressor", "transmuter", "growth_radiator",
          "warmth_radiator", "polarity_field", "balancer", "wave_relay",
          "wave_amplifier", "wave_filter", "wave_splitter",
          "wave_repeater", "wave_coupler", "wave_chest", "signal_relay",
          "transmutation_table"]
ITEMS = ["raw_echocite", "echocite_dust", "echo_ingot", "dull_ingot", "resonant_slag",
         "drumstone_shard", "drum_core", "silentite_crystal", "echo_dust",
         "wave_tuner", "wave_atlas", "light_meter", "resonant_thrusters",
         "resonant_pickaxe", "resonant_axe", "resonant_shovel", "resonant_sword",
         "resonant_hoe",
         "transmutation_tablet", "light_mote", "tonic_mote", "mediant_mote",
         "dominant_mote", "harmonic_mote",
         "octave_star_1", "octave_star_2", "octave_star_3",
         "octave_star_4", "octave_star_5", "octave_star_6"]


def main():
    os.makedirs(ICONS, exist_ok=True)
    for b in BLOCKS:
        icon(b, "block")
    for it in ITEMS:
        icon(it, "item")
    # Carry the overview montages alongside the wiki so links stay relative.
    for m in ("textures.png", "machines.png"):
        shutil.copyfile(f"docs/images/{m}", f"{OUT}/{m}")
    print(f"wrote {len(BLOCKS)+len(ITEMS)} icons to {ICONS} and 2 montages to {OUT}")


if __name__ == "__main__":
    main()
