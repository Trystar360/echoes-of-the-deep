#!/usr/bin/env python3
"""Third Yarn->Mojmap pass: block-Properties builder + misc, verified vs jar."""
import re, pathlib

SUBS = [
    (r'\.requiresTool\(', '.requiresCorrectToolForDrops('),
    (r'\.nonOpaque\(', '.noOcclusion('),
    (r'\.sounds\(', '.sound('),
    (r'\.ticksRandomly\(', '.randomTicks('),
    (r'\.registryKey\(', '.setId('),
    (r'\.getRegistryKey\(', '.dimension('),
    (r'\.getRecipeManager\(', '.recipeAccess('),
    (r'\b(ctx|context)\.getBlockPos\(\)', r'\1.getClickedPos()'),
    (r'\b(e|entity)\.getBlockPos\(\)', r'\1.blockPosition()'),
    (r'\bVec3d\.ofCenter\(', 'Vec3.atCenterOf('),
    (r'\bVec3d\b', 'Vec3'),
]

# markDirty -> setChanged everywhere EXCEPT the energy network files, where
# ResonanceNetwork.markDirty() is a bespoke method that must keep its name.
EXCLUDE_MARKDIRTY = {'ResonanceNetwork.java', 'ResonanceNetworkManager.java'}

roots = [pathlib.Path('src/main/java'), pathlib.Path('src/client/java')]
changed = 0
for root in roots:
    if not root.exists():
        continue
    for f in root.rglob('*.java'):
        t = f.read_text()
        n = t
        for pat, rep in SUBS:
            n = re.sub(pat, rep, n)
        if f.name not in EXCLUDE_MARKDIRTY:
            n = re.sub(r'\bmarkDirty\(', 'setChanged(', n)
        if n != t:
            f.write_text(n)
            changed += 1
print(f"rewrote {changed} files")
