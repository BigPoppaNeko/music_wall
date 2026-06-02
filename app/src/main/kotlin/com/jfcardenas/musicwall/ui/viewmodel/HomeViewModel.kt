package com.jfcardenas.musicwall.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.api.Album
import com.jfcardenas.musicwall.api.ArtistRef
import com.jfcardenas.musicwall.api.LastFmImage
import com.jfcardenas.musicwall.api.browseableGenreTags
import com.jfcardenas.musicwall.api.isBrowseableGenreTag
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.api.RecentTrack
import com.jfcardenas.musicwall.api.UserTag
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.api.TrackInfoDetail
import com.jfcardenas.musicwall.data.CoverFallbackRepository
import com.jfcardenas.musicwall.data.CoverSearchState
import com.jfcardenas.musicwall.data.CoverUpdateBus
import com.jfcardenas.musicwall.data.FavoriteKeys
import com.jfcardenas.musicwall.data.NetworkResult
import com.jfcardenas.musicwall.domain.usecase.GetMusicImagesUseCase
import com.jfcardenas.musicwall.data.local.db.dao.AlbumDao
import com.jfcardenas.musicwall.data.local.db.dao.FavoriteAlbumDao
import com.jfcardenas.musicwall.data.local.db.dao.VetoedAlbumDao
import com.jfcardenas.musicwall.data.local.db.entity.AlbumEntity
import com.jfcardenas.musicwall.data.local.db.entity.FavoriteAlbum
import com.jfcardenas.musicwall.auth.UserSessionRepository
import com.jfcardenas.musicwall.service.CollageWallpaper
import com.jfcardenas.musicwall.scrobble.LocalScrobbleMapper
import com.jfcardenas.musicwall.scrobble.NowPlayingBus
import com.jfcardenas.musicwall.scrobble.ScrobbleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class CoverItem(
    val imageUrl: String,
    val albumName: String,
    val artistName: String,
    val genres: List<String> = emptyList(),
    val similar: List<CoverItem> = emptyList(),
) {
    val key: String
        get() = "${artistName.trim().lowercase()}::${albumName.trim().lowercase()}"
}

@Immutable
data class AlbumInsight(
    val cover: CoverItem,
    val genres: List<String>,
    val similar: List<CoverItem>,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val lastFmService: LastFmService,
    private val albumDao: AlbumDao,
    private val favoriteDao: FavoriteAlbumDao,
    private val vetoedAlbumDao: VetoedAlbumDao,
    private val coverFallbackRepo: CoverFallbackRepository,
    private val coverBus: CoverUpdateBus,
    private val userSession: UserSessionRepository,
    private val scrobbleRepository: ScrobbleRepository,
    private val nowPlayingBus: NowPlayingBus,
    private val getMusicImages: GetMusicImagesUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    @Immutable
    data class UiState(
        val username: String = "",
        val avatarUrl: String? = null,
        val isLoading: Boolean = false,
        val currentTrack: RecentTrack? = null,
        val isNowPlaying: Boolean = false,
        val recentTracks: List<RecentTrack> = emptyList(),
        val playcount: String = "—",
        val artistCount: String = "—",
        val weekAlbums: List<Album> = emptyList(),
        val recommendations: List<Album> = emptyList(),
        val topTags: List<UserTag> = emptyList(),
        val favoriteCovers: List<CoverItem> = emptyList(),
        val favoriteKeys: Set<String> = emptySet(),
        val currentArtistCovers: List<CoverItem> = emptyList(),
        // artist::album (lowercase) → URL override cuando Last.fm no tiene portada
        val coverOverrides: Map<String, String> = emptyMap(),
        val vsCoverA: CoverItem? = null,
        val vsCoverB: CoverItem? = null,
        val vsPool: List<CoverItem> = emptyList(),
        val vsWinStreaks: Map<String, Int> = emptyMap(),
        val lastVsWinnerKey: String? = null,
        val genreRecommendations: Map<String, List<CoverItem>> = emptyMap(),
        val loadingGenre: String? = null,
        val lastSavedFavoriteKey: String? = null,
        val error: String? = null,
        val currentTrackCoverUrl: String? = null,
        val coverSearchState: CoverSearchState = CoverSearchState.Idle,
        val recommendationCovers: List<CoverItem> = emptyList(),
        val discoveryArtistNames: List<String> = emptyList(),
        val isRefreshingRecommendations: Boolean = false,
    )

    companion object {
        private const val DISCOVERY_PREFS = "discovery_prefs"
        private const val KEY_SEEN_RECOMMENDATIONS = "seen_recommendation_keys"
        private const val RECOMMENDATION_COUNT = 8
        private const val MAX_SEEN_KEYS = 400
    }

    var uiState by mutableStateOf(UiState())
        private set

    init {
        refresh()
        viewModelScope.launch {
            coverBus.updates.collect { update -> applyBusUpdate(update) }
        }
        viewModelScope.launch {
            nowPlayingBus.updates.collect { refreshFromLocalQuiet() }
        }
    }

    private fun refreshFromLocalQuiet() {
        val userId = userSession.getUserId()
        if (userId.isBlank() || !userSession.usesLocalScrobbler()) return
        viewModelScope.launch { loadLocalState(userId, userSession.getLastFmUsername()) }
    }

    fun refresh() {
        if (userSession.usesExploreSource()) {
            val exploreArtists = userSession.getExploreArtists()
            if (exploreArtists.isEmpty()) {
                uiState = UiState(error = "Elige artistas para explorar portadas.")
                return
            }
            userSession.ensureExploreSession()
            uiState = uiState.copy(
                username = userSession.getDisplayName().ifBlank { "Explorador" },
                isLoading = true,
                error = null,
            )
            viewModelScope.launch { loadExploreState(exploreArtists) }
            return
        }

        val userId = userSession.getUserId()
        val lastFmUser = userSession.getLastFmUsername()
        if (userId.isBlank() && lastFmUser.isBlank()) {
            uiState = UiState(error = "Sin cuenta conectada. Completa el onboarding.")
            return
        }
        if (userId.isNotBlank() && userSession.usesLocalScrobbler()) {
            uiState = uiState.copy(
                username = userSession.getDisplayName().ifBlank { "Oyente" },
                isLoading = true,
                error = null,
            )
            viewModelScope.launch { loadLocalState(userId, lastFmUser) }
            return
        }
        if (lastFmUser.isBlank()) {
            uiState = UiState(error = "Sin cuenta conectada. Completa el onboarding.")
            return
        }
        refreshFromLastFm(lastFmUser)
    }

    private fun refreshFromLastFm(username: String) {
        uiState = uiState.copy(username = username, isLoading = true, error = null)
        viewModelScope.launch {
            try {
                coroutineScope {
                    val recentJob  = async { lastFmService.getRecentTracks(user = username, limit = 20) }
                    val infoJob    = async { lastFmService.getUserInfo(user = username) }
                    val weekJob    = async {
                        try { lastFmService.getTopAlbums(user = username, period = "7day", limit = 8).topAlbums.albums }
                        catch (_: Exception) { emptyList() }
                    }
                    val archiveAlbumsJob = async {
                        try { lastFmService.getTopAlbums(user = username, period = "overall", limit = 200).topAlbums.albums }
                        catch (_: Exception) { emptyList() }
                    }
                    val tagsJob    = async {
                        try { lastFmService.getUserTopTags(user = username, limit = 12).topTags.tags }
                        catch (_: Exception) { emptyList<UserTag>() }
                    }
                    val artistsJob = async {
                        try { lastFmService.getTopArtists(user = username, period = "1month", limit = 3).topArtists.artists }
                        catch (_: Exception) { emptyList() }
                    }
                    val vsDbJob    = async {
                        try { albumDao.getRandomWithImage(250) }
                        catch (_: Exception) { emptyList<AlbumEntity>() }
                    }
                    val favoritesJob = async {
                        try { favoriteDao.getAll() }
                        catch (_: Exception) { emptyList<FavoriteAlbum>() }
                    }

                    val recent     = recentJob.await()
                    val info       = infoJob.await()
                    val weekAlbums = weekJob.await()
                    val archiveAlbums = archiveAlbumsJob.await()
                    val tags       = tagsJob.await()
                    val topArtists = artistsJob.await()
                    val vsFromDb   = vsDbJob.await()
                    val favorites  = favoritesJob.await()
                    val favoriteKeys = favorites.map { albumKey(it.artistName, it.albumName) }.toSet()
                    val tracks     = recent.recentTracks.tracks
                    val track      = tracks.firstOrNull()

                    // Recomendaciones basadas en los artistas más escuchados del mes
                    val userAlbumKeys = weekAlbums.map { albumKey(it.artist.name, it.name) }.toSet()
                    val recommendations = topArtists
                        .map { artist ->
                            async {
                                try {
                                    lastFmService.getArtistTopAlbums(artist = artist.name, limit = 4)
                                        .topAlbums.albums
                                        .filter { a ->
                                            albumKey(a.artist.name, a.name) !in userAlbumKeys
                                                && albumKey(a.artist.name, a.name) !in favoriteKeys
                                                && a.images.getExtraLargeUrl() != null
                                        }
                                        .take(2)
                                } catch (_: Exception) { emptyList() }
                            }
                        }
                        .awaitAll()
                        .flatten()
                        .distinctBy { albumKey(it.artist.name, it.name) }
                        .take(6)

                    // Overrides de portada desde cache para álbumes sin imagen de Last.fm
                    val allAlbums = weekAlbums + recommendations
                    val coverOverrides = allAlbums
                        .filter { it.images.getExtraLargeUrl() == null }
                        .mapNotNull { album ->
                            coverFallbackRepo.getCachedUrl(album.artist.name, album.name)
                                ?.let { url -> "${album.artist.name}::${album.name}".lowercase() to url }
                        }
                        .toMap()
                        .toMutableMap()

                    // Portada efectiva del track actual: Last.fm → cache
                    val lastFmCover = track?.images?.getExtraLargeUrl()
                    val albumName   = track?.album?.name?.takeIf { it.isNotBlank() }
                    val coverUrl    = lastFmCover
                        ?: albumName?.let { coverFallbackRepo.getCachedUrl(track!!.artist.name, it) }
                    val currentArtistCovers = if (track != null) {
                        try {
                            val currentAlbumKey = albumKey(track.artist.name, track.album.name)
                            lastFmService.getArtistTopAlbums(artist = track.artist.name, limit = 6)
                                .topAlbums.albums
                                .mapNotNull { album ->
                                    album.images.getExtraLargeUrl()?.let {
                                        CoverItem(
                                            imageUrl = it,
                                            albumName = album.name,
                                            artistName = album.artist.name,
                                            genres = tags.map { tag -> tag.name }.take(4),
                                        )
                                    }
                                }
                                .filter { it.key != currentAlbumKey }
                                .distinctBy { it.key }
                                .take(2)
                        } catch (_: Exception) {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }

                    val visibleKeys = (weekAlbums + recommendations)
                        .map { albumKey(it.artist.name, it.name) }
                        .toSet() + favoriteKeys
                    val insightGenres = tags.map { it.name }.take(4)
                    val hiddenSimilar = vsFromDb
                        .filter { it.imageUrl.isNotEmpty() && albumKey(it.artistName, it.albumName) !in visibleKeys }
                        .map { CoverItem(it.imageUrl, it.albumName, it.artistName, insightGenres) }
                        .distinctBy { it.key }
                        .shuffled()
                        .take(5)
                    val similarCovers = hiddenSimilar.ifEmpty { recommendations.toCoverItems(insightGenres).take(5) }
                    val vsPool = buildVsPool(vsFromDb, archiveAlbums + weekAlbums, visibleKeys, insightGenres, similarCovers)
                    val vsA = vsPool.getOrNull(0)
                    val vsB = vsPool.getOrNull(1)
                    val artistNames = topArtists.map { it.name }
                    val vetoedKeys = vetoedAlbumDao.getAllIds().toSet()
                    val previousRecKeys = uiState.recommendationCovers.map { it.key }
                    if (previousRecKeys.isNotEmpty()) {
                        markRecommendationsSeen(previousRecKeys)
                    }
                    val recommendationCovers = fetchFreshRecommendationCovers(
                        excludeKeys = buildRecommendationExcludeKeys(
                            favoriteKeys = favoriteKeys,
                            vetoedKeys = vetoedKeys,
                            weekAlbumKeys = userAlbumKeys,
                            extra = emptySet(),
                        ),
                        genres = insightGenres,
                        artistNames = artistNames,
                        tags = tags.map { it.name },
                        vsPool = vsPool,
                        coverOverrides = coverOverrides,
                    )

                    uiState = UiState(
                        username             = username,
                        avatarUrl            = info.user.images?.getExtraLargeUrl(),
                        currentTrack         = track,
                        isNowPlaying         = track?.attr?.nowplaying == "true",
                        recentTracks         = tracks,
                        playcount            = info.user.playcount,
                        artistCount          = info.user.artistCount ?: "—",
                        weekAlbums           = weekAlbums,
                        recommendations      = recommendations,
                        topTags              = tags,
                        favoriteCovers       = favorites.mapNotNull { favorite ->
                            favorite.imageUrl?.let {
                                CoverItem(it, favorite.albumName, favorite.artistName, tags.map { tag -> tag.name }.take(4))
                            }
                        },
                        favoriteKeys         = favoriteKeys,
                        currentArtistCovers  = currentArtistCovers,
                        coverOverrides       = coverOverrides,
                        vsCoverA             = vsA,
                        vsCoverB             = vsB,
                        vsPool               = vsPool,
                        vsWinStreaks         = uiState.vsWinStreaks,
                        lastVsWinnerKey      = uiState.lastVsWinnerKey,
                        genreRecommendations = uiState.genreRecommendations,
                        isLoading            = false,
                        currentTrackCoverUrl = coverUrl,
                        coverSearchState     = CoverSearchState.Idle,
                        recommendationCovers = recommendationCovers,
                        discoveryArtistNames = artistNames,
                    )
                }
            } catch (_: Exception) {
                uiState = uiState.copy(isLoading = false, error = "No se pudo cargar. Revisa tu conexión.")
            }
        }
    }

    private suspend fun loadLocalState(userId: String, lastFmUser: String) {
        try {
            val localEntries = scrobbleRepository.getRecent(userId, limit = 30)
            val tracks = localEntries.map(LocalScrobbleMapper::toRecentTrack)
            val track = tracks.firstOrNull()
            val isNowPlaying = track?.attr?.nowplaying == "true"
            val favorites = favoriteDao.getAll()
            val favoriteKeys = favorites.map { albumKey(it.artistName, it.albumName) }.toSet()
            val sinceWeek = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
            val weekEntries = localEntries.filter { !it.isNowPlaying && it.playedAt >= sinceWeek }
            val weekAlbums = weekEntries
                .groupBy { albumKey(it.artistName, it.albumName) }
                .values
                .mapNotNull { plays ->
                    plays.maxByOrNull { it.playedAt }?.let { entry ->
                        Album(
                            name = entry.albumName.ifBlank { entry.trackName },
                            artist = ArtistRef(entry.artistName),
                            images = entry.imageUrl?.let { listOf(LastFmImage(it, "extralarge")) } ?: emptyList(),
                            playcount = plays.size.toString(),
                            mbid = entry.albumMbid,
                            url = null,
                        )
                    }
                }
                .take(8)

            val topArtistNames = localEntries
                .groupBy { it.artistName.lowercase() }
                .entries
                .sortedByDescending { it.value.size }
                .take(3)
                .map { it.value.first().artistName }

            val tags = if (lastFmUser.isNotBlank()) {
                try { lastFmService.getUserTopTags(user = lastFmUser, limit = 12).topTags.tags }
                catch (_: Exception) { emptyList() }
            } else {
                emptyList()
            }.ifEmpty {
                topArtistNames.take(4).map { UserTag(name = it, count = "0", url = null) }
            }

            val coverUrl = track?.images?.getExtraLargeUrl()
                ?: track?.album?.name?.takeIf { it.isNotBlank() }?.let { album ->
                    track?.artist?.name?.let { artist -> coverFallbackRepo.getCachedUrl(artist, album) }
                }

            val vsFromDb = try { albumDao.getRandomWithImage(250) } catch (_: Exception) { emptyList() }
            val vetoedKeys = vetoedAlbumDao.getAllIds().toSet()
            val recommendationCovers = fetchFreshRecommendationCovers(
                excludeKeys = buildRecommendationExcludeKeys(
                    favoriteKeys = favoriteKeys,
                    vetoedKeys = vetoedKeys,
                    weekAlbumKeys = weekAlbums.map { albumKey(it.artist.name, it.name) }.toSet(),
                    extra = emptySet(),
                ),
                genres = tags.map { it.name }.take(4),
                artistNames = topArtistNames,
                tags = tags.map { it.name },
                vsPool = emptyList(),
                coverOverrides = emptyMap(),
            )
            val vsPool = buildVsPool(
                vsFromDb,
                weekAlbums + emptyList(),
                emptySet(),
                tags.map { it.name }.take(4),
                recommendationCovers.take(5),
            )

            uiState = uiState.copy(
                username = userSession.getDisplayName().ifBlank { "Oyente" },
                currentTrack = track,
                isNowPlaying = isNowPlaying,
                recentTracks = tracks,
                playcount = localEntries.count { !it.isNowPlaying }.toString(),
                artistCount = topArtistNames.size.toString(),
                weekAlbums = weekAlbums,
                topTags = tags,
                favoriteCovers = favorites.mapNotNull { fav ->
                    fav.imageUrl?.let {
                        CoverItem(it, fav.albumName, fav.artistName, tags.map { tag -> tag.name }.take(4))
                    }
                },
                favoriteKeys = favoriteKeys,
                currentTrackCoverUrl = coverUrl,
                vsCoverA = vsPool.getOrNull(0),
                vsCoverB = vsPool.getOrNull(1),
                vsPool = vsPool,
                recommendationCovers = recommendationCovers,
                discoveryArtistNames = topArtistNames,
                isLoading = false,
                error = null,
            )
        } catch (_: Exception) {
            uiState = uiState.copy(isLoading = false, error = "No se pudo cargar tu historial local.")
        }
    }

    private suspend fun loadExploreState(exploreArtists: List<String>) {
        try {
            val favorites = favoriteDao.getAll()
            val favoriteKeys = favorites.map { albumKey(it.artistName, it.albumName) }.toSet()
            val vetoedKeys = vetoedAlbumDao.getAllIds().toSet()
            when (val result = getMusicImages.artistCatalogAlbums(exploreArtists, limit = 120)) {
                is NetworkResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = result.message ?: "No se pudieron cargar portadas.",
                    )
                }
                is NetworkResult.Success -> {
                    val catalogCovers = result.data
                        .mapNotNull { img ->
                            if (img.url.isBlank()) null
                            else CoverItem(
                                imageUrl = img.url,
                                albumName = img.name,
                                artistName = img.artistName,
                                genres = exploreArtists.take(4),
                            )
                        }
                        .distinctBy { it.key }

                    if (catalogCovers.isEmpty()) {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = "No encontramos portadas para esos artistas.",
                        )
                        return
                    }

                    val tags = exploreArtists.take(6).map { UserTag(name = it, count = "0", url = null) }
                    val vsPool = catalogCovers.shuffled().take(40)
                    val weekAlbums = catalogCovers.take(8).map { cover ->
                        Album(
                            name = cover.albumName,
                            artist = ArtistRef(cover.artistName),
                            images = listOf(LastFmImage(cover.imageUrl, "extralarge")),
                            playcount = null,
                            mbid = null,
                            url = null,
                        )
                    }
                    val recommendationCovers = catalogCovers
                        .filter { it.key !in favoriteKeys && it.key !in vetoedKeys }
                        .shuffled()
                        .take(RECOMMENDATION_COUNT)

                    uiState = UiState(
                        username = userSession.getDisplayName().ifBlank { "Explorador" },
                        topTags = tags,
                        favoriteCovers = favorites.mapNotNull { fav ->
                            fav.imageUrl?.let {
                                CoverItem(it, fav.albumName, fav.artistName, exploreArtists.take(4))
                            }
                        },
                        favoriteKeys = favoriteKeys,
                        recommendationCovers = recommendationCovers,
                        discoveryArtistNames = exploreArtists,
                        vsCoverA = vsPool.getOrNull(0),
                        vsCoverB = vsPool.getOrNull(1),
                        vsPool = vsPool,
                        weekAlbums = weekAlbums,
                        isLoading = false,
                        error = null,
                    )
                }
            }
        } catch (_: Exception) {
            uiState = uiState.copy(isLoading = false, error = "No se pudo cargar. Revisa tu conexión.")
        }
    }

    /** El usuario pidió explícitamente buscar la portada en Deezer. */
    fun searchCurrentCover() {
        val track = uiState.currentTrack ?: return
        val albumName = track.album.name.takeIf { it.isNotBlank() } ?: return
        uiState = uiState.copy(coverSearchState = CoverSearchState.Searching)
        viewModelScope.launch {
            val url = coverFallbackRepo.findAndSave(track.artist.name, albumName)
            uiState = if (url != null) {
                uiState.copy(currentTrackCoverUrl = url, coverSearchState = CoverSearchState.Idle)
            } else {
                uiState.copy(coverSearchState = CoverSearchState.NotFound)
            }
        }
    }

    fun saveFavorite(cover: CoverItem) {
        viewModelScope.launch {
            favoriteDao.insert(
                FavoriteAlbum(
                    id = cover.key,
                    albumName = cover.albumName,
                    artistName = cover.artistName,
                    imageUrl = cover.imageUrl,
                )
            )
            val updated = listOf(cover) + uiState.favoriteCovers.filter { it.key != cover.key }
            uiState = uiState.copy(
                favoriteCovers = updated,
                favoriteKeys = uiState.favoriteKeys + cover.key,
                recommendations = uiState.recommendations.filter { albumKey(it.artist.name, it.name) != cover.key },
                lastSavedFavoriteKey = cover.key,
            )
        }
    }

    fun applyFavoriteKeys(keys: Set<String>) {
        if (keys == uiState.favoriteKeys) return
        viewModelScope.launch {
            val favorites = favoriteDao.getAll()
            val favoriteCovers = favorites.map {
                CoverItem(
                    imageUrl = it.imageUrl.orEmpty(),
                    albumName = it.albumName,
                    artistName = it.artistName,
                )
            }
            uiState = uiState.copy(
                favoriteKeys = keys,
                favoriteCovers = favoriteCovers,
                recommendationCovers = uiState.recommendationCovers.filter { it.key !in keys },
                recommendations = uiState.recommendations.filter {
                    albumKey(it.artist.name, it.name) !in keys
                },
            )
        }
    }

    fun clearFavoriteSavedFeedback() {
        if (uiState.lastSavedFavoriteKey != null) {
            uiState = uiState.copy(lastSavedFavoriteKey = null)
        }
    }

    fun applyCurrentTrackCoverSwap(cover: CoverItem) {
        uiState = uiState.copy(currentTrackCoverUrl = cover.imageUrl)
    }

    fun saveCurrentTrackCover() {
        val track = uiState.currentTrack ?: return
        val imageUrl = uiState.currentTrackCoverUrl ?: return
        val albumName = track.album.name.takeIf { it.isNotBlank() } ?: track.name
        saveFavorite(
            CoverItem(
                imageUrl = imageUrl,
                albumName = albumName,
                artistName = track.artist.name,
                genres = uiState.topTags.map { it.name }.take(4),
            )
        )
    }

    /** Quita una portada de los carruseles de discovery al pasar a favoritas. */
    fun removeFromDiscoveryFeeds(coverKey: String) {
        uiState = uiState.copy(
            recommendationCovers = uiState.recommendationCovers.filter { it.key != coverKey },
        )
    }

    /** Sustituye todas las portadas de Te puede gustar por otras no vistas. */
    fun reloadRecommendationCovers() {
        viewModelScope.launch {
            uiState = uiState.copy(isRefreshingRecommendations = true)
            markRecommendationsSeen(uiState.recommendationCovers.map { it.key })
            val vetoedKeys = vetoedAlbumDao.getAllIds().toSet()
            val weekKeys = uiState.weekAlbums.map { albumKey(it.artist.name, it.name) }.toSet()
            val fresh = fetchFreshRecommendationCovers(
                excludeKeys = buildRecommendationExcludeKeys(
                    favoriteKeys = uiState.favoriteKeys,
                    vetoedKeys = vetoedKeys,
                    weekAlbumKeys = weekKeys,
                    extra = emptySet(),
                ),
                genres = uiState.topTags.map { it.name }.take(4),
                artistNames = uiState.discoveryArtistNames,
                tags = uiState.topTags.map { it.name },
                vsPool = uiState.vsPool,
                coverOverrides = uiState.coverOverrides,
            )
            uiState = uiState.copy(
                recommendationCovers = fresh,
                isRefreshingRecommendations = false,
            )
        }
    }

    private fun loadSeenRecommendationKeys(): Set<String> {
        val prefs = context.getSharedPreferences(DISCOVERY_PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_SEEN_RECOMMENDATIONS, emptySet()).orEmpty()
    }

    private fun saveSeenRecommendationKeys(keys: Set<String>) {
        val trimmed = if (keys.size <= MAX_SEEN_KEYS) {
            keys
        } else {
            keys.toList().takeLast(MAX_SEEN_KEYS).toSet()
        }
        context.getSharedPreferences(DISCOVERY_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_SEEN_RECOMMENDATIONS, trimmed)
            .apply()
    }

    private fun markRecommendationsSeen(keys: Collection<String>) {
        if (keys.isEmpty()) return
        saveSeenRecommendationKeys(loadSeenRecommendationKeys() + keys)
    }

    private fun buildRecommendationExcludeKeys(
        favoriteKeys: Set<String>,
        vetoedKeys: Set<String>,
        weekAlbumKeys: Set<String>,
        extra: Set<String>,
    ): Set<String> = loadSeenRecommendationKeys() +
        favoriteKeys +
        vetoedKeys +
        weekAlbumKeys +
        extra +
        uiState.recommendationCovers.map { it.key }.toSet()

    private suspend fun fetchFreshRecommendationCovers(
        excludeKeys: Set<String>,
        genres: List<String>,
        artistNames: List<String>,
        tags: List<String>,
        vsPool: List<CoverItem>,
        coverOverrides: Map<String, String>,
    ): List<CoverItem> {
        var exclude = excludeKeys
        repeat(2) { attempt ->
            val batch = collectRecommendationCandidates(
                excludeKeys = exclude,
                genres = genres,
                artistNames = artistNames,
                tags = tags,
                vsPool = vsPool,
                coverOverrides = coverOverrides,
            )
            if (batch.size >= RECOMMENDATION_COUNT || attempt == 1) {
                return batch.take(RECOMMENDATION_COUNT)
            }
            val seen = loadSeenRecommendationKeys()
            if (seen.size > 80) {
                saveSeenRecommendationKeys(seen.toList().takeLast(seen.size / 2).toSet())
                exclude = buildRecommendationExcludeKeys(
                    favoriteKeys = uiState.favoriteKeys,
                    vetoedKeys = vetoedAlbumDao.getAllIds().toSet(),
                    weekAlbumKeys = uiState.weekAlbums.map { albumKey(it.artist.name, it.name) }.toSet(),
                    extra = emptySet(),
                )
            }
        }
        return emptyList()
    }

    private suspend fun collectRecommendationCandidates(
        excludeKeys: Set<String>,
        genres: List<String>,
        artistNames: List<String>,
        tags: List<String>,
        vsPool: List<CoverItem>,
        coverOverrides: Map<String, String>,
    ): List<CoverItem> = coroutineScope {
        val fromPool = vsPool.filter { it.key !in excludeKeys && it.imageUrl.isNotBlank() }
        val fromDb = albumDao.getRandomWithImage(120)
            .map { entity ->
                CoverItem(
                    imageUrl = entity.imageUrl,
                    albumName = entity.albumName,
                    artistName = entity.artistName,
                    genres = genres,
                )
            }
            .filter { it.key !in excludeKeys && it.imageUrl.isNotBlank() }

        val fromArtists = artistNames.take(4).map { artist ->
            async {
                try {
                    lastFmService.getArtistTopAlbums(artist = artist, limit = 16)
                        .topAlbums.albums
                        .mapNotNull { album ->
                            val key = albumKey(album.artist.name, album.name)
                            val imageUrl = coverOverrides[key] ?: album.images.getExtraLargeUrl()
                            imageUrl?.let {
                                CoverItem(it, album.name, album.artist.name, genres)
                            }
                        }
                        .filter { it.key !in excludeKeys }
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }.awaitAll().flatten()

        val genreTags = browseableGenreTags(tags.map { UserTag(it, "0", null) }).take(2)
        val fromTags = genreTags.map { tag ->
            async {
                try {
                    lastFmService.getTagTopAlbums(tag = tag, limit = 40)
                        .albums.albums
                        .mapNotNull { album ->
                            val key = albumKey(album.artist.name, album.name)
                            val imageUrl = coverOverrides[key] ?: album.images.getExtraLargeUrl()
                            imageUrl?.let {
                                CoverItem(it, album.name, album.artist.name, genres)
                            }
                        }
                        .filter { it.key !in excludeKeys }
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }.awaitAll().flatten()

        (fromPool + fromDb + fromArtists + fromTags)
            .distinctBy { it.key }
            .shuffled()
            .take(RECOMMENDATION_COUNT * 3)
    }

    fun chooseVsWinner(winner: CoverItem) {
        val current = uiState
        val loser = when (winner.key) {
            current.vsCoverA?.key -> current.vsCoverB
            current.vsCoverB?.key -> current.vsCoverA
            else -> null
        } ?: return
        val occupied = setOf(winner.key, loser.key)
        val replacement = current.vsPool.firstOrNull { it.key !in occupied }
            ?: current.vsPool.shuffled().firstOrNull { it.key != winner.key }
            ?: return

        val newStreaks = current.vsWinStreaks + (winner.key to ((current.vsWinStreaks[winner.key] ?: 0) + 1))
        uiState = if (winner.key == current.vsCoverA?.key) {
            current.copy(
                vsCoverB = replacement,
                vsPool = rotatePool(current.vsPool, replacement.key),
                vsWinStreaks = newStreaks,
                lastVsWinnerKey = winner.key,
            )
        } else {
            current.copy(
                vsCoverA = replacement,
                vsPool = rotatePool(current.vsPool, replacement.key),
                vsWinStreaks = newStreaks,
                lastVsWinnerKey = winner.key,
            )
        }
    }

    fun replaceVsCover(target: CoverItem) {
        val current = uiState
        val occupied = setOfNotNull(current.vsCoverA?.key, current.vsCoverB?.key) - target.key
        val replacement = current.vsPool.firstOrNull { it.key != target.key && it.key !in occupied }
            ?: current.vsPool.filter { it.key != target.key }.shuffled().firstOrNull()
            ?: return

        uiState = when (target.key) {
            current.vsCoverA?.key -> current.copy(
                vsCoverA = replacement,
                vsPool = rotatePool(current.vsPool, replacement.key),
            )
            current.vsCoverB?.key -> current.copy(
                vsCoverB = replacement,
                vsPool = rotatePool(current.vsPool, replacement.key),
            )
            else -> current
        }
    }

    fun loadGenreRecommendations(
        genre: String,
        excludeKeys: Set<String>,
        limit: Int = 12,
        forceRefresh: Boolean = false,
    ) {
        val cleanGenre = genre.trim()
        if (cleanGenre.isEmpty() || !isBrowseableGenreTag(cleanGenre)) return
        val cacheKey = cleanGenre.lowercase()
        val cached = uiState.genreRecommendations[cacheKey]
        if (!forceRefresh && !cached.isNullOrEmpty() && cached.size >= limit.coerceAtMost(8)) return

        uiState = uiState.copy(loadingGenre = cacheKey)
        viewModelScope.launch {
            var covers = fetchGenreCovers(cleanGenre, excludeKeys, limit)
            if (covers.isEmpty() && excludeKeys.isNotEmpty()) {
                covers = fetchGenreCovers(cleanGenre, emptySet(), limit)
                    .filter { it.key !in excludeKeys }
                    .take(limit)
            }
            uiState = uiState.copy(
                loadingGenre = null,
                genreRecommendations = uiState.genreRecommendations + (cacheKey to covers),
            )
        }
    }

    private suspend fun fetchGenreCovers(
        genre: String,
        excludeKeys: Set<String>,
        limit: Int,
    ): List<CoverItem> {
        val fromTagAlbums = fetchTagAlbumCovers(genre, excludeKeys, limit)
        if (fromTagAlbums.isNotEmpty()) return fromTagAlbums
        return fetchGenreCoversViaArtists(genre, excludeKeys, limit)
    }

    private suspend fun fetchTagAlbumCovers(
        genre: String,
        excludeKeys: Set<String>,
        limit: Int,
    ): List<CoverItem> = try {
        lastFmService.getTagTopAlbums(tag = genre, limit = 50)
            .albums.albums
            .toCoverItems(genre, excludeKeys, limit)
    } catch (_: Exception) {
        emptyList()
    }

    private suspend fun fetchGenreCoversViaArtists(
        genre: String,
        excludeKeys: Set<String>,
        limit: Int,
    ): List<CoverItem> = try {
        val artists = lastFmService.getTagTopArtists(tag = genre, limit = 6).topArtists.artists
        artists.flatMap { artist ->
            try {
                lastFmService.getArtistTopAlbums(artist = artist.name, limit = 8)
                    .topAlbums.albums
                    .toCoverItems(genre, excludeKeys, limit)
            } catch (_: Exception) {
                emptyList()
            }
        }
            .distinctBy { it.key }
            .filter { it.key !in excludeKeys }
            .take(limit)
    } catch (_: Exception) {
        emptyList()
    }

    private fun List<Album>.toCoverItems(
        genre: String,
        excludeKeys: Set<String>,
        limit: Int,
    ): List<CoverItem> = mapNotNull { album ->
        album.images.getExtraLargeUrl()?.let {
            CoverItem(
                imageUrl = it,
                albumName = album.name,
                artistName = album.artist.name,
                genres = listOf(genre),
            )
        }
    }
        .filter { it.key !in excludeKeys }
        .distinctBy { it.key }
        .take(limit)

    private fun applyBusUpdate(update: CoverUpdateBus.CoverUpdate) {
        // Actualizar portada del track actual si coincide
        val track = uiState.currentTrack
        if (uiState.currentTrackCoverUrl == null && track != null) {
            val trackKey = "${track.artist.name}::${track.album.name}".lowercase()
            if (trackKey == update.key) {
                uiState = uiState.copy(currentTrackCoverUrl = update.url)
                return
            }
        }
        // Actualizar coverOverrides para álbumes de la semana y recomendaciones
        val allAlbumKeys = (uiState.weekAlbums + uiState.recommendations)
            .map { "${it.artist.name}::${it.name}".lowercase() }
            .toSet()
        if (update.key in allAlbumKeys && update.key !in uiState.coverOverrides) {
            uiState = uiState.copy(
                coverOverrides = uiState.coverOverrides + (update.key to update.url)
            )
        }
    }

    private fun buildVsPool(
        dbAlbums: List<AlbumEntity>,
        weekAlbums: List<Album>,
        excludedKeys: Set<String>,
        genres: List<String>,
        similar: List<CoverItem>,
    ): List<CoverItem> {
        val fromDb = dbAlbums
            .filter { it.imageUrl.isNotEmpty() }
            .map { CoverItem(it.imageUrl, it.albumName, it.artistName, genres, similar) }
            .filter { it.key !in excludedKeys }
            .distinctBy { it.key }
            .shuffled()
        val fromWeek = weekAlbums
            .mapNotNull { a -> a.images.getExtraLargeUrl()?.let { CoverItem(it, a.name, a.artist.name, genres, similar) } }
            .filter { it.key !in excludedKeys }
            .distinctBy { it.key }
            .shuffled()
        val pool = (fromDb + fromWeek).distinctBy { it.key }
        return if (pool.size >= 2) {
            pool
        } else {
            (fromDb + weekAlbums.mapNotNull { a ->
                a.images.getExtraLargeUrl()?.let { CoverItem(it, a.name, a.artist.name, genres, similar) }
            }).distinctBy { it.key }
        }
    }

    private fun List<Album>.toCoverItems(genres: List<String>): List<CoverItem> =
        mapNotNull { album ->
            album.images.getExtraLargeUrl()?.let {
                CoverItem(
                    imageUrl = it,
                    albumName = album.name,
                    artistName = album.artist.name,
                    genres = genres,
                )
            }
        }.distinctBy { it.key }

    private fun rotatePool(pool: List<CoverItem>, usedKey: String): List<CoverItem> {
        val index = pool.indexOfFirst { it.key == usedKey }
        return if (index < 0) pool else pool.drop(index + 1) + pool.take(index + 1)
    }

    private fun albumKey(artist: String, album: String): String =
        FavoriteKeys.id(artist, album)
}

fun relativeTime(uts: String?): String {
    val ts = uts?.toLongOrNull() ?: return "—"
    val diffSeconds = System.currentTimeMillis() / 1000 - ts
    return when {
        diffSeconds < 60      -> "hace un momento"
        diffSeconds < 3600    -> "hace ${diffSeconds / 60} min"
        diffSeconds < 86400   -> "hace ${diffSeconds / 3600} h"
        else                  -> "hace ${diffSeconds / 86400} días"
    }
}
