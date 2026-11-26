"""Render 'Logo 1 · Brick Smash' (canvas design) into Android launcher icons.

Coordinates are the canvas design's 512 px frame; everything is drawn at 4x and downsampled.
Outputs:
  - adaptive icon layers (background + foreground, 108dp canvas) for mdpi..xxxhdpi
  - legacy ic_launcher / ic_launcher_round PNGs for API 24-25
  - Play Store 512x512 icon
"""
import math
import os
import sys
from PIL import Image, ImageDraw, ImageFilter

S = 4
N = 512 * S


def hex2rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(len(a)))


def background():
    """linear-gradient(160deg, #2B1E8C 0%, #0A0F2E 55%, #3A0B4D 100%) + cyan radial glow."""
    stops = [(0.0, hex2rgb('#2B1E8C')), (0.55, hex2rgb('#0A0F2E')), (1.0, hex2rgb('#3A0B4D'))]
    ang = math.radians(160)
    dx, dy = math.sin(ang), -math.cos(ang)  # CSS: 0deg = up, clockwise
    half = (abs(dx) + abs(dy)) * N / 2
    col = Image.new('RGB', (N, N))
    px = col.load()
    # gradient depends only on projection, compute per row/col cheaply with a small image then resize
    small = 256
    g = Image.new('RGB', (small, small))
    gp = g.load()
    for y in range(small):
        for x in range(small):
            X = (x + 0.5) / small * N - N / 2
            Y = (y + 0.5) / small * N - N / 2
            t = ((X * dx + Y * dy) + half) / (2 * half)
            t = min(1, max(0, t))
            for i in range(len(stops) - 1):
                if stops[i][0] <= t <= stops[i + 1][0]:
                    u = (t - stops[i][0]) / (stops[i + 1][0] - stops[i][0])
                    gp[x, y] = lerp(stops[i][1], stops[i + 1][1], u)
                    break
    col = g.resize((N, N), Image.BICUBIC).convert('RGBA')
    # radial-gradient(circle at 62% 62%, rgba(92,235,255,0.28) 0%, transparent 45%)
    glow = Image.new('RGBA', (small, small), (0, 0, 0, 0))
    gl = glow.load()
    cx, cy = 0.62 * small, 0.62 * small
    rmax = 0.45 * math.hypot(small, small) / math.sqrt(2) * 1.0
    for y in range(small):
        for x in range(small):
            d = math.hypot(x - cx, y - cy) / rmax
            a = max(0.0, 1 - d) * 0.28
            gl[x, y] = (92, 235, 255, int(a * 255))
    col.alpha_composite(glow.resize((N, N), Image.BICUBIC))
    return col


def rounded(draw, box, r, fill):
    draw.rounded_rectangle(box, radius=r, fill=fill)


def glow_layer(size, draw_fn, blur, alpha_mul=1.0):
    layer = Image.new('RGBA', size, (0, 0, 0, 0))
    draw_fn(ImageDraw.Draw(layer))
    layer = layer.filter(ImageFilter.GaussianBlur(blur))
    if alpha_mul != 1.0:
        a = layer.getchannel('A').point(lambda v: int(v * alpha_mul))
        layer.putalpha(a)
    return layer


def artwork():
    """Bricks, shards, trail and ball on a transparent 512x512 (x4) layer."""
    art = Image.new('RGBA', (N, N), (0, 0, 0, 0))
    colors = [
        ['#FF4D2D', '#FFC928', '#5CE637', '#E040FB'],
        ['#29D8FF', '#FF4D2D', None, '#5CE637'],
        ['#E040FB', '#29D8FF', '#FFC928', '#FF4D2D'],
    ]
    bricks = []
    for r, row in enumerate(colors):
        for i, c in enumerate(row):
            if c:
                bricks.append((66 + i * 100, 92 + r * 54, 88, 42, c))

    # brick glows (box-shadow: 0 0 22px color99)
    for (x, y, w, h, c) in bricks:
        rgb = hex2rgb(c)
        art.alpha_composite(glow_layer((N, N), lambda d: rounded(d, [x * S, y * S, (x + w) * S, (y + h) * S], 11 * S, rgb + (150,)), 11 * S))
    for (x, y, w, h, c) in bricks:
        rgb = hex2rgb(c)
        tile = Image.new('RGBA', (w * S, h * S), (0, 0, 0, 0))
        d = ImageDraw.Draw(tile)
        rounded(d, [0, 0, w * S - 1, h * S - 1], 11 * S, rgb + (255,))
        # inset highlight (top 5px white 38%) and inset shadow (bottom 5px black 22%), clipped to the brick
        mask = tile.getchannel('A')
        hi = Image.new('RGBA', tile.size, (255, 255, 255, 0))
        ImageDraw.Draw(hi).rectangle([0, 0, w * S, 5 * S], fill=(255, 255, 255, 97))
        sh = Image.new('RGBA', tile.size, (0, 0, 0, 0))
        ImageDraw.Draw(sh).rectangle([0, (h - 5) * S, w * S, h * S], fill=(0, 0, 0, 56))
        for layer in (hi, sh):
            la = Image.composite(layer, Image.new('RGBA', tile.size, (0, 0, 0, 0)), mask)
            tile.alpha_composite(la)
        art.alpha_composite(tile, (x * S, y * S))

    # shards: [x, y, w, h, rot, color]
    shards = [(278, 138, 26, 18, 28, '#FFC928'), (318, 178, 18, 14, -22, '#FFE58A'), (356, 132, 20, 16, 45, '#FFC928'),
              (296, 206, 14, 12, 15, '#FFF3C4'), (372, 186, 16, 12, -35, '#FFC928')]
    for (x, y, w, h, rot, c) in shards:
        rgb = hex2rgb(c)
        pad = 20 * S
        t = Image.new('RGBA', (w * S + 2 * pad, h * S + 2 * pad), (0, 0, 0, 0))
        ImageDraw.Draw(t).rounded_rectangle([pad, pad, pad + w * S, pad + h * S], radius=4 * S, fill=rgb + (255,))
        g = t.filter(ImageFilter.GaussianBlur(6 * S))
        g.alpha_composite(t)
        g = g.rotate(-rot, resample=Image.BICUBIC)  # CSS rotate is clockwise
        cx, cy = (x + w / 2) * S, (y + h / 2) * S
        art.alpha_composite(g, (int(cx - g.width / 2), int(cy - g.height / 2)))

    # trail circles (blur 2px)
    for (x, y, s, a) in [(236, 330, 52, 0.38), (190, 380, 40, 0.26), (150, 424, 30, 0.17), (118, 460, 22, 0.10)]:
        art.alpha_composite(glow_layer((N, N), lambda d: d.ellipse([x * S, y * S, (x + s) * S, (y + s) * S], fill=(92, 235, 255, int(a * 255))), 2 * S))

    # ball glow: 0 0 34px 10px rgba(92,235,255,.75), 0 0 80px 24px rgba(56,189,248,.35)
    bx, by, bs = 286, 248, 76
    for spread, blur, rgba in [(24, 40, (56, 189, 248, 90)), (10, 17, (92, 235, 255, 191))]:
        art.alpha_composite(glow_layer((N, N), lambda d: d.ellipse([(bx - spread) * S, (by - spread) * S, (bx + bs + spread) * S, (by + bs + spread) * S], fill=rgba), blur * S))
    # ball body: radial-gradient(circle at 38% 34%, #FFF 0%, #E0FBFF 28%, #5CEBFF 62%, #0E7490 100%)
    D = bs * S
    ball = Image.new('RGBA', (D, D), (0, 0, 0, 0))
    bp = ball.load()
    stops = [(0, hex2rgb('#FFFFFF')), (0.28, hex2rgb('#E0FBFF')), (0.62, hex2rgb('#5CEBFF')), (1.0, hex2rgb('#0E7490'))]
    fx, fy = 0.38 * D, 0.34 * D
    far = max(math.hypot(fx - cx, fy - cy) for cx in (0, D) for cy in (0, D))  # circle farthest-corner
    for yy in range(D):
        for xx in range(D):
            if (xx - D / 2) ** 2 + (yy - D / 2) ** 2 > (D / 2) ** 2:
                continue
            t = min(1.0, math.hypot(xx - fx, yy - fy) / far)
            for i in range(len(stops) - 1):
                if stops[i][0] <= t <= stops[i + 1][0]:
                    u = (t - stops[i][0]) / (stops[i + 1][0] - stops[i][0])
                    bp[xx, yy] = lerp(stops[i][1], stops[i + 1][1], u) + (255,)
                    break
    art.alpha_composite(ball, (bx * S, by * S))
    return art


def rounded_mask(size, radius):
    m = Image.new('L', (size, size), 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=255)
    return m


def main(res_dir, store_out):
    bg = background()
    art = artwork()
    full = bg.copy()
    full.alpha_composite(art)

    # Play Store icon: full-bleed square (Play applies its own mask)
    full.resize((512, 512), Image.LANCZOS).convert('RGB').save(store_out)

    densities = {'mdpi': 1, 'hdpi': 1.5, 'xhdpi': 2, 'xxhdpi': 3, 'xxxhdpi': 4}
    for name, f in densities.items():
        d = os.path.join(res_dir, 'mipmap-' + name)
        os.makedirs(d, exist_ok=True)
        # Adaptive layers: 108dp canvas. Art (whole 512 frame) scaled to 58% of it, centred,
        # so the bricks and ball stay inside the 66dp safe zone used by circle masks.
        fg_px = int(round(108 * f))
        bg.resize((fg_px, fg_px), Image.LANCZOS).convert('RGB').save(os.path.join(d, 'ic_launcher_background.png'))
        fg = Image.new('RGBA', (N, N), (0, 0, 0, 0))
        k = 0.58
        scaled = art.resize((int(N * k), int(N * k)), Image.LANCZOS)
        off = (N - scaled.width) // 2
        fg.alpha_composite(scaled, (off, off))
        fg.resize((fg_px, fg_px), Image.LANCZOS).save(os.path.join(d, 'ic_launcher_foreground.png'))

        # Legacy icons (API 24-25): 48dp
        lg = int(round(48 * f))
        sq = full.resize((lg, lg), Image.LANCZOS)
        sq.putalpha(rounded_mask(lg, int(lg * 0.22)))
        sq.save(os.path.join(d, 'ic_launcher.png'))
        # Round legacy: art shrunk a bit so the brick wall fits in the circle
        rd = bg.copy()
        small_art = art.resize((int(N * 0.8), int(N * 0.8)), Image.LANCZOS)
        rd.alpha_composite(small_art, ((N - small_art.width) // 2, (N - small_art.height) // 2))
        rd = rd.resize((lg, lg), Image.LANCZOS)
        cm = Image.new('L', (lg, lg), 0)
        ImageDraw.Draw(cm).ellipse([0, 0, lg - 1, lg - 1], fill=255)
        rd.putalpha(cm)
        rd.save(os.path.join(d, 'ic_launcher_round.png'))

    # Previews for checking
    os.makedirs('/home/claude/icon/out', exist_ok=True)
    prev = full.resize((512, 512), Image.LANCZOS)
    prev.putalpha(rounded_mask(512, 112))
    prev.save('/home/claude/icon/out/preview_square.png')
    # simulate a circle-masked adaptive icon at 512 (visible = centre 72/108 of the canvas)
    canvas = bg.resize((768, 768), Image.LANCZOS)
    fgp = Image.open(os.path.join(res_dir, 'mipmap-xxxhdpi', 'ic_launcher_foreground.png')).resize((768, 768), Image.LANCZOS)
    canvas.alpha_composite(fgp)
    vis = canvas.crop((128, 128, 640, 640))
    cm = Image.new('L', (512, 512), 0)
    ImageDraw.Draw(cm).ellipse([0, 0, 511, 511], fill=255)
    vis.putalpha(cm)
    vis.save('/home/claude/icon/out/preview_adaptive_circle.png')


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
