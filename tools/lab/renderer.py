"""
EcosystemRenderer — Python port of the Android EcosystemRenderer.
Replaces android.graphics with Pillow + numpy.

Layer order (bottom → top):
  0  Deep space base (#030306)
  1  Colour landscape   — palette blobs at 1/4 res, upscaled
  2  Dissolved hero     — three blur levels: aura / haze / ghost
  3  Flow lines         — curl-noise vector field
  4  Sacred geometry    — Flower of Life + Golden Spiral
  5  Particle field     — glowing specks
  6  Atmospheric glows  — large soft radial gradients
  7  Vignette + colour grade
"""
import math
import colorsys
import random
from dataclasses import dataclass
from pathlib import Path
from typing import List, Tuple, Optional

import numpy as np
from PIL import Image, ImageDraw, ImageFont

_GOLDEN_ANGLE = math.radians(137.508)

# ── Font utilities ────────────────────────────────────────────────────────────

_FONT_CACHE: dict = {}
_FONT_PATHS = [
    "C:/Windows/Fonts/impact.ttf",
    "C:/Windows/Fonts/ariblk.ttf",
    "C:/Windows/Fonts/arial.ttf",
]

def _font(size: int):
    if size not in _FONT_CACHE:
        for p in _FONT_PATHS:
            try:
                _FONT_CACHE[size] = ImageFont.truetype(p, size)
                break
            except Exception:
                continue
        if size not in _FONT_CACHE:
            try:
                _FONT_CACHE[size] = ImageFont.load_default(size=size)
            except Exception:
                _FONT_CACHE[size] = ImageFont.load_default()
    return _FONT_CACHE[size]


def _text_layer(text: str, size: int, color: RGBA, angle: float = 0) -> Image.Image:
    """Render text as a rotated RGBA image."""
    fnt = _font(size)
    tmp = Image.new("RGBA", (1, 1))
    try:
        bbox = ImageDraw.Draw(tmp).textbbox((0, 0), text, font=fnt)
        tw, th = bbox[2] - bbox[0] + 4, bbox[3] - bbox[1] + 4
    except Exception:
        tw, th = len(text) * size // 2 + 4, size + 4
    img = Image.new("RGBA", (tw, th), (0, 0, 0, 0))
    ImageDraw.Draw(img).text((2, 2), text, font=fnt, fill=color)
    if abs(angle) > 0.5:
        img = img.rotate(-angle, expand=True, resample=Image.BICUBIC)
    return img


def _module_extract_palette(img: Image.Image) -> dict:
    """Lightweight palette extraction for use outside EcosystemRenderer."""
    small = img.resize((64, 64), Image.LANCZOS).convert("RGB")
    try:
        q = small.quantize(colors=8)
        p = q.getpalette()
        colors = [(p[i*3], p[i*3+1], p[i*3+2]) for i in range(8)]
    except Exception:
        colors = [(128, 128, 128)] * 8
    def hsv(c): return colorsys.rgb_to_hsv(c[0]/255, c[1]/255, c[2]/255)
    hsv_l = [hsv(c) for c in colors]
    dominant = colors[0]
    vib_cands = [(c, h) for c, h in zip(colors, hsv_l) if h[1] > 0.3 and 0.3 <= h[2] <= 0.95]
    vibrant = max(vib_cands, key=lambda x: x[1][1], default=(colors[0], hsv_l[0]))[0]
    muted = min(zip(colors, hsv_l), key=lambda x: x[1][1])[0]
    return {"dominant": dominant, "vibrant": vibrant, "muted": muted, "all": colors}


# ── Shared drawing utilities (used by all renderers) ─────────────────────────

def _paste_rgba(canvas: Image.Image, img: Image.Image, cx: float, cy: float) -> Image.Image:
    """Alpha-composite img centered at (cx, cy). Handles out-of-bounds cleanly."""
    cw, ch = canvas.size
    iw, ih = img.size
    x, y = int(cx - iw / 2), int(cy - ih / 2)
    sx, sy = max(0, -x), max(0, -y)
    dx, dy = max(0, x), max(0, y)
    pw, ph = min(iw - sx, cw - dx), min(ih - sy, ch - dy)
    if pw <= 0 or ph <= 0:
        return canvas
    crop = img.crop((sx, sy, sx + pw, sy + ph))
    layer = Image.new("RGBA", (cw, ch), (0, 0, 0, 0))
    layer.paste(crop, (dx, dy), crop)
    return Image.alpha_composite(canvas, layer)


def _make_shadow(img: Image.Image, alpha: int = 160) -> Image.Image:
    """Black shadow matching the shape of img."""
    shadow = Image.new("RGBA", img.size, (0, 0, 0, 0))
    black = Image.new("RGBA", img.size, (0, 0, 0, alpha))
    mask = img.split()[3] if img.mode == "RGBA" else Image.new("L", img.size, 255)
    shadow.paste(black, mask=mask)
    return shadow


def _circle_crop(img: Image.Image, size: int) -> Image.Image:
    """Circular crop of diameter `size`."""
    img = img.resize((size, size), Image.LANCZOS).convert("RGBA")
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, size - 1, size - 1), fill=255)
    img.putalpha(mask)
    return img


def _dominant_rgb(img: Image.Image) -> Tuple[int, int, int]:
    small = img.resize((32, 32), Image.LANCZOS).convert("RGB")
    try:
        q = small.quantize(colors=1)
        p = q.getpalette()
        return (p[0], p[1], p[2])
    except Exception:
        return (100, 100, 180)


def _sort_by_relevance(items):
    if any(i.playcount > 0 for i in items):
        return sorted(items, key=lambda i: i.playcount, reverse=True)
    return sorted(items, key=lambda i: i.rank)

RGBA = Tuple[int, int, int, int]
RGB = Tuple[int, int, int]


@dataclass
class RenderItem:
    image: Image.Image
    name: str
    artist: str = ""
    playcount: int = 0
    rank: int = 0
    mbid: Optional[str] = None
    logo: Optional[Image.Image] = None   # transparent PNG logo from fanart.tv


class EcosystemRenderer:
    GOLDEN_ANGLE = math.radians(137.508)
    GOLDEN_B = math.log(1.618034) / (math.pi / 2)
    NOISE_EPS = 0.01

    def __init__(
        self,
        max_items: int = 12,
        flow_lines: int = 700,
        flow_steps: int = 90,
        hero_opacity_aura: float = 0.38,
        hero_opacity_haze: float = 0.28,
        hero_opacity_ghost: float = 0.13,
        color_field_alpha: int = 210,
        particles: int = 280,
        geometry: bool = True,
        glows: bool = True,
        vignette: bool = True,
    ):
        self.max_items = max_items
        self.flow_lines = flow_lines
        self.flow_steps = flow_steps
        self.hero_opacity_aura = hero_opacity_aura
        self.hero_opacity_haze = hero_opacity_haze
        self.hero_opacity_ghost = hero_opacity_ghost
        self.color_field_alpha = color_field_alpha
        self.particles_count = particles
        self.draw_geometry = geometry
        self.draw_glows = glows
        self.draw_vignette = vignette

    # ── Entry point ───────────────────────────────────────────────────────────

    def render(self, items: List[RenderItem], width: int, height: int) -> Image.Image:
        canvas = Image.new("RGBA", (width, height), (3, 3, 6, 255))

        if not items:
            return canvas

        sorted_items = self._sort_by_relevance(items)[: self.max_items]
        palettes = [self._extract_palette(item.image) for item in sorted_items]

        # Deterministic but decorrelated seeds
        seed = 0
        for item in sorted_items:
            seed = (seed * 31 + hash(item.name)) & 0xFFFFFFFF
        rng1 = random.Random(seed)
        rng2 = random.Random((seed * 6364136223846793005 + 1442695040888963407) & 0xFFFFFFFF)
        noise_ox = rng1.random() * 200
        noise_oy = rng1.random() * 200

        # 0: Deep space base already set above

        # 1: Colour landscape
        fw, fh = width // 4, height // 4
        color_field = self._build_color_field(sorted_items, palettes, fw, fh)
        upscaled = color_field.resize((width, height), Image.BILINEAR)
        canvas = Image.alpha_composite(canvas, self._set_alpha(upscaled, self.color_field_alpha))
        canvas = Image.alpha_composite(canvas, Image.new("RGBA", (width, height), (2, 2, 8, 155)))

        # 2: Dissolved hero
        canvas = self._draw_dissolved_hero(canvas, sorted_items[0].image, width, height)

        # 3: Flow lines
        canvas = self._draw_flow_lines(
            canvas, color_field, palettes, rng1, noise_ox, noise_oy, width, height
        )

        # 4: Sacred geometry
        if self.draw_geometry:
            canvas = self._draw_sacred_geometry(canvas, palettes[0], len(sorted_items), width, height)

        # 5: Particles
        canvas = self._draw_particles(canvas, palettes, rng2, width, height)

        # 6: Atmospheric glows
        if self.draw_glows:
            canvas = self._draw_color_glows(canvas, palettes, width, height)

        # 7: Vignette + colour grade
        if self.draw_vignette:
            canvas = self._draw_vignette(canvas, width, height)
        canvas = self._draw_color_grade(canvas, palettes[0], width, height)

        return canvas

    # ── Layer 1: Colour landscape ─────────────────────────────────────────────

    def _build_color_field(
        self, items: List[RenderItem], palettes: list, w: int, h: int
    ) -> Image.Image:
        canvas = Image.new("RGBA", (w, h), (3, 3, 6, 255))
        for i, pal in enumerate(palettes):
            viv = pal["vibrant"]
            mut = pal["muted"]
            dom = pal["dominant"]
            progress = i / max(len(palettes), 1)
            cx = w * 0.5 + math.cos(i * self.GOLDEN_ANGLE) * w * 0.40 * progress
            cy = h * 0.5 + math.sin(i * self.GOLDEN_ANGLE) * h * 0.40 * progress
            radius = w * (0.65 - 0.30 * progress)
            alpha = max(115 - i * 9, 28)
            blob = self._radial_gradient(w, h, cx, cy, radius, [
                ((*viv, alpha), 0.00),
                ((*mut, alpha // 2), 0.28),
                ((*dom, alpha // 5), 0.58),
                ((0, 0, 0, 0), 1.00),
            ])
            canvas = Image.alpha_composite(canvas, blob)
        return canvas

    # ── Layer 2: Dissolved hero ───────────────────────────────────────────────

    def _draw_dissolved_hero(
        self, canvas: Image.Image, bitmap: Image.Image, width: int, height: int
    ) -> Image.Image:
        hero_sz = width * 0.76
        cx, cy = width * 0.50, height * 0.50

        # Aura: 1/10 res → max blur, widest spread
        aw = max(int(hero_sz / 10), 1)
        aura = bitmap.resize((aw, aw), Image.NEAREST).resize(
            (int(hero_sz * 1.45), int(hero_sz * 1.45)), Image.BILINEAR
        )
        canvas = self._draw_faded(canvas, aura, cx, cy, hero_sz * 1.45, 0.40, self.hero_opacity_aura)

        # Haze: 1/5 res → moderate blur
        hw = max(int(hero_sz / 5), 1)
        haze = bitmap.resize((hw, hw), Image.NEAREST).resize(
            (int(hero_sz), int(hero_sz)), Image.BILINEAR
        )
        canvas = self._draw_faded(canvas, haze, cx, cy, hero_sz, 0.58, self.hero_opacity_haze)

        # Ghost: original quality, barely visible
        canvas = self._draw_faded(canvas, bitmap, cx, cy, hero_sz * 0.60, 0.68, self.hero_opacity_ghost)

        return canvas

    # ── Layer 3: Flow lines ───────────────────────────────────────────────────

    def _draw_flow_lines(
        self,
        canvas: Image.Image,
        color_field: Image.Image,
        palettes: list,
        rng: random.Random,
        noise_ox: float,
        noise_oy: float,
        width: int,
        height: int,
    ) -> Image.Image:
        layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        draw = ImageDraw.Draw(layer)
        noise_scale = 2.9 / width
        step_len = width * 0.0023

        for _ in range(self.flow_lines):
            sx = rng.random() * width
            sy = rng.random() * height
            is_energy = rng.random() < 0.33

            if is_energy:
                pal = palettes[rng.randint(0, len(palettes) - 1)]
                col = pal["vibrant"]
            else:
                col = self._sample_field(color_field, sx, sy, width, height)

            alpha = (45 + rng.randint(0, 39)) if is_energy else (22 + rng.randint(0, 34))
            stroke = max(1, round((0.6 + rng.random() * 0.8) if is_energy else (0.3 + rng.random() * 0.7)))
            color_rgba = (col[0], col[1], col[2], alpha)

            x, y = sx, sy
            pts = [(x, y)]
            for _ in range(self.flow_steps):
                vx, vy = self._curl_at(x * noise_scale + noise_ox, y * noise_scale + noise_oy)
                x += vx * step_len
                y += vy * step_len
                if x < -width * 0.12 or x > width * 1.12 or y < -height * 0.12 or y > height * 1.12:
                    break
                pts.append((x, y))

            if len(pts) >= 2:
                draw.line(pts, fill=color_rgba, width=stroke)

        return Image.alpha_composite(canvas, layer)

    # ── Layer 4: Sacred geometry ──────────────────────────────────────────────

    def _draw_sacred_geometry(
        self, canvas: Image.Image, palette: dict, num_items: int, width: int, height: int
    ) -> Image.Image:
        layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        draw = ImageDraw.Draw(layer)
        viv, mut = palette["vibrant"], palette["muted"]
        cx, cy = width * 0.50, height * 0.50

        fol_col = (*viv, 16)
        self._flower_of_life(draw, cx, cy, width * 0.095, fol_col)
        self._golden_spiral(draw, cx + width * 0.07, cy - height * 0.04, width * 0.37, viv, 18)
        if num_items >= 5:
            self._golden_spiral(draw, cx - width * 0.18, cy + height * 0.13, width * 0.20, mut, 12)

        return Image.alpha_composite(canvas, layer)

    def _flower_of_life(self, draw: ImageDraw.ImageDraw, cx: float, cy: float, r: float, col: RGBA):
        self._circle(draw, cx, cy, r, col)
        for i in range(6):
            a = i * math.pi / 3
            self._circle(draw, cx + math.cos(a) * r, cy + math.sin(a) * r, r, col)
        self._circle(draw, cx, cy, r * 2, col)

    def _circle(self, draw: ImageDraw.ImageDraw, cx: float, cy: float, r: float, col: RGBA):
        draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=col)

    def _golden_spiral(
        self, draw: ImageDraw.ImageDraw, cx: float, cy: float, max_r: float, col: RGB, alpha: int
    ):
        pts, theta = [], 0.0
        while theta < 6 * math.pi:
            r = 2 * math.exp(self.GOLDEN_B * theta)
            if r > max_r:
                break
            pts.append((cx + r * math.cos(theta), cy + r * math.sin(theta)))
            theta += 0.04
        if len(pts) >= 2:
            draw.line(pts, fill=(*col, alpha), width=1)

    # ── Layer 5: Particles ────────────────────────────────────────────────────

    def _draw_particles(
        self, canvas: Image.Image, palettes: list, rng: random.Random, width: int, height: int
    ) -> Image.Image:
        layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        draw = ImageDraw.Draw(layer)
        for i in range(self.particles_count):
            x, y = rng.random() * width, rng.random() * height
            pal = palettes[i % len(palettes)]
            col = pal["vibrant"] if rng.random() > 0.5 else pal["dominant"]
            sz = 0.7 + rng.random() * 3.3
            a = 50 + rng.randint(0, 114)
            if sz > 2.2:
                halo_r = sz * 3.2
                draw.ellipse(
                    (x - halo_r, y - halo_r, x + halo_r, y + halo_r),
                    fill=(*col, a // 6),
                )
            draw.ellipse((x - sz, y - sz, x + sz, y + sz), fill=(*col, a))
        return Image.alpha_composite(canvas, layer)

    # ── Layer 6: Atmospheric glows ────────────────────────────────────────────

    def _draw_color_glows(
        self, canvas: Image.Image, palettes: list, width: int, height: int
    ) -> Image.Image:
        for i, pal in enumerate(palettes[:7]):
            viv, dom = pal["vibrant"], pal["dominant"]
            p = i / 7.0
            cx = width * 0.5 + math.cos(i * self.GOLDEN_ANGLE) * width * 0.27 * p
            cy = height * 0.5 + math.sin(i * self.GOLDEN_ANGLE) * height * 0.27 * p
            radius = width * (0.50 - 0.22 * p)
            alpha = max(68 - i * 9, 16)
            glow = self._radial_gradient(width, height, cx, cy, radius, [
                ((*viv, alpha), 0.00),
                ((*dom, alpha // 3), 0.38),
                ((0, 0, 0, 0), 1.00),
            ])
            canvas = Image.alpha_composite(canvas, glow)
        return canvas

    # ── Layer 7: Vignette + colour grade ─────────────────────────────────────

    def _draw_vignette(self, canvas: Image.Image, width: int, height: int) -> Image.Image:
        vignette = self._radial_gradient(width, height, width / 2, height / 2,
            max(width, height) * 0.68, [
                ((0, 0, 0, 0), 0.28),
                ((0, 0, 5, 200), 1.00),
            ]
        )
        canvas = Image.alpha_composite(canvas, vignette)
        canvas = Image.alpha_composite(canvas, self._linear_gradient_v(
            width, height, 0, height * 0.28, (0, 0, 5, 175), (0, 0, 5, 0)
        ))
        canvas = Image.alpha_composite(canvas, self._linear_gradient_v(
            width, height, height * 0.72, height, (0, 0, 5, 0), (0, 0, 5, 200)
        ))
        return canvas

    def _draw_color_grade(
        self, canvas: Image.Image, palette: dict, width: int, height: int
    ) -> Image.Image:
        dom = palette["dominant"]
        r = min(dom[0] // 8 + 5, 255)
        g = min(dom[1] // 8 + 1, 255)
        b = min(dom[2] // 8 + 32, 255)
        return Image.alpha_composite(canvas, Image.new("RGBA", (width, height), (r, g, b, 30)))

    # ── Primitives ────────────────────────────────────────────────────────────

    def _draw_faded(
        self,
        canvas: Image.Image,
        bitmap: Image.Image,
        cx: float,
        cy: float,
        size: float,
        fade_start: float,
        opacity: float,
    ) -> Image.Image:
        sz = max(int(size), 2)
        scaled = bitmap.resize((sz, sz), Image.BILINEAR).convert("RGBA")
        half = sz / 2.0

        y_arr, x_arr = np.mgrid[0:sz, 0:sz].astype(np.float32)
        dist = np.sqrt((x_arr - half) ** 2 + (y_arr - half) ** 2) / max(half, 1.0)
        fade_range = max(1.0 - fade_start, 0.001)
        alpha_mask = np.where(
            dist <= fade_start, 1.0,
            np.clip(1.0 - (dist - fade_start) / fade_range, 0.0, 1.0),
        )
        alpha_mask = (alpha_mask * 255 * opacity).clip(0, 255).astype(np.uint8)

        _, _, _, a = scaled.split()
        new_alpha = np.minimum(np.array(a, dtype=np.uint8), alpha_mask)
        scaled.putalpha(Image.fromarray(new_alpha, "L"))

        cw, ch = canvas.size
        x_off, y_off = int(cx - sz / 2), int(cy - sz / 2)
        sx0, sy0 = 0, 0
        pw, ph = sz, sz

        if x_off < 0:
            sx0 = -x_off; pw += x_off; x_off = 0
        if y_off < 0:
            sy0 = -y_off; ph += y_off; y_off = 0
        pw = min(pw, cw - x_off)
        ph = min(ph, ch - y_off)
        if pw <= 0 or ph <= 0:
            return canvas

        canvas.alpha_composite(scaled.crop((sx0, sy0, sx0 + pw, sy0 + ph)), dest=(x_off, y_off))
        return canvas

    def _radial_gradient(
        self,
        w: int,
        h: int,
        cx: float,
        cy: float,
        radius: float,
        color_stops: list,  # [(rgba_tuple, stop), ...]
    ) -> Image.Image:
        if not color_stops or radius <= 0 or w <= 0 or h <= 0:
            return Image.new("RGBA", (w, h), (0, 0, 0, 0))

        y_arr, x_arr = np.mgrid[0:h, 0:w].astype(np.float32)
        dist = np.sqrt((x_arr - cx) ** 2 + (y_arr - cy) ** 2) / max(float(radius), 1.0)

        result = np.zeros((h, w, 4), dtype=np.float32)

        for i in range(len(color_stops) - 1):
            c0, s0 = color_stops[i]
            c1, s1 = color_stops[i + 1]
            span = s1 - s0
            if span <= 0:
                continue
            t = np.clip((dist - s0) / span, 0.0, 1.0)
            # Last segment covers everything beyond s0 (CLAMP behaviour)
            in_range = dist >= s0 if i == len(color_stops) - 2 else (dist >= s0) & (dist < s1)
            for ch in range(4):
                result[..., ch] += in_range * ((1.0 - t) * c0[ch] + t * c1[ch])

        np.clip(result, 0, 255, out=result)
        return Image.fromarray(result.astype(np.uint8), "RGBA")

    def _linear_gradient_v(
        self,
        w: int,
        h: int,
        y0: float,
        y1: float,
        c0: RGBA,
        c1: RGBA,
    ) -> Image.Image:
        y_arr = np.tile(np.arange(h, dtype=np.float32).reshape(h, 1), (1, w))
        t = np.clip((y_arr - y0) / max(y1 - y0, 1.0), 0.0, 1.0)
        result = np.zeros((h, w, 4), dtype=np.float32)
        for ch in range(4):
            result[..., ch] = (1.0 - t) * c0[ch] + t * c1[ch]
        return Image.fromarray(result.clip(0, 255).astype(np.uint8), "RGBA")

    def _set_alpha(self, img: Image.Image, alpha: int) -> Image.Image:
        if img.mode != "RGBA":
            img = img.convert("RGBA")
        r, g, b, a = img.split()
        scaled_a = Image.fromarray((np.array(a) * alpha // 255).clip(0, 255).astype(np.uint8))
        return Image.merge("RGBA", (r, g, b, scaled_a))

    def _sample_field(
        self, bmp: Image.Image, x: float, y: float, w: int, h: int
    ) -> RGB:
        bw, bh = bmp.size
        fx = max(0, min(int(x / w * bw), bw - 1))
        fy = max(0, min(int(y / h * bh), bh - 1))
        pixel = bmp.getpixel((fx, fy))
        return (pixel[0], pixel[1], pixel[2])

    # ── Math: curl-noise vector field ─────────────────────────────────────────

    def _curl_at(self, x: float, y: float) -> Tuple[float, float]:
        eps = self.NOISE_EPS

        def n(nx, ny):
            return (
                self._value_noise(nx, ny) * 0.65
                + self._value_noise(nx * 2.7 + 13.1, ny * 2.7 + 7.5) * 0.35
            )

        vx = -(n(x, y + eps) - n(x, y - eps)) / (2 * eps)
        vy = (n(x + eps, y) - n(x - eps, y)) / (2 * eps)
        return vx, vy

    def _value_noise(self, x: float, y: float) -> float:
        xi, yi = int(x), int(y)
        xf, yf = x - xi, y - yi
        xt = xf * xf * (3 - 2 * xf)
        yt = yf * yf * (3 - 2 * yf)
        return self._lerp(
            self._lerp(self._hash2(xi, yi), self._hash2(xi + 1, yi), xt),
            self._lerp(self._hash2(xi, yi + 1), self._hash2(xi + 1, yi + 1), xt),
            yt,
        )

    def _hash2(self, xi: int, yi: int) -> float:
        n = xi * 374761393 + yi * 668265263
        n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
        return ((n ^ (n >> 16)) & 0x7FFFFFFF) / 2147483647.0

    def _lerp(self, a: float, b: float, t: float) -> float:
        return a + (b - a) * t

    # ── Palette extraction ────────────────────────────────────────────────────

    def _extract_palette(self, img: Image.Image) -> dict:
        small = img.resize((64, 64), Image.LANCZOS).convert("RGB")
        try:
            q = small.quantize(colors=8)
            p = q.getpalette()
            colors = [(p[i * 3], p[i * 3 + 1], p[i * 3 + 2]) for i in range(8)]
        except Exception:
            colors = [small.getpixel((x, y)) for x, y in [(0, 0), (63, 0), (0, 63), (63, 63),
                                                            (31, 31), (15, 31), (47, 31), (31, 15)]]

        def hsv(rgb):
            return colorsys.rgb_to_hsv(rgb[0] / 255, rgb[1] / 255, rgb[2] / 255)

        hsv_list = [hsv(c) for c in colors]
        dominant = colors[0]
        vibrant_cands = [(c, h) for c, h in zip(colors, hsv_list) if h[1] > 0.3 and 0.3 <= h[2] <= 0.95]
        vibrant = max(vibrant_cands, key=lambda x: x[1][1], default=(colors[0], hsv_list[0]))[0]
        muted = min(zip(colors, hsv_list), key=lambda x: x[1][1])[0]
        return {"dominant": dominant, "vibrant": vibrant, "muted": muted}

    # ── Sort ──────────────────────────────────────────────────────────────────

    def _sort_by_relevance(self, items: List[RenderItem]) -> List[RenderItem]:
        if any(i.playcount > 0 for i in items):
            return sorted(items, key=lambda i: i.playcount, reverse=True)
        return sorted(items, key=lambda i: i.rank)


# ═════════════════════════════════════════════════════════════════════════════
# MosaicRenderer — portadas reconocibles, composición orgánica
# ═════════════════════════════════════════════════════════════════════════════

class MosaicRenderer:
    """
    Album covers take center stage. No blur, no dissolution.
    Golden-angle scatter with subtle rotation. Cover-first.
    Success metric: can you recognize at least 6 of 12 albums?
    """

    def render(self, items: List[RenderItem], width: int, height: int) -> Image.Image:
        canvas = Image.new("RGBA", (width, height), (8, 8, 12, 255))
        sorted_items = _sort_by_relevance(items)[:16]
        n = len(sorted_items)
        if n == 0:
            return canvas

        seed = sum(hash(it.name) for it in sorted_items) & 0xFFFFFFFF
        rng = random.Random(seed)
        min_dim = min(width, height)

        # Positions: golden-angle spiral outward from center
        # Bigger albums first (top rank), placed closer to center
        placements = []
        for i in range(n):
            size = int(min_dim * max(0.32 - 0.012 * i, 0.16))
            angle = i * _GOLDEN_ANGLE
            spread_x = 0.04 + (i / n) * 0.33
            spread_y = 0.04 + (i / n) * 0.28
            cx = width  * 0.5 + math.cos(angle) * width  * spread_x
            cy = height * 0.45 + math.sin(angle) * height * spread_y
            rot = rng.uniform(-13, 13)
            placements.append((cx, cy, size, rot))

        # Render back-to-front: least important first, top album on top
        for i in range(n - 1, -1, -1):
            cx, cy, sz, rot = placements[i]
            img = sorted_items[i].image.resize((sz, sz), Image.LANCZOS).convert("RGBA")
            if abs(rot) > 0.5:
                img = img.rotate(rot, expand=True, resample=Image.BICUBIC)
            shadow_off = max(sz // 18, 5)
            canvas = _paste_rgba(canvas, _make_shadow(img, alpha=160), cx + shadow_off, cy + shadow_off)
            canvas = _paste_rgba(canvas, img, cx, cy)

        return canvas


# ═════════════════════════════════════════════════════════════════════════════
# HeroRenderer — un protagonista, los demás orbitan
# ═════════════════════════════════════════════════════════════════════════════

class HeroRenderer:
    """
    Most-played album dominates the center. Others orbit at smaller scale.
    Clear visual hierarchy without destroying any cover.
    """

    def render(self, items: List[RenderItem], width: int, height: int) -> Image.Image:
        canvas = Image.new("RGBA", (width, height), (8, 8, 12, 255))
        sorted_items = _sort_by_relevance(items)[:12]
        n = len(sorted_items)
        if n == 0:
            return canvas

        seed = sum(hash(it.name) for it in sorted_items) & 0xFFFFFFFF
        rng = random.Random(seed)
        min_dim = min(width, height)

        # ── Hero
        hero_sz = int(min_dim * 0.52)
        hero_cx, hero_cy = width * 0.5, height * 0.42
        hero_img = sorted_items[0].image.resize((hero_sz, hero_sz), Image.LANCZOS).convert("RGBA")
        shadow_off = max(hero_sz // 22, 8)
        canvas = _paste_rgba(canvas, _make_shadow(hero_img, alpha=200), hero_cx + shadow_off, hero_cy + shadow_off)
        canvas = _paste_rgba(canvas, hero_img, hero_cx, hero_cy)

        # ── Support: ring around hero
        support = sorted_items[1:]
        if not support:
            return canvas

        support_sz_base = int(min_dim * 0.185)
        n_support = len(support)
        for i, item in enumerate(support):
            angle = i * (2 * math.pi / n_support) + math.pi * 0.15
            rx = width  * 0.44
            ry = height * 0.37
            cx = width  * 0.5  + math.cos(angle) * rx
            cy = height * 0.42 + math.sin(angle) * ry
            sz = support_sz_base + rng.randint(-int(support_sz_base * 0.08), int(support_sz_base * 0.08))
            rot = rng.uniform(-10, 10)
            img = item.image.resize((sz, sz), Image.LANCZOS).convert("RGBA")
            if abs(rot) > 0.5:
                img = img.rotate(rot, expand=True, resample=Image.BICUBIC)
            shadow_off = max(sz // 20, 4)
            canvas = _paste_rgba(canvas, _make_shadow(img, alpha=130), cx + shadow_off, cy + shadow_off)
            canvas = _paste_rgba(canvas, img, cx, cy)

        return canvas


# ═════════════════════════════════════════════════════════════════════════════
# ConstellationRenderer — islas visuales conectadas por líneas
# ═════════════════════════════════════════════════════════════════════════════

class ConstellationRenderer:
    """
    Each album = circular island. Connections via constellation lines.
    Dark space. No blur — the connection is geometry, not fog.
    """

    def render(self, items: List[RenderItem], width: int, height: int) -> Image.Image:
        canvas = Image.new("RGBA", (width, height), (4, 5, 14, 255))
        sorted_items = _sort_by_relevance(items)[:12]
        n = len(sorted_items)
        if n == 0:
            return canvas

        canvas = self._draw_stars(canvas, width, height, seed=hash(sorted_items[0].name) & 0xFFFFFFFF)

        # Radius proportional to sqrt(playcount) — bigger = more played
        total_pc = sum(it.playcount for it in sorted_items) or n
        min_r = int(min(width, height) * 0.085)
        max_r = int(min(width, height) * 0.21)
        radii = []
        for it in sorted_items:
            frac = (it.playcount / total_pc) if total_pc > n else (1 / n)
            r = int(min_r + (max_r - min_r) * min(frac * n, 1.0) ** 0.5)
            radii.append(max(min_r, min(max_r, r)))

        # Layout: golden-angle spiral
        positions = []
        for i in range(n):
            angle = i * _GOLDEN_ANGLE
            spread = 0.08 + (i / n) * 0.31
            cx = width  * 0.5 + math.cos(angle) * width  * spread
            cy = height * 0.5 + math.sin(angle) * height * spread * 0.75
            positions.append((cx, cy))

        # Constellation lines (rank-adjacent connections)
        line_layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        ld = ImageDraw.Draw(line_layer)
        for i in range(n - 1):
            x0, y0 = positions[i]
            x1, y1 = positions[i + 1]
            col = _dominant_rgb(sorted_items[i].image)
            ld.line([(x0, y0), (x1, y1)], fill=(*col, 50), width=1)
        if n >= 3:
            ld.line([positions[-1], positions[0]], fill=(70, 80, 130, 35), width=1)
        canvas = Image.alpha_composite(canvas, line_layer)

        # Album circles: large first so small ones render on top
        order = sorted(range(n), key=lambda i: radii[i], reverse=True)
        for i in order:
            cx, cy = positions[i]
            r = radii[i]
            dom = _dominant_rgb(sorted_items[i].image)
            # Glow halo
            glow_r = int(r * 1.22)
            glow = Image.new("RGBA", (glow_r * 2, glow_r * 2), (0, 0, 0, 0))
            ImageDraw.Draw(glow).ellipse((0, 0, glow_r * 2 - 1, glow_r * 2 - 1), fill=(*dom, 40))
            canvas = _paste_rgba(canvas, glow, cx, cy)
            canvas = _paste_rgba(canvas, _circle_crop(sorted_items[i].image, r * 2), cx, cy)

        return canvas

    def _draw_stars(self, canvas, width, height, seed):
        layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        draw = ImageDraw.Draw(layer)
        rng = random.Random(seed)
        for _ in range(350):
            x, y = rng.random() * width, rng.random() * height
            a = rng.randint(25, 110)
            if rng.random() < 0.75:
                draw.point((int(x), int(y)), fill=(210, 215, 255, a))
            else:
                sz = rng.random() * 1.4
                draw.ellipse((x - sz, y - sz, x + sz, y + sz), fill=(210, 215, 255, a // 2))
        return Image.alpha_composite(canvas, layer)


# ═════════════════════════════════════════════════════════════════════════════
# MuseumRenderer — galería / colección de vinilos
# ═════════════════════════════════════════════════════════════════════════════

class MuseumRenderer:
    """
    Gallery wall aesthetic. Albums in thin frames with drop shadows.
    The cover is the art. Effects are just the frame.
    """

    # Row layouts by album count: [cols_row1, cols_row2, ...]
    _LAYOUTS = {
        12: [3, 4, 5], 11: [3, 4, 4], 10: [3, 4, 3],
        9:  [3, 3, 3],  8: [4, 4],      7: [3, 4],
        6:  [3, 3],      5: [3, 2],      4: [2, 2],
        3:  [3],          2: [2],          1: [1],
    }

    def render(self, items: List[RenderItem], width: int, height: int) -> Image.Image:
        canvas = Image.new("RGBA", (width, height), (12, 12, 14, 255))
        sorted_items = _sort_by_relevance(items)[:12]
        n = len(sorted_items)
        if n == 0:
            return canvas

        row_sizes = self._LAYOUTS.get(n, [4] * (n // 4) + ([n % 4] if n % 4 else []))
        max_cols = max(row_sizes)
        margin_x = int(width * 0.05)
        spacing  = int(width * 0.028)
        album_sz = (width - 2 * margin_x - spacing * (max_cols - 1)) // max_cols
        border_w = max(2, album_sz // 70)
        frame_sz = album_sz + border_w * 2

        row_count = len(row_sizes)
        total_h   = frame_sz * row_count + spacing * (row_count - 1)
        y_start   = (height - total_h) // 2

        idx = 0
        for row_i, cols in enumerate(row_sizes):
            row_w   = cols * frame_sz + (cols - 1) * spacing
            x_start = (width - row_w) // 2
            y_top   = y_start + row_i * (frame_sz + spacing)
            for col_i in range(cols):
                if idx >= n:
                    break
                x_left = x_start + col_i * (frame_sz + spacing)
                # Center coords for _paste_rgba
                cx = x_left + frame_sz // 2
                cy = y_top  + frame_sz // 2
                canvas = self._draw_framed(canvas, sorted_items[idx].image, cx, cy, album_sz, border_w, frame_sz)
                idx += 1

        return canvas

    def _draw_framed(self, canvas, img, cx, cy, album_sz, border_w, frame_sz):
        shadow_off = max(4, album_sz // 28)
        shadow = Image.new("RGBA", (frame_sz, frame_sz), (0, 0, 0, 155))
        canvas = _paste_rgba(canvas, shadow, cx + shadow_off, cy + shadow_off)

        frame = Image.new("RGBA", (frame_sz, frame_sz), (225, 225, 225, 255))
        album = img.resize((album_sz, album_sz), Image.LANCZOS).convert("RGBA")
        frame.paste(album, (border_w, border_w))
        return _paste_rgba(canvas, frame, cx, cy)


# ═════════════════════════════════════════════════════════════════════════════
# StreetPosterRenderer — Muro Vivido / pared con historia
# ═════════════════════════════════════════════════════════════════════════════

class StreetPosterRenderer:
    """
    Muro Vivido — una pared que acumuló música durante años.

    Layer order (bottom → top):
      1  Wall surface   — concrete with low/mid/fine texture variation
      2  Archaeology    — ghost prints from previous generations
      3  Old remnants   — torn poster fragments, color patches
      4  Physical wear  — tape strips, water stains, drips, paper scraps
      5  Logo layers    — watermarks (50-82% width) + smart-positioned stencils
      6  Stencil text   — artist names painted on wall
      7  Current posters — heavily overlapping, back→front
    """

    def __init__(self, logo_alpha: int = 110):
        self.logo_alpha = logo_alpha

    def render(self, items: List[RenderItem], width: int, height: int) -> Image.Image:
        items = _sort_by_relevance(items)[:24]
        n = len(items)
        if n == 0:
            return Image.new("RGBA", (width, height), (21, 18, 15, 255))
        seed = sum(hash(it.name) for it in items) & 0xFFFFFFFF
        np_seed = seed % (2**31)

        # Dedicated RNGs — layout computed first for the density map
        layout_rng = random.Random(seed)
        rng = random.Random(seed ^ 0xF00DCAFE)

        placements = self._layout(n, width, height, layout_rng)
        density = self._build_density_map(placements, width, height)

        canvas = self._wall_surface(width, height, np_seed)
        canvas = self._draw_archaeology(canvas, items, rng, width, height)
        canvas = self._draw_old_remnants(canvas, items, rng, width, height)
        canvas = self._draw_wear(canvas, rng, width, height)
        canvas = self._draw_logo_layers(canvas, items, density, rng, width, height)
        canvas = self._draw_stencil_text(canvas, items, rng, width, height)

        for idx in range(n - 1, -1, -1):
            canvas = self._draw_poster(canvas, items[idx], placements[idx], rng)

        return canvas

    # ── 1. Wall surface ───────────────────────────────────────────────────────

    def _wall_surface(self, width, height, np_seed):
        rng_np = np.random.default_rng(np_seed)
        base = np.array([21, 18, 15], dtype=np.float32)

        lf_h, lf_w = height // 18 + 2, width // 18 + 2
        lf_arr = (np.array(
            Image.fromarray(np.clip(rng_np.normal(0, 11, (lf_h, lf_w)), 0, 255).astype(np.uint8), "L")
            .resize((width, height), Image.BILINEAR)
        ).astype(np.float32) - 128) * 0.70

        mf_h, mf_w = height // 6 + 2, width // 6 + 2
        mf_arr = (np.array(
            Image.fromarray(np.clip(rng_np.normal(0, 4, (mf_h, mf_w)), 0, 255).astype(np.uint8), "L")
            .resize((width, height), Image.BILINEAR)
        ).astype(np.float32) - 128) * 0.35

        grain = rng_np.normal(0, 3, (height, width)).astype(np.float32)

        result = np.zeros((height, width, 4), dtype=np.float32)
        for ch, b in enumerate(base):
            result[..., ch] = np.clip(b + lf_arr + mf_arr + grain, 0, 255)
        result[..., 3] = 255
        return Image.fromarray(result.astype(np.uint8), "RGBA")

    # ── 2. Archaeology ────────────────────────────────────────────────────────

    def _draw_archaeology(self, canvas, items, rng, width, height):
        """Ghost prints from previous generations. The wall has memory."""
        layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))

        # Near-grayscale ghost fragments of album art
        for item in items[:min(8, len(items))]:
            sz = rng.randint(int(width * 0.30), int(width * 0.72))
            cx = rng.uniform(0.04, 0.96) * width
            cy = rng.uniform(0.04, 0.96) * height
            rot = rng.uniform(-30, 30)
            arr = np.array(item.image.resize((sz, sz), Image.LANCZOS).convert("RGB"), dtype=np.float32)
            gray = arr.mean(axis=2, keepdims=True)
            arr = arr * 0.05 + gray * 0.95
            arr[..., 0] = np.clip(arr[..., 0] + 12, 0, 255)
            arr[..., 2] = np.clip(arr[..., 2] - 8, 0, 255)
            ghost = Image.fromarray(arr.clip(0, 255).astype(np.uint8)).convert("RGBA")
            ghost.putalpha(Image.fromarray(np.full((sz, sz), rng.randint(8, 22), np.uint8)))
            if abs(rot) > 0.5:
                ghost = ghost.rotate(rot, expand=True, resample=Image.BILINEAR)
            layer = _paste_rgba(layer, ghost, cx, cy)

        # Huge artist typography, barely legible
        sample = list(items)
        rng.shuffle(sample)
        for item in sample[:min(5, len(sample))]:
            if not item.artist:
                continue
            size = rng.randint(130, 240)
            alpha = rng.randint(10, 28)
            angle = rng.choice([0, 0, 90, -90])
            cx = rng.uniform(0.02, 0.98) * width
            cy = rng.uniform(0.02, 0.98) * height
            layer = _paste_rgba(layer, _text_layer(item.artist.upper(), size, (215, 205, 185, alpha), angle), cx, cy)

        return Image.alpha_composite(canvas, layer)

    # ── 3. Old remnants ───────────────────────────────────────────────────────

    def _draw_old_remnants(self, canvas, items, rng, width, height):
        layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        for i in range(16):
            item = items[i % len(items)]
            pal = _module_extract_palette(item.image)
            if rng.random() < 0.50:
                col = pal["muted"]
                rw = rng.randint(int(width * 0.18), int(width * 0.62))
                rh = rng.randint(int(height * 0.06), int(height * 0.24))
                cx = rng.uniform(0.02, 0.98) * width
                cy = rng.uniform(0.02, 0.98) * height
                rot = rng.uniform(-12, 12)
                patch = Image.new("RGBA", (rw, rh), (*col, rng.randint(28, 62)))
                if abs(rot) > 0.5:
                    patch = patch.rotate(rot, expand=True, resample=Image.BILINEAR)
                layer = _paste_rgba(layer, patch, cx, cy)
            else:
                sz_w = rng.randint(int(width * 0.12), int(width * 0.38))
                cx = rng.uniform(0.02, 0.98) * width
                cy = rng.uniform(0.02, 0.98) * height
                rot = rng.uniform(-15, 15)
                frag = item.image.resize((sz_w, sz_w), Image.LANCZOS).convert("RGB")
                arr = np.array(frag, dtype=np.float32)
                gray = arr.mean(axis=2, keepdims=True)
                arr = arr * 0.18 + gray * 0.82
                arr[..., 0] = np.clip(arr[..., 0] + 16, 0, 255)
                arr[..., 2] = np.clip(arr[..., 2] - 12, 0, 255)
                frag_aged = Image.fromarray(arr.astype(np.uint8)).convert("RGBA")
                alpha_val = rng.randint(25, 55)
                r_, g_, b_, _ = frag_aged.split()
                frag_aged = Image.merge("RGBA", (r_, g_, b_, Image.fromarray(np.full((sz_w, sz_w), alpha_val, np.uint8))))
                if abs(rot) > 0.5:
                    frag_aged = frag_aged.rotate(rot, expand=True, resample=Image.BILINEAR)
                layer = _paste_rgba(layer, frag_aged, cx, cy)
        return Image.alpha_composite(canvas, layer)

    # ── 4. Physical wear ──────────────────────────────────────────────────────

    def _draw_wear(self, canvas, rng, width, height):
        """Tape strips, water stains, paint drips, paper scraps."""
        layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        draw = ImageDraw.Draw(layer)

        # Tape strips — yellowed, semi-transparent
        for _ in range(rng.randint(4, 9)):
            tape_w = rng.randint(int(width * 0.06), int(width * 0.22))
            tape_h = rng.randint(5, 12)
            cx = rng.uniform(0.05, 0.95) * width
            cy = rng.uniform(0.05, 0.95) * height
            tape = Image.new("RGBA", (tape_w, tape_h), (
                rng.randint(228, 248), rng.randint(208, 230),
                rng.randint(135, 175), rng.randint(28, 58),
            ))
            rot = rng.uniform(-8, 8)
            if abs(rot) > 0.5:
                tape = tape.rotate(rot, expand=True)
            layer = _paste_rgba(layer, tape, cx, cy)

        # Water stains
        for _ in range(rng.randint(3, 7)):
            cx = rng.uniform(0.05, 0.95) * width
            cy = rng.uniform(0.05, 0.95) * height
            rx, ry = rng.randint(20, 100), rng.randint(15, 65)
            draw.ellipse((cx - rx, cy - ry, cx + rx, cy + ry), fill=(30, 25, 18, rng.randint(8, 25)))

        # Paint drips
        for _ in range(rng.randint(2, 5)):
            x = rng.randint(int(width * 0.05), int(width * 0.95))
            y_top = rng.randint(0, int(height * 0.4))
            length = rng.randint(int(height * 0.07), int(height * 0.28))
            col = (rng.randint(175, 230), rng.randint(165, 220), rng.randint(135, 185), rng.randint(10, 32))
            draw.line([(x, y_top), (x + rng.randint(-5, 5), y_top + length)], fill=col, width=rng.randint(1, 3))

        # Paper scraps
        for _ in range(rng.randint(6, 14)):
            sw, sh = rng.randint(18, 85), rng.randint(7, 28)
            cx = rng.uniform(0.01, 0.99) * width
            cy = rng.uniform(0.01, 0.99) * height
            if rng.random() < 0.5:
                col = (rng.randint(220, 242), rng.randint(210, 232), rng.randint(185, 210), rng.randint(22, 52))
            else:
                col = (rng.randint(60, 90), rng.randint(50, 80), rng.randint(40, 68), rng.randint(12, 32))
            scrap = Image.new("RGBA", (sw, sh), col)
            rot = rng.uniform(-25, 25)
            if abs(rot) > 0.5:
                scrap = scrap.rotate(rot, expand=True)
            layer = _paste_rgba(layer, scrap, cx, cy)

        return Image.alpha_composite(canvas, layer)

    # ── 5. Logos ──────────────────────────────────────────────────────────────

    def _build_density_map(self, placements, width, height, grid_res=8):
        density = np.zeros((grid_res, grid_res), dtype=np.float32)
        cw, ch = width / grid_res, height / grid_res
        for p in placements:
            ci = int(np.clip(p["cx"] / cw, 0, grid_res - 1))
            ri = int(np.clip(p["cy"] / ch, 0, grid_res - 1))
            w = p["size"] / min(width, height)
            for dr in range(-1, 2):
                for dc in range(-1, 2):
                    r, c = ri + dr, ci + dc
                    if 0 <= r < grid_res and 0 <= c < grid_res:
                        density[r, c] += w * max(0.0, 0.8 - abs(dr) * 0.3 - abs(dc) * 0.3)
        return density

    def _find_sparse_position(self, density, width, height, rng):
        grid_res = density.shape[0]
        noise = np.array([[rng.random() * 0.12 for _ in range(grid_res)]
                           for _ in range(grid_res)], dtype=np.float32)
        scores = 1.0 / (density + noise + 0.05)
        flat_sorted = np.argsort(scores.flatten())[::-1]
        pick = flat_sorted[rng.randint(0, min(2, len(flat_sorted) - 1))]
        row, col = np.unravel_index(pick, density.shape)
        cw, ch = width / grid_res, height / grid_res
        return float((col + rng.uniform(0.15, 0.85)) * cw), float((row + rng.uniform(0.15, 0.85)) * ch)

    def _draw_logo_layers(self, canvas, items, density, rng, width, height):
        from logos import stencil as make_stencil, as_watermark

        logos_available = [it for it in items if it.logo is not None]
        if not logos_available:
            return canvas

        # Primary watermark — felt before seen
        wm_w = int(width * rng.choice([0.50, 0.68, 0.82]))
        wm = as_watermark(logos_available[0].logo, alpha=rng.randint(10, 18), max_width=wm_w)
        canvas = _paste_rgba(canvas, wm, width * 0.5, height * 0.42)

        # Secondary watermark
        if len(logos_available) > 1:
            wm2 = as_watermark(logos_available[1].logo,
                                alpha=rng.randint(6, 13),
                                max_width=int(width * rng.uniform(0.38, 0.55)))
            canvas = _paste_rgba(canvas, wm2,
                                  rng.uniform(0.25, 0.75) * width,
                                  rng.uniform(0.22, 0.68) * height)

        # Stencils — placed in low-density zones
        stencil_pool = list(logos_available[:5])
        rng.shuffle(stencil_pool)
        density_copy = density.copy()
        for item in stencil_pool[:3]:
            stencil_w = rng.randint(int(width * 0.32), int(width * 0.58))
            ratio = stencil_w / max(item.logo.width, 1)
            stencil_h = max(int(item.logo.height * ratio), 20)
            pal = _module_extract_palette(item.image)
            paint_color = [(235, 230, 215), (210, 195, 155), pal["vibrant"]][rng.randint(0, 2)]
            cx, cy = self._find_sparse_position(density_copy, width, height, rng)
            s = make_stencil(item.logo, paint_color, self.logo_alpha, (stencil_w, stencil_h))
            rot = rng.uniform(-10, 10)
            if abs(rot) > 0.5:
                s = s.rotate(rot, expand=True, resample=Image.BILINEAR)
            canvas = _paste_rgba(canvas, s, cx, cy)
            # Mark zone occupied so next stencil seeks a different gap
            grid_res = density_copy.shape[0]
            ci = int(np.clip(cx / (width / grid_res), 0, grid_res - 1))
            ri = int(np.clip(cy / (height / grid_res), 0, grid_res - 1))
            density_copy[max(0, ri-1):min(grid_res, ri+2), max(0, ci-1):min(grid_res, ci+2)] += 0.5

        return canvas

    # ── 6. Stencil text ───────────────────────────────────────────────────────

    def _draw_stencil_text(self, canvas, items, rng, width, height):
        texts = [it.artist.upper() for it in items if it.artist]
        rng.shuffle(texts)
        for text in texts[:6]:
            if len(text) > 18:
                text = text[:18]
            size = rng.randint(52, 96)
            angle = rng.choice([0, 0, 0, rng.uniform(-4, 4), 90])
            cx = rng.uniform(0.04, 0.96) * width
            cy = rng.uniform(0.05, 0.95) * height
            alpha = rng.randint(55, 110)
            col_idx = rng.randint(0, 2)
            if col_idx == 0:
                fg = (240, 235, 220, alpha)
            elif col_idx == 1:
                fg = (220, 200, 150, alpha)
            else:
                c = _module_extract_palette(rng.choice(items).image)["muted"]
                fg = (*c, alpha)
            canvas = _paste_rgba(canvas, _text_layer(text, size, fg, angle), cx, cy)
        return canvas

    # ── 7. Layout + poster drawing ────────────────────────────────────────────

    def _layout(self, n, width, height, rng):
        cols = 4
        rows = math.ceil(n / cols)
        cw, ch = width / cols, height / rows
        min_dim = min(width, height)

        n_large  = max(3, n // 5)
        n_medium = max(6, n * 2 // 5)
        n_small  = n - n_large - n_medium
        sizes = (
            [int(min_dim * rng.uniform(0.33, 0.44)) for _ in range(n_large)]  +
            [int(min_dim * rng.uniform(0.24, 0.32)) for _ in range(n_medium)] +
            [int(min_dim * rng.uniform(0.14, 0.22)) for _ in range(n_small)]
        )
        rng.shuffle(sizes)

        placements = []
        for i in range(n):
            col, row = i % cols, i // cols
            cx = (col + 0.5) * cw + rng.uniform(-cw * 0.42, cw * 0.42)
            cy = (row + 0.5) * ch + rng.uniform(-ch * 0.38, ch * 0.38)
            rot = rng.uniform(-20, 20)
            placements.append({"cx": cx, "cy": cy, "size": sizes[i], "rot": rot})
        return placements

    def _draw_poster(self, canvas, item, p, rng):
        sz = p["size"]
        scaled = item.image.resize((sz, sz), Image.LANCZOS).convert("RGBA")

        if rng.random() < 0.40:
            border = max(4, sz // 40)
            paper = (242, 238, 228) if rng.random() < 0.5 else (255, 255, 255)
            framed_sz = sz + border * 2
            framed = Image.new("RGBA", (framed_sz, framed_sz), (*paper, 255))
            framed.paste(scaled, (border, border), scaled)
            if rng.random() < 0.50 and (item.artist or item.name):
                framed = self._add_label(framed, item, border, rng)
            scaled = framed
            sz = framed_sz
        elif rng.random() < 0.30:
            scaled.putalpha(self._torn_mask(sz, sz, rng))

        if abs(p["rot"]) > 0.5:
            scaled = scaled.rotate(p["rot"], expand=True, resample=Image.BICUBIC)

        shadow_off = max(sz // 20, 6)
        canvas = _paste_rgba(canvas, _make_shadow(scaled, alpha=80),  p["cx"] + shadow_off * 1.8, p["cy"] + shadow_off * 1.8)
        canvas = _paste_rgba(canvas, _make_shadow(scaled, alpha=110), p["cx"] + shadow_off,       p["cy"] + shadow_off)
        return _paste_rgba(canvas, scaled, p["cx"], p["cy"])

    def _add_label(self, poster, item, border, rng):
        pw, ph = poster.size
        label_h = max(20, border * 3)
        text = (item.artist or item.name or "").upper()[:20]
        size = max(10, label_h - 6)
        bg = (15, 12, 10, 240) if rng.random() < 0.6 else (40, 35, 28, 240)
        label = Image.new("RGBA", (pw, label_h), bg)
        txt_img = _text_layer(text, size, (240, 235, 220, 230))
        lx = max(4, (pw - txt_img.width) // 2)
        ly = max(0, (label_h - txt_img.height) // 2)
        if lx + txt_img.width <= pw and ly + txt_img.height <= label_h:
            label.alpha_composite(txt_img, dest=(lx, ly))
        result = poster.copy()
        result.alpha_composite(label, dest=(0, ph - label_h))
        return result

    def _torn_mask(self, w, h, rng, intensity=0.055):
        tear = int(min(w, h) * intensity)
        pad  = tear + 2
        mask = Image.new("L", (w + pad * 2, h + pad * 2), 0)
        draw = ImageDraw.Draw(mask)
        steps = 10
        pts = []
        for i in range(steps + 1):
            pts.append((pad + int(w*i/steps) + rng.randint(-tear, tear),
                         pad + rng.randint(0, tear)))
        for i in range(1, steps + 1):
            pts.append((pad + w - rng.randint(0, tear),
                         pad + int(h*i/steps) + rng.randint(-tear, tear)))
        for i in range(steps, -1, -1):
            pts.append((pad + int(w*i/steps) + rng.randint(-tear, tear),
                         pad + h - rng.randint(0, tear)))
        for i in range(steps, 0, -1):
            pts.append((pad + rng.randint(0, tear),
                         pad + int(h*i/steps) + rng.randint(-tear, tear)))
        draw.polygon(pts, fill=255)
        return mask.crop((pad, pad, pad + w, pad + h))


# ═════════════════════════════════════════════════════════════════════════════
# MuroVivoRenderer — capas de afiches de diferentes eras
# ═════════════════════════════════════════════════════════════════════════════

class MuroVivoRenderer:
    """
    Un muro urbano que acumula tiempo.
    Los álbumes menos escuchados son los más viejos (desaturados, abajo).
    Los más escuchados son los más recientes (vívidos, arriba).
    """

    def render(self, items: List[RenderItem], width: int, height: int) -> Image.Image:
        items = _sort_by_relevance(items)[:22]
        n = len(items)
        seed = sum(hash(it.name) for it in items) & 0xFFFFFFFF
        rng = random.Random(seed)

        canvas = Image.new("RGBA", (width, height), (24, 18, 14, 255))

        # Fondo cálido texturizado
        canvas = self._draw_wall_base(canvas, items, rng, width, height)

        # Capas de afiches: de más viejos (al fondo) a más nuevos (encima)
        reversed_items = list(reversed(items))   # oldest last = rendered first
        for era_idx, item in enumerate(reversed_items):
            age = era_idx / max(n - 1, 1)        # 0 = newest, 1 = oldest
            canvas = self._draw_era_poster(canvas, item, age, rng, width, height, n)

        # Texto de era sobre todo
        canvas = self._draw_era_text(canvas, items[:6], rng, width, height)

        return canvas

    def _draw_wall_base(self, canvas, items, rng, width, height):
        layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        draw = ImageDraw.Draw(layer)
        # Grandes bloques de color de fondo, muy desaturados
        for item in items[:8]:
            pal = _module_extract_palette(item.image)
            col = pal["muted"]
            x0 = rng.randint(-50, width // 2)
            y0 = rng.randint(-50, height // 2)
            x1 = rng.randint(width // 2, width + 50)
            y1 = rng.randint(height // 2, height + 50)
            draw.rectangle((x0, y0, x1, y1), fill=(*col, rng.randint(30, 60)))
        return Image.alpha_composite(canvas, layer)

    def _draw_era_poster(self, canvas, item, age, rng, width, height, n_total):
        min_dim = min(width, height)
        # Más nuevo → más grande y más nítido
        size = int(min_dim * (0.17 + (1 - age) * 0.22))
        alpha = int(180 + (1 - age) * 75)

        # Posición: distribuida por el muro
        angle = rng.uniform(-0.5, 0.5)   # era_idx seed already applied via rng seed
        angle_place = rng.uniform(0, 2 * math.pi)
        cx = width  * 0.5 + math.cos(angle_place) * width  * 0.36
        cy = height * 0.5 + math.sin(angle_place) * height * 0.36
        rot = rng.uniform(-18, 18)

        img = item.image.resize((size, size), Image.LANCZOS).convert("RGB")

        # Edad: desaturación y tinte amarillo
        if age > 0.1:
            arr = np.array(img, dtype=np.float32)
            gray = arr.mean(axis=2, keepdims=True)
            arr = arr * (1 - age * 0.75) + gray * (age * 0.75)
            arr[..., 0] = np.clip(arr[..., 0] + age * 20, 0, 255)
            arr[..., 2] = np.clip(arr[..., 2] - age * 25, 0, 255)
            img = Image.fromarray(arr.astype(np.uint8), "RGB")

        img_rgba = img.convert("RGBA")
        r, g, b, a = img_rgba.split()
        a = Image.fromarray(np.full((size, size), alpha, dtype=np.uint8))
        img_rgba = Image.merge("RGBA", (r, g, b, a))

        if abs(rot) > 0.5:
            img_rgba = img_rgba.rotate(rot, expand=True, resample=Image.BICUBIC)

        # Sombra solo en capas recientes
        if age < 0.4:
            shadow_off = max(4, size // 24)
            canvas = _paste_rgba(canvas, _make_shadow(img_rgba, alpha=120),
                                  cx + shadow_off, cy + shadow_off)
        return _paste_rgba(canvas, img_rgba, cx, cy)

    def _draw_era_text(self, canvas, items, rng, width, height):
        for item in items:
            if not item.name: continue
            text = item.name.upper()[:18]
            size = rng.randint(32, 60)
            angle = rng.uniform(-15, 15)
            cx = rng.random() * width
            cy = rng.random() * height
            pal = _module_extract_palette(item.image)
            col = pal["vibrant"]
            fg = (*col, rng.randint(190, 240))
            canvas = _paste_rgba(canvas, _text_layer(text, size, fg, angle), cx, cy)
        return canvas


# ═════════════════════════════════════════════════════════════════════════════
# ExplosionRenderer — portadas como explosiones de color y fragmentos
# ═════════════════════════════════════════════════════════════════════════════

class ExplosionRenderer:
    """
    Cada álbum explota hacia afuera: fragmentos, manchas, trazos.
    Las portadas son el origen de la explosión, no solo decoración.
    """

    def render(self, items: List[RenderItem], width: int, height: int) -> Image.Image:
        items = _sort_by_relevance(items)[:16]
        n = len(items)
        seed = sum(hash(it.name) for it in items) & 0xFFFFFFFF
        rng = random.Random(seed)

        canvas = Image.new("RGBA", (width, height), (5, 5, 8, 255))

        # Posiciones de los álbumes (golden angle)
        positions = []
        for i in range(n):
            ang = i * _GOLDEN_ANGLE
            spread = 0.06 + (i / n) * 0.34
            cx = width  * 0.5 + math.cos(ang) * width  * spread
            cy = height * 0.5 + math.sin(ang) * height * spread * 0.78
            positions.append((cx, cy))

        album_sz = int(min(width, height) * 0.19)

        # Render: explosión primero, portada encima
        for i, item in enumerate(items):
            cx, cy = positions[i]
            pal = _module_extract_palette(item.image)
            canvas = self._draw_explosion(canvas, item.image, cx, cy, pal, rng, width, height)

        for i, item in enumerate(items):
            cx, cy = positions[i]
            scaled = item.image.resize((album_sz, album_sz), Image.LANCZOS).convert("RGBA")
            shadow_off = album_sz // 18
            canvas = _paste_rgba(canvas, _make_shadow(scaled, 160), cx + shadow_off, cy + shadow_off)
            canvas = _paste_rgba(canvas, scaled, cx, cy)

        # Líneas de energía entre álbumes similares por color
        canvas = self._draw_energy_lines(canvas, items, positions, rng, width, height)

        return canvas

    def _draw_explosion(self, canvas, img, cx, cy, pal, rng, width, height):
        vib = pal["vibrant"]
        dom = pal["dominant"]

        # Radial color burst
        glow = self._radial_burst(width, height, cx, cy,
                                   min(width, height) * rng.uniform(0.18, 0.32),
                                   vib, dom, alpha=rng.randint(55, 90))
        canvas = Image.alpha_composite(canvas, glow)

        # Fragmentos dispersos (random crops)
        frag_layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        n_frags = rng.randint(4, 9)
        for _ in range(n_frags):
            crop_sz = rng.randint(28, 75)
            if img.width > crop_sz and img.height > crop_sz:
                sx = rng.randint(0, img.width - crop_sz)
                sy = rng.randint(0, img.height - crop_sz)
                frag = img.crop((sx, sy, sx + crop_sz, sy + crop_sz)).convert("RGBA")
                ang = rng.uniform(0, 2 * math.pi)
                dist = rng.uniform(60, min(width, height) * 0.30)
                fx = cx + math.cos(ang) * dist
                fy = cy + math.sin(ang) * dist
                rot = rng.uniform(-35, 35)
                if abs(rot) > 1:
                    frag = frag.rotate(rot, expand=True, resample=Image.BICUBIC)
                alpha_val = rng.randint(100, 200)
                r_, g_, b_, a_ = frag.split()
                a_ = Image.fromarray((np.array(a_) * alpha_val // 255).astype(np.uint8))
                frag = Image.merge("RGBA", (r_, g_, b_, a_))
                frag_layer = _paste_rgba(frag_layer, frag, fx, fy)

        return Image.alpha_composite(canvas, frag_layer)

    def _radial_burst(self, w, h, cx, cy, radius, col0, col1, alpha):
        y_arr, x_arr = np.mgrid[0:h, 0:w].astype(np.float32)
        dist = np.sqrt((x_arr - cx)**2 + (y_arr - cy)**2) / max(radius, 1.0)
        stops = [((* col0, alpha), 0.0), ((*col1, alpha // 2), 0.5), ((0, 0, 0, 0), 1.0)]
        result = np.zeros((h, w, 4), dtype=np.float32)
        for i in range(len(stops) - 1):
            c0, s0 = stops[i]; c1, s1 = stops[i+1]
            span = s1 - s0
            if span <= 0: continue
            t = np.clip((dist - s0) / span, 0.0, 1.0)
            in_r = dist >= s0 if i == len(stops)-2 else (dist >= s0) & (dist < s1)
            for ch in range(4):
                result[..., ch] += in_r * ((1-t)*c0[ch] + t*c1[ch])
        return Image.fromarray(result.clip(0, 255).astype(np.uint8), "RGBA")

    def _draw_energy_lines(self, canvas, items, positions, rng, width, height):
        layer = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        draw = ImageDraw.Draw(layer)
        n = len(items)
        for i in range(n):
            for j in range(i + 1, min(i + 3, n)):
                x0, y0 = positions[i]
                x1, y1 = positions[j]
                c0 = _dominant_rgb(items[i].image)
                c1 = _dominant_rgb(items[j].image)
                # Gradient line approximated as segments
                segs = 8
                for s in range(segs):
                    t0, t1 = s/segs, (s+1)/segs
                    lx0 = x0 + (x1-x0)*t0; ly0 = y0 + (y1-y0)*t0
                    lx1 = x0 + (x1-x0)*t1; ly1 = y0 + (y1-y0)*t1
                    t_mid = (t0+t1)/2
                    r = int(c0[0]*(1-t_mid) + c1[0]*t_mid)
                    g = int(c0[1]*(1-t_mid) + c1[1]*t_mid)
                    b = int(c0[2]*(1-t_mid) + c1[2]*t_mid)
                    draw.line([(lx0, ly0), (lx1, ly1)], fill=(r, g, b, 55), width=1)
        return Image.alpha_composite(canvas, layer)


# ═════════════════════════════════════════════════════════════════════════════
# ADNMusicalRenderer — álbumes como nodos conectados por hilos de color
# ═════════════════════════════════════════════════════════════════════════════

class ADNMusicalRenderer:
    """
    Tu colección musical como mapa genético.
    Cada álbum es un nodo. Los hilos que los conectan son su relación cromática.
    Oscuro, preciso, íntimo.
    """

    def render(self, items: List[RenderItem], width: int, height: int) -> Image.Image:
        items = _sort_by_relevance(items)[:14]
        n = len(items)
        seed = sum(hash(it.name) for it in items) & 0xFFFFFFFF
        rng = random.Random(seed)

        canvas = Image.new("RGBA", (width, height), (3, 3, 8, 255))

        # Star field muy sutil
        canvas = self._draw_field(canvas, rng, width, height)

        # Posiciones: espiral áurea
        positions = []
        for i in range(n):
            ang = i * _GOLDEN_ANGLE
            spread = 0.07 + (i / max(n-1, 1)) * 0.30
            cx = width  * 0.5 + math.cos(ang) * width  * spread
            cy = height * 0.5 + math.sin(ang) * height * spread * 0.75
            positions.append((cx, cy))

        # Extraer paletas para conexiones
        palettes = [_module_extract_palette(it.image) for it in items]

        # Conexiones DNA entre todos los nodos adyacentes
        canvas = self._draw_connections(canvas, positions, palettes, rng, width, height)

        # Halos de nodo
        for i, item in enumerate(items):
            cx, cy = positions[i]
            col = palettes[i]["vibrant"]
            halo_r = int(min(width, height) * (0.075 + item.playcount / max(
                1, max(it.playcount for it in items)) * 0.045))
            glow = Image.new("RGBA", (halo_r*2, halo_r*2), (0,0,0,0))
            ImageDraw.Draw(glow).ellipse((0,0,halo_r*2-1,halo_r*2-1), fill=(*col,35))
            canvas = _paste_rgba(canvas, glow, cx, cy)

        # Portadas como círculos
        node_sz_base = int(min(width, height) * 0.13)
        for i, item in enumerate(items):
            cx, cy = positions[i]
            pc_frac = item.playcount / max(1, max(it.playcount for it in items))
            sz = int(node_sz_base * (0.75 + pc_frac * 0.50))
            node_img = _circle_crop(item.image, sz)
            # Thin colored ring
            ring = Image.new("RGBA", (sz + 6, sz + 6), (0,0,0,0))
            col = palettes[i]["vibrant"]
            ImageDraw.Draw(ring).ellipse((0,0,sz+5,sz+5), outline=(*col,160), width=3)
            ring.alpha_composite(node_img, dest=(3, 3))
            canvas = _paste_rgba(canvas, ring, cx, cy)

        # Etiquetas mínimas bajo los nodos más escuchados
        for i, item in enumerate(items[:5]):
            if not item.artist: continue
            cx, cy = positions[i]
            node_sz = int(node_sz_base * (0.75 + 0.50))
            label = _text_layer(item.artist[:18].upper(), 18,
                                 (*palettes[i]["vibrant"], 200))
            canvas = _paste_rgba(canvas, label, cx, cy + node_sz // 2 + 14)

        return canvas

    def _draw_field(self, canvas, rng, width, height):
        layer = Image.new("RGBA", (width, height), (0,0,0,0))
        draw = ImageDraw.Draw(layer)
        for _ in range(200):
            x, y = rng.random()*width, rng.random()*height
            a = rng.randint(20, 80)
            draw.point((int(x), int(y)), fill=(180, 190, 220, a))
        return Image.alpha_composite(canvas, layer)

    def _draw_connections(self, canvas, positions, palettes, rng, width, height):
        layer = Image.new("RGBA", (width, height), (0,0,0,0))
        draw = ImageDraw.Draw(layer)
        n = len(positions)
        for i in range(n):
            for j in range(i+1, n):
                # Conexión entre todos los nodos, opacidad inversamente proporcional a distancia
                x0, y0 = positions[i]
                x1, y1 = positions[j]
                dist = math.sqrt((x1-x0)**2+(y1-y0)**2)
                max_dist = math.sqrt(width**2+height**2)*0.4
                if dist > max_dist: continue

                alpha = int(70 * (1 - dist/max_dist))
                c0 = palettes[i]["vibrant"]
                c1 = palettes[j]["vibrant"]

                # Hilo DNA: sinusoidal
                segs = 16
                pts = []
                dx, dy = x1-x0, y1-y0
                length = max(dist, 1)
                perp_x, perp_y = -dy/length, dx/length
                for s in range(segs+1):
                    t = s/segs
                    x = x0 + dx*t + perp_x * math.sin(t*math.pi*3) * 6
                    y = y0 + dy*t + perp_y * math.sin(t*math.pi*3) * 6
                    pts.append((x, y))
                for s in range(segs):
                    t = s/segs
                    r = int(c0[0]*(1-t)+c1[0]*t)
                    g_ = int(c0[1]*(1-t)+c1[1]*t)
                    b = int(c0[2]*(1-t)+c1[2]*t)
                    draw.line([pts[s], pts[s+1]], fill=(r, g_, b, alpha), width=1)
        return Image.alpha_composite(canvas, layer)


# ── OrganicRenderer ───────────────────────────────────────────────────────────

class OrganicRenderer:
    """
    Python port of Android OrganicRenderer.
    Composites real album art into AI-generated scene images using slot layouts.
    """

    ASSETS_DIR = Path(__file__).parent.parent.parent / "app" / "src" / "main" / "assets" / "organic"

    def __init__(self, layout_id: str):
        self.layout_id = layout_id
        self._bg: Optional[Image.Image] = None
        self._layout: Optional[dict] = None

    def _get_layout(self) -> dict:
        if self._layout is None:
            p = self.ASSETS_DIR / f"{self.layout_id}.json"
            import json as _json
            self._layout = _json.loads(p.read_text(encoding="utf-8"))
        return self._layout

    def _get_background(self, w: int, h: int) -> Optional[Image.Image]:
        if self._bg is not None:
            return self._bg
        for ext in (".png", ".jpg", ".jpeg", ".webp"):
            p = self.ASSETS_DIR / f"{self.layout_id}{ext}"
            if p.exists():
                raw = Image.open(str(p)).convert("RGBA")
                scale = max(w / raw.width, h / raw.height)
                sw, sh = int(raw.width * scale), int(raw.height * scale)
                scaled = raw.resize((sw, sh), Image.LANCZOS)
                dx, dy = (sw - w) // 2, (sh - h) // 2
                self._bg = scaled.crop((dx, dy, dx + w, dy + h))
                return self._bg
        return None

    @staticmethod
    def _rounded_mask(w: int, h: int, radius: int) -> Image.Image:
        mask = Image.new("L", (w, h), 0)
        d = ImageDraw.Draw(mask)
        d.rounded_rectangle((0, 0, w - 1, h - 1), radius=radius, fill=255)
        return mask

    @staticmethod
    def _apply_lighting(img: Image.Image, zone_rgb, strength: float = 0.28) -> Image.Image:
        r, g, b = zone_rgb[0] / 255.0, zone_rgb[1] / 255.0, zone_rgb[2] / 255.0
        s = max(0.0, min(1.0, strength))
        rs = 1 - s + s * r * 1.25
        gs = 1 - s + s * g * 1.25
        bs = 1 - s + s * b * 1.25
        arr = np.array(img).astype(np.float32)
        arr[..., 0] = np.clip(arr[..., 0] * rs, 0, 255)
        arr[..., 1] = np.clip(arr[..., 1] * gs, 0, 255)
        arr[..., 2] = np.clip(arr[..., 2] * bs, 0, 255)
        if arr.shape[2] == 4:
            arr[..., 3] = np.clip(arr[..., 3] * 0.94, 0, 255)
        return Image.fromarray(arr.astype(np.uint8))

    @staticmethod
    def _shadow_offset(direction: str, slot_px: float):
        d = slot_px * 0.055
        return {
            "top-left": (-d, -d), "top": (0, -d), "top-right": (d, -d),
            "left": (-d, 0), "right": (d, 0),
            "bottom-left": (-d, d), "bottom": (0, d), "bottom-right": (d, d),
        }.get(direction, (d, d))

    def _render_slot(self, canvas: Image.Image, album: Image.Image, slot: dict,
                     cw: int, ch: int, shadow_dir: str):
        pw = max(4, int(slot["width"]  * cw))
        ph = max(4, int(slot["height"] * ch))
        px = int(slot["x"] * cw)
        py = int(slot["y"] * ch)
        rot = -slot.get("rotation", 0.0)   # PIL rotates CCW
        zone = slot.get("lightingZone", [128, 128, 128])
        radius = max(2, pw // 20)

        # Scale-crop album to slot
        scale = max(pw / album.width, ph / album.height)
        sw, sh = int(album.width * scale), int(album.height * scale)
        scaled = album.resize((sw, sh), Image.LANCZOS)
        dx, dy = (sw - pw) // 2, (sh - ph) // 2
        cropped = scaled.crop((dx, dy, dx + pw, dy + ph)).convert("RGBA")

        # Apply lighting
        graded = self._apply_lighting(cropped, zone)

        # Rounded mask
        mask = self._rounded_mask(pw, ph, radius)
        graded.putalpha(mask)

        # Shadow layer
        ox, oy = self._shadow_offset(shadow_dir, pw)
        shadow = Image.new("RGBA", (pw, ph), (0, 0, 0, 0))
        shadow_body = Image.new("RGBA", (pw, ph), (0, 0, 0, 90))
        shadow_body.putalpha(mask)
        from PIL import ImageFilter
        shadow_blurred = shadow_body.filter(ImageFilter.GaussianBlur(radius=max(1, pw // 16)))

        cx_f = px + pw / 2.0
        cy_f = py + ph / 2.0

        # Rotate and composite shadow
        if abs(rot) > 0.2:
            shadow_blurred = shadow_blurred.rotate(rot, expand=True, resample=Image.BICUBIC)
            graded = graded.rotate(rot, expand=True, resample=Image.BICUBIC)

        canvas = _paste_rgba(canvas, shadow_blurred, cx_f + ox, cy_f + oy)
        canvas = _paste_rgba(canvas, graded, cx_f, cy_f)
        return canvas

    def render(self, items: list, width: int, height: int) -> Image.Image:
        layout = self._get_layout()
        bg = self._get_background(width, height)

        canvas = bg.copy() if bg else Image.new("RGBA", (width, height), (10, 10, 10, 255))

        if not items:
            return canvas.convert("RGB")

        shadow_dir = layout.get("lightingMap", {}).get("shadowDirection", "bottom-right")
        slots = layout.get("slots", [])

        for idx, slot in enumerate(slots):
            item = items[idx % len(items)]
            album = item.image.convert("RGBA") if item.image.mode != "RGBA" else item.image
            canvas = self._render_slot(canvas, album, slot, width, height, shadow_dir)

        # Vignette
        vig = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        from PIL import ImageFilter
        cx, cy = width // 2, height // 2
        r = int(max(width, height) * 0.72)
        vig_draw = ImageDraw.Draw(vig)
        for i in range(min(r, 80), 0, -1):
            alpha = int(170 * (1 - i / min(r, 80)) ** 1.5)
            vig_draw.ellipse((cx - i * r // 80, cy - i * r // 80,
                              cx + i * r // 80, cy + i * r // 80),
                             fill=(0, 0, 0, max(0, alpha - 140)))
        canvas = Image.alpha_composite(canvas, vig)

        return canvas.convert("RGB")


# ── Registry ──────────────────────────────────────────────────────────────────

RENDERERS = {
    "ecosystem":      EcosystemRenderer,
    "mosaic":         MosaicRenderer,
    "hero":           HeroRenderer,
    "constellation":  ConstellationRenderer,
    "museum":         MuseumRenderer,
    "street":         StreetPosterRenderer,
    "muro":           MuroVivoRenderer,
    "explosion":      ExplosionRenderer,
    "adn":            ADNMusicalRenderer,
}
