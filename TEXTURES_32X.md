# 32x Texture Pipeline

All block/item textures are now **32x32** (32x(32n) for animated strips), upgraded
from the original 16x procedural pixel-art in a "faithful" style: each source pixel
becomes a 2x2 block with deterministic grain, hue-preserving luminance modulation,
and crisp inner-edge shading along hard color boundaries. Palette and silhouettes
are unchanged — the deep-resonance style (deepslate base, patinated bronze, teal
bloom) is preserved at double resolution.

## Regenerating

```
python3 scripts/textures_32x.py
```

Idempotent: 16x sources are upgraded in place; files already at 32x are skipped.
The original 16x generator is `gen_textures.py` — run it first if you need to
rebuild from scratch (restore 16x), then run the 32x pass.

## Verification

`verifier/v1/check_textures32.py` fails CI if any block/item texture is not 32x.
