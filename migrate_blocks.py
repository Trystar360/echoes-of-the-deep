#!/usr/bin/env python3
"""Block removal/placement hook migration for 26.1.
  onBlockAdded(...)  -> onPlace(...)            [same signature: pure rename]
  onStateReplaced(...) -> affectNeighborsAfterRemoval(BlockState, ServerLevel, BlockPos, boolean)
The removal hook now only fires on real removal and hands us a ServerLevel, so the
`!state.is(newState.getBlock())` / `instanceof ServerLevel` guards collapse away.
"""
import re, pathlib

NET = "ResonanceNetworkManager.get(world)"

def new_method(body_action):
    return (
        "    @Override\n"
        "    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {\n"
        f"{body_action}"
        "        super.affectNeighborsAfterRemoval(state, world, pos, moved);\n"
        "    }"
    )

# action bodies keyed by a unique substring of the original onStateReplaced body
ACTIONS = [
    ("onConduitBroken",
     f"        {NET}.onConduitBroken(pos.immutable());\n"),
    ("dropBankedLight",
     "        if (world.getBlockEntity(pos) instanceof TransmutationTableBlockEntity be) {\n"
     "            be.dropBankedLight(world, pos); // legacy banked Light -> Mote coins, never silently lost\n"
     "        }\n"),
    ("CrusherBlockEntity be",
     "        if (world.getBlockEntity(pos) instanceof CrusherBlockEntity be) {\n"
     "            Containers.spawn(world, pos, be.getItems());\n"
     "        }\n"
     f"        {NET}.onAttachedNodeChanged(pos.immutable());\n"),
    ("AttunementFurnaceBlockEntity be",
     "        if (world.getBlockEntity(pos) instanceof AttunementFurnaceBlockEntity be) {\n"
     "            Containers.spawn(world, pos, be.getItems());\n"
     "        }\n"
     f"        {NET}.onAttachedNodeChanged(pos.immutable());\n"),
    ("ResonantChestBlockEntity be",
     "        if (world.getBlockEntity(pos) instanceof ResonantChestBlockEntity be) {\n"
     "            Containers.spawn(world, pos, be.getItems());\n"
     "        }\n"),
    # default network-node block (must be last; generic onAttachedNodeChanged)
    ("onAttachedNodeChanged",
     f"        {NET}.onAttachedNodeChanged(pos.immutable());\n"),
]

# match the entire onStateReplaced method block
METHOD_RE = re.compile(
    r"[ \t]*@Override\n[ \t]*public void onStateReplaced\(.*?\n(?:.*?\n)*?[ \t]*super\.onStateReplaced\(.*?\);\n[ \t]*\}",
)

blockdir = pathlib.Path('src/main/java/com/echoes/block')
changed = 0
for f in sorted(blockdir.glob('*.java')):
    t = f.read_text()
    orig = t
    # pure rename of the placement hook
    t = t.replace('public void onBlockAdded(', 'protected void onPlace(')
    m = METHOD_RE.search(t)
    if m:
        block = m.group(0)
        action = None
        for key, act in ACTIONS:
            if key in block:
                action = act
                break
        if action is not None:
            t = t[:m.start()] + new_method(action) + t[m.end():]
    if t != orig:
        f.write_text(t)
        changed += 1
        print("updated", f.name)
print(f"rewrote {changed} block files")
