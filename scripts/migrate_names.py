#!/usr/bin/env python3
"""One-shot migration to the cohesive naming scheme. Idempotent-safe: only
renames whole-token old ids and exact old display-name phrases."""
import os, re, sys

ROOT = "/home/user/echoes-of-the-deep"

# old_id -> new_id  (registry ids; drives file renames + content tokens + lang keys)
ID = {
    "resonator": "resonant_coil",
    "resonance_capacitor": "resonance_cell",
    "greater_accumulator": "greater_resonance_cell",
    "tuning_conduit": "wave_conduit",
    "dense_conduit": "dense_wave_conduit",
    "crusher": "compressor",
    "attunement_furnace": "transmuter",
    "radiator": "growth_radiator",
    "resonant_relay": "wave_relay",
    "resonant_amplifier": "wave_amplifier",
    "harmonic_filter": "wave_filter",
    "resonant_splitter": "wave_splitter",
    "echo_repeater": "wave_repeater",
    "conduit_coupler": "wave_coupler",
    "resonant_chest": "wave_chest",
    "note_relay": "signal_relay",
    "frequency_tuner": "wave_tuner",
    "channel_atlas": "wave_atlas",
    "resonance_meter": "light_meter",
    "resonance_thrusters": "resonant_thrusters",
}

# display-name phrases that CHANGE (order: longest/most-specific first).
# "Radiator" handled specially below (must not touch "Warmth Radiator").
NAME = [
    ("Greater Accumulator", "Greater Resonance Cell"),
    ("Generative Coil", "Resonant Coil"),
    ("Amplitude Coil", "Wave Amplifier"),
    ("Harmonic Filter", "Wave Filter"),
    ("Interchange Splitter", "Wave Splitter"),
    ("Octave Repeater", "Wave Repeater"),
    ("Polarity Coupler", "Wave Coupler"),
    ("Locked Potential Vault", "Wave Chest"),
    ("Centrifugal Thrusters", "Resonant Thrusters"),
    ("Tone Relay", "Signal Relay"),
    ("Octave Tuner", "Wave Tuner"),
    ("Octave Atlas", "Wave Atlas"),
    ("Accumulator", "Resonance Cell"),  # after "Greater Accumulator"
]

# id regexes, longest-first, whole-token (not flanked by word chars)
ID_RES = [(re.compile(r"(?<![A-Za-z0-9_])" + re.escape(o) + r"(?![A-Za-z0-9_])"), n)
          for o, n in sorted(ID.items(), key=lambda kv: -len(kv[0]))]
# display-name regexes, word-boundary so they can never match inside an
# identifier (e.g. "Accumulator" inside GreaterAccumulatorBlock stays put).
NAME_RES = [(re.compile(r"\b" + re.escape(o) + r"\b"), n) for o, n in NAME]
# standalone "Radiator" not preceded by "Warmth " or "Growth "
RAD_RE = re.compile(r"(?<!Warmth )(?<!Growth )\bRadiator\b")

ID_EXTS = {".java", ".json", ".mcmeta", ".py", ".md", ".txt"}
NAME_EXTS = {".java", ".json", ".py", ".md", ".txt"}  # word-boundary-safe; NOT .mcmeta
SKIP_DIRS = {".git", "build", ".gradle"}
SKIP_FILES = {
    os.path.join(ROOT, "docs", "naming-proposal.md"),  # historical record
    os.path.join(ROOT, "scripts", "migrate_names.py"),
}

def sub_content(text, ext):
    if ext in ID_EXTS:
        for rx, n in ID_RES:
            text = rx.sub(n, text)
    if ext in NAME_EXTS:
        for rx, n in NAME_RES:
            text = rx.sub(n, text)
        text = RAD_RE.sub("Growth Radiator", text)
    return text

def walk_content(base):
    changed = 0
    for dp, dns, fns in os.walk(base):
        dns[:] = [d for d in dns if d not in SKIP_DIRS]
        if os.path.join(ROOT, "docs", "site") in dp:  # regenerated separately
            continue
        for fn in fns:
            p = os.path.join(dp, fn)
            ext = os.path.splitext(fn)[1]
            if p in SKIP_FILES or (ext not in ID_EXTS and ext not in NAME_EXTS):
                continue
            try:
                old = open(p, encoding="utf-8").read()
            except (UnicodeDecodeError, IsADirectoryError):
                continue
            new = sub_content(old, ext)
            if new != old:
                open(p, "w", encoding="utf-8").write(new)
                changed += 1
    return changed

def rename_files(base):
    olds = sorted(ID, key=len, reverse=True)
    renamed = 0
    # bottom-up so dir contents move before any dir (none here, but safe)
    for dp, dns, fns in os.walk(base, topdown=False):
        if ".git" in dp or "/build" in dp:
            continue
        for fn in fns:
            for o in olds:
                if fn == o or fn.startswith(o + ".") or fn.startswith(o + "_"):
                    newfn = ID[o] + fn[len(o):]
                    os.rename(os.path.join(dp, fn), os.path.join(dp, newfn))
                    renamed += 1
                    break
    return renamed

if __name__ == "__main__":
    c = walk_content(os.path.join(ROOT, "src"))
    c += walk_content(os.path.join(ROOT, "scripts"))
    c += walk_content(os.path.join(ROOT, "docs"))
    for f in ("gallery.py", "gen_phase2_assets.py", "gen_textures.py", "montage.py"):
        p = os.path.join(ROOT, f)
        if os.path.exists(p):
            t = open(p, encoding="utf-8").read(); n = sub_content(t, ".py")
            if n != t:
                open(p, "w", encoding="utf-8").write(n); c += 1
    r = rename_files(os.path.join(ROOT, "src", "main", "resources"))
    print(f"content files changed: {c}")
    print(f"resource files renamed: {r}")
