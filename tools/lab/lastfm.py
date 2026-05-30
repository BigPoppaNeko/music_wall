"""Last.fm + iTunes API client."""
import os
import time
import requests
from typing import List, Optional
from dataclasses import dataclass

BASE_URL = "https://ws.audioscrobbler.com/2.0/"
ITUNES_URL = "https://itunes.apple.com/search"


@dataclass
class MediaImage:
    album: str
    artist: str
    rank: int
    playcount: int
    image_url: Optional[str]
    provider: str = "lastfm"
    mbid: Optional[str] = None          # MusicBrainz ID — needed for fanart.tv logos


def load_api_key() -> str:
    key = os.environ.get("LASTFM_API_KEY", "")
    if key:
        return key
    props = os.path.join(os.path.dirname(__file__), "..", "..", "local.properties")
    try:
        with open(props, "r") as f:
            for line in f:
                if line.startswith("LASTFM_API_KEY="):
                    return line.split("=", 1)[1].strip()
    except FileNotFoundError:
        pass
    raise RuntimeError("LASTFM_API_KEY not found in env or local.properties")


def get_top_albums(username: str, limit: int = 50, period: str = "overall") -> List[MediaImage]:
    api_key = load_api_key()
    resp = requests.get(BASE_URL, params={
        "method": "user.gettopalbums",
        "user": username, "period": period, "limit": limit,
        "api_key": api_key, "format": "json",
    }, timeout=15)
    resp.raise_for_status()
    data = resp.json()
    if "error" in data:
        raise RuntimeError(f"Last.fm error {data['error']}: {data.get('message', '')}")

    results = []
    for i, album in enumerate(data.get("topalbums", {}).get("album", [])):
        url = _best_image(album.get("image", []))
        artist_data = album.get("artist", {})
        mbid = (artist_data.get("mbid") or album.get("mbid") or "").strip() or None
        results.append(MediaImage(
            album=album.get("name", ""),
            artist=artist_data.get("name", ""),
            rank=i + 1,
            playcount=int(album.get("playcount", 0) or 0),
            image_url=url,
            provider="lastfm" if url else "none",
            mbid=mbid,
        ))
    return results


def get_top_artists(username: str, limit: int = 50, period: str = "overall") -> List[MediaImage]:
    api_key = load_api_key()
    resp = requests.get(BASE_URL, params={
        "method": "user.gettopartists",
        "user": username, "period": period, "limit": limit,
        "api_key": api_key, "format": "json",
    }, timeout=15)
    resp.raise_for_status()
    data = resp.json()
    if "error" in data:
        raise RuntimeError(f"Last.fm error {data['error']}: {data.get('message', '')}")

    results = []
    for i, artist in enumerate(data.get("topartists", {}).get("artist", [])):
        url = _best_image(artist.get("image", []) or [])
        provider = "lastfm"
        if not url:
            url = _itunes_artist(artist.get("name", ""))
            provider = "itunes" if url else "none"
        mbid = (artist.get("mbid") or "").strip() or None
        results.append(MediaImage(
            album=artist.get("name", ""),
            artist=artist.get("name", ""),
            rank=i + 1,
            playcount=int(artist.get("playcount", 0) or 0),
            image_url=url,
            provider=provider,
            mbid=mbid,
        ))
    return results


def _best_image(images: list) -> Optional[str]:
    for size in ("mega", "extralarge", "large"):
        for img in images:
            if img.get("size") == size:
                url = (img.get("#text") or "").strip()
                if url:
                    return url
    return None


def _itunes_artist(name: str) -> Optional[str]:
    try:
        resp = requests.get(ITUNES_URL, params={
            "term": name, "media": "music", "entity": "musicArtist", "limit": 1,
        }, timeout=8)
        resp.raise_for_status()
        results = resp.json().get("results", [])
        if not results:
            return None
        url = results[0].get("artworkUrl100", "")
        if url and url.startswith("http"):
            return url.replace("100x100bb", "600x600bb")
    except Exception:
        pass
    return None
