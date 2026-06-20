#!/usr/bin/env python3
"""Generate the "Great Work" advancement tree for Octaves of the One.

The mod's progression spine — Echocite -> Echo Ingot -> Coil/Cell/Conduit ->
Compressor/Transmuter -> radiation devices -> wireless -> the Octave climb
(Seed/Radiant) -> the transmutation economy — is encoded here as a single,
connected advancement tree so the whole mod explains itself in-game.

Run from the repo root:  python3 gen_advancements.py
It writes one JSON per advancement into
``src/main/resources/data/echoes/advancement/great_work/`` and prints the
matching ``advancement.echoes.*`` lang block to merge into ``en_us.json``
(the generator also patches en_us.json in place if --write-lang is passed).

This mirrors the project's procedural-asset philosophy (textures and the wiki
are generated too), so the tree stays consistent and easy to extend.
"""
from __future__ import annotations

import json
import os
import sys

MOD = "echoes"
OUT_DIR = os.path.join(
    "src", "main", "resources", "data", MOD, "advancement", "great_work"
)
LANG_PATH = os.path.join(
    "src", "main", "resources", "assets", MOD, "lang", "en_us.json"
)
TAG_DIR = os.path.join("src", "main", "resources", "data", MOD, "tags", "item")
BACKGROUND = "minecraft:textures/gui/advancements/backgrounds/stone.png"

# Reusable item tags for the "any of a family" advancement criteria. Tags are the
# idiomatic, guaranteed-to-parse way to express an OR over items, and they're handy
# elsewhere (recipe-viewers, future recipes) too.
TAGS = {
    "resonant_tools": [
        "echoes:resonant_pickaxe", "echoes:resonant_axe", "echoes:resonant_shovel",
        "echoes:resonant_sword", "echoes:resonant_hoe",
    ],
    "octave_stars": [
        f"echoes:octave_star_{i}" for i in range(1, 7)
    ],
}


def has_item(*items: str) -> dict:
    """An inventory_changed criterion that fires when the player holds the given
    item, any of the listed items, or any item in a ``#tag`` reference."""
    ids = list(items)
    spec = ids[0] if len(ids) == 1 else ids
    return {
        "trigger": "minecraft:inventory_changed",
        "conditions": {"items": [{"items": spec}]},
    }


# Each entry: key -> (parent, icon_id, frame, title, description, xp, *criterion_items)
# parent is None for the root. criterion_items default to [icon_id] when omitted.
A = "advancement"

NODES: list[dict] = [
    dict(key="root", parent=None, icon="echoes:raw_echocite", frame="task",
         title="Octaves of the One",
         desc="Mine Echocite and begin the Great Work.",
         xp=0, items=["echoes:raw_echocite"]),

    dict(key="echo_ingot", parent="root", icon="echoes:echo_ingot", frame="task",
         title="The First Tone",
         desc="Smelt Raw Echocite into an Echo Ingot — the core of every recipe.",
         xp=0),

    dict(key="resonant_coil", parent="echo_ingot", icon="echoes:resonant_coil",
         frame="task", title="A Winding Engine",
         desc="Craft a Resonant Coil — it winds ambient sound into stored Light.",
         xp=0),

    dict(key="resonance_cell", parent="resonant_coil", icon="echoes:resonance_cell",
         frame="task", title="Banked Light",
         desc="Craft a Resonance Cell to bank the Light your Coil makes.",
         xp=0),

    dict(key="wave_conduit", parent="resonant_coil", icon="echoes:wave_conduit",
         frame="task", title="Carried, Not Consumed",
         desc="Craft a Wave Conduit and wire Light from a source to a machine.",
         xp=0),

    dict(key="wave_relay", parent="wave_conduit", icon="echoes:wave_relay",
         frame="goal", title="Cut the Cord",
         desc="Craft a Wave Relay — beam items, fluids, and Light over a channel, "
              "no conduit required.",
         xp=50),

    dict(key="compressor", parent="resonant_coil", icon="echoes:compressor",
         frame="task", title="Doubling Down",
         desc="Craft a Compressor and double your ore with Light.",
         xp=0),

    dict(key="transmuter", parent="resonant_coil", icon="echoes:transmuter",
         frame="task", title="Fuelless Fire",
         desc="Craft a Transmuter — any furnace recipe, powered by Light, no fuel.",
         xp=0),

    dict(key="growth_radiator", parent="resonant_coil",
         icon="echoes:growth_radiator", frame="task", title="Light as Life",
         desc="Craft a Growth Radiator — pour Light back into the world to grow crops. "
              "The other half of the interchange.",
         xp=0),

    dict(key="warmth_radiator", parent="growth_radiator",
         icon="echoes:warmth_radiator", frame="task", title="A Hearth of Light",
         desc="Craft a Warmth Radiator to cook dropped items and melt the cold away.",
         xp=0),

    dict(key="polarity_field", parent="growth_radiator",
         icon="echoes:polarity_field", frame="task", title="Two Poles, One Field",
         desc="Craft a Polarity Field — Attract pulls items in, Repel casts mobs out.",
         xp=0),

    dict(key="balancer", parent="growth_radiator", icon="echoes:balancer",
         frame="task", title="The Grid Breathes Evenly",
         desc="Craft a Balancer so no cell hoards — Light evens across the network.",
         xp=0),

    dict(key="resonant_tools", parent="echo_ingot", icon="echoes:resonant_pickaxe",
         frame="task", title="Over-Tuned",
         desc="Craft any Resonant tool — deliberately stronger than netherite.",
         xp=0, items=["#echoes:resonant_tools"]),

    dict(key="resonant_thrusters", parent="resonant_tools",
         icon="echoes:resonant_thrusters", frame="challenge", title="Where You Look",
         desc="Craft the Resonant Thrusters and fly the direction of your gaze.",
         xp=100),

    dict(key="silentite", parent="root", icon="echoes:silentite_crystal",
         frame="goal", title="The Unstruck Tone",
         desc="Find Silentite in the Deep Dark — the silence the Octave is built on.",
         xp=50),

    dict(key="stillness_core", parent="silentite", icon="echoes:stillness_core",
         frame="task", title="The Still Centre",
         desc="Craft a Stillness Core — Light from rest, where all motion springs.",
         xp=0),

    dict(key="octave_seed", parent="silentite", icon="echoes:octave_seed",
         frame="task", title="The Rest Point",
         desc="Craft an Octave Seed — the catalyst that opens the transmutation chain.",
         xp=0),

    dict(key="radiant_ingot", parent="octave_seed", icon="echoes:radiant_ingot",
         frame="goal", title="Condensed Light",
         desc="Forge a Radiant Ingot — matter wound a full octave higher.",
         xp=50),

    dict(key="greater_resonance_cell", parent="radiant_ingot",
         icon="echoes:greater_resonance_cell", frame="task",
         title="High-Octave Bank",
         desc="Build a Greater Resonance Cell — store two million Light.",
         xp=0),

    dict(key="octave_coil", parent="radiant_ingot", icon="echoes:octave_coil",
         frame="task", title="The Higher Octave",
         desc="Build an Octave Coil — a strong baseline generator for the late grid.",
         xp=0),

    dict(key="storm_caller", parent="resonance_cell", icon="echoes:storm_caller",
         frame="goal", title="Catch the Sky",
         desc="Raise a Storm Caller under open sky and bank the lightning's windfall.",
         xp=50),

    dict(key="transmutation_table", parent="radiant_ingot",
         icon="echoes:transmutation_table", frame="goal",
         title="Balanced Interchange",
         desc="Build a Transmutation Table — dissolve matter into Bound Light, then "
              "condense it back into anything you've attuned.",
         xp=50),

    dict(key="octave_star", parent="transmutation_table", icon="echoes:octave_star_3",
         frame="challenge", title="Carry the Light",
         desc="Charge an Octave Star and carry your Bound Light in your pocket.",
         xp=100, items=["#echoes:octave_stars"]),

    dict(key="harmonic_mote", parent="transmutation_table",
         icon="echoes:harmonic_mote", frame="challenge", title="The Resolved Crest",
         desc="Wind a Mote all the way to the Harmonic tone — the chord resolved to "
              "balance.",
         xp=100),
]


def build(node: dict) -> dict:
    icon = node["icon"]
    items = node.get("items", [icon])
    adv: dict = {
        "display": {
            "icon": {"id": icon},
            "title": {"translate": f"{A}.{MOD}.{node['key']}.title"},
            "description": {"translate": f"{A}.{MOD}.{node['key']}.description"},
            "frame": node["frame"],
            "show_toast": True,
            "announce_to_chat": True,
            "hidden": False,
        },
        "criteria": {"unlock": has_item(*items)},
        "requirements": [["unlock"]],
    }
    if node["parent"] is None:
        adv["display"]["background"] = BACKGROUND
    else:
        adv["parent"] = f"{MOD}:great_work/{node['parent']}"
    if node.get("xp"):
        adv["rewards"] = {"experience": node["xp"]}
    return adv


def main() -> int:
    os.makedirs(TAG_DIR, exist_ok=True)
    for name, values in TAGS.items():
        with open(os.path.join(TAG_DIR, f"{name}.json"), "w", encoding="utf-8") as f:
            json.dump({"replace": False, "values": values}, f, indent=2)
            f.write("\n")
    print(f"Wrote {len(TAGS)} item tags to {TAG_DIR}")

    os.makedirs(OUT_DIR, exist_ok=True)
    for node in NODES:
        path = os.path.join(OUT_DIR, f"{node['key']}.json")
        with open(path, "w", encoding="utf-8") as f:
            json.dump(build(node), f, indent=2, ensure_ascii=False)
            f.write("\n")
    print(f"Wrote {len(NODES)} advancements to {OUT_DIR}")

    # Emit the lang block (and optionally splice it into en_us.json).
    lang: dict[str, str] = {}
    for node in NODES:
        lang[f"{A}.{MOD}.{node['key']}.title"] = node["title"]
        lang[f"{A}.{MOD}.{node['key']}.description"] = node["desc"]

    if "--write-lang" in sys.argv:
        with open(LANG_PATH, "r", encoding="utf-8") as f:
            data = json.load(f)
        data.update(lang)
        with open(LANG_PATH, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")
        print(f"Patched {len(lang)} lang keys into {LANG_PATH}")
    else:
        print("\nLang keys (add to en_us.json, or re-run with --write-lang):")
        print(json.dumps(lang, indent=2, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
