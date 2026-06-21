#!/usr/bin/env python3
"""Third pass: bare nested type (Settings->Properties) + verified static factories.

Only the renames verified against the 26.1.2 jars and unambiguous in this codebase.
Ambiguous ones (getWorld/getPos overloads), signature-changing ones (writeNbt ->
saveAdditional(ValueOutput)), and API reworks (tools, GUI, recipes, Fabric) are left
for hand edits. Run after migrate_mojmap.py + migrate_mojmap2.py.
"""
import re, glob

# bare inherited nested type used in Block/Item subclass constructors
def bare_settings(t):
    return re.sub(r"(?<![\w.])Settings(?![\w])", "Properties", t)

# verified static factory renames
LITERAL = {
    "Identifier.of(": "Identifier.fromNamespaceAndPath(",
    ".ofFloored(": ".containing(",
}

# NOTE: instance-method renames (markDirty->setChanged, getCachedState->getBlockState,
# up/down->above/below, ...) are intentionally NOT scripted — vanilla names collide with
# the mod's own methods (e.g. ResonanceNetwork.markDirty), so they are fixed by hand.


def main():
    n = 0
    for f in glob.glob("src/**/*.java", recursive=True):
        t = open(f, encoding="utf-8").read()
        o = t
        t = bare_settings(t)
        for a, b in LITERAL.items():
            t = t.replace(a, b)
        if t != o:
            open(f, "w", encoding="utf-8").write(t)
            n += 1
    print(f"pass 3: updated {n} files")


if __name__ == "__main__":
    main()
