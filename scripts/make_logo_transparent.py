"""Convert the Fleet Maintenance Service logo to a transparent-background
PNG so it visually blends with white surfaces (login card, navbar) instead
of carrying its native light-gray backdrop.

Strategy: corners are sampled to determine the dominant background color,
then every pixel within a small color distance of that color is made
transparent. A soft alpha falloff is applied to avoid hard edges.
"""

from PIL import Image
from pathlib import Path

SRC = Path("src/main/resources/static/img/logo.png")
OUT = Path("src/main/resources/static/img/logo.png")
TOLERANCE = 18          # squared distance threshold for hard-cut
FALLOFF = 14            # additional band that becomes partially transparent

img = Image.open(SRC).convert("RGBA")
w, h = img.size
px = img.load()

samples = [px[0, 0], px[w - 1, 0], px[0, h - 1], px[w - 1, h - 1]]
br = sum(s[0] for s in samples) // 4
bg = sum(s[1] for s in samples) // 4
bb = sum(s[2] for s in samples) // 4
print(f"Detected background: rgb({br},{bg},{bb})")

for y in range(h):
    for x in range(w):
        r, g, b, a = px[x, y]
        dr, dg, db = r - br, g - bg, b - bb
        d2 = dr * dr + dg * dg + db * db
        cutoff = TOLERANCE * TOLERANCE
        soft = (TOLERANCE + FALLOFF) * (TOLERANCE + FALLOFF)
        if d2 <= cutoff:
            px[x, y] = (r, g, b, 0)
        elif d2 < soft:
            frac = (d2 - cutoff) / (soft - cutoff)
            px[x, y] = (r, g, b, int(a * frac))

img.save(OUT, format="PNG", optimize=True)
print(f"Wrote {OUT} ({OUT.stat().st_size} bytes)")
