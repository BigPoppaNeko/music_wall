# Music Wall

An Android live wallpaper that turns your Last.fm listening history into a dynamic, art-directed collage on your home screen.

## How it works

Music Wall fetches your top albums, artists, or tracks from the Last.fm API and renders their cover art into a full-screen wallpaper using one of several visual styles. The wallpaper is generated in the background, cached locally, and refreshes automatically.

## Setup

### 1. Get a Last.fm API key

Create a free API account at [Last.fm API](https://www.last.fm/api/account/create) and copy your API key.

### 2. Add the key to `local.properties`

```properties
LASTFM_API_KEY=your_api_key_here
```

This file is git-ignored — the key is never committed.

### 3. Build and install

```bash
./gradlew installDebug
```

### 4. Set as live wallpaper

Go to **Wallpaper & style** on your Android device, choose **Live wallpapers**, and select **Music Wall**.

### 5. Configure

Open the wallpaper settings to connect your Last.fm account and choose:

| Setting | Options |
|---|---|
| Last.fm username | Your Last.fm username |
| Image type | Albums / Artists / Tracks / Loved tracks |
| Time period | Last 7 days, month, 3 months, 6 months, year, or all time |
| Number of images | Up to 20 |
| Visual style | See below |

## Visual styles

Each style is a self-contained renderer that interprets your music differently.

| Style | ID | Description |
|---|---|---|
| Street Poster | `street` | Urban collage — posters layered on a concrete wall with torn edges, tape, stencil text, and physical wear |
| Dreamscape | `ecosystem` | Abstract generative art — color fields, curl-noise flow lines, sacred geometry, and particle clouds drawn from album palettes |
| Cinematic | `cinematic` | Dark cinematic composition — hero album centered with orbital secondary images fading into a color-washed background |
| Album Wall | `album` | Clean editorial grid — hero tile with treemap mosaic, colored glows, and palette-driven background |
| Physical | `physical` | Every album as a physical object — each cover is randomly assigned a material type (vinyl sleeve, CD jewel case, worn poster, polaroid, magazine clipping, sticker, or torn paper print) with realistic shadows and subtle imperfections |

### Physical style — material types

| Object | Visual character |
|---|---|
| Vinyl sleeve | Cardboard grain, spine shadow, open right edge |
| CD jewel case | Dark plastic tray, molded ridge highlights, diagonal sheen |
| Worn poster | Age-yellowed paper, desaturated art, fold lines |
| Polaroid | Thick white bottom border, photo gloss, italic handwritten label |
| Magazine clipping | Newsprint texture, CMYK print cast, scissor-cut edges |
| Sticker | Die-cut rounded corners, gloss sheen, corner peel |
| Torn paper print | Ragged torn boundary, paper fiber trails |

Every object casts a double-layer drop shadow and can carry worn corners, scratches, tape strips, or thumbtacks depending on type.

## Requirements

- Android 8.0 (API 26) or higher
- A [Last.fm](https://www.last.fm) account with scrobbling history

## Tech stack

| Layer | Library |
|---|---|
| Live wallpaper | `WallpaperService` (Android SDK) |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Database | Room |
| Background work | WorkManager |
| API client | Retrofit 2 + Gson |
| HTTP | OkHttp 4 |
| Image loading | Coil 2 |
| Color extraction | AndroidX Palette |
| Async | Kotlin Coroutines |

## Project structure

```
app/src/main/kotlin/com/jfcardenas/musicwall/
├── data/               # Room database, repositories, Last.fm API client
├── domain/model/       # Core models (MusicImage, etc.)
├── features/
│   └── wallpaper/
│       └── renderer/   # WallpaperRenderer interface + all style implementations
├── service/            # WallpaperService engine (CollageWallpaper)
├── ui/                 # Compose screens, navigation, theming
└── workers/            # WorkManager background refresh tasks
```

## License

MIT
