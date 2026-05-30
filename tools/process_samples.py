#!/usr/bin/env python3
"""
process_samples.py — Procesa todas las imágenes en tools/samples/
y genera los layout.json correspondientes en app/src/main/assets/organic/

Uso:
    python process_samples.py
    python process_samples.py --genre jazz
    python process_samples.py --debug
    python process_samples.py --out-dir app/src/main/assets/organic/

Cada imagen en samples/ debe tener rectángulos de color que marcan dónde
van las portadas de álbumes. Colores soportados: green, pink, gold, cyan,
orange, red, blue, magenta, yellow.
"""

import sys
import os
import argparse
import subprocess
from pathlib import Path

SAMPLES_DIR  = Path(__file__).parent / "samples"
ASSETS_DIR   = Path(__file__).parent.parent / "app/src/main/assets/organic"
SUPPORTED_EXT = {".jpg", ".jpeg", ".png", ".webp"}


def main():
    parser = argparse.ArgumentParser(description="Procesa imágenes de samples/ → layout.json")
    parser.add_argument("--genre",   default="mixed",   help="Género por defecto (se puede sobreescribir por imagen)")
    parser.add_argument("--out-dir", default=None,      help=f"Directorio de salida (default: {ASSETS_DIR})")
    parser.add_argument("--debug",   action="store_true", help="Guarda debug_detected.png junto a cada imagen")
    parser.add_argument("--min-area", type=float, default=0.003, help="Área mínima relativa del slot")
    args = parser.parse_args()

    out_dir = Path(args.out_dir) if args.out_dir else ASSETS_DIR
    out_dir.mkdir(parents=True, exist_ok=True)

    images = [f for f in SAMPLES_DIR.iterdir() if f.suffix.lower() in SUPPORTED_EXT]
    if not images:
        print(f"No hay imágenes en {SAMPLES_DIR}")
        print("Pega tus imágenes generadas (.jpg / .png) en esa carpeta y vuelve a correr.")
        sys.exit(0)

    detect_script = Path(__file__).parent / "detect_layout.py"
    print(f"Procesando {len(images)} imagen(es)  →  {out_dir}\n")

    ok = 0
    for img_path in sorted(images):
        print(f"  {img_path.name}")
        cmd = [
            sys.executable, str(detect_script),
            str(img_path),
            "--genre",    args.genre,
            "--out",      str(out_dir / (img_path.stem + ".json")),
            "--min-area", str(args.min_area),
        ]
        if args.debug:
            cmd.append("--debug")
        result = subprocess.run(cmd, capture_output=False)
        if result.returncode == 0:
            ok += 1
        else:
            print(f"    [ERROR] {img_path.name} falló con código {result.returncode}")

    print(f"\n{ok}/{len(images)} layouts generados en {out_dir}")
    if ok > 0:
        print("\nSiguiente paso:")
        print("  Copia las imágenes de samples/ a app/src/main/assets/organic/")
        print("  y prueba con:  python tools/lab/lab.py render --user TU_USUARIO")


if __name__ == "__main__":
    main()
