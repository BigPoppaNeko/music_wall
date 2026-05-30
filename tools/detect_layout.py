#!/usr/bin/env python3
"""
detect_layout.py — MusicWall Organic Layout Detector
=====================================================
Procesa una imagen de fondo que contiene rectángulos de color numerados
(marcadores de posición para portadas de álbumes) y genera un layout.json
listo para el OrganicRenderer de Android.

Uso:
    python detect_layout.py imagen.jpg --genre punk
    python detect_layout.py imagen.png --genre jazz --debug
    python detect_layout.py imagen.jpg --genre kpop --out assets/organic/

Dependencias:
    pip install opencv-python numpy
    pip install pytesseract   (opcional, para leer números de los rectángulos)
"""

import cv2
import numpy as np
import json
import sys
import os
import argparse
from typing import List, Optional, Tuple

# ─── Rangos HSV de colores de marcador ────────────────────────────────────────
# Añade o ajusta colores según los que uses en Midjourney/DALL-E
MARKER_COLORS = [
    ("green",    [36,  80,  80], [85,  255, 255]),
    ("pink",     [140, 80,  100],[175, 255, 255]),
    ("gold",     [15,  120, 150],[35,  255, 255]),
    ("cyan",     [80,  80,  80], [105, 255, 255]),
    ("orange",   [5,   150, 150],[14,  255, 255]),
    ("red_lo",   [0,   100, 100],[5,   255, 255]),  # rojo parte baja HSV
    ("red_hi",   [175, 100, 100],[180, 255, 255]),  # rojo parte alta HSV
    ("blue",     [100, 100, 60], [130, 255, 255]),
    ("magenta",  [130, 80,  80], [145, 255, 255]),
    ("yellow",   [22,  120, 150],[36,  255, 255]),
]

MIN_SLOT_AREA_RATIO = 0.003   # El slot debe ocupar al menos el 0.3% de la imagen
MAX_SLOTS           = 64
MORPH_KERNEL        = 5       # Tamaño del kernel morfológico


# ─── Detección de slots ───────────────────────────────────────────────────────

def detect_slots(img_bgr: np.ndarray, debug: bool = False) -> Tuple[List[dict], np.ndarray]:
    h, w = img_bgr.shape[:2]
    min_area = h * w * MIN_SLOT_AREA_RATIO

    hsv = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2HSV)
    combined_mask = np.zeros((h, w), dtype=np.uint8)
    raw_rects: List[dict] = []
    debug_img = img_bgr.copy() if debug else None

    red_mask = np.zeros((h, w), dtype=np.uint8)

    for color_name, lower, upper in MARKER_COLORS:
        mask = cv2.inRange(hsv,
                           np.array(lower, dtype=np.uint8),
                           np.array(upper, dtype=np.uint8))

        # Acumular rojo por separado (dos rangos HSV)
        if color_name.startswith("red"):
            red_mask |= mask
            combined_mask |= mask
            continue

        k = cv2.getStructuringElement(cv2.MORPH_RECT, (MORPH_KERNEL, MORPH_KERNEL))
        mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, k, iterations=2)
        mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN,  k, iterations=1)
        combined_mask |= mask

        contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        for cnt in contours:
            if cv2.contourArea(cnt) >= min_area:
                _add_rect(raw_rects, cnt, color_name)

    # Procesar máscara roja unificada
    k = cv2.getStructuringElement(cv2.MORPH_RECT, (MORPH_KERNEL, MORPH_KERNEL))
    red_mask = cv2.morphologyEx(red_mask, cv2.MORPH_CLOSE, k, iterations=2)
    red_mask = cv2.morphologyEx(red_mask, cv2.MORPH_OPEN,  k, iterations=1)
    for cnt in cv2.findContours(red_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)[0]:
        if cv2.contourArea(cnt) >= min_area:
            _add_rect(raw_rects, cnt, "red")

    # Eliminar duplicados muy próximos entre sí (mismo slot detectado por dos rangos)
    raw_rects = _deduplicate(raw_rects, w, h)

    # Ordenar por lectura (top→bottom, left→right) para numeración de fallback
    raw_rects.sort(key=lambda r: (
        round(r["cy"] * 8),
        round(r["cx"] * 8),
    ))

    slots = []
    used_ids = set()
    for i, r in enumerate(raw_rects):
        slot_id = _try_read_number(img_bgr, r, w, h)
        if slot_id is None or slot_id in used_ids or not (1 <= slot_id <= MAX_SLOTS):
            slot_id = i + 1
            while slot_id in used_ids:
                slot_id += 1
        used_ids.add(slot_id)

        lz = _sample_lighting_zone(img_bgr, combined_mask, r, w, h)

        slots.append({
            "id":           slot_id,
            "x":            round(r["x"],  4),
            "y":            round(r["y"],  4),
            "width":        round(r["nw"], 4),
            "height":       round(r["nh"], 4),
            "rotation":     round(r["rot"], 1),
            "lightingZone": lz,
        })

        if debug_img is not None:
            _draw_debug_rect(debug_img, r, slot_id, w, h)

    slots.sort(key=lambda s: s["id"])

    if debug and debug_img is not None:
        debug_path = "debug_detected.png"
        cv2.imwrite(debug_path, debug_img)
        print(f"[debug] Guardado: {debug_path}  ({len(slots)} slots)")

    return slots, combined_mask


def _add_rect(rects: List[dict], cnt: np.ndarray, color_name: str):
    """Convierte un contorno en un dict normalizado."""
    rect = cv2.minAreaRect(cnt)
    (cx_px, cy_px), (rw_px, rh_px), angle = rect

    # Convención: width ≥ height
    if rw_px < rh_px:
        rw_px, rh_px = rh_px, rw_px
        angle = (angle + 90) % 180

    # Normalizar ángulo a [-45, 45]
    rot = float(angle if angle <= 90 else angle - 180)

    rects.append({
        "cx": cx_px, "cy": cy_px,
        "rw_px": rw_px, "rh_px": rh_px,
        "rot": rot,
        "color": color_name,
    })


def _deduplicate(rects: List[dict], w: int, h: int, threshold: float = 0.05) -> List[dict]:
    """Elimina rectángulos cuyo centro esté muy próximo al de otro."""
    kept = []
    for r in rects:
        nx, ny = r["cx"] / w, r["cy"] / h
        duplicate = False
        for k in kept:
            dx = abs(nx - k["cx"] / w)
            dy = abs(ny - k["cy"] / h)
            if dx < threshold and dy < threshold:
                duplicate = True
                break
        if not duplicate:
            kept.append(r)

    # Añadir campos normalizados necesarios para el resto del pipeline
    for r in kept:
        nw = r["rw_px"] / w
        nh = r["rh_px"] / h
        r["nw"] = max(0.01, min(1.0 - r["cx"] / w, nw))
        r["nh"] = max(0.01, min(1.0 - r["cy"] / h, nh))
        r["x"]  = max(0.0,  r["cx"] / w - r["nw"] / 2)
        r["y"]  = max(0.0,  r["cy"] / h - r["nh"] / 2)

    return kept


# ─── OCR para leer el número del slot ─────────────────────────────────────────

def _try_read_number(img_bgr: np.ndarray, rect: dict, w: int, h: int) -> Optional[int]:
    try:
        import pytesseract

        x1 = max(0, int(rect["x"] * w))
        y1 = max(0, int(rect["y"] * h))
        x2 = min(w, x1 + int(rect["nw"] * w))
        y2 = min(h, y1 + int(rect["nh"] * h))

        crop = img_bgr[y1:y2, x1:x2]
        if crop.size == 0:
            return None

        scale = 4
        crop = cv2.resize(crop, (crop.shape[1] * scale, crop.shape[0] * scale),
                          interpolation=cv2.INTER_CUBIC)
        gray  = cv2.cvtColor(crop, cv2.COLOR_BGR2GRAY)
        _, bw = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)

        cfg  = r'--psm 7 --oem 3 -c tessedit_char_whitelist=0123456789'
        text = pytesseract.image_to_string(bw, config=cfg).strip()
        digits = ''.join(c for c in text if c.isdigit())
        return int(digits) if digits else None

    except ImportError:
        return None
    except Exception:
        return None


# ─── Zona de iluminación del slot ─────────────────────────────────────────────

def _sample_lighting_zone(img_bgr: np.ndarray, marker_mask: np.ndarray,
                           rect: dict, w: int, h: int,
                           margin: float = 0.6) -> List[int]:
    """
    Muestrea el color de fondo ALREDEDOR del slot (fuera del marcador)
    para conocer el color de luz ambiental en esa zona de la escena.
    """
    rw = rect["rw_px"]
    rh = rect["rh_px"]
    cx = int(rect["cx"])
    cy = int(rect["cy"])
    m  = int(max(rw, rh) * margin)

    x1, x2 = max(0, cx - int(rw / 2) - m), min(w, cx + int(rw / 2) + m)
    y1, y2 = max(0, cy - int(rh / 2) - m), min(h, cy + int(rh / 2) + m)

    region_bgr  = img_bgr[y1:y2, x1:x2]
    region_mask = marker_mask[y1:y2, x1:x2]
    bg_mask     = cv2.bitwise_not(region_mask)
    bg_pixels   = region_bgr[bg_mask > 0]

    if len(bg_pixels) == 0:
        avg = np.mean(img_bgr.reshape(-1, 3), axis=0)
    else:
        avg = np.mean(bg_pixels.reshape(-1, 3), axis=0)

    b, g, r = [max(0, min(255, int(c))) for c in avg]
    return [r, g, b]


# ─── Debug visual ─────────────────────────────────────────────────────────────

def _draw_debug_rect(debug_img: np.ndarray, rect: dict, slot_id: int, w: int, h: int):
    box = cv2.boxPoints(((rect["cx"], rect["cy"]),
                          (rect["rw_px"], rect["rh_px"]),
                          rect["rot"]))
    box = np.int0(box)
    cv2.drawContours(debug_img, [box], 0, (0, 255, 0), 3)
    cv2.putText(debug_img, str(slot_id),
                (int(rect["x"] * w) + 4, int(rect["y"] * h) + 24),
                cv2.FONT_HERSHEY_SIMPLEX, 0.9, (255, 255, 255), 2)


# ─── Análisis de iluminación global ──────────────────────────────────────────

def analyze_lighting(img_bgr: np.ndarray, marker_mask: np.ndarray) -> dict:
    """
    Analiza la iluminación general de la escena excluyendo los marcadores.
    Divide la imagen en una cuadrícula 3×3 y detecta la zona más brillante.
    """
    h, w = img_bgr.shape[:2]
    bg_mask = cv2.bitwise_not(marker_mask)
    lab     = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2LAB)

    rows, cols = 3, 3
    cell_h, cell_w = h // rows, w // cols

    brightness: List[float] = []
    colors_bgr: List[List[float]] = []

    for r in range(rows):
        for c in range(cols):
            y1, y2 = r * cell_h, (r + 1) * cell_h
            x1, x2 = c * cell_w, (c + 1) * cell_w

            cell_lab  = lab[y1:y2, x1:x2, 0]
            cell_bgr  = img_bgr[y1:y2, x1:x2]
            cell_mask = bg_mask[y1:y2, x1:x2]

            bg_l = cell_lab[cell_mask > 0]
            bg_p = cell_bgr[cell_mask > 0]

            brightness.append(float(np.mean(bg_l)) if len(bg_l) > 0 else 0.0)
            colors_bgr.append(
                np.mean(bg_p.reshape(-1, 3), axis=0).tolist()
                if len(bg_p) > 0 else [128.0, 128.0, 128.0]
            )

    best_idx = int(np.argmax(brightness))
    avg_b    = float(np.mean(brightness))

    positions = [
        "top-left",    "top",    "top-right",
        "left",        "center", "right",
        "bottom-left", "bottom", "bottom-right",
    ]
    opposite = {
        "top-left": "bottom-right", "top": "bottom",    "top-right": "bottom-left",
        "left":     "right",        "center": "center", "right":     "left",
        "bottom-left": "top-right", "bottom": "top",    "bottom-right": "top-left",
    }

    light_dir  = positions[best_idx]
    shadow_dir = opposite[light_dir]

    main_light_hex = _bgr_to_hex(colors_bgr[best_idx])

    dark_cells = [colors_bgr[i] for i, b in enumerate(brightness) if b < avg_b * 0.75]
    ambient_bgr = np.mean(dark_cells, axis=0) if dark_cells else np.mean(colors_bgr, axis=0)
    ambient_hex = _bgr_to_hex(ambient_bgr.tolist())

    return {
        "type":            "gradient",
        "mainLightColor":  main_light_hex,
        "ambientColor":    ambient_hex,
        "shadowDirection": shadow_dir,
    }


def _bgr_to_hex(bgr: List[float]) -> str:
    b, g, r = [max(0, min(255, int(c))) for c in bgr]
    return f"#{r:02X}{g:02X}{b:02X}"


# ─── Ajuste fino de rangos de color ──────────────────────────────────────────

def tune_colors_interactive(img_bgr: np.ndarray):
    """
    Modo interactivo con trackbars para ajustar los rangos HSV.
    Ejecutar con --tune para calibrar en imágenes nuevas.
    """
    hsv = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2HSV)
    win = "Tune HSV (presiona Q para salir)"
    cv2.namedWindow(win)

    def nothing(_): pass
    cv2.createTrackbar("H min", win, 36,  180, nothing)
    cv2.createTrackbar("H max", win, 85,  180, nothing)
    cv2.createTrackbar("S min", win, 80,  255, nothing)
    cv2.createTrackbar("S max", win, 255, 255, nothing)
    cv2.createTrackbar("V min", win, 80,  255, nothing)
    cv2.createTrackbar("V max", win, 255, 255, nothing)

    while True:
        lo = np.array([cv2.getTrackbarPos("H min", win),
                       cv2.getTrackbarPos("S min", win),
                       cv2.getTrackbarPos("V min", win)])
        hi = np.array([cv2.getTrackbarPos("H max", win),
                       cv2.getTrackbarPos("S max", win),
                       cv2.getTrackbarPos("V max", win)])
        mask = cv2.inRange(hsv, lo, hi)
        small = cv2.resize(mask, (img_bgr.shape[1] // 2, img_bgr.shape[0] // 2))
        cv2.imshow(win, small)
        if cv2.waitKey(30) & 0xFF == ord('q'):
            break
        print(f"\r[{lo[0]},{lo[1]},{lo[2]}] → [{hi[0]},{hi[1]},{hi[2]}]", end="")

    cv2.destroyAllWindows()
    print()


# ─── Entrada principal ────────────────────────────────────────────────────────

def main():
    global MIN_SLOT_AREA_RATIO
    parser = argparse.ArgumentParser(
        description="MusicWall Organic Layout Detector — detecta slots y genera layout.json",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Ejemplos:
  python detect_layout.py fondo.jpg --genre punk
  python detect_layout.py fondo.png --genre jazz --debug --out assets/organic/
  python detect_layout.py fondo.jpg --tune          # calibrar rangos HSV
  python detect_layout.py fondo.jpg --min-area 0.006  # slots más grandes

Colores de marcador soportados:
  green, pink, gold, cyan, orange, red, blue, magenta, yellow
  (mezcla libremente colores en la misma imagen)
        """,
    )
    parser.add_argument("image",         help="Ruta de la imagen de fondo")
    parser.add_argument("--genre",       default="mixed",
                        help="Género musical (punk, jazz, kpop, prog, etc.)")
    parser.add_argument("--debug",       action="store_true",
                        help="Guarda debug_detected.png con los slots marcados")
    parser.add_argument("--tune",        action="store_true",
                        help="Modo interactivo para calibrar rangos de color HSV")
    parser.add_argument("--out",         default=None,
                        help="Ruta de salida del JSON (por defecto: misma carpeta que la imagen)")
    parser.add_argument("--min-area",    type=float, default=MIN_SLOT_AREA_RATIO,
                        help=f"Área mínima relativa del slot (default: {MIN_SLOT_AREA_RATIO})")
    args = parser.parse_args()
    MIN_SLOT_AREA_RATIO = args.min_area

    if not os.path.isfile(args.image):
        print(f"✗ Imagen no encontrada: {args.image}", file=sys.stderr)
        sys.exit(1)

    img = cv2.imread(args.image)
    if img is None:
        print(f"✗ No se pudo cargar: {args.image}", file=sys.stderr)
        sys.exit(1)

    h, w = img.shape[:2]
    print(f"Imagen: {os.path.basename(args.image)}  ({w}×{h} px)")

    if args.tune:
        tune_colors_interactive(img)
        return

    print("Detectando slots...", end=" ", flush=True)
    slots, combined_mask = detect_slots(img, debug=args.debug)
    print(f"✓ {len(slots)} slots")

    print("Analizando iluminación...", end=" ", flush=True)
    lighting = analyze_lighting(img, combined_mask)
    print(f"✓  luz {lighting['mainLightColor']}  ·  sombra → {lighting['shadowDirection']}")

    name = os.path.splitext(os.path.basename(args.image))[0]

    # Directorio de salida
    if args.out:
        if os.path.isdir(args.out):
            out_path = os.path.join(args.out, f"{name}.json")
        else:
            out_path = args.out
    else:
        out_path = os.path.join(os.path.dirname(args.image) or ".", f"{name}.json")

    layout = {
        "id":          name,
        "genre":       args.genre,
        "totalSlots":  len(slots),
        "slots":       slots,
        "lightingMap": lighting,
    }

    os.makedirs(os.path.dirname(os.path.abspath(out_path)), exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(layout, f, indent=2, ensure_ascii=False)

    print(f"\n✓ Layout guardado: {out_path}")
    print(f"  Siguiente paso: copia {name}.json y {os.path.basename(args.image)}")
    print(f"  a  app/src/main/assets/organic/")


if __name__ == "__main__":
    main()
