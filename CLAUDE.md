# MusicWall — Guía para Claude Code

## Qué es este proyecto

Live wallpaper Android que transforma el historial de Last.fm en collages visuales. No es un reproductor ni un scrobbler; la identidad del producto es **contemplación visual de la música personal**. Ver `docs/visual_research/` para la filosofía de diseño.

## Stack

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin 2.2.10 |
| Build | AGP 9.2.1, Gradle 9.2.1, KSP |
| DI | Hilt 2.59.2 |
| UI | Jetpack Compose + Material 3, Navigation Compose |
| DB | Room v3 (AlbumEntity, ArtistEntity, TrackEntity, MuralRecord) |
| Red | Retrofit 2.11.0 + OkHttp 4.12.0 |
| Imágenes | Coil 2.7.0 (memoria 15% RAM + disco 100MB) |
| Background | WorkManager 2.9.1 |
| SDK | min 26, target/compile 35 |

## Estructura de paquetes

```
com.jfcardenas.musicwall/
├── api/            — Retrofit interfaces + DTOs de Last.fm/Spotify
├── data/           — LastFmRepository, ArtistImageResolver, NetworkResult, Room entities/DAOs
├── domain/         — MusicImage model, MusicRepository interface, GetMusicImagesUseCase
├── service/        — CollageWallpaper (WallpaperService)
├── features/wallpaper/renderer/  — WallpaperRenderer interface + 5 implementaciones
├── worker/         — WallpaperRefreshWorker
├── ui/             — screens, viewmodels, navigation, theme, components
├── settings/       — WallpaperSettingsActivity
└── di/             — DatabaseModule, NetworkModule, RepositoryModule, SpotifyModule
```

## Renderers disponibles

Los IDs canónicos que usa `WallpaperRendererFactory` y `STYLE_OPTIONS` son:

| ID | Clase | Estilo |
|---|---|---|
| `street` | StreetPosterRenderer | Urban collage, texturas de pared |
| `cinematic` | CinematicWallRenderer | Oscuro, capas, glow radial |
| `album` | AlbumWallRenderer | Hero tile + treemap mosaic |
| `ecosystem` | EcosystemRenderer | Arte generativo, geometría sagrada |
| `physical` | PhysicalCollageRenderer | Vinilo, CD, Polaroid, sticker |

**Regla:** Todo renderer nuevo implementa `WallpaperRenderer` y se registra en `WallpaperRendererFactory.all` + `STYLE_OPTIONS` en `UiModels.kt`.

## Secrets

`local.properties` (git-ignorado) debe tener:
```
LASTFM_API_KEY=xxx
LASTFM_SHARED_SECRET=xxx
```
Estos se inyectan vía `BuildConfig` en el build.

## Flujo de generación

```
StyleSelectionScreen → GeneratingScreen → PreviewScreen → Aplicar wallpaper
```

El render real ocurre en `CollageWallpaper.CollageEngine.refresh()` (WallpaperService), disparado por el broadcast `ACTION_REFRESH`. **Problema conocido (Sprint 1 pendiente):** `GeneratingScreen` muestra animación falsa y no dispara el render ni persiste el estilo seleccionado.

## Convenciones

- Strings de UI en español (castellano).
- No agregar comentarios salvo que el WHY no sea obvio.
- No mockear Room en tests; si se escriben tests de integración deben usar una DB real o `inMemoryDatabaseBuilder`.
- `NetworkResult<T>` es el tipo de retorno de todo lo que va a red: `Success(data)` o `Error(type, message)`.
- Errores de Last.fm → `LastFmApiException.code` mapea a `ErrorType` en `LastFmRepository`.

## Próximos sprints

Ver sección de roadmap en la memoria del proyecto (`~/.claude/projects/.../memory/project_musicwall_arch.md`).
