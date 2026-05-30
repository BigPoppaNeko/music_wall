"""
PerspectiveRenderer — composites album art into fixed quad slots using homography.

Workflow:
  1. Layout JSON contains 12 corner-quads (TL TR BR BL in pixels).
  2. For each slot, shrink the quad inward by MARGIN_FRAC to preserve the white border.
  3. Compute the homography H that maps a flat album rectangle → inner quad.
  4. warpPerspective the album art into the full canvas.
  5. Blend using the warped alpha mask.
  6. Background image is never modified outside the inner quads.
"""

import json
import os
from pathlib import Path
from typing import List, Optional

import cv2
import numpy as np
from PIL import Image

# ── Config ────────────────────────────────────────────────────────────────────

ASSETS_DIR   = Path(__file__).parent.parent.parent / "app" / "src" / "main" / "assets" / "organic"
MARGIN_FRAC  = 0.035     # 3.5% inset from each border edge


# ── Geometry helpers ──────────────────────────────────────────────────────────

def inner_quad(corners: List, margin: float = MARGIN_FRAC) -> np.ndarray:
    """Shrink a quad inward by `margin` fraction (preserves border)."""
    pts = np.array(corners, dtype=np.float32)
    cx, cy = pts[:, 0].mean(), pts[:, 1].mean()
    scale = 1.0 - 2.0 * margin
    inner = np.array([
        [cx + scale * (p[0] - cx), cy + scale * (p[1] - cy)]
        for p in pts
    ], dtype=np.float32)
    return inner     # order: TL TR BR BL


def quad_dimensions(quad: np.ndarray):
    """Average width and height of a (possibly skewed) quad."""
    tl, tr, br, bl = quad
    w = int((np.linalg.norm(tr - tl) + np.linalg.norm(br - bl)) / 2)
    h = int((np.linalg.norm(bl - tl) + np.linalg.norm(br - tr)) / 2)
    return max(w, 1), max(h, 1)


# ── Core renderer ─────────────────────────────────────────────────────────────

class PerspectiveRenderer:
    """
    Fixed-template renderer: uses pre-detected quad slots and perspective
    homography to composite album art perfectly inside each frame.
    """

    def __init__(self, layout_id: str):
        self.layout_id = layout_id
        self._layout: Optional[dict] = None
        self._bg: Optional[np.ndarray] = None   # BGR numpy array

    # ── Asset loading ──────────────────────────────────────────────────────────

    def _get_layout(self) -> dict:
        if self._layout is None:
            p = ASSETS_DIR / f"{self.layout_id}.json"
            self._layout = json.loads(p.read_text(encoding="utf-8"))
        return self._layout

    def _get_background(self, canvas_w: int, canvas_h: int) -> np.ndarray:
        if self._bg is not None:
            return self._bg.copy()
        for ext in (".png", ".jpg", ".jpeg", ".webp"):
            p = ASSETS_DIR / f"{self.layout_id}{ext}"
            if p.exists():
                raw = cv2.imread(str(p))
                if raw is None:
                    continue
                # Scale to fill canvas preserving aspect ratio
                scale = max(canvas_w / raw.shape[1], canvas_h / raw.shape[0])
                sw = int(raw.shape[1] * scale)
                sh = int(raw.shape[0] * scale)
                scaled = cv2.resize(raw, (sw, sh), interpolation=cv2.INTER_LANCZOS4)
                dx = (sw - canvas_w) // 2
                dy = (sh - canvas_h) // 2
                self._bg = scaled[dy:dy + canvas_h, dx:dx + canvas_w]
                return self._bg.copy()
        return np.zeros((canvas_h, canvas_w, 3), dtype=np.uint8)

    # ── Slot rendering ─────────────────────────────────────────────────────────

    def _render_slot(self, canvas: np.ndarray, album_bgr: np.ndarray,
                     slot: dict, margin: float) -> np.ndarray:
        """Warp `album_bgr` into the inner quad of `slot` on `canvas`."""
        ch, cw = canvas.shape[:2]
        corners = slot["corners_px"]  # [[x,y], [x,y], [x,y], [x,y]] TL TR BR BL

        dst = inner_quad(corners, margin=margin)   # float32 [4,2]
        slot_w, slot_h = quad_dimensions(dst)

        # Resize album to slot dimensions (center-crop to maintain aspect ratio)
        ah, aw = album_bgr.shape[:2]
        scale = max(slot_w / aw, slot_h / ah)
        resized = cv2.resize(album_bgr, (int(aw * scale), int(ah * scale)),
                             interpolation=cv2.INTER_LANCZOS4)
        ox = (resized.shape[1] - slot_w) // 2
        oy = (resized.shape[0] - slot_h) // 2
        cropped = resized[oy:oy + slot_h, ox:ox + slot_w]

        # Source rectangle corners: TL TR BR BL
        src_pts = np.array([
            [0,       0],
            [slot_w,  0],
            [slot_w,  slot_h],
            [0,       slot_h],
        ], dtype=np.float32)

        # Homography: src_pts → dst (inner quad in canvas space)
        H, _ = cv2.findHomography(src_pts, dst)
        if H is None:
            return canvas

        # Warp album art to canvas size
        warped = cv2.warpPerspective(cropped, H, (cw, ch),
                                     flags=cv2.INTER_LANCZOS4,
                                     borderMode=cv2.BORDER_CONSTANT,
                                     borderValue=(0, 0, 0))

        # Build alpha mask (white rectangle → warped)
        white = np.ones((slot_h, slot_w), dtype=np.uint8) * 255
        mask = cv2.warpPerspective(white, H, (cw, ch),
                                   flags=cv2.INTER_LINEAR,
                                   borderMode=cv2.BORDER_CONSTANT,
                                   borderValue=0)

        # Feather the mask edges slightly (1px blur to anti-alias)
        mask = cv2.GaussianBlur(mask, (3, 3), 0)

        # Alpha composite: canvas * (1 - mask/255) + warped * (mask/255)
        alpha = mask.astype(np.float32)[:, :, np.newaxis] / 255.0
        canvas = (canvas.astype(np.float32) * (1.0 - alpha) +
                  warped.astype(np.float32) * alpha).astype(np.uint8)
        return canvas

    # ── Public render ──────────────────────────────────────────────────────────

    def render(self, items: list, width: int, height: int,
               margin: float = MARGIN_FRAC) -> Image.Image:
        """
        items: list of RenderItem (must have .image as PIL Image).
        Returns PIL RGB image.
        """
        layout = self._get_layout()
        canvas = self._get_background(width, height)
        slots  = layout.get("slots", [])

        if not items:
            return Image.fromarray(cv2.cvtColor(canvas, cv2.COLOR_BGR2RGB))

        for idx, slot in enumerate(slots):
            item = items[idx % len(items)]
            # Convert PIL → BGR numpy
            pil = item.image.convert("RGB")
            album_bgr = cv2.cvtColor(np.array(pil), cv2.COLOR_RGB2BGR)
            canvas = self._render_slot(canvas, album_bgr, slot, margin)

        result_rgb = cv2.cvtColor(canvas, cv2.COLOR_BGR2RGB)
        return Image.fromarray(result_rgb)
