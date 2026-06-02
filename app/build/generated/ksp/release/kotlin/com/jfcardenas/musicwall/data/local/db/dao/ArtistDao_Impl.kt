package com.jfcardenas.musicwall.`data`.local.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.jfcardenas.musicwall.`data`.local.db.entity.ArtistEntity
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
public class ArtistDao_Impl(
  __db: RoomDatabase,
) : ArtistDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfArtistEntity: EntityInsertAdapter<ArtistEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfArtistEntity = object : EntityInsertAdapter<ArtistEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `artists` (`id`,`username`,`period`,`artistName`,`imageUrl`,`rank`,`playcount`,`mbid`,`lastFmUrl`,`fetchedAt`) VALUES (?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ArtistEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.username)
        statement.bindText(3, entity.period)
        statement.bindText(4, entity.artistName)
        statement.bindText(5, entity.imageUrl)
        statement.bindLong(6, entity.rank.toLong())
        statement.bindLong(7, entity.playcount.toLong())
        val _tmpMbid: String? = entity.mbid
        if (_tmpMbid == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpMbid)
        }
        val _tmpLastFmUrl: String? = entity.lastFmUrl
        if (_tmpLastFmUrl == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpLastFmUrl)
        }
        statement.bindLong(10, entity.fetchedAt)
      }
    }
  }

  public override suspend fun insertAll(artists: List<ArtistEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfArtistEntity.insert(_connection, artists)
  }

  public override suspend fun `get`(
    username: String,
    period: String,
    limit: Int,
  ): List<ArtistEntity> {
    val _sql: String = "SELECT * FROM artists WHERE username = ? AND period = ? ORDER BY rank ASC LIMIT ?"
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
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfPlaycount: Int = getColumnIndexOrThrow(_stmt, "playcount")
        val _columnIndexOfMbid: Int = getColumnIndexOrThrow(_stmt, "mbid")
        val _columnIndexOfLastFmUrl: Int = getColumnIndexOrThrow(_stmt, "lastFmUrl")
        val _columnIndexOfFetchedAt: Int = getColumnIndexOrThrow(_stmt, "fetchedAt")
        val _result: MutableList<ArtistEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ArtistEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpPeriod: String
          _tmpPeriod = _stmt.getText(_columnIndexOfPeriod)
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
          _item = ArtistEntity(_tmpId,_tmpUsername,_tmpPeriod,_tmpArtistName,_tmpImageUrl,_tmpRank,_tmpPlaycount,_tmpMbid,_tmpLastFmUrl,_tmpFetchedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun oldestFetchTime(username: String, period: String): Long? {
    val _sql: String = "SELECT MIN(fetchedAt) FROM artists WHERE username = ? AND period = ?"
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

  public override suspend fun deleteForUserPeriod(username: String, period: String) {
    val _sql: String = "DELETE FROM artists WHERE username = ? AND period = ?"
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
    val _sql: String = "DELETE FROM artists WHERE fetchedAt < ?"
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
