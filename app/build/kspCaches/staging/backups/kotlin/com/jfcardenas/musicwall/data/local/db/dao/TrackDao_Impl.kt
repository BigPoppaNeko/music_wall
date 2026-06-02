package com.jfcardenas.musicwall.`data`.local.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.jfcardenas.musicwall.`data`.local.db.entity.TrackEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TrackDao_Impl(
  __db: RoomDatabase,
) : TrackDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTrackEntity: EntityInsertAdapter<TrackEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfTrackEntity = object : EntityInsertAdapter<TrackEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `tracks` (`id`,`username`,`period`,`kind`,`trackName`,`artistName`,`albumName`,`imageUrl`,`rank`,`playcount`,`mbid`,`lastFmUrl`,`fetchedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TrackEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.username)
        statement.bindText(3, entity.period)
        statement.bindText(4, entity.kind)
        statement.bindText(5, entity.trackName)
        statement.bindText(6, entity.artistName)
        statement.bindText(7, entity.albumName)
        statement.bindText(8, entity.imageUrl)
        statement.bindLong(9, entity.rank.toLong())
        statement.bindLong(10, entity.playcount.toLong())
        val _tmpMbid: String? = entity.mbid
        if (_tmpMbid == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpMbid)
        }
        val _tmpLastFmUrl: String? = entity.lastFmUrl
        if (_tmpLastFmUrl == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpLastFmUrl)
        }
        statement.bindLong(13, entity.fetchedAt)
      }
    }
  }

  public override suspend fun insertAll(tracks: List<TrackEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTrackEntity.insert(_connection, tracks)
  }

  public override suspend fun `get`(
    username: String,
    period: String,
    kind: String,
    limit: Int,
  ): List<TrackEntity> {
    val _sql: String = "SELECT * FROM tracks WHERE username = ? AND period = ? AND kind = ? ORDER BY rank ASC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, username)
        _argIndex = 2
        _stmt.bindText(_argIndex, period)
        _argIndex = 3
        _stmt.bindText(_argIndex, kind)
        _argIndex = 4
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _columnIndexOfPeriod: Int = getColumnIndexOrThrow(_stmt, "period")
        val _columnIndexOfKind: Int = getColumnIndexOrThrow(_stmt, "kind")
        val _columnIndexOfTrackName: Int = getColumnIndexOrThrow(_stmt, "trackName")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfAlbumName: Int = getColumnIndexOrThrow(_stmt, "albumName")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfPlaycount: Int = getColumnIndexOrThrow(_stmt, "playcount")
        val _columnIndexOfMbid: Int = getColumnIndexOrThrow(_stmt, "mbid")
        val _columnIndexOfLastFmUrl: Int = getColumnIndexOrThrow(_stmt, "lastFmUrl")
        val _columnIndexOfFetchedAt: Int = getColumnIndexOrThrow(_stmt, "fetchedAt")
        val _result: MutableList<TrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TrackEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpPeriod: String
          _tmpPeriod = _stmt.getText(_columnIndexOfPeriod)
          val _tmpKind: String
          _tmpKind = _stmt.getText(_columnIndexOfKind)
          val _tmpTrackName: String
          _tmpTrackName = _stmt.getText(_columnIndexOfTrackName)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpAlbumName: String
          _tmpAlbumName = _stmt.getText(_columnIndexOfAlbumName)
          val _tmpImageUrl: String
          _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          val _tmpRank: Int
          _tmpRank = _stmt.getLong(_columnIndexOfRank).toInt()
          val _tmpPlaycount: Int
          _tmpPlaycount = _stmt.getLong(_columnIndexOfPlaycount).toInt()
          val _tmpMbid: String?
          if (_stmt.isNull(_columnIndexOfMbid)) {
            _tmpMbid = null
          } else {
            _tmpMbid = _stmt.getText(_columnIndexOfMbid)
          }
          val _tmpLastFmUrl: String?
          if (_stmt.isNull(_columnIndexOfLastFmUrl)) {
            _tmpLastFmUrl = null
          } else {
            _tmpLastFmUrl = _stmt.getText(_columnIndexOfLastFmUrl)
          }
          val _tmpFetchedAt: Long
          _tmpFetchedAt = _stmt.getLong(_columnIndexOfFetchedAt)
          _item = TrackEntity(_tmpId,_tmpUsername,_tmpPeriod,_tmpKind,_tmpTrackName,_tmpArtistName,_tmpAlbumName,_tmpImageUrl,_tmpRank,_tmpPlaycount,_tmpMbid,_tmpLastFmUrl,_tmpFetchedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun oldestFetchTime(
    username: String,
    period: String,
    kind: String,
  ): Long? {
    val _sql: String = "SELECT MIN(fetchedAt) FROM tracks WHERE username = ? AND period = ? AND kind = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, username)
        _argIndex = 2
        _stmt.bindText(_argIndex, period)
        _argIndex = 3
        _stmt.bindText(_argIndex, kind)
        val _result: Long?
        if (_stmt.step()) {
          val _tmp: Long?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0)
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteForUserPeriodKind(
    username: String,
    period: String,
    kind: String,
  ) {
    val _sql: String = "DELETE FROM tracks WHERE username = ? AND period = ? AND kind = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, username)
        _argIndex = 2
        _stmt.bindText(_argIndex, period)
        _argIndex = 3
        _stmt.bindText(_argIndex, kind)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteOlderThan(before: Long) {
    val _sql: String = "DELETE FROM tracks WHERE fetchedAt < ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, before)
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
