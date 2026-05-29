# Music Wall

An Android live wallpaper that turns your Last.fm listening history into a dynamic album art collage on your home screen.

## How it works

Music Wall fetches your top albums or top artists from the Last.fm API and assembles their cover images into a full-screen collage. The collage is cached locally and can be refreshed on demand from the settings screen.

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

Open the wallpaper settings to set:

| Setting | Options |
|---|---|
| Last.fm username | Your Last.fm username |
| Image type | Albums / Artists |
| Time period | Last 7 days, month, 3 months, 6 months, year, or all time |
| Number of images | 9 – 50 |

Tap **Actualizar ahora** to regenerate the collage immediately.

## Requirements

- Android 8.0 (API 26) or higher
- A [Last.fm](https://www.last.fm) account with scrobbling history

## Tech stack

| Layer | Library |
|---|---|
| Live wallpaper | `WallpaperService` (Android SDK) |
| API client | Retrofit 2 + Gson |
| HTTP | OkHttp 4 |
| Async | Kotlin Coroutines |
| Settings UI | AndroidX Preference |

## Project structure

```
app/src/main/kotlin/com/jfcardenas/musicwall/
├── api/            # Last.fm Retrofit service and response models
├── collage/        # Bitmap collage renderer (CollageMaker)
└── service/        # WallpaperService engine + settings activity
```

## License

MIT
