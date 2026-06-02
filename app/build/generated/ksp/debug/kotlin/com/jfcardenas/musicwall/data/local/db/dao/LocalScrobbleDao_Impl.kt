package com.jfcardenas.musicwall.`data`.local.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.jfcardenas.musicwall.`data`.local.db.entity.LocalScrobbleEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class LocalScrobbleDao_Impl(
  __db: RoomDatabase,
) : LocalScrobbleDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfLocalScrobbleEntity: EntityInsertAdapter<LocalScrobbleEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfLocalScrobbleEntity = object : EntityInsertAdapter<LocalScrobbleEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `local_scrobbles` (`id`,`userId`,`artistName`,`trackName`,`albumName`,`imageUrl`,`artistMbid`,`trackMbid`,`albumMbid`,`matchConfidence`,`rawArtist`,`rawTrack`,`rawAlbum`,`sourceApp`,`playedAt`,`durationMs`,`isNowPlaying`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: LocalScrobbleEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.userId)
        statement.bindText(3, entity.artistName)
        statement.bindText(4, entity.trackName)
        statement.bindText(5, entity.albumName)
        val _tmpImageUrl: String? = entity.imageUrl
        if (_tmpImageUrl == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpImageUrl)
        }
        val _tmpArtistMbid: String? = entity.artistMbid
        if (_tmpArtistMbid == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpArtistMbid)
        }
        val _tmpTrackMbid: String? = entity.trackMbid
        if (_tmpTrackMbid == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpTrackMbid)
        }
        val _tmpAlbumMbid: String? = entity.albumMbid
        if (_tmpAlbumMbid == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpAlbumMbid)
        }
        statement.bindDouble(10, entity.matchConfidence.toDouble())
        statement.bindText(11, entity.rawArtist)
        statement.bindText(12, entity.rawTrack)
        statement.bindText(13, entity.rawAlbum)
        val _tmpSourceApp: String? = entity.sourceApp
        if (_tmpSourceApp == null) {
          statement.bindNull(14)
        } else {
          statement.bindText(14, _tmpSourceApp)
        }
        statement.bindLong(15, entity.playedAt)
        statement.bindLong(16, entity.durationMs)
        val _tmp: Int = if (entity.isNowPlaying) 1 else 0
        statement.bindLong(17, _tmp.toLong())
      }
    }
  }

  public override suspend fun insert(scrobble: LocalScrobbleEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfLocalScrobbleEntity.insert(_connection, scrobble)
  }

  public override suspend fun getRecent(userId: String, limit: Int): List<LocalScrobbleEntity> {
    val _sql: String = "SELECT * FROM local_scrobbles WHERE userId = ? ORDER BY playedAt DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfTrackName: Int = getColumnIndexOrThrow(_stmt, "trackName")
        val _columnIndexOfAlbumName: Int = getColumnIndexOrThrow(_stmt, "albumName")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfArtistMbid: Int = getColumnIndexOrThrow(_stmt, "artistMbid")
        val _columnIndexOfTrackMbid: Int = getColumnIndexOrThrow(_stmt, "trackMbid")
        val _columnIndexOfAlbumMbid: Int = getColumnIndexOrThrow(_stmt, "albumMbid")
        val _columnIndexOfMatchConfidence: Int = getColumnIndexOrThrow(_stmt, "matchConfidence")
        val _columnIndexOfRawArtist: Int = getColumnIndexOrThrow(_stmt, "rawArtist")
        val _columnIndexOfRawTrack: Int = getColumnIndexOrThrow(_stmt, "rawTrack")
        val _columnIndexOfRawAlbum: Int = getColumnIndexOrThrow(_stmt, "rawAlbum")
        val _columnIndexOfSourceApp: Int = getColumnIndexOrThrow(_stmt, "sourceApp")
        val _columnIndexOfPlayedAt: Int = getColumnIndexOrThrow(_stmt, "playedAt")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfIsNowPlaying: Int = getColumnIndexOrThrow(_stmt, "isNowPlaying")
        val _result: MutableList<LocalScrobbleEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LocalScrobbleEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpTrackName: String
          _tmpTrackName = _stmt.getText(_columnIndexOfTrackName)
          val _tmpAlbumName: String
          _tmpAlbumName = _stmt.getText(_columnIndexOfAlbumName)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpArtistMbid: String?
          if (_stmt.isNull(_columnIndexOfArtistMbid)) {
            _tmpArtistMbid = null
          } else {
            _tmpArtistMbid = _stmt.getText(_columnIndexOfArtistMbid)
          }
          val _tmpTrackMbid: String?
          if (_stmt.isNull(_columnIndexOfTrackMbid)) {
            _tmpTrackMbid = null
          } else {
            _tmpTrackMbid = _stmt.getText(_columnIndexOfTrackMbid)
          }
          val _tmpAlbumMbid: String?
          if (_stmt.isNull(_columnIndexOfAlbumMbid)) {
            _tmpAlbumMbid = null
          } else {
            _tmpAlbumMbid = _stmt.getText(_columnIndexOfAlbumMbid)
          }
          val _tmpMatchConfidence: Float
          _tmpMatchConfidence = _stmt.getDouble(_columnIndexOfMatchConfidence).toFloat()
          val _tmpRawArtist: String
          _tmpRawArtist = _stmt.getText(_columnIndexOfRawArtist)
          val _tmpRawTrack: String
          _tmpRawTrack = _stmt.getText(_columnIndexOfRawTrack)
          val _tmpRawAlbum: String
          _tmpRawAlbum = _stmt.getText(_columnIndexOfRawAlbum)
          val _tmpSourceApp: String?
          if (_stmt.isNull(_columnIndexOfSourceApp)) {
            _tmpSourceApp = null
          } else {
            _tmpSourceApp = _stmt.getText(_columnIndexOfSourceApp)
          }
          val _tmpPlayedAt: Long
          _tmpPlayedAt = _stmt.getLong(_columnIndexOfPlayedAt)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpIsNowPlaying: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsNowPlaying).toInt()
          _tmpIsNowPlaying = _tmp != 0
          _item = LocalScrobbleEntity(_tmpId,_tmpUserId,_tmpArtistName,_tmpTrackName,_tmpAlbumName,_tmpImageUrl,_tmpArtistMbid,_tmpTrackMbid,_tmpAlbumMbid,_tmpMatchConfidence,_tmpRawArtist,_tmpRawTrack,_tmpRawAlbum,_tmpSourceApp,_tmpPlayedAt,_tmpDurationMs,_tmpIsNowPlaying)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeRecent(userId: String, limit: Int): Flow<List<LocalScrobbleEntity>> {
    val _sql: String = "SELECT * FROM local_scrobbles WHERE userId = ? ORDER BY playedAt DESC LIMIT ?"
    return createFlow(__db, false, arrayOf("local_scrobbles")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfTrackName: Int = getColumnIndexOrThrow(_stmt, "trackName")
        val _columnIndexOfAlbumName: Int = getColumnIndexOrThrow(_stmt, "albumName")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfArtistMbid: Int = getColumnIndexOrThrow(_stmt, "artistMbid")
        val _columnIndexOfTrackMbid: Int = getColumnIndexOrThrow(_stmt, "trackMbid")
        val _columnIndexOfAlbumMbid: Int = getColumnIndexOrThrow(_stmt, "albumMbid")
        val _columnIndexOfMatchConfidence: Int = getColumnIndexOrThrow(_stmt, "matchConfidence")
        val _columnIndexOfRawArtist: Int = getColumnIndexOrThrow(_stmt, "rawArtist")
        val _columnIndexOfRawTrack: Int = getColumnIndexOrThrow(_stmt, "rawTrack")
        val _columnIndexOfRawAlbum: Int = getColumnIndexOrThrow(_stmt, "rawAlbum")
        val _columnIndexOfSourceApp: Int = getColumnIndexOrThrow(_stmt, "sourceApp")
        val _columnIndexOfPlayedAt: Int = getColumnIndexOrThrow(_stmt, "playedAt")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfIsNowPlaying: Int = getColumnIndexOrThrow(_stmt, "isNowPlaying")
        val _result: MutableList<LocalScrobbleEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LocalScrobbleEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpTrackName: String
          _tmpTrackName = _stmt.getText(_columnIndexOfTrackName)
          val _tmpAlbumName: String
          _tmpAlbumName = _stmt.getText(_columnIndexOfAlbumName)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpArtistMbid: String?
          if (_stmt.isNull(_columnIndexOfArtistMbid)) {
            _tmpArtistMbid = null
          } else {
            _tmpArtistMbid = _stmt.getText(_columnIndexOfArtistMbid)
          }
          val _tmpTrackMbid: String?
          if (_stmt.isNull(_columnIndexOfTrackMbid)) {
            _tmpTrackMbid = null
          } else {
            _tmpTrackMbid = _stmt.getText(_columnIndexOfTrackMbid)
          }
          val _tmpAlbumMbid: String?
          if (_stmt.isNull(_columnIndexOfAlbumMbid)) {
            _tmpAlbumMbid = null
          } else {
            _tmpAlbumMbid = _stmt.getText(_columnIndexOfAlbumMbid)
          }
          val _tmpMatchConfidence: Float
          _tmpMatchConfidence = _stmt.getDouble(_columnIndexOfMatchConfidence).toFloat()
          val _tmpRawArtist: String
          _tmpRawArtist = _stmt.getText(_columnIndexOfRawArtist)
          val _tmpRawTrack: String
          _tmpRawTrack = _stmt.getText(_columnIndexOfRawTrack)
          val _tmpRawAlbum: String
          _tmpRawAlbum = _stmt.getText(_columnIndexOfRawAlbum)
          val _tmpSourceApp: String?
          if (_stmt.isNull(_columnIndexOfSourceApp)) {
            _tmpSourceApp = null
          } else {
            _tmpSourceApp = _stmt.getText(_columnIndexOfSourceApp)
          }
          val _tmpPlayedAt: Long
          _tmpPlayedAt = _stmt.getLong(_columnIndexOfPlayedAt)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpIsNowPlaying: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsNowPlaying).toInt()
          _tmpIsNowPlaying = _tmp != 0
          _item = LocalScrobbleEntity(_tmpId,_tmpUserId,_tmpArtistName,_tmpTrackName,_tmpAlbumName,_tmpImageUrl,_tmpArtistMbid,_tmpTrackMbid,_tmpAlbumMbid,_tmpMatchConfidence,_tmpRawArtist,_tmpRawTrack,_tmpRawAlbum,_tmpSourceApp,_tmpPlayedAt,_tmpDurationMs,_tmpIsNowPlaying)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getNowPlaying(userId: String): LocalScrobbleEntity? {
    val _sql: String = "SELECT * FROM local_scrobbles WHERE userId = ? AND isNowPlaying = 1 LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfTrackName: Int = getColumnIndexOrThrow(_stmt, "trackName")
        val _columnIndexOfAlbumName: Int = getColumnIndexOrThrow(_stmt, "albumName")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfArtistMbid: Int = getColumnIndexOrThrow(_stmt, "artistMbid")
        val _columnIndexOfTrackMbid: Int = getColumnIndexOrThrow(_stmt, "trackMbid")
        val _columnIndexOfAlbumMbid: Int = getColumnIndexOrThrow(_stmt, "albumMbid")
        val _columnIndexOfMatchConfidence: Int = getColumnIndexOrThrow(_stmt, "matchConfidence")
        val _columnIndexOfRawArtist: Int = getColumnIndexOrThrow(_stmt, "rawArtist")
        val _columnIndexOfRawTrack: Int = getColumnIndexOrThrow(_stmt, "rawTrack")
        val _columnIndexOfRawAlbum: Int = getColumnIndexOrThrow(_stmt, "rawAlbum")
        val _columnIndexOfSourceApp: Int = getColumnIndexOrThrow(_stmt, "sourceApp")
        val _columnIndexOfPlayedAt: Int = getColumnIndexOrThrow(_stmt, "playedAt")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfIsNowPlaying: Int = getColumnIndexOrThrow(_stmt, "isNowPlaying")
        val _result: LocalScrobbleEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpTrackName: String
          _tmpTrackName = _stmt.getText(_columnIndexOfTrackName)
          val _tmpAlbumName: String
          _tmpAlbumName = _stmt.getText(_columnIndexOfAlbumName)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpArtistMbid: String?
          if (_stmt.isNull(_columnIndexOfArtistMbid)) {
            _tmpArtistMbid = null
          } else {
            _tmpArtistMbid = _stmt.getText(_columnIndexOfArtistMbid)
          }
          val _tmpTrackMbid: String?
          if (_stmt.isNull(_columnIndexOfTrackMbid)) {
            _tmpTrackMbid = null
          } else {
            _tmpTrackMbid = _stmt.getText(_columnIndexOfTrackMbid)
          }
          val _tmpAlbumMbid: String?
          if (_stmt.isNull(_columnIndexOfAlbumMbid)) {
            _tmpAlbumMbid = null
          } else {
            _tmpAlbumMbid = _stmt.getText(_columnIndexOfAlbumMbid)
          }
          val _tmpMatchConfidence: Float
          _tmpMatchConfidence = _stmt.getDouble(_columnIndexOfMatchConfidence).toFloat()
          val _tmpRawArtist: String
          _tmpRawArtist = _stmt.getText(_columnIndexOfRawArtist)
          val _tmpRawTrack: String
          _tmpRawTrack = _stmt.getText(_columnIndexOfRawTrack)
          val _tmpRawAlbum: String
          _tmpRawAlbum = _stmt.getText(_columnIndexOfRawAlbum)
          val _tmpSourceApp: String?
          if (_stmt.isNull(_columnIndexOfSourceApp)) {
            _tmpSourceApp = null
          } else {
            _tmpSourceApp = _stmt.getText(_columnIndexOfSourceApp)
          }
          val _tmpPlayedAt: Long
          _tmpPlayedAt = _stmt.getLong(_columnIndexOfPlayedAt)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpIsNowPlaying: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsNowPlaying).toInt()
          _tmpIsNowPlaying = _tmp != 0
          _result = LocalScrobbleEntity(_tmpId,_tmpUserId,_tmpArtistName,_tmpTrackName,_tmpAlbumName,_tmpImageUrl,_tmpArtistMbid,_tmpTrackMbid,_tmpAlbumMbid,_tmpMatchConfidence,_tmpRawArtist,_tmpRawTrack,_tmpRawAlbum,_tmpSourceApp,_tmpPlayedAt,_tmpDurationMs,_tmpIsNowPlaying)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(userId: String): Int {
    val _sql: String = "SELECT COUNT(*) FROM local_scrobbles WHERE userId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSince(userId: String, sinceMs: Long): List<LocalScrobbleEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM local_scrobbles
        |        WHERE userId = ? AND playedAt >= ?
        |        ORDER BY playedAt DESC
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, sinceMs)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfTrackName: Int = getColumnIndexOrThrow(_stmt, "trackName")
        val _columnIndexOfAlbumName: Int = getColumnIndexOrThrow(_stmt, "albumName")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfArtistMbid: Int = getColumnIndexOrThrow(_stmt, "artistMbid")
        val _columnIndexOfTrackMbid: Int = getColumnIndexOrThrow(_stmt, "trackMbid")
        val _columnIndexOfAlbumMbid: Int = getColumnIndexOrThrow(_stmt, "albumMbid")
        val _columnIndexOfMatchConfidence: Int = getColumnIndexOrThrow(_stmt, "matchConfidence")
        val _columnIndexOfRawArtist: Int = getColumnIndexOrThrow(_stmt, "rawArtist")
        val _columnIndexOfRawTrack: Int = getColumnIndexOrThrow(_stmt, "rawTrack")
        val _columnIndexOfRawAlbum: Int = getColumnIndexOrThrow(_stmt, "rawAlbum")
        val _columnIndexOfSourceApp: Int = getColumnIndexOrThrow(_stmt, "sourceApp")
        val _columnIndexOfPlayedAt: Int = getColumnIndexOrThrow(_stmt, "playedAt")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfIsNowPlaying: Int = getColumnIndexOrThrow(_stmt, "isNowPlaying")
        val _result: MutableList<LocalScrobbleEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LocalScrobbleEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpTrackName: String
          _tmpTrackName = _stmt.getText(_columnIndexOfTrackName)
          val _tmpAlbumName: String
          _tmpAlbumName = _stmt.getText(_columnIndexOfAlbumName)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpArtistMbid: String?
          if (_stmt.isNull(_columnIndexOfArtistMbid)) {
            _tmpArtistMbid = null
          } else {
            _tmpArtistMbid = _stmt.getText(_columnIndexOfArtistMbid)
          }
          val _tmpTrackMbid: String?
          if (_stmt.isNull(_columnIndexOfTrackMbid)) {
            _tmpTrackMbid = null
          } else {
            _tmpTrackMbid = _stmt.getText(_columnIndexOfTrackMbid)
          }
          val _tmpAlbumMbid: String?
          if (_stmt.isNull(_columnIndexOfAlbumMbid)) {
            _tmpAlbumMbid = null
          } else {
            _tmpAlbumMbid = _stmt.getText(_columnIndexOfAlbumMbid)
          }
          val _tmpMatchConfidence: Float
          _tmpMatchConfidence = _stmt.getDouble(_columnIndexOfMatchConfidence).toFloat()
          val _tmpRawArtist: String
          _tmpRawArtist = _stmt.getText(_columnIndexOfRawArtist)
          val _tmpRawTrack: String
          _tmpRawTrack = _stmt.getText(_columnIndexOfRawTrack)
          val _tmpRawAlbum: String
          _tmpRawAlbum = _stmt.getText(_columnIndexOfRawAlbum)
          val _tmpSourceApp: String?
          if (_stmt.isNull(_columnIndexOfSourceApp)) {
            _tmpSourceApp = null
          } else {
            _tmpSourceApp = _stmt.getText(_columnIndexOfSourceApp)
          }
          val _tmpPlayedAt: Long
          _tmpPlayedAt = _stmt.getLong(_columnIndexOfPlayedAt)
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpIsNowPlaying: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsNowPlaying).toInt()
          _tmpIsNowPlaying = _tmp != 0
          _item = LocalScrobbleEntity(_tmpId,_tmpUserId,_tmpArtistName,_tmpTrackName,_tmpAlbumName,_tmpImageUrl,_tmpArtistMbid,_tmpTrackMbid,_tmpAlbumMbid,_tmpMatchConfidence,_tmpRawArtist,_tmpRawTrack,_tmpRawAlbum,_tmpSourceApp,_tmpPlayedAt,_tmpDurationMs,_tmpIsNowPlaying)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearNowPlaying(userId: String) {
    val _sql: String = "UPDATE local_scrobbles SET isNowPlaying = 0 WHERE userId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
