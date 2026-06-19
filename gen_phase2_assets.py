#!/usr/bin/env python3
"""Generate blockstates, models, loot tables, recipes and tags for the Phase II
"Octave Grove" content. Idempotent: safe to re-run. Pure stdlib."""
import json, os

ROOT = "src/main/resources"
A = f"{ROOT}/assets/echoes"
D = f"{ROOT}/data/echoes"
MC = "echoes"

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")

def bs(name, obj):      w(f"{A}/blockstates/{name}.json", obj)
def bmodel(name, obj):  w(f"{A}/models/block/{name}.json", obj)
def imodel(name, obj):  w(f"{A}/models/item/{name}.json", obj)
def loot(name, obj):    w(f"{D}/loot_table/blocks/{name}.json", obj)
def recipe(name, obj):  w(f"{D}/recipe/{name}.json", obj)

def tex(n): return f"{MC}:block/{n}"

# ---------------------------------------------------------------- simple drop-self loot
def self_drop(name):
    loot(name, {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "bonus_rolls": 0,
                   "entries": [{"type": "minecraft:item", "name": f"{MC}:{name}"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}]
    })

def slab_drop(name):
    loot(name, {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{
            "type": "minecraft:item", "name": f"{MC}:{name}",
            "functions": [{
                "function": "minecraft:set_count",
                "count": 2,
                "conditions": [{"condition": "minecraft:block_state_property",
                                "block": f"{MC}:{name}",
                                "properties": {"type": "double"}}]
            }, {"function": "minecraft:explosion_decay"}]
        }], "conditions": [{"condition": "minecraft:survives_explosion"}]}]
    })

# ---------------------------------------------------------------- cube_all
def cube_all(name, glow_item=False):
    bs(name, {"variants": {"": {"model": f"{MC}:block/{name}"}}})
    bmodel(name, {"parent": "minecraft:block/cube_all", "textures": {"all": tex(name)}})
    imodel(name, {"parent": f"{MC}:block/{name}"})
    self_drop(name)

# ---------------------------------------------------------------- pillar (log/wood)
def pillar(name, side_tex, end_tex):
    bs(name, {"variants": {
        "axis=y": {"model": f"{MC}:block/{name}"},
        "axis=z": {"model": f"{MC}:block/{name}", "x": 90},
        "axis=x": {"model": f"{MC}:block/{name}", "x": 90, "y": 90}
    }})
    bmodel(name, {"parent": "minecraft:block/cube_column",
                  "textures": {"end": tex(end_tex), "side": tex(side_tex)}})
    imodel(name, {"parent": f"{MC}:block/{name}"})
    self_drop(name)

# ---------------------------------------------------------------- stairs
def stairs(name, base_tex):
    t = {"bottom": tex(base_tex), "top": tex(base_tex), "side": tex(base_tex)}
    bmodel(name, {"parent": "minecraft:block/stairs", "textures": t})
    bmodel(f"{name}_inner", {"parent": "minecraft:block/inner_stairs", "textures": t})
    bmodel(f"{name}_outer", {"parent": "minecraft:block/outer_stairs", "textures": t})
    imodel(name, {"parent": f"{MC}:block/{name}"})
    variants = {}
    facings = {"east": 0, "south": 90, "west": 180, "north": 270}
    for facing, yf in facings.items():
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                key = f"facing={facing},half={half},shape={shape}"
                model = f"{MC}:block/{name}"
                if "inner" in shape: model += "_inner"
                elif "outer" in shape: model += "_outer"
                v = {"model": model}
                y = yf
                if half == "top": v["x"] = 180
                # shape rotation adjustments
                if shape == "inner_left" or shape == "outer_left":
                    y = (yf + 270) % 360 if half == "bottom" else (yf + 90) % 360
                if shape == "inner_right" or shape == "outer_right":
                    if half == "top": y = (yf + 270) % 360 if shape == "outer_right" else yf
                # use vanilla-equivalent table
                v = stair_variant(name, facing, half, shape)
                variants[key] = v
    bs(name, {"variants": variants})
    self_drop(name)

def stair_variant(name, facing, half, shape):
    # Mirrors vanilla oak_stairs blockstate rotations.
    base = f"{MC}:block/{name}"
    inner = base + "_inner"; outer = base + "_outer"
    fmap = {"north": 0, "east": 1, "south": 2, "west": 3}
    fi = fmap[facing]
    top = half == "top"
    def rot(model, y, x=None):
        v = {"model": model}
        if y: v["y"] = y % 360
        if x: v["x"] = x
        if top: v["x"] = 180
        if y: v["y"] = y % 360
        return v
    # Build per vanilla mapping
    if shape == "straight":
        ymap = {"north": 270, "east": 0, "south": 90, "west": 180}
        v = {"model": base}
        if not top and ymap[facing]: v["y"] = ymap[facing]
        if top:
            v["x"] = 180
            ytop = {"north": 270, "east": 0, "south": 90, "west": 180}
            if ytop[facing]: v["y"] = ytop[facing]
        return v
    if shape == "outer_right":
        ymap = {"north": 270, "east": 0, "south": 90, "west": 180}
        v = {"model": outer}
        if not top and ymap[facing]: v["y"] = ymap[facing]
        if top:
            v["x"] = 180
            yt = {"north": 0, "east": 90, "south": 180, "west": 270}
            if yt[facing]: v["y"] = yt[facing]
        return v
    if shape == "outer_left":
        ymap = {"north": 180, "east": 270, "south": 0, "west": 90}
        v = {"model": outer}
        if not top:
            if ymap[facing]: v["y"] = ymap[facing]
        else:
            v["x"] = 180
            yt = {"north": 270, "east": 0, "south": 90, "west": 180}
            if yt[facing]: v["y"] = yt[facing]
        return v
    if shape == "inner_right":
        ymap = {"north": 270, "east": 0, "south": 90, "west": 180}
        v = {"model": inner}
        if not top and ymap[facing]: v["y"] = ymap[facing]
        if top:
            v["x"] = 180
            yt = {"north": 0, "east": 90, "south": 180, "west": 270}
            if yt[facing]: v["y"] = yt[facing]
        return v
    # inner_left
    ymap = {"north": 180, "east": 270, "south": 0, "west": 90}
    v = {"model": inner}
    if not top:
        if ymap[facing]: v["y"] = ymap[facing]
    else:
        v["x"] = 180
        yt = {"north": 270, "east": 0, "south": 90, "west": 180}
        if yt[facing]: v["y"] = yt[facing]
    return v

# ---------------------------------------------------------------- slab
def slab(name, base_tex, double_block):
    t = {"bottom": tex(base_tex), "top": tex(base_tex), "side": tex(base_tex)}
    bmodel(name, {"parent": "minecraft:block/slab", "textures": t})
    bmodel(f"{name}_top", {"parent": "minecraft:block/slab_top", "textures": t})
    imodel(name, {"parent": f"{MC}:block/{name}"})
    bs(name, {"variants": {
        "type=bottom": {"model": f"{MC}:block/{name}"},
        "type=top": {"model": f"{MC}:block/{name}_top"},
        "type=double": {"model": f"{MC}:block/{double_block}"}
    }})
    slab_drop(name)

# ---------------------------------------------------------------- fence
def fence(name, base_tex):
    t = {"texture": tex(base_tex)}
    bmodel(f"{name}_post", {"parent": "minecraft:block/fence_post", "textures": t})
    bmodel(f"{name}_side", {"parent": "minecraft:block/fence_side", "textures": t})
    bmodel(f"{name}_inventory", {"parent": "minecraft:block/fence_inventory", "textures": t})
    imodel(name, {"parent": f"{MC}:block/{name}_inventory"})
    apply = []
    apply.append({"apply": {"model": f"{MC}:block/{name}_post"}})
    sides = {"north": 0, "east": 90, "south": 180, "west": 270}
    multipart = [{"apply": {"model": f"{MC}:block/{name}_post"}}]
    for s, y in sides.items():
        e = {"when": {s: "true"}, "apply": {"model": f"{MC}:block/{name}_side", "uvlock": True}}
        if y: e["apply"]["y"] = y
        multipart.append(e)
    bs(name, {"multipart": multipart})
    self_drop(name)

# ---------------------------------------------------------------- fence gate
def fence_gate(name, base_tex):
    t = {"texture": tex(base_tex)}
    for suffix, parent in [("", "template_fence_gate"),
                           ("_open", "template_fence_gate_open"),
                           ("_wall", "template_fence_gate_wall"),
                           ("_wall_open", "template_fence_gate_wall_open")]:
        bmodel(f"{name}{suffix}", {"parent": f"minecraft:block/{parent}", "textures": t})
    imodel(name, {"parent": f"{MC}:block/{name}"})
    facings = {"north": 0, "east": 90, "south": 180, "west": 270}
    variants = {}
    for f, y in facings.items():
        for in_wall in ("false", "true"):
            for op in ("false", "true"):
                wall = "_wall" if in_wall == "true" else ""
                opens = "_open" if op == "true" else ""
                key = f"facing={f},in_wall={in_wall},open={op}"
                v = {"model": f"{MC}:block/{name}{wall}{opens}", "uvlock": True}
                if y: v["y"] = y
                variants[key] = v
    bs(name, {"variants": variants})
    self_drop(name)

# ---------------------------------------------------------------- trapdoor
def trapdoor(name, base_tex):
    t = {"texture": tex(base_tex)}
    bmodel(f"{name}_bottom", {"parent": "minecraft:block/template_orientable_trapdoor_bottom", "textures": t})
    bmodel(f"{name}_top", {"parent": "minecraft:block/template_orientable_trapdoor_top", "textures": t})
    bmodel(f"{name}_open", {"parent": "minecraft:block/template_orientable_trapdoor_open", "textures": t})
    imodel(name, {"parent": f"{MC}:block/{name}_bottom"})
    facings = {"north": 0, "east": 90, "south": 180, "west": 270}
    variants = {}
    for f, y in facings.items():
        for half in ("bottom", "top"):
            for op in ("true", "false"):
                key = f"facing={f},half={half},open={op}"
                if op == "true":
                    v = {"model": f"{MC}:block/{name}_open"}
                    if y: v["y"] = y
                    if half == "top": v["x"] = 180
                else:
                    model = f"{MC}:block/{name}_{'top' if half == 'top' else 'bottom'}"
                    v = {"model": model}
                    if y: v["y"] = y
                variants[key] = v
    bs(name, {"variants": variants})
    self_drop(name)

# ---------------------------------------------------------------- cross (sapling / flower)
def cross(name, item_flat=True):
    bs(name, {"variants": {"": {"model": f"{MC}:block/{name}"}}})
    bmodel(name, {"parent": "minecraft:block/cross", "textures": {"cross": tex(name)}})
    if item_flat:
        imodel(name, {"parent": "minecraft:item/generated", "textures": {"layer0": tex(name)}})
    self_drop(name)

# ================================================================ build everything
# Lumewood building set
pillar("lumewood_log", "lumewood_log", "lumewood_log_top")
pillar("lumewood_wood", "lumewood_log", "lumewood_log")
cube_all("lumewood_planks")
stairs("lumewood_stairs", "lumewood_planks")
slab("lumewood_slab", "lumewood_planks", "lumewood_planks")
fence("lumewood_fence", "lumewood_planks")
fence_gate("lumewood_fence_gate", "lumewood_planks")
trapdoor("lumewood_trapdoor", "lumewood_trapdoor")
cube_all("lumewood_leaves")
cross("lumewood_sapling")

# Garden
cross("lumebloom")
cube_all("lume_lantern")
cube_all("verdant_loam")

# Stone building
cube_all("echocite_bricks")
stairs("echocite_brick_stairs", "echocite_bricks")
slab("echocite_brick_slab", "echocite_bricks", "echocite_bricks")

# Greater Accumulator (animated like the capacitor; cube_all)
cube_all("greater_accumulator")

# Simple item models for new items
for it in ("octave_seed", "radiant_dust", "radiant_ingot"):
    imodel(it, {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MC}:item/{it}"}})

# ================================================================ recipes
def shaped(name, pattern, keys, result, count=1):
    recipe(name, {"type": "minecraft:crafting_shaped", "category": "building",
                  "pattern": pattern,
                  "key": {k: v for k, v in keys.items()},
                  "result": {"id": f"{MC}:{result}", "count": count}})

def shapeless(name, ingredients, result, count=1):
    recipe(name, {"type": "minecraft:crafting_shapeless", "category": "misc",
                  "ingredients": list(ingredients),
                  "result": {"id": f"{MC}:{result}", "count": count}})

def smelt(name, ingredient, result, xp=0.5, time=200, blasting=False):
    recipe(name, {"type": "minecraft:blasting" if blasting else "minecraft:smelting",
                  "category": "misc", "ingredient": ingredient,
                  "result": {"id": f"{MC}:{result}"}, "experience": xp,
                  "cookingtime": time if not blasting else time // 2})

P = f"{MC}:lumewood_planks"; L = f"{MC}:lumewood_log"; S = "minecraft:stick"
shapeless("lumewood_planks", [L], "lumewood_planks", 4)
shaped("lumewood_wood", ["##", "##"], {"#": L}, "lumewood_wood", 3)
shaped("lumewood_stairs", ["#  ", "## ", "###"], {"#": P}, "lumewood_stairs", 4)
shaped("lumewood_slab", ["###"], {"#": P}, "lumewood_slab", 6)
shaped("lumewood_fence", ["#/#", "#/#"], {"#": P, "/": S}, "lumewood_fence", 3)
shaped("lumewood_fence_gate", ["/#/", "/#/"], {"#": P, "/": S}, "lumewood_fence_gate", 1)
shaped("lumewood_trapdoor", ["###", "###"], {"#": P}, "lumewood_trapdoor", 2)

B = f"{MC}:echocite_bricks"
shaped("echocite_bricks", ["##", "##"], {"#": f"{MC}:echocite_dust"}, "echocite_bricks", 4)
shaped("echocite_brick_stairs", ["#  ", "## ", "###"], {"#": B}, "echocite_brick_stairs", 4)
shaped("echocite_brick_slab", ["###"], {"#": B}, "echocite_brick_slab", 6)

# Garden
shapeless("lume_lantern", [f"{MC}:lumebloom", f"{MC}:echocite_dust"], "lume_lantern", 1)
shaped("verdant_loam", ["#g#", "gdg", "#g#"],
       {"#": "minecraft:dirt", "g": "minecraft:bone_meal", "d": f"{MC}:echocite_dust"},
       "verdant_loam", 4)

# Inert-gas Seed (progression catalyst) + transmutation chain
shapeless("octave_seed", [f"{MC}:silentite_crystal", f"{MC}:drum_core", f"{MC}:echo_dust"],
          "octave_seed", 1)
shapeless("radiant_dust", [f"{MC}:echocite_dust", f"{MC}:echocite_dust",
                           f"{MC}:echocite_dust", f"{MC}:echocite_dust",
                           f"{MC}:octave_seed"], "radiant_dust", 4)
smelt("radiant_ingot_from_smelting", f"{MC}:radiant_dust", "radiant_ingot")
smelt("radiant_ingot_from_blasting", f"{MC}:radiant_dust", "radiant_ingot", blasting=True)
# Tier II Accumulator: charge a Capacitor with radiant ingots (block-of-light tier)
shaped("greater_accumulator", ["###", "#C#", "###"],
       {"#": f"{MC}:radiant_ingot", "C": f"{MC}:resonance_capacitor"}, "greater_accumulator", 1)

print("Phase II assets generated.")
