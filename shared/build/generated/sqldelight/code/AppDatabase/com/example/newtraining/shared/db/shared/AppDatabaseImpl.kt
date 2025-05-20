package com.example.newtraining.shared.db.shared

import com.example.newtraining.shared.db.AppDatabase
import com.example.newtraining.shared.db.AppDatabaseQueries
import com.example.newtraining.shared.db.Item
import com.example.newtraining.shared.db.ItemQueries
import com.example.newtraining.shared.db.Player
import com.example.newtraining.shared.db.PlayerQueries
import com.example.newtraining.shared.db.Sale
import com.example.newtraining.shared.db.SaleQueries
import com.squareup.sqldelight.Query
import com.squareup.sqldelight.TransacterImpl
import com.squareup.sqldelight.`internal`.copyOnWriteList
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Unit
import kotlin.collections.MutableList
import kotlin.reflect.KClass

internal val KClass<AppDatabase>.schema: SqlDriver.Schema
  get() = AppDatabaseImpl.Schema

internal fun KClass<AppDatabase>.newInstance(driver: SqlDriver): AppDatabase =
    AppDatabaseImpl(driver)

private class AppDatabaseImpl(
  driver: SqlDriver
) : TransacterImpl(driver), AppDatabase {
  public override val appDatabaseQueries: AppDatabaseQueriesImpl = AppDatabaseQueriesImpl(this,
      driver)

  public override val itemQueries: ItemQueriesImpl = ItemQueriesImpl(this, driver)

  public override val playerQueries: PlayerQueriesImpl = PlayerQueriesImpl(this, driver)

  public override val saleQueries: SaleQueriesImpl = SaleQueriesImpl(this, driver)

  public object Schema : SqlDriver.Schema {
    public override val version: Int
      get() = 1

    public override fun create(driver: SqlDriver): Unit {
      driver.execute(null, """
          |CREATE TABLE Item (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    name TEXT NOT NULL,
          |    price REAL NOT NULL,
          |    type TEXT NOT NULL,
          |    sellPrice REAL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE Player (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    uniqueId TEXT NOT NULL,
          |    fullName TEXT NOT NULL,
          |    age INTEGER NOT NULL,
          |    height INTEGER NOT NULL,
          |    gender TEXT NOT NULL,
          |    medicalCondition TEXT NOT NULL,
          |    pictureUri TEXT
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE Sale (
          |    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
          |    itemId INTEGER NOT NULL,
          |    playerId INTEGER NOT NULL,
          |    price REAL NOT NULL,
          |    discount REAL NOT NULL DEFAULT 0.0,
          |    paid REAL NOT NULL DEFAULT 0.0,
          |    debt REAL NOT NULL DEFAULT 0.0,
          |    date INTEGER NOT NULL,
          |    FOREIGN KEY(itemId) REFERENCES Item(id),
          |    FOREIGN KEY(playerId) REFERENCES Player(id)
          |)
          """.trimMargin(), 0)
    }

    public override fun migrate(
      driver: SqlDriver,
      oldVersion: Int,
      newVersion: Int
    ): Unit {
    }
  }
}

private class AppDatabaseQueriesImpl(
  private val database: AppDatabaseImpl,
  private val driver: SqlDriver
) : TransacterImpl(driver), AppDatabaseQueries

private class ItemQueriesImpl(
  private val database: AppDatabaseImpl,
  private val driver: SqlDriver
) : TransacterImpl(driver), ItemQueries {
  internal val selectAll: MutableList<Query<*>> = copyOnWriteList()

  internal val selectById: MutableList<Query<*>> = copyOnWriteList()

  internal val lastInsertRowId: MutableList<Query<*>> = copyOnWriteList()

  public override fun <T : Any> selectAll(mapper: (
    id: Long,
    name: String,
    price: Double,
    type: String,
    sellPrice: Double?
  ) -> T): Query<T> = Query(1720388940, selectAll, driver, "Item.sq", "selectAll",
      "SELECT * FROM Item") { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getDouble(2)!!,
      cursor.getString(3)!!,
      cursor.getDouble(4)
    )
  }

  public override fun selectAll(): Query<Item> = selectAll { id, name, price, type, sellPrice ->
    Item(
      id,
      name,
      price,
      type,
      sellPrice
    )
  }

  public override fun <T : Any> selectById(id: Long, mapper: (
    id: Long,
    name: String,
    price: Double,
    type: String,
    sellPrice: Double?
  ) -> T): Query<T> = SelectByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getDouble(2)!!,
      cursor.getString(3)!!,
      cursor.getDouble(4)
    )
  }

  public override fun selectById(id: Long): Query<Item> = selectById(id) { id_, name, price, type,
      sellPrice ->
    Item(
      id_,
      name,
      price,
      type,
      sellPrice
    )
  }

  public override fun lastInsertRowId(): Query<Long> = Query(-1048598035, lastInsertRowId, driver,
      "Item.sq", "lastInsertRowId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  public override fun insertItem(
    name: String,
    price: Double,
    type: String,
    sellPrice: Double?
  ): Unit {
    driver.execute(119849989, """
    |INSERT INTO Item (name, price, type, sellPrice)
    |VALUES (?, ?, ?, ?)
    """.trimMargin(), 4) {
      bindString(1, name)
      bindDouble(2, price)
      bindString(3, type)
      bindDouble(4, sellPrice)
    }
    notifyQueries(119849989, {database.itemQueries.selectAll + database.itemQueries.selectById})
  }

  public override fun updateItem(
    name: String,
    price: Double,
    type: String,
    sellPrice: Double?,
    id: Long
  ): Unit {
    driver.execute(-1142246891, """
    |UPDATE Item SET name = ?, price = ?, type = ?, sellPrice = ?
    |WHERE id = ?
    """.trimMargin(), 5) {
      bindString(1, name)
      bindDouble(2, price)
      bindString(3, type)
      bindDouble(4, sellPrice)
      bindLong(5, id)
    }
    notifyQueries(-1142246891, {database.itemQueries.selectAll + database.itemQueries.selectById})
  }

  public override fun deleteById(id: Long): Unit {
    driver.execute(918068918, """DELETE FROM Item WHERE id = ?""", 1) {
      bindLong(1, id)
    }
    notifyQueries(918068918, {database.itemQueries.selectAll + database.itemQueries.selectById})
  }

  private inner class SelectByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T
  ) : Query<T>(selectById, mapper) {
    public override fun execute(): SqlCursor = driver.executeQuery(1792490887,
        """SELECT * FROM Item WHERE id = ?""", 1) {
      bindLong(1, id)
    }

    public override fun toString(): String = "Item.sq:selectById"
  }
}

private class PlayerQueriesImpl(
  private val database: AppDatabaseImpl,
  private val driver: SqlDriver
) : TransacterImpl(driver), PlayerQueries {
  internal val playerSelectAll: MutableList<Query<*>> = copyOnWriteList()

  internal val playerSelectById: MutableList<Query<*>> = copyOnWriteList()

  internal val lastInsertRowId: MutableList<Query<*>> = copyOnWriteList()

  public override fun <T : Any> playerSelectAll(mapper: (
    id: Long,
    uniqueId: String,
    fullName: String,
    age: Long,
    height: Long,
    gender: String,
    medicalCondition: String,
    pictureUri: String?
  ) -> T): Query<T> = Query(1370158493, playerSelectAll, driver, "Player.sq", "playerSelectAll",
      "SELECT * FROM Player") { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getLong(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)
    )
  }

  public override fun playerSelectAll(): Query<Player> = playerSelectAll { id, uniqueId, fullName,
      age, height, gender, medicalCondition, pictureUri ->
    Player(
      id,
      uniqueId,
      fullName,
      age,
      height,
      gender,
      medicalCondition,
      pictureUri
    )
  }

  public override fun <T : Any> playerSelectById(id: Long, mapper: (
    id: Long,
    uniqueId: String,
    fullName: String,
    age: Long,
    height: Long,
    gender: String,
    medicalCondition: String,
    pictureUri: String?
  ) -> T): Query<T> = PlayerSelectByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getLong(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getString(7)
    )
  }

  public override fun playerSelectById(id: Long): Query<Player> = playerSelectById(id) { id_,
      uniqueId, fullName, age, height, gender, medicalCondition, pictureUri ->
    Player(
      id_,
      uniqueId,
      fullName,
      age,
      height,
      gender,
      medicalCondition,
      pictureUri
    )
  }

  public override fun lastInsertRowId(): Query<Long> = Query(-1729793697, lastInsertRowId, driver,
      "Player.sq", "lastInsertRowId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  public override fun playerInsert(
    uniqueId: String,
    fullName: String,
    age: Long,
    height: Long,
    gender: String,
    medicalCondition: String,
    pictureUri: String?
  ): Unit {
    driver.execute(-664965343, """
    |INSERT INTO Player (uniqueId, fullName, age, height, gender, medicalCondition, pictureUri)
    |VALUES (?, ?, ?, ?, ?, ?, ?)
    """.trimMargin(), 7) {
      bindString(1, uniqueId)
      bindString(2, fullName)
      bindLong(3, age)
      bindLong(4, height)
      bindString(5, gender)
      bindString(6, medicalCondition)
      bindString(7, pictureUri)
    }
    notifyQueries(-664965343, {database.playerQueries.playerSelectById +
        database.playerQueries.playerSelectAll})
  }

  public override fun playerUpdate(
    uniqueId: String,
    fullName: String,
    age: Long,
    height: Long,
    gender: String,
    medicalCondition: String,
    pictureUri: String?,
    id: Long
  ): Unit {
    driver.execute(-320019151, """
    |UPDATE Player SET uniqueId = ?, fullName = ?, age = ?, height = ?, gender = ?, medicalCondition = ?, pictureUri = ?
    |WHERE id = ?
    """.trimMargin(), 8) {
      bindString(1, uniqueId)
      bindString(2, fullName)
      bindLong(3, age)
      bindLong(4, height)
      bindString(5, gender)
      bindString(6, medicalCondition)
      bindString(7, pictureUri)
      bindLong(8, id)
    }
    notifyQueries(-320019151, {database.playerQueries.playerSelectById +
        database.playerQueries.playerSelectAll})
  }

  public override fun playerDeleteById(id: Long): Unit {
    driver.execute(-1349140347, """DELETE FROM Player WHERE id = ?""", 1) {
      bindLong(1, id)
    }
    notifyQueries(-1349140347, {database.playerQueries.playerSelectById +
        database.playerQueries.playerSelectAll})
  }

  private inner class PlayerSelectByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T
  ) : Query<T>(playerSelectById, mapper) {
    public override fun execute(): SqlCursor = driver.executeQuery(-474718378,
        """SELECT * FROM Player WHERE id = ?""", 1) {
      bindLong(1, id)
    }

    public override fun toString(): String = "Player.sq:playerSelectById"
  }
}

private class SaleQueriesImpl(
  private val database: AppDatabaseImpl,
  private val driver: SqlDriver
) : TransacterImpl(driver), SaleQueries {
  internal val selectAll: MutableList<Query<*>> = copyOnWriteList()

  internal val selectById: MutableList<Query<*>> = copyOnWriteList()

  internal val selectByPlayerId: MutableList<Query<*>> = copyOnWriteList()

  internal val lastInsertRowId: MutableList<Query<*>> = copyOnWriteList()

  public override fun <T : Any> selectAll(mapper: (
    id: Long,
    itemId: Long,
    playerId: Long,
    price: Double,
    discount: Double,
    paid: Double,
    debt: Double,
    date: Long
  ) -> T): Query<T> = Query(896261272, selectAll, driver, "Sale.sq", "selectAll",
      "SELECT * FROM Sale") { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3)!!,
      cursor.getDouble(4)!!,
      cursor.getDouble(5)!!,
      cursor.getDouble(6)!!,
      cursor.getLong(7)!!
    )
  }

  public override fun selectAll(): Query<Sale> = selectAll { id, itemId, playerId, price, discount,
      paid, debt, date ->
    Sale(
      id,
      itemId,
      playerId,
      price,
      discount,
      paid,
      debt,
      date
    )
  }

  public override fun <T : Any> selectById(id: Long, mapper: (
    id: Long,
    itemId: Long,
    playerId: Long,
    price: Double,
    discount: Double,
    paid: Double,
    debt: Double,
    date: Long
  ) -> T): Query<T> = SelectByIdQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3)!!,
      cursor.getDouble(4)!!,
      cursor.getDouble(5)!!,
      cursor.getDouble(6)!!,
      cursor.getLong(7)!!
    )
  }

  public override fun selectById(id: Long): Query<Sale> = selectById(id) { id_, itemId, playerId,
      price, discount, paid, debt, date ->
    Sale(
      id_,
      itemId,
      playerId,
      price,
      discount,
      paid,
      debt,
      date
    )
  }

  public override fun <T : Any> selectByPlayerId(playerId: Long, mapper: (
    id: Long,
    itemId: Long,
    playerId: Long,
    price: Double,
    discount: Double,
    paid: Double,
    debt: Double,
    date: Long
  ) -> T): Query<T> = SelectByPlayerIdQuery(playerId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getLong(2)!!,
      cursor.getDouble(3)!!,
      cursor.getDouble(4)!!,
      cursor.getDouble(5)!!,
      cursor.getDouble(6)!!,
      cursor.getLong(7)!!
    )
  }

  public override fun selectByPlayerId(playerId: Long): Query<Sale> = selectByPlayerId(playerId) {
      id, itemId, playerId_, price, discount, paid, debt, date ->
    Sale(
      id,
      itemId,
      playerId_,
      price,
      discount,
      paid,
      debt,
      date
    )
  }

  public override fun lastInsertRowId(): Query<Long> = Query(-962408391, lastInsertRowId, driver,
      "Sale.sq", "lastInsertRowId", "SELECT last_insert_rowid()") { cursor ->
    cursor.getLong(0)!!
  }

  public override fun insertSale(
    itemId: Long,
    playerId: Long,
    price: Double,
    discount: Double,
    paid: Double,
    debt: Double,
    date: Long
  ): Unit {
    driver.execute(341975917, """
    |INSERT INTO Sale (itemId, playerId, price, discount, paid, debt, date)
    |VALUES (?, ?, ?, ?, ?, ?, ?)
    """.trimMargin(), 7) {
      bindLong(1, itemId)
      bindLong(2, playerId)
      bindDouble(3, price)
      bindDouble(4, discount)
      bindDouble(5, paid)
      bindDouble(6, debt)
      bindLong(7, date)
    }
    notifyQueries(341975917, {database.saleQueries.selectAll +
        database.saleQueries.selectByPlayerId + database.saleQueries.selectById})
  }

  public override fun updateSale(
    itemId: Long,
    playerId: Long,
    price: Double,
    discount: Double,
    paid: Double,
    debt: Double,
    date: Long,
    id: Long
  ): Unit {
    driver.execute(-920120963, """
    |UPDATE Sale SET itemId = ?, playerId = ?, price = ?, discount = ?, paid = ?, debt = ?, date = ?
    |WHERE id = ?
    """.trimMargin(), 8) {
      bindLong(1, itemId)
      bindLong(2, playerId)
      bindDouble(3, price)
      bindDouble(4, discount)
      bindDouble(5, paid)
      bindDouble(6, debt)
      bindLong(7, date)
      bindLong(8, id)
    }
    notifyQueries(-920120963, {database.saleQueries.selectAll +
        database.saleQueries.selectByPlayerId + database.saleQueries.selectById})
  }

  public override fun deleteById(id: Long): Unit {
    driver.execute(1139914986, """DELETE FROM Sale WHERE id = ?""", 1) {
      bindLong(1, id)
    }
    notifyQueries(1139914986, {database.saleQueries.selectAll +
        database.saleQueries.selectByPlayerId + database.saleQueries.selectById})
  }

  private inner class SelectByIdQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T
  ) : Query<T>(selectById, mapper) {
    public override fun execute(): SqlCursor = driver.executeQuery(2014336955,
        """SELECT * FROM Sale WHERE id = ?""", 1) {
      bindLong(1, id)
    }

    public override fun toString(): String = "Sale.sq:selectById"
  }

  private inner class SelectByPlayerIdQuery<out T : Any>(
    public val playerId: Long,
    mapper: (SqlCursor) -> T
  ) : Query<T>(selectByPlayerId, mapper) {
    public override fun execute(): SqlCursor = driver.executeQuery(957956732,
        """SELECT * FROM Sale WHERE playerId = ?""", 1) {
      bindLong(1, playerId)
    }

    public override fun toString(): String = "Sale.sq:selectByPlayerId"
  }
}
