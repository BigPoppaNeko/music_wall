package com.jfcardenas.musicwall.`data`.local.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.jfcardenas.musicwall.`data`.local.db.dao.AlbumDao
import com.jfcardenas.musicwall.`data`.local.db.dao.AlbumDao_Impl
import com.jfcardenas.musicwall.`data`.local.db.dao.ArtistDao
import com.jfcardenas.musicwall.`data`.local.db.dao.ArtistDao_Impl
import com.jfcardenas.musicwall.`data`.local.db.dao.TrackDao
import com.jfcardenas.musicwall.`data`.local.db.dao.TrackDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MusicWallDatabase_Impl : MusicWallDatabase() {
  private val _albumDao: Lazy<AlbumDao> = lazy {
    AlbumDao_Impl(this)
  }

  private val _artistDao: Lazy<ArtistDao> = lazy {
    ArtistDao_Impl(this)
  }

  private val _trackDao: Lazy<TrackDao> = lazy {
    TrackDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(2, "110a5bbb08451a16d0aa3a2860c3feb0", "971e5b414aae9efb2c7bf11568664f37") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `albums` (`id` TEXT NOT NULL, `username` TEXT NOT NULL, `period` TEXT NOT NULL, `albumName` TEXT NOT NULL, `artistName` TEXT NOT NULL, `imageUrl` TEXT NOT NULL, `rank` INTEGER NOT NULL, `playcount` INTEGER NOT NULL, `mbid` TEXT, `lastFmUrl` TEXT, `fetchedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_albums_user_period` ON `albums` (`username`, `period`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `artists` (`id` TEXT NOT NULL, `username` TEXT NOT NULL, `period` TEXT NOT NULL, `artistName` TEXT NOT NULL, `imageUrl` TEXT NOT NULL, `rank` INTEGER NOT NULL, `playcount` INTEGER NOT NULL, `mbid` TEXT, `lastFmUrl` TEXT, `fetchedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_artists_user_period` ON `artists` (`username`, `period`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tracks` (`id` TEXT NOT NULL, `username` TEXT NOT NULL, `period` TEXT NOT NULL, `kind` TEXT NOT NULL, `trackName` TEXT NOT NULL, `artistName` TEXT NOT NULL, `albumName` TEXT NOT NULL, `imageUrl` TEXT NOT NULL, `rank` INTEGER NOT NULL, `playcount` INTEGER NOT NULL, `mbid` TEXT, `lastFmUrl` TEXT, `fetchedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `idx_tracks_user_period_kind` ON `tracks` (`username`, `period`, `kind`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '110a5bbb08451a16d0aa3a2860c3feb0')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `albums`")
        connection.execSQL("DROP TABLE IF EXISTS `artists`")
        connection.execSQL("DROP TABLE IF EXISTS `tracks`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsAlbums: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsAlbums.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbums.put("username", TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbums.put("period", TableInfo.Column("period", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbums.put("albumName", TableInfo.Column("albumName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbums.put("artistName", TableInfo.Column("artistName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbums.put("imageUrl", TableInfo.Column("imageUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbums.put("rank", TableInfo.Column("rank", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbums.put("playcount", TableInfo.Column("playcount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbums.put("mbid", TableInfo.Column("mbid", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbums.put("lastFmUrl", TableInfo.Column("lastFmUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAlbums.put("fetchedAt", TableInfo.Column("fetchedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAlbums: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesAlbums: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesAlbums.add(TableInfo.Index("idx_albums_user_period", false, listOf("username", "period"), listOf("ASC", "ASC")))
        val _infoAlbums: TableInfo = TableInfo("albums", _columnsAlbums, _foreignKeysAlbums, _indicesAlbums)
        val _existingAlbums: TableInfo = read(connection, "albums")
        if (!_infoAlbums.equals(_existingAlbums)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |albums(com.jfcardenas.musicwall.data.local.db.entity.AlbumEntity).
              | Expected:
              |""".trimMargin() + _infoAlbums + """
              |
              | Found:
              |""".trimMargin() + _existingAlbums)
        }
        val _columnsArtists: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsArtists.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArtists.put("username", TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArtists.put("period", TableInfo.Column("period", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArtists.put("artistName", TableInfo.Column("artistName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArtists.put("imageUrl", TableInfo.Column("imageUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArtists.put("rank", TableInfo.Column("rank", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArtists.put("playcount", TableInfo.Column("playcount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArtists.put("mbid", TableInfo.Column("mbid", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArtists.put("lastFmUrl", TableInfo.Column("lastFmUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsArtists.put("fetchedAt", TableInfo.Column("fetchedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysArtists: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesArtists: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesArtists.add(TableInfo.Index("idx_artists_user_period", false, listOf("username", "period"), listOf("ASC", "ASC")))
        val _infoArtists: TableInfo = TableInfo("artists", _columnsArtists, _foreignKeysArtists, _indicesArtists)
        val _existingArtists: TableInfo = read(connection, "artists")
        if (!_infoArtists.equals(_existingArtists)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |artists(com.jfcardenas.musicwall.data.local.db.entity.ArtistEntity).
              | Expected:
              |""".trimMargin() + _infoArtists + """
              |
              | Found:
              |""".trimMargin() + _existingArtists)
        }
        val _columnsTracks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTracks.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("username", TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("period", TableInfo.Column("period", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("kind", TableInfo.Column("kind", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("trackName", TableInfo.Column("trackName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("artistName", TableInfo.Column("artistName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("albumName", TableInfo.Column("albumName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("imageUrl", TableInfo.Column("imageUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("rank", TableInfo.Column("rank", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("playcount", TableInfo.Column("playcount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("mbid", TableInfo.Column("mbid", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("lastFmUrl", TableInfo.Column("lastFmUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTracks.put("fetchedAt", TableInfo.Column("fetchedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTracks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTracks: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTracks.add(TableInfo.Index("idx_tracks_user_period_kind", false, listOf("username", "period", "kind"), listOf("ASC", "ASC", "ASC")))
        val _infoTracks: TableInfo = TableInfo("tracks", _columnsTracks, _foreignKeysTracks, _indicesTracks)
        val _existingTracks: TableInfo = read(connection, "tracks")
        if (!_infoTracks.equals(_existingTracks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |tracks(com.jfcardenas.musicwall.data.local.db.entity.TrackEntity).
              | Expected:
              |""".trimMargin() + _infoTracks + """
              |
              | Found:
              |""".trimMargin() + _existingTracks)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "albums", "artists", "tracks")
  }

  public override fun clearAllTables() {
    super.performClear(false, "albums", "artists", "tracks")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(AlbumDao::class, AlbumDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ArtistDao::class, ArtistDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TrackDao::class, TrackDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun albumDao(): AlbumDao = _albumDao.value

  public override fun artistDao(): ArtistDao = _artistDao.value

  public override fun trackDao(): TrackDao = _trackDao.value
}
