"""
Descarga 100 portadas de álbumes desde Last.fm API para experimentos.
Usa múltiples tags para garantizar diversidad visual.
"""

import requests
import os
import random
import time
import re
from pathlib import Path

API_KEY = "df630f853e5b0a3c42f0e0c1e44991f8"
BASE_URL = "https://ws.audioscrobbler.com/2.0/"
OUT_DIR = Path(__file__).parent / "album_covers"
OUT_DIR.mkdir(exist_ok=True)

TAGS = [
    "electronic", "hip-hop", "jazz", "post-rock", "ambient",
    "punk", "shoegaze", "soul", "metal", "latin",
    "indie", "classical", "reggae", "blues", "folk",
    "psychedelic", "rnb", "noise", "bossa nova", "synth-pop",
]

def safe_filename(text: str) -> str:
    return re.sub(r'[\\/*?:"<>|]', "_", text)[:60]

def get_tag_albums(tag: str, limit: int = 10) -> list[dict]:
    resp = requests.get(BASE_URL, params={
        "method": "tag.gettopalbums",
        "tag": tag,
        "limit": limit,
        "api_key": API_KEY,
        "format": "json",
    }, timeout=15)
    resp.raise_for_status()
    data = resp.json()
    return data.get("albums", {}).get("album", [])

def best_image_url(images: list[dict]) -> str | None:
    priority = ["mega", "extralarge", "large"]
    by_size = {img.get("size"): img.get("#text", "") for img in images}
    for size in priority:
        url = by_size.get(size, "")
        if url and not url.endswith("2a96cbd8b46e442fc41c2b86b821562f.png"):
            return url
    return None

def download_image(url: str, dest: Path) -> bool:
    try:
        r = requests.get(url, timeout=20, stream=True)
        r.raise_for_status()
        with open(dest, "wb") as f:
            for chunk in r.iter_content(8192):
                f.write(chunk)
        return True
    except Exception as e:
        print(f"  ERROR descargando {url}: {e}")
        return False

def main():
    print(f"Destino: {OUT_DIR}")
    print()

    # Recolectar candidatos de todos los tags
    candidates: list[tuple[str, str, str]] = []  # (artist, album, img_url)

    for tag in TAGS:
        print(f"Obteniendo tag: {tag} ...", end=" ")
        try:
            albums = get_tag_albums(tag, limit=12)
            added = 0
            for a in albums:
                url = best_image_url(a.get("image", []))
                if url:
                    artist = a.get("artist", {}).get("name", "Unknown")
                    name = a.get("name", "Unknown")
                    candidates.append((artist, name, url))
                    added += 1
            print(f"{added} álbumes")
        except Exception as e:
            print(f"FALLÓ: {e}")
        time.sleep(0.25)  # ser amables con la API

    # Deduplicar por URL y mezclar
    seen_urls: set[str] = set()
    unique: list[tuple[str, str, str]] = []
    for item in candidates:
        if item[2] not in seen_urls:
            seen_urls.add(item[2])
            unique.append(item)

    random.shuffle(unique)
    selection = unique[:100]

    print(f"\nCandidatos unicos: {len(unique)} -> descargando {len(selection)}\n")

    downloaded = 0
    for i, (artist, album, url) in enumerate(selection, 1):
        fname = f"{i:03d}_{safe_filename(artist)}_{safe_filename(album)}.jpg"
        dest = OUT_DIR / fname
        if dest.exists():
            print(f"[{i:3d}/100] YA EXISTE  {fname}")
            downloaded += 1
            continue
        print(f"[{i:3d}/100] {artist} — {album}")
        if download_image(url, dest):
            downloaded += 1
        time.sleep(0.1)

    print(f"\nListo: {downloaded}/100 portadas en {OUT_DIR}")

if __name__ == "__main__":
    main()
