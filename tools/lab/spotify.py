"""
Spotify playlist data source.

Fetches album art from a public Spotify playlist URL.
No user OAuth required — only client credentials.

Setup:
  1. Go to developer.spotify.com/dashboard → Create app
  2. Add to local.properties:
       SPOTIFY_CLIENT_ID=your_client_id
       SPOTIFY_CLIENT_SECRET=your_client_secret
"""
import base64
import os
import re
from dataclasses import dataclass
from typing import List, Optional

import requests


SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token"
SPOTIFY_API_BASE  = "https://api.spotify.com/v1"

_token_cache: dict = {}


@dataclass
class SpotifyItem:
    album: str
    artist: str
    image_url: str
    rank: int = 0
    playcount: int = 0
    mbid: Optional[str] = None


def load_spotify_credentials() -> tuple[str, str]:
    for key in ("SPOTIFY_CLIENT_ID", "SPOTIFY_CLIENT_SECRET"):
        if not os.environ.get(key):
            props = os.path.join(os.path.dirname(__file__), "..", "..", "local.properties")
            try:
                with open(props, "r") as f:
                    for line in f:
                        k, _, v = line.partition("=")
                        if k.strip() in ("SPOTIFY_CLIENT_ID", "SPOTIFY_CLIENT_SECRET"):
                            os.environ[k.strip()] = v.strip()
            except FileNotFoundError:
                pass
    client_id     = os.environ.get("SPOTIFY_CLIENT_ID", "")
    client_secret = os.environ.get("SPOTIFY_CLIENT_SECRET", "")
    return client_id, client_secret


def _get_token(client_id: str, client_secret: str) -> str:
    cache_key = client_id
    if cache_key in _token_cache:
        return _token_cache[cache_key]
    auth = base64.b64encode(f"{client_id}:{client_secret}".encode()).decode()
    resp = requests.post(
        SPOTIFY_TOKEN_URL,
        headers={"Authorization": f"Basic {auth}"},
        data={"grant_type": "client_credentials"},
        timeout=10,
    )
    resp.raise_for_status()
    token = resp.json()["access_token"]
    _token_cache[cache_key] = token
    return token


def _extract_playlist_id(url: str) -> str:
    # https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M?si=...
    # spotify:playlist:37i9dQZF1DXcBWIGoYBM5M
    m = re.search(r"playlist[/:]([A-Za-z0-9]+)", url)
    if not m:
        raise ValueError(f"Cannot extract playlist ID from: {url!r}")
    return m.group(1)


def get_playlist_items(url: str, limit: int = 100) -> List[SpotifyItem]:
    """
    Fetch tracks from a public Spotify playlist URL.
    Returns up to `limit` items with album art URLs.
    """
    client_id, client_secret = load_spotify_credentials()
    if not client_id or not client_secret:
        raise RuntimeError(
            "Missing Spotify credentials.\n"
            "Add SPOTIFY_CLIENT_ID and SPOTIFY_CLIENT_SECRET to local.properties.\n"
            "Get them free at developer.spotify.com/dashboard"
        )

    token = _get_token(client_id, client_secret)
    playlist_id = _extract_playlist_id(url)

    seen_albums: set = set()
    items: List[SpotifyItem] = []
    endpoint = f"{SPOTIFY_API_BASE}/playlists/{playlist_id}/tracks"
    params = {
        "fields": "items(track(name,artists(name),album(name,images))),next",
        "limit": 100,
    }

    while endpoint and len(items) < limit:
        resp = requests.get(
            endpoint,
            headers={"Authorization": f"Bearer {token}"},
            params=params,
            timeout=12,
        )
        resp.raise_for_status()
        data = resp.json()

        for entry in data.get("items", []):
            if len(items) >= limit:
                break
            track = entry.get("track")
            if not track or not track.get("album"):
                continue
            album_name  = track["album"].get("name", "")
            artist_name = track["artists"][0]["name"] if track.get("artists") else ""
            images      = track["album"].get("images", [])
            if not images or not album_name:
                continue
            # Largest available image
            img_url = max(images, key=lambda x: x.get("width", 0) * x.get("height", 0)).get("url", "")
            if not img_url:
                continue
            # One entry per album (avoid duplicates from multi-track albums)
            album_key = (album_name.lower(), artist_name.lower())
            if album_key in seen_albums:
                continue
            seen_albums.add(album_key)
            items.append(SpotifyItem(
                album=album_name,
                artist=artist_name,
                image_url=img_url,
                rank=len(items),
            ))

        endpoint = data.get("next")
        params = {}   # next URL already contains all query params

    return items
