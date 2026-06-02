package com.jfcardenas.musicwall.`data`.local.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.jfcardenas.musicwall.`data`.local.db.entity.LfMatchCacheEntity
import javax.`annotation`.processing.Generated
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class LfMatchCacheDao_Impl(
  __db: RoomDatabase,
) : LfMatchCacheDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfLfMatchCacheEntity: EntityInsertAdapter<LfMatchCacheEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfLfMatchCacheEntity = object : EntityInsertAdapter<LfMatchCacheEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `lf_match_cache` (`cacheKey`,`artistName`,`trackName`,`albumName`,`imageUrl`,`artistMbid`,`trackMbid`,`albumMbid`,`matchConfidence`,`cachedAt`) VALUES (?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: LfMatchCacheEntity) {
        statement.bindText(1, entity.cacheKey)
        statement.bindText(2, entity.artistName)
        statement.bindText(3, entity.trackName)
        statement.bindText(4, entity.albumName)
        val _tmpImageUrl: String? = entity.imageUrl
        if (_tmpImageUrl == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpImageUrl)
        }
        val _tmpArtistMbid: String? = entity.artistMbid
        if (_tmpArtistMbid == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpArtistMbid)
        }
        val _tmpTrackMbid: String? = entity.trackMbid
        if (_tmpTrackMbid == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpTrackMbid)
        }
        val _tmpAlbumMbid: String? = entity.albumMbid
        if (_tmpAlbumMbid == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpAlbumMbid)
        }
        statement.bindDouble(9, entity.matchConfidence.toDouble())
        statement.bindLong(10, entity.cachedAt)
      }
    }
  }

  public override suspend fun insert(entry: LfMatchCacheEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfLfMatchCacheEntity.insert(_connection, entry)
  }

  public override suspend fun `get`(key: String): LfMatchCacheEntity? {
    val _sql: String = "SELECT * FROM lf_match_cache WHERE cacheKey = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, key)
        val _columnIndexOfCacheKey: Int = getColumnIndexOrThrow(_stmt, "cacheKey")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfTrackName: Int = getColumnIndexOrThrow(_stmt, "trackName")
        val _columnIndexOfAlbumName: Int = getColumnIndexOrThrow(_stmt, "albumName")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfArtistMbid: Int = getColumnIndexOrThrow(_stmt, "artistMbid")
        val _columnIndexOfTrackMbid: Int = getColumnIndexOrThrow(_stmt, "trackMbid")
        val _columnIndexOfAlbumMbid: Int = getColumnIndexOrThrow(_stmt, "albumMbid")
        val _columnIndexOfMatchConfidence: Int = getColumnIndexOrThrow(_stmt, "matchConfidence")
        val _columnIndexOfCachedAt: Int = getColumnIndexOrThrow(_stmt, "cachedAt")
        val _result: LfMatchCacheEntity?
        if (_stmt.step()) {
          val _tmpCacheKey: String
          _tmpCacheKey = _stmt.getText(_columnIndexOfCacheKey)
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
          val _tmpCachedAt: Long
          _tmpCachedAt = _stmt.getLong(_columnIndexOfCachedAt)
          _result = LfMatchCacheEntity(_tmpCacheKey,_tmpArtistName,_tmpTrackName,_tmpAlbumName,_tmpImageUrl,_tmpArtistMbid,_tmpTrackMbid,_tmpAlbumMbid,_tmpMatchConfidence,_tmpCachedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun evictOlderThan(beforeMs: Long) {
    val _sql: String = "DELETE FROM lf_match_cache WHERE cachedAt < ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, beforeMs)
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
