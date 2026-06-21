#!/usr/bin/env python3
"""Second Yarn->Mojmap pass: method-name renames verified against the deobf jar.
Context-sensitive cases (BE `world` field, recipe codecs, screen ClickType) are
handled separately."""
import re, pathlib

SUBS = [
    # entity motion / use
    (r'\.getVelocity\(', '.getDeltaMovement('),
    (r'\.setVelocity\(', '.setDeltaMovement('),
    (r'\.velocityModified\b', '.hasImpulse'),
    (r'\.getRotationVector\(\)', '.getViewVector(1.0F)'),
    (r'\.setCurrentHand\(', '.startUsingItem('),
    (r'\.getOffHandStack\(', '.getOffhandItem('),
    (r'\.openHandledScreen\(', '.openMenu('),
    (r'\.getCursorStack\(', '.getCarried('),
    # itemstack
    (r'\.increment\(', '.grow('),
    (r'\.decrement\(', '.shrink('),
    (r'\.splitStack\(', '.split('),
    (r'\.getMaxCountPerStack\(', '.getMaxStackSize('),
    (r'\.getMaxCount\(', '.getMaxStackSize('),
    (r'\bareItemsAndComponentsEqual\(', 'isSameItemSameComponents('),
    # item properties builder
    (r'\.maxCount\(', '.stacksTo('),
    # level / world
    (r'\.isReceivingRedstonePower\(', '.hasNeighborSignal('),
    (r'\.getEntitiesByClass\(', '.getEntitiesOfClass('),
    (r'\.getRegistryManager\(', '.registryAccess('),
    (r'\.syncWorldEvent\(', '.levelEvent('),
    (r'\.spawnEntity\(', '.addFreshEntity('),
    # phys
    (r'\.lengthSquared\(', '.lengthSqr('),
    (r'\.expand\(', '.inflate('),
    # block settings factory
    (r'\bProperties\.create\(', 'Properties.of('),
    (r'\bAbstractBlock\.Settings\b', 'BlockBehaviour.Properties'),
    # getWorld() — receiver-specific
    (r'\b(ctx|context)\.getWorld\(\)', r'\1.getLevel()'),
    (r'\b(player|user|p|entity)\.getWorld\(\)', r'\1.level()'),
]

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
        if n != t:
            f.write_text(n)
            changed += 1
print(f"rewrote {changed} files")
