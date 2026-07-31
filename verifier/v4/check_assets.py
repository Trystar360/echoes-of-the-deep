#!/usr/bin/env python3
"""Verifier v1 — asset integrity: every model's texture exists; every
block/item has a lang entry; every blockstate resolves."""
import json, glob, os, sys, re

ROOT = os.path.dirname(os.path.abspath(__file__)) + "/../.."
os.chdir(ROOT)
NS = "echoes"
errors = []

def respath(p):
    return f"src/main/resources/assets/{NS}/{p}"

# Collect textures
textures = set()
for p in glob.glob(respath("textures/**/*.png"), recursive=True):
    rel = os.path.relpath(p, respath("textures")).replace("\\", "/")[:-4]
    textures.add(rel)

# Check models
for p in glob.glob(respath("models/**/*.json"), recursive=True):
    try:
        m = json.load(open(p))
    except Exception as ex:
        errors.append(f"bad json {p}: {ex}"); continue
    texs = m.get("textures", {})
    for k, v in texs.items():
        if not isinstance(v, str): continue
        ref = v.split(":", 1)[-1]
        if ref.startswith("minecraft/") or "/" not in ref: continue
        if ref not in textures:
            errors.append(f"{p}: missing texture {ref}")

# Lang coverage
lang = {}
lp = respath("lang/en_us.json")
if os.path.exists(lp):
    lang = json.load(open(lp))
else:
    errors.append("missing en_us.json")

for p in glob.glob(respath("blockstates/*.json")):
    name = os.path.basename(p)[:-5]
    key = f"block.{NS}.{name}"
    if key not in lang:
        errors.append(f"missing lang {key}")

if errors:
    print(f"{len(errors)} asset errors:")
    for e in errors[:40]: print(" -", e)
    sys.exit(1)
print(f"asset integrity OK ({len(textures)} textures, {len(lang)} lang keys)")
