#!/usr/bin/env python3
"""
MusicWall Visual Lab
Fast collage experimentation without deploying to Android.

Commands:
  render      — generate a single collage
  experiment  — grid search over parameters
  analyze     — dataset coverage + quality report
"""
import json
import sys
import time
from itertools import product
from pathlib import Path

import click
from tqdm import tqdm

from lastfm import get_top_albums, get_top_artists
from downloader import download
from analyzer import analyze
from renderer import EcosystemRenderer, RenderItem, RENDERERS, StreetPosterRenderer, OrganicRenderer
from perspective_renderer import PerspectiveRenderer

RENDERS_DIR = Path(__file__).parent / "renders"


# ── render ────────────────────────────────────────────────────────────────────

@click.group()
def cli():
    """MusicWall Visual Lab — generate collages from Last.fm data."""


@cli.command()
@click.option("--user", default=None, help="Last.fm username")
@click.option("--spotify", default=None, help="Spotify playlist URL (public)")
@click.option("--mode", default="albums", type=click.Choice(["albums", "artists"]))
@click.option("--limit", default=20, help="Items to fetch")
@click.option("--period", default="overall",
              type=click.Choice(["overall", "7day", "1month", "3month", "6month", "12month"]))
@click.option("--width", default=1080)
@click.option("--height", default=1920)
@click.option("--out", default=None, help="Output file (default: renders/render_NNNN.png)")
# EcosystemRenderer knobs
@click.option("--flow-lines", default=700)
@click.option("--particles", default=280)
@click.option("--hero-aura", default=0.38, type=float)
@click.option("--hero-haze", default=0.28, type=float)
@click.option("--hero-ghost", default=0.13, type=float)
@click.option("--color-field-alpha", default=210, type=int)
@click.option("--no-geometry", is_flag=True)
@click.option("--no-glows", is_flag=True)
@click.option("--no-vignette", is_flag=True)
@click.option("--renderer", default="ecosystem",
              type=click.Choice(list(RENDERERS.keys()) + ["organic", "perspective"]), show_default=True)
@click.option("--logos", is_flag=True, help="Fetch artist logos from fanart.tv (needs FANART_API_KEY)")
@click.option("--logo-alpha", default=110, type=int, help="Stencil logo alpha for street renderer (try 90/110/130)")
@click.option("--layout", default=None, help="Layout ID for organic renderer (filename without extension in assets/organic/)")
def render(user, spotify, mode, limit, period, width, height, out,
           flow_lines, particles, hero_aura, hero_haze, hero_ghost,
           color_field_alpha, no_geometry, no_glows, no_vignette, renderer, logos, logo_alpha, layout):
    """Fetch + render a single collage. Source: --user (Last.fm) or --spotify (playlist URL)."""
    if not user and not spotify:
        click.echo("Error: provide --user (Last.fm) or --spotify (playlist URL).", err=True)
        sys.exit(1)

    RENDERS_DIR.mkdir(exist_ok=True)

    if spotify:
        render_items, t_fetch = _fetch_spotify(spotify, limit)
    else:
        items_data, t_fetch = _fetch(user, mode, limit, period)
        render_items = _download_items(items_data, with_logos=logos)

    if len(render_items) < 4:
        click.echo("Need at least 4 images. Aborting.", err=True)
        sys.exit(1)

    click.echo(f"Rendering [{renderer}]...")
    t0 = time.time()
    if renderer == "ecosystem":
        r = EcosystemRenderer(
            flow_lines=flow_lines, particles=particles,
            hero_opacity_aura=hero_aura, hero_opacity_haze=hero_haze, hero_opacity_ghost=hero_ghost,
            color_field_alpha=color_field_alpha,
            geometry=not no_geometry, glows=not no_glows, vignette=not no_vignette,
        )
    elif renderer == "street":
        r = StreetPosterRenderer(logo_alpha=logo_alpha)
    elif renderer == "organic":
        if not layout:
            click.echo("Error: --layout required for organic renderer.", err=True)
            sys.exit(1)
        r = OrganicRenderer(layout)
    elif renderer == "perspective":
        if not layout:
            click.echo("Error: --layout required for perspective renderer.", err=True)
            sys.exit(1)
        r = PerspectiveRenderer(layout)
    else:
        r = RENDERERS[renderer]()
    img = r.render(render_items, width, height)
    render_ms = int((time.time() - t0) * 1000)

    if out is None:
        n = len(list(RENDERS_DIR.glob("render_*.png"))) + 1
        out = RENDERS_DIR / f"render_{n:04d}.png"
    out = Path(out)
    out.parent.mkdir(parents=True, exist_ok=True)
    img.convert("RGB").save(str(out), format="PNG")

    click.echo(f"Saved   {out}")
    click.echo(f"Render {render_ms}ms  |  Fetch {t_fetch}ms")


# ── experiment ────────────────────────────────────────────────────────────────

@cli.command()
@click.option("--user", required=True)
@click.option("--mode", default="albums", type=click.Choice(["albums", "artists"]))
@click.option("--limit", default=20)
@click.option("--period", default="overall",
              type=click.Choice(["overall", "7day", "1month", "3month", "6month", "12month"]))
@click.option("--width", default=1080)
@click.option("--height", default=1920)
@click.option("--tag", default="exp", help="Output subfolder name under renders/")
# Grid parameters — pass comma-separated values, e.g. --flow-lines 200,700
@click.option("--flow-lines", default="700")
@click.option("--particles", default="280")
@click.option("--hero-aura", default="0.38")
@click.option("--hero-haze", default="0.28")
@click.option("--hero-ghost", default="0.13")
@click.option("--color-field-alpha", default="210")
@click.option("--geometry", default="true", help="true,false")
@click.option("--glows", default="true", help="true,false")
def experiment(user, mode, limit, period, width, height, tag,
               flow_lines, particles, hero_aura, hero_haze, hero_ghost,
               color_field_alpha, geometry, glows):
    """Grid search over renderer parameters. Downloads images once, renders N combinations."""
    RENDERS_DIR.mkdir(exist_ok=True)
    exp_dir = RENDERS_DIR / tag
    exp_dir.mkdir(parents=True, exist_ok=True)

    # Parse grids
    def ints(s):   return [int(x) for x in s.split(",")]
    def floats(s): return [float(x) for x in s.split(",")]
    def bools(s):  return [x.strip().lower() == "true" for x in s.split(",")]

    grid = list(product(
        ints(flow_lines), ints(particles),
        floats(hero_aura), floats(hero_haze), floats(hero_ghost),
        ints(color_field_alpha), bools(geometry), bools(glows),
    ))
    click.echo(f"{len(grid)} combinations × {width}×{height}px")

    items_data, _ = _fetch(user, mode, limit, period)
    render_items = _download_items(items_data)

    if len(render_items) < 4:
        click.echo("Need at least 4 images. Aborting.", err=True)
        sys.exit(1)

    manifest = []
    with tqdm(grid, desc="Rendering", unit="img") as pbar:
        for n, (fl, pt, aura, haze, ghost, cfa, geo, glo) in enumerate(pbar, 1):
            r = EcosystemRenderer(
                flow_lines=fl, particles=pt,
                hero_opacity_aura=aura, hero_opacity_haze=haze, hero_opacity_ghost=ghost,
                color_field_alpha=cfa, geometry=geo, glows=glo,
            )
            t0 = time.time()
            img = r.render(render_items, width, height)
            ms = int((time.time() - t0) * 1000)

            fname = f"exp_{n:04d}_fl{fl}_pt{pt}_aura{aura}_haze{haze}_ghost{ghost}_cfa{cfa}_geo{geo}_glo{glo}.png"
            img.convert("RGB").save(str(exp_dir / fname))
            manifest.append({
                "n": n, "file": fname, "render_ms": ms,
                "params": dict(
                    flow_lines=fl, particles=pt,
                    hero_aura=aura, hero_haze=haze, hero_ghost=ghost,
                    color_field_alpha=cfa, geometry=geo, glows=glo,
                ),
            })
            pbar.set_postfix(ms=ms)

    manifest_path = exp_dir / "manifest.json"
    manifest_path.write_text(json.dumps(manifest, indent=2))
    click.echo(f"\n{len(grid)} renders saved to {exp_dir}")
    click.echo(f"Manifest: {manifest_path}")


# ── analyze ───────────────────────────────────────────────────────────────────

@cli.command()
@click.option("--user", required=True)
@click.option("--mode", default="albums", type=click.Choice(["albums", "artists"]))
@click.option("--period", default="overall",
              type=click.Choice(["overall", "7day", "1month", "3month", "6month", "12month"]))
@click.option("--limits", default="50,100,250,500", help="Batch sizes to analyse")
def analyze_cmd(user, mode, period, limits):
    """Download images and produce coverage + quality reports."""
    RENDERS_DIR.mkdir(exist_ok=True)
    reports_dir = RENDERS_DIR / "reports"
    reports_dir.mkdir(exist_ok=True)

    batch_sizes = [int(x) for x in limits.split(",")]
    max_limit = max(batch_sizes)

    click.echo(f"Fetching top {max_limit} {mode} for {user} [{period}]...")
    t0 = time.time()
    items_data = (get_top_albums if mode == "albums" else get_top_artists)(user, max_limit, period)
    fetch_ms = int((time.time() - t0) * 1000)
    click.echo(f"  API returned {len(items_data)} items in {fetch_ms}ms")

    records = []
    with tqdm(items_data, desc="Downloading + analysing", unit="img") as pbar:
        for item in pbar:
            rec = {
                "rank": item.rank,
                "album": item.album,
                "artist": item.artist,
                "playcount": item.playcount,
                "provider": item.provider,
                "url": item.image_url,
            }
            if item.image_url:
                result = download(item.image_url)
                rec["download_ms"] = result.download_ms
                rec["attempts"] = result.attempts
                rec["file_size"] = result.file_size
                rec["width"] = result.width
                rec["height"] = result.height
                rec["error"] = result.error
                if result.image:
                    rec.update(analyze(result.image))
            else:
                rec.update(download_ms=0, attempts=0, file_size=0, width=0, height=0, error="no_url")
            records.append(rec)

    for batch in batch_sizes:
        data = records[:batch]
        _write_report(reports_dir, user, mode, period, batch, fetch_ms, data)

    click.echo("\nAll reports saved to renders/reports/")


def _write_report(reports_dir, user, mode, period, batch, fetch_ms, data):
    total = len(data)
    downloaded = sum(1 for x in data if x.get("width", 0) > 0)
    no_url = sum(1 for x in data if not x.get("url"))
    errors = sum(1 for x in data if x.get("error") and x["error"] != "no_url")
    itunes = sum(1 for x in data if x.get("provider") == "itunes")
    lastfm_ok = sum(1 for x in data if x.get("provider") == "lastfm")

    widths   = [x["width"]        for x in data if x.get("width", 0) > 0]
    heights  = [x["height"]       for x in data if x.get("height", 0) > 0]
    sizes    = [x["file_size"]    for x in data if x.get("file_size", 0) > 0]
    dl_times = [x["download_ms"]  for x in data if x.get("download_ms", 0) > 0]
    brights  = [x["brightness"]   for x in data if "brightness" in x]
    sats     = [x["saturation"]   for x in data if "saturation" in x]

    def stats(lst):
        if not lst:
            return {"min": 0, "max": 0, "avg": 0}
        return {"min": min(lst), "max": max(lst), "avg": round(sum(lst) / len(lst), 3)}

    report = {
        "user": user, "mode": mode, "period": period,
        "batch_size": batch, "fetch_ms": fetch_ms,
        "summary": {
            "requested": batch,
            "returned": total,
            "downloaded_ok": downloaded,
            "no_url": no_url,
            "download_errors": errors,
            "coverage_pct": round(downloaded / total * 100, 1) if total else 0,
            "provider_lastfm": lastfm_ok,
            "provider_itunes": itunes,
            "fallback_pct": round(itunes / total * 100, 1) if total else 0,
        },
        "resolution": {**stats(widths), "unique": sorted(set(widths))},
        "file_size_bytes": stats(sizes),
        "download_timing_ms": stats(dl_times),
        "image_quality": {
            "brightness": stats(brights),
            "saturation": stats(sats),
        },
        "images": data,
    }

    path = reports_dir / f"{mode}_{batch}_{user}_{period}.json"
    path.write_text(json.dumps(report, indent=2, ensure_ascii=False), encoding="utf-8")

    click.echo(f"\n── Top {batch} {mode} ──────────────────")
    click.echo(f"  Coverage  : {downloaded}/{total} ({report['summary']['coverage_pct']}%)")
    click.echo(f"  No URL    : {no_url}  |  Errors: {errors}")
    click.echo(f"  LastFM    : {lastfm_ok}  |  iTunes fallback: {itunes} ({report['summary']['fallback_pct']}%)")
    if widths:
        click.echo(f"  Resolution: {min(widths)}–{max(widths)}px  (avg {round(sum(widths)/len(widths))})")
    if dl_times:
        click.echo(f"  DL time   : avg {round(sum(dl_times)/len(dl_times))}ms  total {sum(dl_times)}ms")
    click.echo(f"  Report    : {path}")


# ── compare ───────────────────────────────────────────────────────────────────

@cli.command()
@click.option("--user", required=True)
@click.option("--mode", default="albums", type=click.Choice(["albums", "artists"]))
@click.option("--limit", default=20)
@click.option("--period", default="overall",
              type=click.Choice(["overall", "7day", "1month", "3month", "6month", "12month"]))
@click.option("--width", default=1080)
@click.option("--height", default=1920)
@click.option("--renderers", default=",".join(RENDERERS.keys()),
              help="Comma-separated renderer names")
@click.option("--tag", default=None, help="Output subfolder (default: compare_YYYYMMDD)")
def compare(user, mode, limit, period, width, height, renderers, tag):
    """Run multiple renderers on the same dataset. Download once, render N times."""
    import datetime
    RENDERS_DIR.mkdir(exist_ok=True)
    folder = tag or f"compare_{datetime.date.today().strftime('%Y%m%d')}"
    out_dir = RENDERS_DIR / folder
    out_dir.mkdir(parents=True, exist_ok=True)

    selected = [r.strip() for r in renderers.split(",") if r.strip() in RENDERERS]
    if not selected:
        click.echo("No valid renderers selected.", err=True)
        sys.exit(1)

    items_data, _ = _fetch(user, mode, limit, period)
    render_items = _download_items(items_data)

    if len(render_items) < 4:
        click.echo("Need at least 4 images. Aborting.", err=True)
        sys.exit(1)

    results = []
    for name in selected:
        click.echo(f"  [{name}]", nl=False)
        t0 = time.time()
        if name == "ecosystem":
            r = EcosystemRenderer()
        else:
            r = RENDERERS[name]()
        img = r.render(render_items, width, height)
        ms = int((time.time() - t0) * 1000)
        out_path = out_dir / f"{name}.png"
        img.convert("RGB").save(str(out_path))
        click.echo(f"  {ms}ms  ->  {out_path.name}")
        results.append({"renderer": name, "render_ms": ms, "file": out_path.name})

    manifest = out_dir / "manifest.json"
    manifest.write_text(json.dumps({"user": user, "mode": mode, "period": period,
                                     "width": width, "height": height,
                                     "renders": results}, indent=2))
    click.echo(f"\nAll renders in {out_dir}")


# ── Shared helpers ────────────────────────────────────────────────────────────

def _fetch(user, mode, limit, period):
    click.echo(f"Fetching top {limit} {mode} for {user} [{period}]...")
    t0 = time.time()
    fn = get_top_albums if mode == "albums" else get_top_artists
    items = fn(user, limit, period)
    ms = int((time.time() - t0) * 1000)
    click.echo(f"  {len(items)} items from Last.fm ({ms}ms)")
    return items, ms


def _fetch_spotify(playlist_url: str, limit: int) -> tuple:
    from spotify import get_playlist_items
    from downloader import download
    click.echo(f"Fetching Spotify playlist...")
    t0 = time.time()
    sp_items = get_playlist_items(playlist_url, limit=limit)
    ms = int((time.time() - t0) * 1000)
    click.echo(f"  {len(sp_items)} unique albums from playlist ({ms}ms)")

    render_items = []
    errors = 0
    with tqdm(sp_items, desc="Downloading", unit="img") as pbar:
        for item in pbar:
            result = download(item.image_url)
            if result.image:
                render_items.append(RenderItem(
                    image=result.image,
                    name=item.album,
                    artist=item.artist,
                    playcount=0,
                    rank=item.rank,
                ))
            else:
                errors += 1
    click.echo(f"  {len(render_items)} ok  |  {errors} errors")
    return render_items, ms


def _download_items(items_data, with_logos: bool = False):
    from logos import get_logo
    render_items = []
    errors = 0
    with tqdm(items_data, desc="Downloading", unit="img") as pbar:
        for item in pbar:
            if not item.image_url:
                errors += 1
                continue
            result = download(item.image_url)
            if result.image:
                logo = None
                if with_logos:
                    logo = get_logo(item.artist, mbid=item.mbid, artist_img=result.image)
                render_items.append(RenderItem(
                    image=result.image,
                    name=item.album,
                    artist=item.artist,
                    playcount=item.playcount,
                    rank=item.rank,
                    mbid=item.mbid,
                    logo=logo,
                ))
            else:
                errors += 1
    logos_found = sum(1 for r in render_items if r.logo is not None)
    msg = f"  {len(render_items)} ok  |  {errors} errors"
    if with_logos:
        msg += f"  |  {logos_found} logos"
    click.echo(msg)
    return render_items


# ── compare-logos ─────────────────────────────────────────────────────────────

@cli.command("compare-logos")
@click.option("--user", required=True)
@click.option("--mode", default="albums", type=click.Choice(["albums", "artists"]))
@click.option("--limit", default=20)
@click.option("--period", default="overall",
              type=click.Choice(["overall", "7day", "1month", "3month", "6month", "12month"]))
@click.option("--width", default=1080)
@click.option("--height", default=1920)
def compare_logos(user, mode, limit, period, width, height):
    """Render street renderer at logo alpha 90 / 110 / 130 for visual comparison."""
    import datetime
    RENDERS_DIR.mkdir(exist_ok=True)
    out_dir = RENDERS_DIR / f"logos_{datetime.date.today().strftime('%Y%m%d')}"
    out_dir.mkdir(parents=True, exist_ok=True)

    items_data, _ = _fetch(user, mode, limit, period)
    render_items = _download_items(items_data, with_logos=True)

    if len(render_items) < 4:
        click.echo("Need at least 4 images. Aborting.", err=True)
        sys.exit(1)

    for alpha in [90, 110, 130]:
        click.echo(f"  [street alpha={alpha}]", nl=False)
        t0 = time.time()
        img = StreetPosterRenderer(logo_alpha=alpha).render(render_items, width, height)
        ms = int((time.time() - t0) * 1000)
        out = out_dir / f"street_logo_alpha_{alpha}.png"
        img.convert("RGB").save(str(out))
        click.echo(f"  {ms}ms  ->  {out.name}")

    click.echo(f"\nComparison saved to {out_dir}")


if __name__ == "__main__":
    cli()
