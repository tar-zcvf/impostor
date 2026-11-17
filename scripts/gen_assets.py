#!/usr/bin/env python3
"""Generate professional branding assets for the Masked party game.

Produces:
  - Adaptive icon foreground (108dp safe zone) + background, at all mipmap densities.
  - Legacy square/round launcher icons for older densities.
  - Play Store feature graphic (1024x500) and icon (512x512).
"""
import os
from PIL import Image, ImageDraw

OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res")

# Brand palette: deep plum -> violet gradient with a gold "eye" mask motif.
BG_TOP = (46, 20, 66)
BG_BOTTOM = (88, 34, 110)
GOLD = (242, 197, 92)
GOLD_DARK = (196, 150, 60)
CREAM = (250, 246, 238)
MASK = (24, 12, 34)


def vgrad(size, top, bottom):
    w, h = size
    img = Image.new("RGB", (w, h))
    d = ImageDraw.Draw(img)
    for y in range(h):
        t = y / max(1, h - 1)
        c = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
        d.line([(0, y), (w, y)], fill=c)
    return img


def draw_mask(d, cx, cy, s, fill=MASK, stroke=GOLD, sw=1):
    """Draw a stylized venetian mask (eye motif) centered at (cx,cy), scaled by s."""
    # Two almond eye shapes joined by a bridge, with a rounded bottom.
    def almond(x0, x1, y0, y1, w):
        d.polygon([(x0, y1 // 2), ((x0 + x1) // 2, y0), (x1, y1 // 2),
                   ((x0 + x1) // 2, y1)], fill=fill, outline=stroke, width=sw)
    ex = int(0.30 * s)   # eye horizontal radius
    ey = int(0.16 * s)   # eye vertical radius
    gap = int(0.06 * s)  # bridge gap
    # left eye
    d.ellipse([cx - ex - gap, cy - ey, cx - gap, cy + ey], fill=fill, outline=stroke, width=sw)
    # right eye
    d.ellipse([cx + gap, cy - ey, cx + ex + gap, cy + ey], fill=fill, outline=stroke, width=sw)
    # bridge
    d.rectangle([cx - gap, cy - int(0.03 * s), cx + gap, cy + int(0.03 * s)], fill=stroke)
    # "gaze" dots (the eye sees the imposter)
    dr = int(0.05 * s)
    d.ellipse([cx - int(0.18 * s) - dr, cy - dr, cx - int(0.18 * s) + dr, cy + dr], fill=CREAM)
    d.ellipse([cx + int(0.18 * s) - dr, cy - dr, cx + int(0.18 * s) + dr, cy + dr], fill=CREAM)
    # bottom curve of the mask
    d.arc([cx - ex - gap, cy - int(0.02 * s), cx + ex + gap, cy + int(0.6 * s)],
          start=20, end=160, fill=stroke, width=sw)


def make_foreground(size):
    """Transparent foreground with the mask motif inside the 108dp safe zone."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    cx = cy = size // 2
    s = int(size * 0.52)
    # soft halo
    for r in range(int(s * 0.62), int(s * 0.40), -2):
        a = max(0, min(255, int(90 * (r - s * 0.40) / (s * 0.22))))
        d.ellipse([cx - r, cy - r, cx + r, cy + r], outline=(GOLD[0], GOLD[1], GOLD[2], a), width=2)
    draw_mask(d, cx, cy, s, fill=MASK, stroke=GOLD, sw=max(2, size // 160))
    return img


def make_background(size):
    return vgrad((size, size), BG_TOP, BG_BOTTOM)


def make_legacy_icon(size, round_icon=False):
    """Legacy (pre-adaptive) launcher icon: gradient + mask on a solid plate."""
    base = vgrad((size, size), BG_TOP, BG_BOTTOM).convert("RGBA")
    d = ImageDraw.Draw(base)
    cx = cy = size // 2
    plate = int(size * 0.92)
    if round_icon:
        d.ellipse([cx - plate // 2, cy - plate // 2, cx + plate // 2, cy + plate // 2],
                  fill=(30, 14, 40, 255))
    else:
        d.rounded_rectangle([cx - plate // 2, cy - plate // 2, cx + plate // 2, cy + plate // 2],
                            radius=int(size * 0.22), fill=(30, 14, 40, 255))
    draw_mask(d, cx, cy, int(size * 0.5), fill=MASK, stroke=GOLD, sw=max(2, size // 150))
    return base


def main():
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    # Adaptive icon: foreground 108dp + background, full-bleed 432dp source scaled down.
    fg_src = make_foreground(432)
    bg_src = make_background(432)
    for folder, px in densities.items():
        # adaptive parts are 108dp => px * (108/108) but source 432 = 108*4
        scale = px / 108 * 4  # from 432 source
        fgs = int(round(432 * (px / 108) / 4))
        fg = fg_src.resize((px * 4 // 4, px * 4 // 4), Image.LANCZOS)
        bg = bg_src.resize((px * 4 // 4, px * 4 // 4), Image.LANCZOS)
        d = os.path.join(OUT, folder)
        os.makedirs(d, exist_ok=True)
        fg.save(os.path.join(d, "ic_launcher_foreground.png"))
        bg.save(os.path.join(d, "ic_launcher_background.png"))
        # legacy
        make_legacy_icon(px).save(os.path.join(d, "ic_launcher.png"))
        make_legacy_icon(px, round_icon=True).save(os.path.join(d, "ic_launcher_round.png"))

    # anydpi-v26: adaptive icon references only (no PNGs needed)
    anydpi = os.path.join(OUT, "mipmap-anydpi-v26")
    os.makedirs(anydpi, exist_ok=True)
    with open(os.path.join(anydpi, "ic_launcher.xml"), "w") as f:
        f.write('''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
    <monochrome android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
''')
    with open(os.path.join(anydpi, "ic_launcher_round.xml"), "w") as f:
        f.write('''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
    <monochrome android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
''')

    # Play Store assets
    ps = os.path.join(OUT, "..", "playstore")
    os.makedirs(ps, exist_ok=True)
    make_legacy_icon(512).save(os.path.join(ps, "icon_512.png"))
    # Feature graphic 1024x500
    fg = vgrad((1024, 500), BG_TOP, BG_BOTTOM).convert("RGBA")
    d = ImageDraw.Draw(fg)
    draw_mask(d, 250, 250, 300, fill=MASK, stroke=GOLD, sw=8)
    # wordmark
    try:
        from PIL import ImageFont
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 120)
        small = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 44)
    except Exception:
        font = ImageFont.load_default()
        small = ImageFont.load_default()
    d.text((430, 170), "MASKED", font=font, fill=CREAM)
    d.text((434, 320), "guess the word. catch the imposter.", font=small, fill=GOLD)
    fg.save(os.path.join(ps, "feature_graphic_1024x500.png"))

    print("assets generated under", OUT)


if __name__ == "__main__":
    main()
