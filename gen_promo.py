#!/usr/bin/env python3
"""Generate promotional gallery graphics for Octaves of the One.

Composes the mod's own 3D block renders and item icons onto styled
"deep resonance" backgrounds (sculk-dark gradients, teal sound-wave ripples,
bronze panels) into 16:9 cards suitable for the Modrinth / CurseForge gallery.

These are promo/diagram images, not in-game screenshots — they're produced
headlessly so a GPU-less CI box can make them. Run:  python3 gen_promo.py
Outputs to promo/*.png.

Requires Pillow (pip install Pillow).
"""
from __future__ import annotations

import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = os.path.dirname(os.path.abspath(__file__))
BLOCKS3D = os.path.join(ROOT, "docs/wiki/images/blocks3d")
ICONS = os.path.join(ROOT, "docs/wiki/images/icons")
OUT = os.path.join(ROOT, "promo")

W, H = 1280, 720

# Palette — the "deep resonance" identity.
BG_TOP = (8, 12, 16)
BG_BOT = (10, 26, 30)
TEAL = (52, 211, 192)
TEAL_DIM = (28, 120, 110)
BRONZE = (184, 138, 74)
AMBER = (240, 165, 0)
AMETHYST = (168, 85, 247)
CREAM = (232, 230, 223)
MUTE = (138, 160, 160)
PANEL = (17, 24, 29)

FONTS = "/usr/share/fonts/truetype/liberation"


def font(name, size):
    for cand in (name, "LiberationSans-Regular.ttf"):
        p = os.path.join(FONTS, cand)
        if os.path.exists(p):
            return ImageFont.truetype(p, size)
    return ImageFont.load_default()


F_TITLE = lambda s: font("LiberationSans-Bold.ttf", s)
F_BODY = lambda s: font("LiberationSans-Regular.ttf", s)
F_ITAL = lambda s: font("LiberationSerif-Italic.ttf", s)
F_MONO = lambda s: font("LiberationMono-Bold.ttf", s)


def background(cx=None, cy=None):
    """Vertical gradient + concentric sound-wave rings + vignette."""
    img = Image.new("RGB", (W, H))
    px = img.load()
    for y in range(H):
        t = y / (H - 1)
        r = int(BG_TOP[0] + (BG_BOT[0] - BG_TOP[0]) * t)
        g = int(BG_TOP[1] + (BG_BOT[1] - BG_TOP[1]) * t)
        b = int(BG_TOP[2] + (BG_BOT[2] - BG_TOP[2]) * t)
        for x in range(W):
            px[x, y] = (r, g, b)
    img = img.convert("RGBA")

    cx = W // 2 if cx is None else cx
    cy = H // 2 if cy is None else cy
    rings = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    rd = ImageDraw.Draw(rings)
    for i in range(1, 26):
        rad = i * 46
        a = max(0, 42 - i * 2)
        rd.ellipse([cx - rad, cy - rad, cx + rad, cy + rad],
                   outline=(TEAL[0], TEAL[1], TEAL[2], a), width=2)
    rings = rings.filter(ImageFilter.GaussianBlur(0.6))
    img = Image.alpha_composite(img, rings)

    # vignette
    vig = Image.new("L", (W, H), 0)
    vd = ImageDraw.Draw(vig)
    vd.ellipse([-W * 0.3, -H * 0.3, W * 1.3, H * 1.3], fill=255)
    vig = vig.filter(ImageFilter.GaussianBlur(180))
    dark = Image.new("RGBA", (W, H), (0, 0, 0, 130))
    img = Image.composite(img, Image.alpha_composite(img, dark), vig)
    return img


def load_render(name, h):
    """Load a 3D block render (preferred) or flat icon, upscaled to height h (nearest)."""
    for base in (BLOCKS3D, ICONS):
        p = os.path.join(base, name + ".png")
        if os.path.exists(p):
            im = Image.open(p).convert("RGBA")
            scale = max(1, round(h / im.height))
            return im.resize((im.width * scale, im.height * scale), Image.NEAREST)
    return None


def paste_render(canvas, im, cx, cy):
    """Paste centered at (cx,cy) with a soft drop shadow + teal glow."""
    if im is None:
        return
    x, y = cx - im.width // 2, cy - im.height // 2
    shadow = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    sm = im.split()[3].point(lambda a: int(a * 0.55))
    blk = Image.new("RGBA", im.size, (0, 0, 0, 255))
    shadow.paste(blk, (x + 6, y + 8), sm)
    shadow = shadow.filter(ImageFilter.GaussianBlur(7))
    canvas.alpha_composite(shadow)
    canvas.alpha_composite(im, (x, y))


def rounded(canvas, box, fill=PANEL, outline=BRONZE, glow=TEAL, width=2, radius=16):
    overlay = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(overlay)
    d.rounded_rectangle(box, radius=radius, fill=(fill[0], fill[1], fill[2], 205))
    canvas.alpha_composite(overlay)
    g = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    gd = ImageDraw.Draw(g)
    gd.rounded_rectangle(box, radius=radius, outline=(glow[0], glow[1], glow[2], 70), width=width + 4)
    g = g.filter(ImageFilter.GaussianBlur(5))
    canvas.alpha_composite(g)
    d2 = ImageDraw.Draw(canvas)
    d2.rounded_rectangle(box, radius=radius, outline=(outline[0], outline[1], outline[2], 255), width=width)


def text(d, xy, s, fnt, fill=CREAM, anchor="la", glow=None, shadow=True):
    x, y = xy
    if shadow:
        d.text((x + 2, y + 2), s, font=fnt, fill=(0, 0, 0, 180), anchor=anchor)
    if glow:
        d.text(xy, s, font=fnt, fill=glow, anchor=anchor, stroke_width=2, stroke_fill=glow)
    d.text(xy, s, font=fnt, fill=fill, anchor=anchor)


def wave_divider(d, y, x0=120, x1=W - 120, color=BRONZE):
    d.line([(x0, y), (x1, y)], fill=(color[0], color[1], color[2], 160), width=2)
    d.ellipse([W // 2 - 5, y - 5, W // 2 + 5, y + 5], outline=TEAL, width=2)


def save(img, name):
    os.makedirs(OUT, exist_ok=True)
    img.convert("RGB").save(os.path.join(OUT, name), "PNG")
    print("wrote", os.path.join("promo", name))


# ───────────────────────── 1. hero ─────────────────────────
def hero():
    img = background(cy=300)
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 92), "OCTAVES OF THE ONE", F_TITLE(74), fill=CREAM, anchor="ma", glow=None)
    text(d, (W // 2, 178), "a two-way universe of rhythmic balanced interchange",
         F_ITAL(30), fill=TEAL, anchor="ma")
    wave_divider(d, 232)

    row = ["resonant_coil", "resonance_cell", "wave_conduit", "transmuter", "growth_radiator", "wave_relay"]
    n = len(row)
    gap = (W - 160) // n
    for i, name in enumerate(row):
        cx = 80 + gap // 2 + i * gap
        paste_render(img, load_render(name, 150), cx, 360)
    d = ImageDraw.Draw(img)

    labels = "Generate  ·  Bank  ·  Carry  ·  Transmute  ·  Radiate  ·  Broadcast"
    text(d, (W // 2, 470), labels, F_BODY(26), fill=MUTE, anchor="ma")

    rounded(img, [W // 2 - 430, 540, W // 2 + 430, 626], radius=20)
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 566), "Draw Light from stillness, wind it through the octaves, spend it",
         F_BODY(25), fill=CREAM, anchor="ma")
    text(d, (W // 2, 596), "across a wired & wireless grid.", F_BODY(25), fill=CREAM, anchor="ma")
    text(d, (W // 2, 666), "FABRIC  ·  MINECRAFT 1.21.4  ·  LIGHT IS CARRIED, NOT CONSUMED",
         F_MONO(20), fill=BRONZE, anchor="ma")
    save(img, "01_hero.png")


# ───────────────────────── 2. energy grid ─────────────────────────
def energy():
    img = background()
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 56), "THE TWO-WAY GRID", F_TITLE(56), anchor="ma")
    text(d, (W // 2, 128), "generation winds Light up · radiation pours it back out",
         F_ITAL(27), fill=TEAL, anchor="ma")
    wave_divider(d, 176)

    cards = [
        ("GENERATE", "resonant_coil", "Wind ambient sound into stored Light."),
        ("BANK", "resonance_cell", "Store surplus — up to 2,000,000 Light."),
        ("SPEND", "compressor", "Double ore, smelt with no fuel."),
        ("RADIATE", "growth_radiator", "Pour Light back into the world as life."),
    ]
    cw, ch, gap = 264, 360, 24
    total = len(cards) * cw + (len(cards) - 1) * gap
    x0 = (W - total) // 2
    for i, (title, name, cap) in enumerate(cards):
        bx = x0 + i * (cw + gap)
        rounded(img, [bx, 220, bx + cw, 220 + ch], radius=18)
        paste_render(img, load_render(name, 150), bx + cw // 2, 330)
        dd = ImageDraw.Draw(img)
        text(dd, (bx + cw // 2, 440), title, F_TITLE(30), fill=AMBER if title in ("SPEND", "RADIATE") else TEAL, anchor="ma")
        # wrap caption
        words, line, yy = cap.split(), "", 484
        for w in words:
            test = (line + " " + w).strip()
            if dd.textlength(test, font=F_BODY(20)) > cw - 36:
                text(dd, (bx + cw // 2, yy), line, F_BODY(20), fill=CREAM, anchor="ma")
                line, yy = w, yy + 26
            else:
                line = test
        if line:
            text(dd, (bx + cw // 2, yy), line, F_BODY(20), fill=CREAM, anchor="ma")
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 632), "Conduits share Light fairly — proportional under scarcity, no starvation.",
         F_BODY(24), fill=MUTE, anchor="ma")
    save(img, "02_energy.png")


# ───────────────────────── 3. wireless ─────────────────────────
def wireless():
    img = background()
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 56), "WIRELESS TRANSPORT", F_TITLE(56), anchor="ma")
    text(d, (W // 2, 128), "tune devices to a shared octave — they resonate",
         F_ITAL(27), fill=TEAL, anchor="ma")
    wave_divider(d, 176)

    # two relays beaming across a gap
    paste_render(img, load_render("wave_relay", 150), 300, 300)
    paste_render(img, load_render("wave_chest", 150), 980, 300)
    d = ImageDraw.Draw(img)
    for i in range(7):
        x = 430 + i * 60
        a = 210 - i * 12
        d.ellipse([x - 6, 296 - 6, x + 6, 296 + 6], outline=(TEAL[0], TEAL[1], TEAL[2], a), width=3)
    text(d, (300, 392), "SEND", F_MONO(22), fill=AMBER, anchor="ma")
    text(d, (980, 392), "RECEIVE", F_MONO(22), fill=TEAL, anchor="ma")
    text(d, (W // 2, 288), "items · fluids · Light", F_BODY(22), fill=CREAM, anchor="ma")

    fam = ["wave_amplifier", "wave_filter", "wave_splitter", "wave_repeater", "wave_coupler", "signal_relay"]
    names = ["Amplifier", "Filter", "Splitter", "Repeater", "Coupler", "Signal"]
    n = len(fam)
    gap = (W - 160) // n
    rounded(img, [70, 452, W - 70, 624], radius=18)
    for i, (nm, lab) in enumerate(zip(fam, names)):
        cx = 80 + gap // 2 + i * gap
        paste_render(img, load_render(nm, 92), cx, 520)
        dd = ImageDraw.Draw(img)
        text(dd, (cx, 576), lab, F_BODY(21), fill=CREAM, anchor="ma")
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 664), "16 octave channels · one per dye colour · spans dimensions with a Repeater",
         F_BODY(23), fill=MUTE, anchor="ma")
    save(img, "03_wireless.png")


# ───────────────────────── 4. transmutation ─────────────────────────
def transmutation():
    img = background()
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 56), "THE LIGHT ECONOMY", F_TITLE(56), anchor="ma")
    text(d, (W // 2, 128), "matter is condensed Light — dissolve it, bank it, condense it back",
         F_ITAL(26), fill=TEAL, anchor="ma")
    wave_divider(d, 176)

    paste_render(img, load_render("transmutation_table", 150), 250, 330)
    d = ImageDraw.Draw(img)
    text(d, (250, 430), "Transmutation Table", F_BODY(24), fill=CREAM, anchor="ma")
    text(d, (250, 462), "your Bound-Light account", F_BODY(20), fill=MUTE, anchor="ma")

    motes = [("light_mote", "Light", "64"), ("tonic_mote", "Tonic", "256"),
             ("mediant_mote", "Mediant", "1,024"), ("dominant_mote", "Dominant", "4,096"),
             ("harmonic_mote", "Harmonic", "16,384")]
    rounded(img, [470, 232, W - 70, 470], radius=18)
    dd = ImageDraw.Draw(img)
    text(dd, (490, 250), "THE MOTE LADDER", F_TITLE(24), fill=AMBER, anchor="la")
    text(dd, (W - 90, 256), "×4 per octave", F_MONO(18), fill=MUTE, anchor="ra")
    n = len(motes)
    span = (W - 70 - 510)
    for i, (nm, lab, val) in enumerate(motes):
        cx = 510 + span // (2 * n) + i * (span // n)
        paste_render(img, load_render(nm, 64), cx, 360)
        text(dd, (cx, 404), lab, F_BODY(19), fill=CREAM, anchor="ma")
        text(dd, (cx, 428), val, F_MONO(17), fill=TEAL, anchor="ma")

    # octave stars row
    rounded(img, [70, 504, W - 70, 624], radius=18)
    dd = ImageDraw.Draw(img)
    text(dd, (96, 520), "OCTAVE STARS — carry Bound Light in your pocket (×4 capacity per tier)",
         F_BODY(22), fill=CREAM, anchor="la")
    for i in range(6):
        cx = 150 + i * ((W - 300) // 5)
        paste_render(img, load_render(f"octave_star_{i+1}", 60), cx, 588)
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 664),
         "Values derived across the whole recipe graph — you can never craft up in value.",
         F_BODY(23), fill=MUTE, anchor="ma")
    save(img, "04_transmutation.png")


# ───────────────────────── 5. the great work ─────────────────────────
def great_work():
    img = background(cy=360)
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 56), "THE GREAT WORK", F_TITLE(56), anchor="ma")
    text(d, (W // 2, 128), "a guided in-game advancement tree walks the whole progression",
         F_ITAL(26), fill=TEAL, anchor="ma")
    wave_divider(d, 176)

    steps = [
        ("echocite_ore", "Refine", "Echocite → Echo Ingot"),
        ("resonant_coil", "Generate", "Coil · Core · Storm"),
        ("wave_conduit", "Carry & Bank", "Conduits · Cells"),
        ("growth_radiator", "Radiate", "the other half"),
        ("wave_relay", "Broadcast", "wireless octaves"),
        ("transmutation_table", "Transmute", "the Light economy"),
    ]
    n = len(steps)
    gap = (W - 140) // n
    y = 330
    for i, (name, title, cap) in enumerate(steps):
        cx = 70 + gap // 2 + i * gap
        if i < n - 1:
            nx = 70 + gap // 2 + (i + 1) * gap
            d.line([(cx + 50, y), (nx - 50, y)], fill=(BRONZE[0], BRONZE[1], BRONZE[2], 200), width=3)
            d.polygon([(nx - 50, y - 6), (nx - 50, y + 6), (nx - 38, y)], fill=TEAL)
    for i, (name, title, cap) in enumerate(steps):
        cx = 70 + gap // 2 + i * gap
        paste_render(img, load_render(name, 96), cx, y)
        dd = ImageDraw.Draw(img)
        text(dd, (cx, y + 70), title, F_TITLE(23), fill=TEAL, anchor="ma")
        text(dd, (cx, y + 100), cap, F_BODY(17), fill=MUTE, anchor="ma")
    d = ImageDraw.Draw(img)
    rounded(img, [W // 2 - 440, 520, W // 2 + 440, 614], radius=18)
    dd = ImageDraw.Draw(img)
    text(dd, (W // 2, 548), "24 connected advancements — goals & challenges with XP rewards",
         F_BODY(25), fill=CREAM, anchor="ma")
    text(dd, (W // 2, 582), "open your advancements (L) and follow the toasts",
         F_BODY(23), fill=MUTE, anchor="ma")
    text(dd, (W // 2, 664), "From the still centre of zero to the resolved crest.",
         F_ITAL(24), fill=BRONZE, anchor="ma")
    save(img, "05_great_work.png")


def main():
    hero()
    energy()
    wireless()
    transmutation()
    great_work()
    print("done — promo/")


if __name__ == "__main__":
    main()
