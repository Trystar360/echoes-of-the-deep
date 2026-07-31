#!/usr/bin/env python3
"""Verifier v1 — texture resolution check: every block/item texture must be
32x32, or 32x(32*n) for animated vertical strips."""
import glob, os, struct, sys

ROOT = os.path.dirname(os.path.abspath(__file__)) + "/../.."
os.chdir(ROOT)

def png_size(p):
    with open(p, "rb") as f:
        d = f.read(33)
    if d[:8] != b"\x89PNG\r\n\x1a\n": return None
    w, h = struct.unpack(">II", d[16:24])
    return w, h

bad = []
checked = 0
for p in glob.glob("src/main/resources/assets/echoes/textures/block/*.png") + \
         glob.glob("src/main/resources/assets/echoes/textures/item/*.png"):
    sz = png_size(p)
    checked += 1
    if sz is None:
        bad.append(f"{p}: unreadable"); continue
    w, h = sz
    ok = (w == 32 and h == 32) or (w == 32 and h % 32 == 0)
    if not ok:
        bad.append(f"{p}: {w}x{h}")

if bad:
    print(f"{len(bad)}/{checked} textures not 32x:")
    for b in bad[:60]: print(" -", b)
    sys.exit(1)
print(f"all {checked} block/item textures are 32x (or 32x animated strips)")
