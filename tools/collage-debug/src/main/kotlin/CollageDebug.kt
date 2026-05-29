import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.util.Properties
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.system.measureTimeMillis

// ── Entry point ───────────────────────────────────────────────────────────────

fun main(args: Array<String>) {
    val props = readLocalProperties()

    val username = args.getOrNull(0)
        ?: System.getenv("LASTFM_USER")
        ?: props.getProperty("LASTFM_DEBUG_USER")
        ?: prompt("Usuario de Last.fm: ")

    val apiKey = args.getOrNull(1)
        ?: System.getenv("LASTFM_API_KEY")
        ?: props.getProperty("LASTFM_API_KEY")
        ?: error("API key no encontrada. Pásala como arg, env LASTFM_API_KEY o en local.properties")

    val period = args.getOrNull(2) ?: "7day"
    val limit  = args.getOrNull(3)?.toIntOrNull() ?: 25
    val width  = args.getOrNull(4)?.toIntOrNull() ?: 1080
    val height = args.getOrNull(5)?.toIntOrNull() ?: 1920

    printBanner(username, period, limit, width, height)

    val client = OkHttpClient()
    val gson   = Gson()

    var albumsReceived = 0
    var imagesDownloaded = 0

    val elapsed = measureTimeMillis {
        // ── 1. Fetch album URLs ────────────────────────────────────────────────
        println("\n[1/4] Consultando Last.fm API…")
        val urls = try {
            fetchTopAlbumUrls(client, gson, username, apiKey, period, limit)
        } catch (e: LastFmException) {
            println("  ✗ Error Last.fm (code=${e.code}): ${e.message}")
            return
        } catch (e: Exception) {
            println("  ✗ Error de red: ${e.message}")
            return
        }

        albumsReceived = urls.size
        if (urls.isEmpty()) {
            println("  ✗ 0 URLs recibidas. Verifica usuario, API key y período.")
            return
        }
        println("  ✓ $albumsReceived álbumes recibidos")
        urls.forEachIndexed { i, url -> println("    [${i+1}] $url") }

        // ── 2. Download images ─────────────────────────────────────────────────
        println("\n[2/4] Descargando portadas…")
        val images = downloadImages(client, urls)
        imagesDownloaded = images.size
        println("  ✓ $imagesDownloaded/${urls.size} imágenes descargadas")

        if (images.isEmpty()) {
            println("  ✗ 0 imágenes disponibles. Abortando.")
            return
        }

        // ── 3. Generate collage ────────────────────────────────────────────────
        println("\n[3/4] Generando collage ${width}x${height}…")
        val collage = generateCollage(images, width, height)
        println("  ✓ Collage generado: ${collage.width}x${collage.height}")

        // ── 4. Save ────────────────────────────────────────────────────────────
        println("\n[4/4] Guardando…")
        val outDir  = File("output").also { it.mkdirs() }
        val outFile = File(outDir, "collage.jpg")
        ImageIO.write(collage, "jpg", outFile)
        println("  ✓ ${outFile.canonicalPath}")
    }

    printSummary(username, albumsReceived, imagesDownloaded, elapsed)
}

// ── Last.fm API ───────────────────────────────────────────────────────────────

class LastFmException(val code: Int, message: String) : Exception(message)

private data class TopAlbumsResponse(
    @SerializedName("topalbums") val topAlbums: TopAlbums?
)
private data class TopAlbums(
    @SerializedName("album") val albums: List<Album>
)
private data class Album(
    val name: String,
    val artist: ArtistRef,
    @SerializedName("image") val images: List<LastFmImage>
)
private data class ArtistRef(val name: String)
private data class LastFmImage(
    @SerializedName("#text") val url: String,
    val size: String
)
private data class ErrorBody(val error: Int?, val message: String?)

fun fetchTopAlbumUrls(
    client: OkHttpClient,
    gson: Gson,
    username: String,
    apiKey: String,
    period: String,
    limit: Int
): List<String> {
    val url = "https://ws.audioscrobbler.com/2.0/" +
        "?method=user.gettopalbums" +
        "&user=${username.trim()}" +
        "&period=$period" +
        "&limit=$limit" +
        "&api_key=$apiKey" +
        "&format=json"

    println("  → GET $url")

    val response = client.newCall(Request.Builder().url(url).build()).execute()
    val body = response.body?.string() ?: return emptyList()

    println("  → HTTP ${response.code} (${body.length} bytes)")

    // Last.fm returns HTTP 200 even on errors
    if (body.contains("\"error\"")) {
        val err = gson.fromJson(body, ErrorBody::class.java)
        if (err?.error != null && err.error > 0) {
            throw LastFmException(err.error, err.message ?: "API error ${err.error}")
        }
    }

    val parsed = gson.fromJson(body, TopAlbumsResponse::class.java)
    val albums = parsed.topAlbums?.albums ?: return emptyList()

    return albums.mapNotNull { album ->
        val url = album.images.firstOrNull { it.size == "extralarge" }?.url?.takeIf { it.isNotEmpty() }
            ?: album.images.firstOrNull { it.size == "large" }?.url?.takeIf { it.isNotEmpty() }
        if (url != null) println("  ✓ ${album.artist.name} — ${album.name}")
        else println("  ✗ Sin imagen: ${album.artist.name} — ${album.name}")
        url
    }
}

// ── Image download ────────────────────────────────────────────────────────────

fun downloadImages(client: OkHttpClient, urls: List<String>): List<BufferedImage> =
    urls.mapIndexedNotNull { i, url ->
        try {
            print("  [${i+1}/${urls.size}] Descargando… ")
            val response = client.newCall(Request.Builder().url(url).build()).execute()
            val bytes = response.body?.bytes()
            if (bytes == null || bytes.isEmpty()) {
                println("✗ body vacío")
                return@mapIndexedNotNull null
            }
            val img = ImageIO.read(bytes.inputStream())
            if (img == null) {
                println("✗ ImageIO no pudo decodificar")
                null
            } else {
                println("✓ ${img.width}x${img.height}")
                img
            }
        } catch (e: Exception) {
            println("✗ ${e.javaClass.simpleName}: ${e.message}")
            null
        }
    }

// ── Collage generation (Java2D — replica de CollageMaker.kt) ─────────────────

private const val TILE_SIZE = 240
private const val DARKEN_ALPHA = 77 // 30% de 255

fun generateCollage(images: List<BufferedImage>, width: Int, height: Int): BufferedImage {
    // Convertir todas las imágenes a RGB para evitar problemas con canales alpha
    val tiles = images.map { toRgb(it) }

    val collage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val g = collage.createGraphics().apply {
        setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY)
    }

    // Fondo negro
    g.color = Color.BLACK
    g.fillRect(0, 0, width, height)

    val colTiles = 1 + ((width  - TILE_SIZE / 2) / TILE_SIZE)
    val rowTiles = 1 + ((height - TILE_SIZE / 2) / TILE_SIZE)
    val totalNeeded = maxOf(colTiles * rowTiles * 2, tiles.size)
    val ring = RingList(tiles, totalNeeded)

    var drawn = 0

    // Grid principal
    outer@ for (col in 0 until colTiles) {
        for (row in 0 until rowTiles) {
            val tile = ring.next() ?: break@outer
            g.drawImage(tile, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, null)
            drawn++
        }
    }

    // Tiles adicionales en posiciones aleatorias
    while (ring.hasNext()) {
        val tile = ring.next() ?: break
        if (width > TILE_SIZE && height > TILE_SIZE) {
            val x = Random.nextInt(width  - TILE_SIZE)
            val y = Random.nextInt(height - TILE_SIZE)
            g.drawImage(tile, x, y, TILE_SIZE, TILE_SIZE, null)
            drawn++
        }
    }

    println("  ✓ $drawn tiles dibujadas")

    // Overlay oscuro (30%)
    g.color = Color(0, 0, 0, DARKEN_ALPHA)
    g.fillRect(0, 0, width, height)

    g.dispose()
    return collage
}

private fun toRgb(src: BufferedImage): BufferedImage {
    if (src.type == BufferedImage.TYPE_INT_RGB) return src
    val dst = BufferedImage(src.width, src.height, BufferedImage.TYPE_INT_RGB)
    val g = dst.createGraphics()
    g.drawImage(src, 0, 0, null)
    g.dispose()
    return dst
}

private class RingList<T>(private val items: List<T>, private val total: Int) {
    private var index = 0
    private var count = 0
    fun hasNext() = count < total && items.isNotEmpty()
    fun next(): T? {
        if (!hasNext()) return null
        return items[index++ % items.size].also { count++ }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

fun readLocalProperties(): Properties {
    val props = Properties()
    var dir = File(".").canonicalFile
    repeat(5) {
        val f = File(dir, "local.properties")
        if (f.exists()) {
            f.inputStream().use { props.load(it) }
            println("▸ Credenciales leídas de: ${f.canonicalPath}")
            return props
        }
        dir = dir.parentFile ?: return props
    }
    return props
}

fun prompt(message: String): String {
    print(message)
    return readLine()?.trim() ?: error("Input vacío")
}

fun printBanner(username: String, period: String, limit: Int, w: Int, h: Int) {
    println("╔══════════════════════════════════════════╗")
    println("║   MusicWall — Collage Debug Tool         ║")
    println("╠══════════════════════════════════════════╣")
    println("║  Usuario : $username".padEnd(44) + "║")
    println("║  Período : $period".padEnd(44) + "║")
    println("║  Límite  : $limit álbumes".padEnd(44) + "║")
    println("║  Tamaño  : ${w}x${h}".padEnd(44) + "║")
    println("╚══════════════════════════════════════════╝")
}

fun printSummary(username: String, albums: Int, images: Int, ms: Long) {
    println("\n╔══════════════════════════════════════════╗")
    println("║   RESUMEN                                ║")
    println("╠══════════════════════════════════════════╣")
    println("║  Usuario            : $username".padEnd(44) + "║")
    println("║  Álbumes recibidos  : $albums".padEnd(44) + "║")
    println("║  Imágenes desc.     : $images".padEnd(44) + "║")
    println("║  Tiempo total       : ${ms}ms".padEnd(44) + "║")
    println("║  Output             : output/collage.jpg".padEnd(44) + "║")
    println("╚══════════════════════════════════════════╝")
}
