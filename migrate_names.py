#!/usr/bin/env python3
"""Global Yarn->Mojmap name-only renames for the 26.1 port. Verified against the
deobf jar. Signature-changing overrides (onStateReplaced, onBlockAdded, sendMessage)
are handled by a separate pass, not here."""
import re, sys, pathlib

# (regex, replacement) applied per-line. Word boundaries keep them from biting custom names.
SUBS = [
    (r'getStateManager\(\)\.getDefaultState\(\)', 'getStateDefinition().any()'),
    (r'\bgetStateManager\(\)', 'getStateDefinition()'),
    (r'\bsetDefaultState\(', 'registerDefaultState('),
    (r'\bappendProperties\(', 'createBlockStateDefinition('),
    (r'\bgetPlacementState\(', 'getStateForPlacement('),
    (r'\.getHorizontalPlayerFacing\(', '.getHorizontalDirection('),
    (r'\.getSide\(\)', '.getClickedFace()'),
    (r'\bcreateBlockEntity\(', 'newBlockEntity('),
    (r'\.isClient\b(?!Side)', '.isClientSide()'),
    (r'\.isSneaking\(', '.isShiftKeyDown('),
    (r'\.getMainHandStack\(', '.getMainHandItem('),
    (r'\.getStackInHand\(', '.getItemInHand('),
    (r'\.isOf\(', '.is('),
    (r'\.canInsert\(', '.mayPlace('),
    (r'\bonUse\(', 'useWithoutItem('),
    (r'\.with\(', '.setValue('),
    (r'\bBlock\.NOTIFY_ALL\b', 'Block.UPDATE_ALL'),
]

# bare Block.getDefaultState() -> defaultBlockState(); must run AFTER the
# getStateManager().getDefaultState() combo above is already gone.
BARE_DEFAULT = (r'\.getDefaultState\(\)', '.defaultBlockState()')

def convert(text):
    out = []
    for line in text.split('\n'):
        for pat, rep in SUBS:
            line = re.sub(pat, rep, line)
        line = re.sub(BARE_DEFAULT[0], BARE_DEFAULT[1], line)
        # setBlockState: 3-arg (has UPDATE flag) -> setBlock; else -> setBlockAndUpdate
        if 'setBlockState(' in line:
            if 'UPDATE_' in line:
                line = line.replace('setBlockState(', 'setBlock(')
            else:
                line = line.replace('setBlockState(', 'setBlockAndUpdate(')
        out.append(line)
    return '\n'.join(out)

roots = [pathlib.Path('src/main/java'), pathlib.Path('src/client/java')]
changed = 0
for root in roots:
    if not root.exists():
        continue
    for f in root.rglob('*.java'):
        t = f.read_text()
        n = convert(t)
        if n != t:
            f.write_text(n)
            changed += 1
print(f"rewrote {changed} files")
