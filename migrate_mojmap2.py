#!/usr/bin/env python3
"""Second migration pass: inline FQNs + nested-type renames (Mojmap/26.1).

Run AFTER migrate_mojmap.py (do not re-run that one — its body token swaps are
not idempotent). This pass only does literal/FQN replacements, which are safe.
"""
import re, glob
from migrate_mojmap import MAP

# extra FQNs the first map missed
MAP = dict(MAP)
MAP["net.minecraft.registry.tag.BiomeTags"] = "net.minecraft.tags.BiomeTags"

# nested types renamed under their (already-renamed) owner
NESTED = {
    ".WrapperLookup": ".Provider",          # HolderLookup.WrapperLookup -> HolderLookup.Provider
    "Item.Settings": "Item.Properties",
    "BlockBehaviour.Settings": "BlockBehaviour.Properties",
}


def main():
    n = 0
    for f in glob.glob("src/**/*.java", recursive=True):
        txt = open(f, encoding="utf-8").read()
        orig = txt
        for y, m in MAP.items():                       # inline FQNs everywhere
            txt = re.sub(re.escape(y) + r"\b", m, txt)
        for a, b in NESTED.items():
            txt = txt.replace(a, b)
        if txt != orig:
            open(f, "w", encoding="utf-8").write(txt)
            n += 1
    print(f"pass 2: updated {n} files")


if __name__ == "__main__":
    main()
