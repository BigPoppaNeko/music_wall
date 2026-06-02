package com.jfcardenas.musicwall.`data`.local.db.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.jfcardenas.musicwall.`data`.local.db.entity.MuralRecord
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
public class MuralDao_Impl(
  __db: RoomDatabase,
) : MuralDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMuralRecord: EntityInsertAdapter<MuralRecord>

  private val __deleteAdapterOfMuralRecord: EntityDeleteOrUpdateAdapter<MuralRecord>
  init {
    this.__db = __db
    this.__insertAdapterOfMuralRecord = object : EntityInsertAdapter<MuralRecord>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `mural_history` (`id`,`styleId`,`filePath`,`createdAt`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MuralRecord) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.styleId)
        statement.bindText(3, entity.filePath)
        statement.bindLong(4, entity.createdAt)
      }
    }
    this.__deleteAdapterOfMuralRecord = object : EntityDeleteOrUpdateAdapter<MuralRecord>() {
      protected override fun createQuery(): String = "DELETE FROM `mural_history` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MuralRecord) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insert(mural: MuralRecord): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfMuralRecord.insert(_connection, mural)
  }

  public override suspend fun delete(mural: MuralRecord): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfMuralRecord.handle(_connection, mural)
  }

  public override suspend fun getAll(): List<MuralRecord> {
    val _sql: String = "SELECT * FROM mural_history ORDER BY createdAt DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfStyleId: Int = getColumnIndexOrThrow(_stmt, "styleId")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<MuralRecord> = mutableListOf()
        while (_stmt.step()) {
          val _item: MuralRecord
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpStyleId: String
          _tmpStyleId = _stmt.getText(_columnIndexOfStyleId)
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = MuralRecord(_tmpId,_tmpStyleId,_tmpFilePath,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM mural_history"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
