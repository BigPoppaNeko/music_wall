# Sprint 003 — Logos, Símbolos y ADN Visual

## Estado de implementación

### Fuentes investigadas

| Fuente | Logos | Calidad | Auth | Viable |
|--------|-------|---------|------|--------|
| **fanart.tv** | Sí — `hdmusiclogo` + `musiclogo` | PNG transparente HD | API key gratuita | ✅ Principal |
| MusicBrainz | No directamente | — | No | ❌ Solo para MBID |
| Last.fm | No | — | Ya tenemos key | ❌ Solo imágenes de artista |
| Discogs | Imágenes de artista | Variable | OAuth | ⚠️ Alternativo |
| Wikipedia | Logos en infoboxes | Variable | Sin auth | ⚠️ Poco confiable |

### Configuración requerida

Para activar logos de fanart.tv:

1. Registrarse en https://fanart.tv (gratis)
2. Obtener API key personal en: https://fanart.tv/profile/apikeys/
3. Agregar a `local.properties`:
   ```
   FANART_API_KEY=tu_clave_aqui
   ```

### Uso

```bash
# Render con logos (requiere FANART_API_KEY)
python lab.py render --user TU_USUARIO --renderer street --limit 25 --logos

# Sin logos (funciona sin clave)
python lab.py render --user TU_USUARIO --renderer street --limit 25
```

---

## Arquitectura del sistema de logos

### Pipeline completo

```
Last.fm API
  └── artist.mbid (MusicBrainz ID)
        └── fanart.tv API
              └── hdmusiclogo (PNG transparente)
                    └── StreetPosterRenderer
                          ├── Watermark: 78% ancho, alpha 14 (textura de fondo)
                          └── Stencils: 2-3 logos, color plano, alpha 38-72
```

### Fallback si no hay MBID o no hay logo en fanart.tv

```
artist_img (de iTunes)
  └── _extract_from_image()
        └── Detección de fondo: esquinas predominantemente claras u oscuras
              └── Color-key removal con threshold=40
                    └── Logo extraído o None
```

---

## Tratamiento visual de logos

Los logos no aparecen como imágenes completas limpias. Se procesan como:

### Watermark (marca de agua)
- Tamaño: 78% del ancho del canvas
- Alpha: 14 (casi invisible — textura de fondo)
- El logo más escuchado que tenga imagen disponible
- Queda debajo de TODOS los afiches

### Stencil (aerosol / pintura)
- 2-3 logos de artistas distintos
- Convertidos a color plano con `logos.stencil()`
- Ruido de pintura aplicado al canal alpha (simula aerosol irregular)
- Colores: blanco envejecido / crema / color vibrante del álbum
- Alpha: 38-72 (visible pero integrado al muro)
- Rotación: ±8°
- Quedan bajo los afiches actuales pero sobre los fragmentos viejos

---

## Cobertura esperada por fanart.tv

Artistas con probabilidad alta de tener logo:
- Tool, Pink Floyd, Dream Theater, Metallica ✅
- Snoop Dogg, Dr. Dre, Jay-Z ✅
- King Crimson, Led Zeppelin, Black Sabbath ✅
- Radiohead, Nirvana, Pearl Jam ✅

Artistas con probabilidad baja:
- Artistas independientes o de nicho
- Artistas sin MBID registrado en Last.fm
- Artistas con menos de ~50k oyentes en Last.fm

Cobertura estimada para top-25: ~60-70% si el usuario escucha artistas conocidos.

---

## Próximas iteraciones (ADN Visual completo)

Cada álbum debería aportar 5 capas de información:

| Capa | Fuente | Estado |
|------|--------|--------|
| Portada | Last.fm / iTunes | ✅ Implementado |
| Colores | `_module_extract_palette()` | ✅ Implementado |
| Tipografía | Artist/album name via `_draw_stencil_text()` | ✅ Implementado |
| Logo/símbolo | fanart.tv via `logos.py` | ✅ Implementado (requiere key) |
| Motivos gráficos | Por implementar — patrones derivados del logo | 🔜 Sprint futuro |

### Sprint futuro: motivos gráficos

Para artistas como Tool (patrones geométricos), Pink Floyd (prismas) o King Crimson (espirales), se podría generar motivos gráficos procedurales basados en:
- La paleta de colores del artista
- La forma general del logo (geométrico vs. orgánico)
- El género musical (según mapeo del ensayo 03)

Esto requeriría análisis de forma del logo y generación procedural — tarea para Sprint 004+.
