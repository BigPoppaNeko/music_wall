"""Per-image quality metrics: brightness, contrast, saturation, dominant color."""
from typing import Tuple

import numpy as np
from PIL import Image


def analyze(img: Image.Image) -> dict:
    rgb = img.convert("RGB")
    arr = np.array(rgb, dtype=np.float32) / 255.0
    r, g, b = arr[..., 0], arr[..., 1], arr[..., 2]

    cmax = np.maximum(np.maximum(r, g), b)
    cmin = np.minimum(np.minimum(r, g), b)

    brightness = float(cmax.mean())
    luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
    contrast = float(luminance.std())

    with np.errstate(divide="ignore", invalid="ignore"):
        sat = np.where(cmax > 0, (cmax - cmin) / cmax, 0.0)
    saturation = float(sat.mean())

    dominant = _dominant_color(rgb)

    return {
        "brightness": round(brightness, 3),
        "contrast": round(contrast, 3),
        "saturation": round(saturation, 3),
        "dominantColor": "#{:02X}{:02X}{:02X}".format(*dominant),
    }


def _dominant_color(img: Image.Image) -> Tuple[int, int, int]:
    small = img.resize((64, 64), Image.LANCZOS)
    try:
        q = small.quantize(colors=5)
        p = q.getpalette()
        return (p[0], p[1], p[2])
    except Exception:
        return (128, 128, 128)
