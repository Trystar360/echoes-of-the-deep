#!/usr/bin/env python3
"""Generate promotional gallery graphics for Octaves of the One.

Renders the mod's blocks as proper isometric cubes straight from the real
16x16 textures (machines show their glowing front over the bronze device
casing), and items from their real sprites — composed onto styled
"deep resonance" backgrounds into 16:9 gallery cards. Headless (Pillow only),
so a GPU-less box can produce them.

Run:  python3 gen_promo.py   ->   promo/*.png
"""
from __future__ import annotations

import math
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter, ImageEnhance

ROOT = os.path.dirname(os.path.abspath(__file__))
TEX = os.path.join(ROOT, "src/main/resources/assets/echoes/textures")
TB = os.path.join(TEX, "block")
TI = os.path.join(TEX, "item")
OUT = os.path.join(ROOT, "promo")

W, H = 1280, 720

BG_TOP = (8, 12, 16); BG_BOT = (10, 26, 30)
TEAL = (52, 211, 192); BRONZE = (184, 138, 74); AMBER = (240, 165, 0)
AMETHYST = (168, 85, 247); CREAM = (232, 230, 223); MUTE = (138, 160, 160)
GREEN = (122, 200, 96); PANEL = (17, 24, 29)
FONTS = "/usr/share/fonts/truetype/liberation"


def font(name, size):
    p = os.path.join(FONTS, name)
    return ImageFont.truetype(p, size) if os.path.exists(p) else ImageFont.load_default()


F_TITLE = lambda s: font("LiberationSans-Bold.ttf", s)
F_BODY = lambda s: font("LiberationSans-Regular.ttf", s)
F_ITAL = lambda s: font("LiberationSerif-Italic.ttf", s)
F_MONO = lambda s: font("LiberationMono-Bold.ttf", s)

# ───────────────────── texture loading ─────────────────────
MACHINES = set()  # filled lazily: blocks whose texture is an animated front strip


def _raw(path):
    return Image.open(path).convert("RGBA") if os.path.exists(path) else None


def block_tex(name):
    """Return (16x16 RGBA brightest frame, is_machine) for a block texture, or (None, False)."""
    im = _raw(os.path.join(TB, name + ".png"))
    if im is None:
        return None, False
    if im.height > im.width:  # animated vertical strip -> machine front; pick brightest frame
        n = im.height // im.width
        frames = [im.crop((0, i * im.width, im.width, (i + 1) * im.width)) for i in range(n)]
        best = max(frames, key=lambda f: sum(f.convert("L").tobytes()))
        return best, True
    return im, False


def item_tex(name):
    im = _raw(os.path.join(TI, name + ".png"))
    if im is None:
        im = _raw(os.path.join(TB, name + ".png"))
        if im is not None and im.height > im.width:
            im = im.crop((0, 0, im.width, im.width))
    return im

# ───────────────────── isometric cube ─────────────────────
def _solve3(p, q, r, tx):
    """Affine coeffs (a,b,c) with t = a*x + b*y + c through 3 points p,q,r -> tx[0..2]."""
    (x0, y0), (x1, y1), (x2, y2) = p, q, r
    det = (x0 * (y1 - y2) - y0 * (x1 - x2) + (x1 * y2 - x2 * y1))
    if abs(det) < 1e-9:
        return (0, 0, tx[0])
    a = (tx[0] * (y1 - y2) - y0 * (tx[1] - tx[2]) + (tx[1] * y2 - tx[2] * y1)) / det
    b = (x0 * (tx[1] - tx[2]) - tx[0] * (x1 - x2) + (x1 * tx[2] - x2 * tx[1])) / det
    c = (x0 * (y1 * tx[2] - y2 * tx[1]) - y0 * (x1 * tx[2] - x2 * tx[1]) + tx[0] * (x1 * y2 - x2 * y1)) / det
    return (a, b, c)


def _face(canvas, tex, d0, d1, d2, bright):
    """Map tex corners (0,0),(16,0),(0,16) -> output d0,d1,d2 (a parallelogram) and composite."""
    S = canvas.size
    ax, bx, cx = _solve3(d0, d1, d2, (0, 0, 16))           # output->src x
    ay, by, cy = _solve3(d0, d1, d2, (0, 16, 0))           # output->src y
    warped = tex.transform(S, Image.AFFINE, (ax, bx, cx, ay, by, cy), resample=Image.NEAREST)
    warped = ImageEnhance.Brightness(warped).enhance(bright)
    mask = Image.new("L", S, 0)
    d4 = (d1[0] + d2[0] - d0[0], d1[1] + d2[1] - d0[1])
    ImageDraw.Draw(mask).polygon([d0, d1, d4, d2], fill=255)
    canvas.paste(warped, (0, 0), Image.composite(warped.split()[3], Image.new("L", S, 0), mask))


def render_block(name, target_h):
    """A pixel-art isometric cube of the block, ~target_h px tall, with a soft contact shadow."""
    base, machine = block_tex(name)
    if base is None:
        it = item_tex(name)              # fallback billboard
        return render_item_img(it, target_h) if it else None
    top, left, right = base, base, base
    if machine:
        dt, _ = block_tex("device_top"); ds, _ = block_tex("device_side")
        if dt is not None: top = dt
        if ds is not None: right = ds      # front (bright) stays on the left face
    elif name == "lumewood_log":
        lt, _ = block_tex("lumewood_log_top")
        if lt is not None: top = lt

    s = max(24, int(target_h * 0.5))
    pad = int(s * 0.5)
    S = (2 * s + 2 * pad, 2 * s + 2 * pad)
    cx, cy = S[0] // 2, S[1] // 2
    canvas = Image.new("RGBA", S, (0, 0, 0, 0))

    # contact shadow
    sh = Image.new("RGBA", S, (0, 0, 0, 0))
    ImageDraw.Draw(sh).ellipse([cx - s, cy + s - s // 4, cx + s, cy + s + s // 4],
                               fill=(0, 0, 0, 120))
    canvas.alpha_composite(sh.filter(ImageFilter.GaussianBlur(6)))

    T_top = (cx, cy - s); T_right = (cx + s, cy - s // 2)
    T_front = (cx, cy); T_left = (cx - s, cy - s // 2)
    L_blb = (cx - s, cy + s // 2); R_bf = (cx, cy + s)
    _face(canvas, top, T_left, T_top, T_front, 1.0)        # top
    _face(canvas, left, T_left, T_front, L_blb, 0.78)      # left / front
    _face(canvas, right, T_front, T_right, R_bf, 0.60)     # right
    # crisp edge highlight
    ImageDraw.Draw(canvas).line([T_top, T_left, T_front, T_right, T_top],
                                fill=(255, 255, 255, 30), width=1)
    return canvas


def render_item_img(im, target_h):
    if im is None:
        return None
    scale = max(1, round(target_h / im.height))
    return im.resize((im.width * scale, im.height * scale), Image.NEAREST)


def render_item(name, target_h):
    return render_item_img(item_tex(name), target_h)

# ───────────────────── scene helpers ─────────────────────
def background(cy=None):
    img = Image.new("RGB", (W, H)); px = img.load()
    for y in range(H):
        t = y / (H - 1)
        px_row = tuple(int(BG_TOP[i] + (BG_BOT[i] - BG_TOP[i]) * t) for i in range(3))
        for x in range(W):
            px[x, y] = px_row
    img = img.convert("RGBA")
    ccx, ccy = W // 2, (H // 2 if cy is None else cy)
    rings = Image.new("RGBA", (W, H), (0, 0, 0, 0)); rd = ImageDraw.Draw(rings)
    for i in range(1, 26):
        rad = i * 46; a = max(0, 40 - i * 2)
        rd.ellipse([ccx - rad, ccy - rad, ccx + rad, ccy + rad], outline=(*TEAL, a), width=2)
    img = Image.alpha_composite(img, rings.filter(ImageFilter.GaussianBlur(0.6)))
    vig = Image.new("L", (W, H), 0)
    ImageDraw.Draw(vig).ellipse([-W * 0.3, -H * 0.3, W * 1.3, H * 1.3], fill=255)
    vig = vig.filter(ImageFilter.GaussianBlur(180))
    img = Image.composite(img, Image.alpha_composite(img, Image.new("RGBA", (W, H), (0, 0, 0, 130))), vig)
    return img


def paste(canvas, im, cx, cy):
    if im is None:
        return
    canvas.alpha_composite(im, (cx - im.width // 2, cy - im.height // 2))


def rounded(canvas, box, fill=PANEL, outline=BRONZE, glow=TEAL, width=2, radius=16):
    ov = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    ImageDraw.Draw(ov).rounded_rectangle(box, radius=radius, fill=(*fill, 205))
    canvas.alpha_composite(ov)
    g = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    ImageDraw.Draw(g).rounded_rectangle(box, radius=radius, outline=(*glow, 70), width=width + 4)
    canvas.alpha_composite(g.filter(ImageFilter.GaussianBlur(5)))
    ImageDraw.Draw(canvas).rounded_rectangle(box, radius=radius, outline=(*outline, 255), width=width)


def text(d, xy, s, fnt, fill=CREAM, anchor="la", shadow=True):
    if shadow:
        d.text((xy[0] + 2, xy[1] + 2), s, font=fnt, fill=(0, 0, 0, 180), anchor=anchor)
    d.text(xy, s, font=fnt, fill=fill, anchor=anchor)


def wrap(d, cx, y, s, fnt, color, maxw, lh):
    line = ""
    for w in s.split():
        if d.textlength((line + " " + w).strip(), font=fnt) > maxw:
            text(d, (cx, y), line, fnt, fill=color, anchor="ma"); line = w; y += lh
        else:
            line = (line + " " + w).strip()
    if line:
        text(d, (cx, y), line, fnt, fill=color, anchor="ma")


def divider(d, y):
    d.line([(120, y), (W - 120, y)], fill=(*BRONZE, 160), width=2)
    d.ellipse([W // 2 - 5, y - 5, W // 2 + 5, y + 5], outline=TEAL, width=2)


def arrow(img, x0, y0, x1, y1, color=BRONZE, width=5, label=None, lfnt=None, lcolor=None):
    d = ImageDraw.Draw(img)
    d.line([(x0, y0), (x1, y1)], fill=(*color, 235), width=width)
    ang = math.atan2(y1 - y0, x1 - x0); L = 16
    for da in (math.radians(152), math.radians(-152)):
        d.line([(x1, y1), (x1 + L * math.cos(ang + da), y1 + L * math.sin(ang + da))],
               fill=(*color, 235), width=width)
    if label:
        mx, my = (x0 + x1) // 2, (y0 + y1) // 2
        text(d, (mx, my - 22), label, lfnt or F_MONO(18), fill=lcolor or color, anchor="ma")


def orb(img, cx, cy, r=58, caption="BOUND LIGHT", inner="light_mote"):
    g = Image.new("RGBA", img.size, (0, 0, 0, 0))
    ImageDraw.Draw(g).ellipse([cx - r - 14, cy - r - 14, cx + r + 14, cy + r + 14], fill=(*TEAL, 80))
    img.alpha_composite(g.filter(ImageFilter.GaussianBlur(12)))
    d = ImageDraw.Draw(img)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(13, 38, 40, 240), outline=(*TEAL, 255), width=3)
    paste(img, render_item(inner, 44), cx, cy - 8)
    text(d, (cx, cy + r - 24), caption, F_MONO(15), fill=TEAL, anchor="ma")


def header(img, title, sub, cy=None):
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 52), title, F_TITLE(56), anchor="ma")
    text(d, (W // 2, 126), sub, F_ITAL(27), fill=TEAL, anchor="ma")
    divider(d, 174)
    return d


def save(img, name):
    os.makedirs(OUT, exist_ok=True)
    img.convert("RGB").save(os.path.join(OUT, name), "PNG")
    print("wrote promo/" + name)


# ───────────────────── cards ─────────────────────
def hero():
    img = background(cy=300); d = ImageDraw.Draw(img)
    text(d, (W // 2, 84), "OCTAVES OF THE ONE", F_TITLE(76), anchor="ma")
    text(d, (W // 2, 172), "a two-way universe of rhythmic balanced interchange", F_ITAL(30), fill=TEAL, anchor="ma")
    divider(d, 228)
    row = ["resonant_coil", "resonance_cell", "wave_conduit", "transmuter", "growth_radiator", "wave_relay"]
    gap = (W - 160) // len(row)
    for i, n in enumerate(row):
        paste(img, render_block(n, 150), 80 + gap // 2 + i * gap, 360)
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 470), "Generate · Bank · Carry · Transmute · Radiate · Broadcast", F_BODY(26), fill=MUTE, anchor="ma")
    rounded(img, [W // 2 - 440, 536, W // 2 + 440, 626], radius=20)
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 562), "Draw Light from stillness, wind it up through the octaves,", F_BODY(25), anchor="ma")
    text(d, (W // 2, 594), "and spend it across a wired & wireless grid.", F_BODY(25), anchor="ma")
    text(d, (W // 2, 668), "FABRIC · MINECRAFT 1.21.4 · LIGHT IS CARRIED, NOT CONSUMED", F_MONO(20), fill=BRONZE, anchor="ma")
    save(img, "01_hero.png")


def card_row(img, y0, cards, accent_for=lambda t: TEAL, kind="block"):
    cw, ch, gap = 264, 348, 24
    total = len(cards) * cw + (len(cards) - 1) * gap
    x0 = (W - total) // 2
    for i, (title, name, cap) in enumerate(cards):
        bx = x0 + i * (cw + gap)
        rounded(img, [bx, y0, bx + cw, y0 + ch])
        im = render_block(name, 148) if kind == "block" else render_item(name, 120)
        paste(img, im, bx + cw // 2, y0 + 110)
        d = ImageDraw.Draw(img)
        text(d, (bx + cw // 2, y0 + 216), title, F_TITLE(29), fill=accent_for(title), anchor="ma")
        wrap(d, bx + cw // 2, y0 + 262, cap, F_BODY(20), CREAM, cw - 36, 26)


def energy():
    img = background(); header(img, "THE TWO-WAY GRID", "generation winds Light up · radiation pours it back out")
    card_row(img, 214, [
        ("GENERATE", "resonant_coil", "Wind ambient sound into stored Light."),
        ("BANK", "resonance_cell", "Bank surplus — up to 2,000,000 Light."),
        ("SPEND", "compressor", "Double ore; smelt with no fuel."),
        ("RADIATE", "growth_radiator", "Pour Light back into the world as life."),
    ], accent_for=lambda t: AMBER if t in ("SPEND", "RADIATE") else TEAL)
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 632), "Conduits share Light fairly — proportional under scarcity, no starvation.", F_BODY(24), fill=MUTE, anchor="ma")
    save(img, "02_energy.png")


def wireless():
    img = background(); header(img, "WIRELESS TRANSPORT", "tune devices to a shared octave — they resonate")
    paste(img, render_block("wave_relay", 150), 300, 304)
    paste(img, render_block("wave_chest", 150), 980, 304)
    d = ImageDraw.Draw(img)
    for i in range(7):
        x = 432 + i * 60
        d.ellipse([x - 6, 290, x + 6, 302], outline=(*TEAL, 210 - i * 12), width=3)
    text(d, (300, 392), "SEND", F_MONO(22), fill=AMBER, anchor="ma")
    text(d, (980, 392), "RECEIVE", F_MONO(22), fill=TEAL, anchor="ma")
    text(d, (W // 2, 250), "items · fluids · Light", F_BODY(22), anchor="ma")
    fam = [("wave_amplifier", "Amplifier"), ("wave_filter", "Filter"), ("wave_splitter", "Splitter"),
           ("wave_repeater", "Repeater"), ("wave_coupler", "Coupler"), ("signal_relay", "Signal")]
    rounded(img, [70, 446, W - 70, 624], radius=18)
    gap = (W - 160) // len(fam)
    for i, (nm, lab) in enumerate(fam):
        cx = 80 + gap // 2 + i * gap
        paste(img, render_block(nm, 96), cx, 518)
        text(ImageDraw.Draw(img), (cx, 578), lab, F_BODY(21), anchor="ma")
    d = ImageDraw.Draw(img)
    text(d, (W // 2, 664), "16 octave channels · one per dye colour · spans dimensions with a Repeater", F_BODY(23), fill=MUTE, anchor="ma")
    save(img, "03_wireless.png")


def values():
    img = background(); header(img, "EVERY ITEM IS CONDENSED LIGHT",
                               "vanilla or modded — everything carries a Light Value")
    # item strip: a diverse row, each tagged as carrying value
    rounded(img, [70, 200, W - 70, 332], radius=18); dd = ImageDraw.Draw(img)
    text(dd, (W // 2, 214), "EVERY ITEM  →  A LIGHT VALUE", F_TITLE(24), fill=TEAL, anchor="ma")
    strip = ["raw_echocite", "echocite_dust", "echo_ingot", "drum_core",
             "silentite_crystal", "radiant_ingot", "octave_seed", "echo_dust"]
    gap = (W - 200) // len(strip)
    for i, nm in enumerate(strip):
        cx = 100 + gap // 2 + i * gap
        paste(img, render_item(nm, 52), cx, 282)
        ImageDraw.Draw(img).text((cx, 308), "≈ Light", font=F_MONO(13), fill=MUTE, anchor="ma")

    # how values are set (left) + the Mote scale (right)
    rounded(img, [70, 354, 624, 624], radius=18); dd = ImageDraw.Draw(img)
    text(dd, (96, 370), "HOW VALUES ARE SET", F_TITLE(22), fill=AMBER)
    for j, ln in enumerate([
        "A small seed set of primitives — ores, mob",
        "drops, plants — is authoritative. Every other",
        "item, vanilla or modded, is derived across the",
        "whole recipe graph: the cheapest sum(inputs)",
        "÷ output, to a fixed point.",
        "",
        "Min + floor means you can never craft UP in",
        "value, so ore progression stays safe — and",
        "modpacks get sensible values for free.",
    ]):
        text(dd, (96, 406 + j * 24), ln, F_BODY(19), fill=CREAM)

    motes = [("light_mote", "Light", "64"), ("tonic_mote", "Tonic", "256"),
             ("mediant_mote", "Mediant", "1,024"), ("dominant_mote", "Dominant", "4,096"),
             ("harmonic_mote", "Harmonic", "16,384")]
    rounded(img, [668, 354, W - 70, 624], radius=18); dd = ImageDraw.Draw(img)
    text(dd, ((668 + W - 70) // 2, 370), "THE MOTE LADDER — the value scale", F_TITLE(21), fill=AMBER, anchor="ma")
    text(dd, ((668 + W - 70) // 2, 400), "Bound-Light coins · ×4 per octave", F_BODY(18), fill=MUTE, anchor="ma")
    span = (W - 70 - 668); n = len(motes)
    for i, (nm, lab, val) in enumerate(motes):
        cx = 668 + span // (2 * n) + i * (span // n)
        paste(img, render_item(nm, 58), cx, 470)
        text(dd, (cx, 512), lab, F_BODY(18), anchor="ma")
        text(dd, (cx, 536), val, F_MONO(16), fill=TEAL, anchor="ma")
    text(dd, ((668 + W - 70) // 2, 584), "withdraw & re-dissolve are exact inverses", F_BODY(18), fill=MUTE, anchor="ma")
    save(img, "04_values.png")


def tablet_table():
    img = background(); header(img, "THE TABLE & THE TABLET",
                               "your personal Bound-Light account — dissolve · withdraw · condense")
    cy = 322
    orb(img, W // 2, cy, r=60)
    # DISSOLVE — item into the account
    paste(img, render_item("radiant_ingot", 72), 250, cy)
    arrow(img, 300, cy, W // 2 - 70, cy, color=TEAL, label="DISSOLVE", lcolor=TEAL)
    d = ImageDraw.Draw(img)
    text(d, (250, cy + 56), "any item", F_BODY(19), fill=CREAM, anchor="ma")
    text(d, ((250 + W // 2) // 2, cy + 40), "bank its value · attune its tone", F_BODY(17), fill=MUTE, anchor="ma")
    # WITHDRAW — out to motes
    for k, nm in enumerate(["light_mote", "tonic_mote", "harmonic_mote"]):
        paste(img, render_item(nm, 46), 980 + k * 52, cy - 70)
    arrow(img, W // 2 + 60, cy - 24, 950, cy - 70, color=AMBER, label="WITHDRAW", lcolor=AMBER)
    text(d, (1030, cy - 28), "pay out as Mote coins", F_BODY(17), fill=MUTE, anchor="ma")
    # CONDENSE — out to an attuned item
    paste(img, render_item("radiant_ingot", 56), 1000, cy + 74)
    arrow(img, W // 2 + 60, cy + 24, 968, cy + 74, color=GREEN, label="CONDENSE", lcolor=GREEN)
    text(d, (1030, cy + 116), "re-create an attuned item · ×1 or ×64", F_BODY(17), fill=MUTE, anchor="ma")

    # one shared account: table + tablet
    rounded(img, [70, 470, W - 70, 624], radius=18)
    paste(img, render_block("transmutation_table", 118), 230, 547)
    paste(img, render_item("transmutation_tablet", 92), W - 230, 547)
    dd = ImageDraw.Draw(img)
    text(dd, (230, 612), "Transmutation Table", F_BODY(19), fill=CREAM, anchor="ma")
    text(dd, (W - 230, 612), "Transmutation Tablet", F_BODY(19), fill=CREAM, anchor="ma")
    text(dd, (W // 2, 506), "ONE SHARED ACCOUNT", F_TITLE(26), fill=TEAL, anchor="ma")
    wrap(dd, W // 2, 544, "Per-player Bound Light and the tones you've learned — at the block or in your pocket. Your Light and knowledge follow you.",
         F_BODY(21), CREAM, 440, 28)
    save(img, "05_transmutation.png")


def grove():
    img = background(); header(img, "THE OCTAVE GROVE", "a living, glowing garden — Light poured back as growth")
    # left: growth story
    rounded(img, [70, 214, 612, 470], radius=18)
    paste(img, render_block("verdant_loam", 130), 200, 312)
    paste(img, render_block("growth_radiator", 130), 470, 312)
    d = ImageDraw.Draw(img)
    text(d, (200, 392), "Verdant Loam", F_BODY(22), anchor="ma")
    text(d, (470, 392), "Growth Radiator", F_BODY(22), anchor="ma")
    text(d, (341, 430), "pulse Light upward to grow crops & saplings", F_BODY(20), fill=GREEN, anchor="ma")
    # right: building set
    rounded(img, [668, 214, W - 70, 470], radius=18)
    bs = [("lumewood_planks", "Lumewood"), ("echocite_bricks", "Bricks"), ("lume_lantern", "Lantern")]
    for i, (nm, lab) in enumerate(bs):
        cx = 740 + i * 158
        paste(img, render_block(nm, 120), cx, 312)
        text(ImageDraw.Draw(img), (cx, 392), lab, F_BODY(21), anchor="ma")
    text(ImageDraw.Draw(img), ((668 + W - 70) // 2, 430), "a full glowing building palette", F_BODY(20), fill=AMBER, anchor="ma")
    # flora strip
    rounded(img, [70, 504, W - 70, 624], radius=18); dd = ImageDraw.Draw(img)
    text(dd, (96, 520), "GROVE FLORA — grown from the Lumewood tree", F_BODY(22), fill=GREEN)
    flora = [("lumebloom", "Lumebloom"), ("lumewood_sapling", "Sapling"), ("lumewood_log", "Lumewood Log"), ("lumewood_leaves", "Leaves")]
    gap = (W - 360) // len(flora)
    for i, (nm, lab) in enumerate(flora):
        cx = 360 + gap // 2 + i * gap
        im = render_block(nm, 78) if nm in ("lumewood_log",) else render_item(nm, 56)
        paste(img, im, cx, 580)
        text(dd, (cx + 70, 580), lab, F_BODY(20), fill=CREAM, anchor="lm")
    text(ImageDraw.Draw(img), (W // 2, 664), "Glowing wood, masonry, flowers, and living soil — utilitarian and pretty.", F_BODY(23), fill=MUTE, anchor="ma")
    save(img, "06_grove.png")


def gear():
    img = background(); header(img, "FLIGHT & OVER-TUNED GEAR", "Light spent on motion — the body thrown outward from centre")
    rounded(img, [70, 214, 612, 520], radius=18)
    paste(img, render_item("resonant_thrusters", 150), 341, 312)
    d = ImageDraw.Draw(img)
    text(d, (341, 408), "RESONANT THRUSTERS", F_TITLE(26), fill=TEAL, anchor="ma")
    wrap(d, 341, 446, "Hold use to fly where you look, with fall immunity. Recharge on any Coil or Cell.", F_BODY(21), CREAM, 500, 28)
    rounded(img, [668, 214, W - 70, 520], radius=18)
    tools = ["resonant_pickaxe", "resonant_axe", "resonant_sword", "resonant_shovel", "resonant_hoe"]
    gap = (W - 70 - 668 - 40) // len(tools)
    for i, nm in enumerate(tools):
        paste(img, render_item(nm, 96), 668 + 40 + gap // 2 + i * gap, 320)
    dd = ImageDraw.Draw(img)
    text(dd, ((668 + W - 70) // 2, 408), "RESONANT TOOLS", F_TITLE(26), fill=AMBER, anchor="ma")
    wrap(dd, (668 + W - 70) // 2, 446, "A full set on the Echo material — faster than netherite, tough, and highly enchantable.", F_BODY(21), CREAM, 520, 28)
    text(ImageDraw.Draw(img), (W // 2, 588), "Deliberately strong — a device tuned to its octave gives back as freely as the grid pours in.", F_BODY(24), fill=MUTE, anchor="ma")
    text(ImageDraw.Draw(img), (W // 2, 648), "( flavour, not physics )", F_ITAL(22), fill=BRONZE, anchor="ma")
    save(img, "07_gear.png")


def great_work():
    img = background(cy=360); header(img, "THE GREAT WORK", "a guided in-game advancement tree walks the whole progression")
    steps = [("echocite_ore", "Refine", "ore → ingot"), ("resonant_coil", "Generate", "coil · core · storm"),
             ("wave_conduit", "Carry & Bank", "conduits · cells"), ("growth_radiator", "Radiate", "the other half"),
             ("wave_relay", "Broadcast", "wireless octaves"), ("transmutation_table", "Transmute", "the Light economy")]
    n = len(steps); gap = (W - 140) // n; y = 332; d = ImageDraw.Draw(img)
    for i in range(n - 1):
        cx = 70 + gap // 2 + i * gap; nx = 70 + gap // 2 + (i + 1) * gap
        d.line([(cx + 52, y), (nx - 52, y)], fill=(*BRONZE, 200), width=3)
        d.polygon([(nx - 52, y - 6), (nx - 52, y + 6), (nx - 40, y)], fill=TEAL)
    for i, (name, title, cap) in enumerate(steps):
        cx = 70 + gap // 2 + i * gap
        paste(img, render_block(name, 100), cx, y)
        dd = ImageDraw.Draw(img)
        text(dd, (cx, y + 78), title, F_TITLE(23), fill=TEAL, anchor="ma")
        text(dd, (cx, y + 108), cap, F_BODY(17), fill=MUTE, anchor="ma")
    rounded(img, [W // 2 - 440, 520, W // 2 + 440, 616], radius=18); dd = ImageDraw.Draw(img)
    text(dd, (W // 2, 548), "24 connected advancements — goals & challenges with XP rewards", F_BODY(25), anchor="ma")
    text(dd, (W // 2, 582), "open your advancements (L) and follow the toasts", F_BODY(23), fill=MUTE, anchor="ma")
    text(ImageDraw.Draw(img), (W // 2, 664), "From the still centre of zero to the resolved crest.", F_ITAL(24), fill=BRONZE, anchor="ma")
    save(img, "08_great_work.png")


def main():
    hero(); energy(); wireless(); values(); tablet_table(); grove(); gear(); great_work()
    print("done — promo/")


if __name__ == "__main__":
    main()
