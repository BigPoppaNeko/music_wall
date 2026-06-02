package com.jfcardenas.musicwall.`data`.local.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.jfcardenas.musicwall.`data`.local.db.entity.VetoedAlbum
import javax.`annotation`.processing.Generated
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class VetoedAlbumDao_Impl(
  __db: RoomDatabase,
) : VetoedAlbumDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfVetoedAlbum: EntityInsertAdapter<VetoedAlbum>
  init {
    this.__db = __db
    this.__insertAdapterOfVetoedAlbum = object : EntityInsertAdapter<VetoedAlbum>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `vetoed_albums` (`id`,`albumName`,`artistName`,`vetoedAt`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: VetoedAlbum) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.albumName)
        statement.bindText(3, entity.artistName)
        statement.bindLong(4, entity.vetoedAt)
      }
    }
  }

  public override suspend fun insert(album: VetoedAlbum): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfVetoedAlbum.insert(_connection, album)
  }

  public override suspend fun getAllIds(): List<String> {
    val _sql: String = "SELECT id FROM vetoed_albums"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
