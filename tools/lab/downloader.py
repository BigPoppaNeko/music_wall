"""Image downloader with retry and per-image metrics."""
import io
import time
from dataclasses import dataclass
from typing import Optional

import requests
from PIL import Image


@dataclass
class DownloadResult:
    url: str
    image: Optional[Image.Image]
    width: int = 0
    height: int = 0
    file_size: int = 0
    download_ms: int = 0
    attempts: int = 0
    error: Optional[str] = None


def download(url: str, retries: int = 3) -> DownloadResult:
    for attempt in range(retries):
        t0 = time.time()
        try:
            resp = requests.get(url, timeout=10, headers={"User-Agent": "MusicWallLab/1.0"})
            resp.raise_for_status()
            content = resp.content
            img = Image.open(io.BytesIO(content)).convert("RGBA")
            return DownloadResult(
                url=url, image=img,
                width=img.width, height=img.height,
                file_size=len(content),
                download_ms=int((time.time() - t0) * 1000),
                attempts=attempt + 1,
            )
        except Exception as e:
            if attempt < retries - 1:
                time.sleep(0.3 * (attempt + 1))
            else:
                return DownloadResult(url=url, image=None, attempts=retries, error=str(e))
    return DownloadResult(url=url, image=None, error="max retries exceeded")
