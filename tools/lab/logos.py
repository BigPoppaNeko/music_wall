"""
Logo resolver for music artists.

Priority:
1. fanart.tv — transparent HD PNGs via MusicBrainz ID
2. Background removal from artist image (color-key, no AI)
3. Graceful None when nothing works

Setup:
  Add FANART_API_KEY to local.properties (free key at fanart.tv/profile/apikeys)
"""
import os
import io
import time
import requests
import numpy as np
from typing import Optional
from PIL import Image


FANART_BASE = "https://webservice.fanart.tv/v3/music/{mbid}"
_logo_cache: dict = {}


def load_fanart_key() -> str:
    key = os.environ.get("FANART_API_KEY", "")
    if key:
        return key
    props = os.path.join(os.path.dirname(__file__), "..", "..", "local.properties")
    try:
        with open(props, "r") as f:
            for line in f:
                if line.startswith("FANART_API_KEY="):
                    return line.split("=", 1)[1].strip()
    except FileNotFoundError:
        pass
    return ""


def get_logo(
    artist_name: str,
    mbid: Optional[str] = None,
    artist_img: Optional[Image.Image] = None,
) -> Optional[Image.Image]:
    """
    Resolve a transparent logo for an artist.
    Returns RGBA image or None.
    """
    cache_key = mbid or artist_name
    if cache_key in _logo_cache:
        return _logo_cache[cache_key]

    logo = None

    # 1 — fanart.tv (best quality, transparent PNGs)
    if mbid:
        logo = _fanart_logo(mbid)

    # 2 — background removal from artist image
    if logo is None and artist_img is not None:
        logo = _extract_from_image(artist_img)

    _logo_cache[cache_key] = logo
    return logo


def _fanart_logo(mbid: str) -> Optional[Image.Image]:
    api_key = load_fanart_key()
    if not api_key:
        return None
    try:
        resp = requests.get(
            FANART_BASE.format(mbid=mbid),
            params={"api_key": api_key},
            timeout=8,
        )
        resp.raise_for_status()
        data = resp.json()

        # hdmusiclogo preferred over musiclogo
        logos = data.get("hdmusiclogo") or data.get("musiclogo") or []
        if not logos:
            return None

        # Most-liked logo
        best = max(logos, key=lambda x: int(x.get("likes", 0) or 0))
        url = best.get("url", "")
        if not url:
            return None

        img_resp = requests.get(url, timeout=10)
        img_resp.raise_for_status()
        img = Image.open(io.BytesIO(img_resp.content)).convert("RGBA")
        return img

    except Exception:
        return None


def _extract_from_image(img: Image.Image) -> Optional[Image.Image]:
    """
    Simple background removal via color-key.
    Works when background is near-white or near-black.
    """
    arr = np.array(img.convert("RGBA"), dtype=np.float32)
    r, g, b, a = arr[..., 0], arr[..., 1], arr[..., 2], arr[..., 3]

    # Sample corners to detect dominant background color
    h, w = arr.shape[:2]
    corners = arr[
        [0, 0, h-1, h-1],
        [0, w-1, 0, w-1],
    ]
    avg_bg = corners[:, :3].mean(axis=0)

    threshold = 40.0
    dist = np.sqrt(
        (r - avg_bg[0])**2 +
        (g - avg_bg[1])**2 +
        (b - avg_bg[2])**2
    )

    # Only remove background if corners are predominantly light or dark
    bg_brightness = avg_bg.mean()
    if bg_brightness > 200 or bg_brightness < 55:
        arr[..., 3] = np.where(dist < threshold, 0.0, a)
        result = Image.fromarray(arr.clip(0, 255).astype(np.uint8), "RGBA")
        # Check if removal produced something meaningful (not all transparent)
        alpha_arr = np.array(result.split()[3])
        if alpha_arr.mean() > 10:
            return result

    return None


def stencil(
    logo: Image.Image,
    color: tuple,
    alpha: int,
    size: tuple,
) -> Image.Image:
    """
    Convert logo to flat single-color stencil.
    Simulates spray paint or screen-printed look.
    """
    logo_resized = logo.resize(size, Image.LANCZOS)
    arr = np.array(logo_resized)

    # Replace all non-transparent pixels with the target color
    mask = arr[..., 3] > 20
    result = np.zeros_like(arr)
    result[mask] = [color[0], color[1], color[2], alpha]

    # Subtle paint texture: slight noise on alpha
    noise = np.random.default_rng(42).integers(-18, 18, result.shape[:2])
    result[..., 3] = np.clip(result[..., 3].astype(int) + noise, 0, 255)
    return Image.fromarray(result.astype(np.uint8), "RGBA")


def as_watermark(logo: Image.Image, alpha: int, max_width: int) -> Image.Image:
    """Resize logo to max_width and set uniform alpha."""
    ratio = max_width / max(logo.width, 1)
    new_h = int(logo.height * ratio)
    resized = logo.resize((max_width, new_h), Image.LANCZOS)
    arr = np.array(resized)
    # Scale existing alpha by target alpha
    arr[..., 3] = (arr[..., 3].astype(float) * alpha / 255).clip(0, 255).astype(np.uint8)
    return Image.fromarray(arr, "RGBA")
