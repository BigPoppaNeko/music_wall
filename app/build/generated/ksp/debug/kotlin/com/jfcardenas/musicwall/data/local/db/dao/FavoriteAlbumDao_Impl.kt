package com.jfcardenas.musicwall.`data`.local.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.jfcardenas.musicwall.`data`.local.db.entity.FavoriteAlbum
import javax.`annotation`.processing.Generated
import kotlin.Boolean
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
public class FavoriteAlbumDao_Impl(
  __db: RoomDatabase,
) : FavoriteAlbumDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFavoriteAlbum: EntityInsertAdapter<FavoriteAlbum>
  init {
    this.__db = __db
    this.__insertAdapterOfFavoriteAlbum = object : EntityInsertAdapter<FavoriteAlbum>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `favorite_albums` (`id`,`albumName`,`artistName`,`imageUrl`,`mbid`,`savedAt`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FavoriteAlbum) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.albumName)
        statement.bindText(3, entity.artistName)
        val _tmpImageUrl: String? = entity.imageUrl
        if (_tmpImageUrl == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpImageUrl)
        }
        val _tmpMbid: String? = entity.mbid
        if (_tmpMbid == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpMbid)
        }
        statement.bindLong(6, entity.savedAt)
      }
    }
  }

  public override suspend fun insert(album: FavoriteAlbum): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfFavoriteAlbum.insert(_connection, album)
  }

  public override suspend fun getAll(): List<FavoriteAlbum> {
    val _sql: String = "SELECT * FROM favorite_albums ORDER BY savedAt DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAlbumName: Int = getColumnIndexOrThrow(_stmt, "albumName")
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfImageUrl: Int = getColumnIndexOrThrow(_stmt, "imageUrl")
        val _columnIndexOfMbid: Int = getColumnIndexOrThrow(_stmt, "mbid")
        val _columnIndexOfSavedAt: Int = getColumnIndexOrThrow(_stmt, "savedAt")
        val _result: MutableList<FavoriteAlbum> = mutableListOf()
        while (_stmt.step()) {
          val _item: FavoriteAlbum
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpAlbumName: String
          _tmpAlbumName = _stmt.getText(_columnIndexOfAlbumName)
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpImageUrl: String?
          if (_stmt.isNull(_columnIndexOfImageUrl)) {
            _tmpImageUrl = null
          } else {
            _tmpImageUrl = _stmt.getText(_columnIndexOfImageUrl)
          }
          val _tmpMbid: String?
          if (_stmt.isNull(_columnIndexOfMbid)) {
            _tmpMbid = null
          } else {
            _tmpMbid = _stmt.getText(_columnIndexOfMbid)
          }
          val _tmpSavedAt: Long
          _tmpSavedAt = _stmt.getLong(_columnIndexOfSavedAt)
          _item = FavoriteAlbum(_tmpId,_tmpAlbumName,_tmpArtistName,_tmpImageUrl,_tmpMbid,_tmpSavedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun isFavorite(id: String): Boolean {
    val _sql: String = "SELECT EXISTS(SELECT 1 FROM favorite_albums WHERE id = ?)"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM favorite_albums WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
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
