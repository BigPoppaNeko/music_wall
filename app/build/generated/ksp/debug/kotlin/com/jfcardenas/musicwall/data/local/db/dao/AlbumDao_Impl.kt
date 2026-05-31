package com.jfcardenas.musicwall.`data`.local.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.jfcardenas.musicwall.`data`.local.db.entity.AlbumEntity
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
public class AlbumDao_Impl(
  __db: RoomDatabase,
) : AlbumDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfAlbumEntity: EntityInsertAdapter<AlbumEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfAlbumEntity = object : EntityInsertAdapter<AlbumEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `albums` (`id`,`username`,`period`,`albumName`,`artistName`,`imageUrl`,`rank`,`playcount`,`mbid`,`lastFmUrl`,`fetchedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AlbumEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.username)
        statement.bindText(3, entity.period)
        statement.bindText(4, entity.albumName)
        statement.bindText(5, entity.artistName)
        statement.bindText(6, entity.imageUrl)
        statement.bindLong(7, entity.rank.toLong())
        statement.bindLong(8, entity.playcount.toLong())
        val _tmpMbid: String? = entity.mbid
        if (_tmpMbid == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpMbid)
        }
        val _tmpLastFmUrl: String? = entity.lastFmUrl
        if (_tmpLastFmUrl == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpLastFmUrl)
        }
        statement.bindLong(11, entity.fetchedAt)
      }
    }
  }

  public override suspend fun insertAll(albums: List<AlbumEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfAlbumEntity.insert(_connection, albums)
  }

  public override suspend fun `get`(
    username: String,
    period: String,
    limit: Int,
  ): List<AlbumEntity> {
    val _sql: String = "SELECT * FROM albums WHERE username = ? AND period = ? ORDER BY rank ASC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, username)
        _argIndex = 2
        _stmt.bindText(_argIndex, period)
        _argIndex = 3
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _columnIndexOfPeriod: Int = getColumnIndexOrThrow(_stmt, "period")
        val _columnIndexOfAlbumName: Int = getColumnIndexOrThrow(_stmt, "albumName")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfPlaycount: Int = getColumnIndexOrThrow(_stmt, "playcount")
        val _columnIndexOfMbid: Int = getColumnIndexOrThrow(_stmt, "mbid")
        val _columnIndexOfLastFmUrl: Int = getColumnIndexOrThrow(_stmt, "lastFmUrl")
        val _columnIndexOfFetchedAt: Int = getColumnIndexOrThrow(_stmt, "fetchedAt")
        val _result: MutableList<AlbumEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AlbumEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpPeriod: String
          _tmpPeriod = _stmt.getText(_columnIndexOfPeriod)
          val _tmpAlbumName: String
          _tmpAlbumName = _stmt.getText(_columnIndexOfAlbumName)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
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
          _item = AlbumEntity(_tmpId,_tmpUsername,_tmpPeriod,_tmpAlbumName,_tmpArtistName,_tmpImageUrl,_tmpRank,_tmpPlaycount,_tmpMbid,_tmpLastFmUrl,_tmpFetchedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun oldestFetchTime(username: String, period: String): Long? {
    val _sql: String = "SELECT MIN(fetchedAt) FROM albums WHERE username = ? AND period = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, username)
        _argIndex = 2
        _stmt.bindText(_argIndex, period)
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

  public override suspend fun getRandomWithImage(limit: Int): List<AlbumEntity> {
    val _sql: String = "SELECT * FROM albums WHERE imageUrl != '' ORDER BY RANDOM() LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _columnIndexOfPeriod: Int = getColumnIndexOrThrow(_stmt, "period")
        val _columnIndexOfAlbumName: Int = getColumnIndexOrThrow(_stmt, "albumName")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfPlaycount: Int = getColumnIndexOrThrow(_stmt, "playcount")
        val _columnIndexOfMbid: Int = getColumnIndexOrThrow(_stmt, "mbid")
        val _columnIndexOfLastFmUrl: Int = getColumnIndexOrThrow(_stmt, "lastFmUrl")
        val _columnIndexOfFetchedAt: Int = getColumnIndexOrThrow(_stmt, "fetchedAt")
        val _result: MutableList<AlbumEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AlbumEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpPeriod: String
          _tmpPeriod = _stmt.getText(_columnIndexOfPeriod)
          val _tmpAlbumName: String
          _tmpAlbumName = _stmt.getText(_columnIndexOfAlbumName)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
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
          _item = AlbumEntity(_tmpId,_tmpUsername,_tmpPeriod,_tmpAlbumName,_tmpArtistName,_tmpImageUrl,_tmpRank,_tmpPlaycount,_tmpMbid,_tmpLastFmUrl,_tmpFetchedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteForUserPeriod(username: String, period: String) {
    val _sql: String = "DELETE FROM albums WHERE username = ? AND period = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, username)
        _argIndex = 2
        _stmt.bindText(_argIndex, period)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteOlderThan(before: Long) {
    val _sql: String = "DELETE FROM albums WHERE fetchedAt < ?"
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

  public override suspend fun updateImageUrlIfEmpty(
    albumName: String,
    artistName: String,
    imageUrl: String,
  ) {
    val _sql: String = "UPDATE albums SET imageUrl = ? WHERE albumName = ? AND artistName = ? AND imageUrl = ''"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, imageUrl)
        _argIndex = 2
        _stmt.bindText(_argIndex, albumName)
        _argIndex = 3
        _stmt.bindText(_argIndex, artistName)
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
