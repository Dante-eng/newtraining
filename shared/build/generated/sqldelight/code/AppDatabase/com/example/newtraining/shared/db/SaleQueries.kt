package com.example.newtraining.shared.db

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter
import kotlin.Any
import kotlin.Double
import kotlin.Long
import kotlin.Unit

public interface SaleQueries : Transacter {
  public fun <T : Any> selectAll(mapper: (
    id: Long,
    itemId: Long,
    playerId: Long,
    price: Double,
    discount: Double,
    paid: Double,
    debt: Double,
    date: Long
  ) -> T): Query<T>

  public fun selectAll(): Query<Sale>

  public fun <T : Any> selectById(id: Long, mapper: (
    id: Long,
    itemId: Long,
    playerId: Long,
    price: Double,
    discount: Double,
    paid: Double,
    debt: Double,
    date: Long
  ) -> T): Query<T>

  public fun selectById(id: Long): Query<Sale>

  public fun <T : Any> selectByPlayerId(playerId: Long, mapper: (
    id: Long,
    itemId: Long,
    playerId: Long,
    price: Double,
    discount: Double,
    paid: Double,
    debt: Double,
    date: Long
  ) -> T): Query<T>

  public fun selectByPlayerId(playerId: Long): Query<Sale>

  public fun lastInsertRowId(): Query<Long>

  public fun insertSale(
    itemId: Long,
    playerId: Long,
    price: Double,
    discount: Double,
    paid: Double,
    debt: Double,
    date: Long
  ): Unit

  public fun updateSale(
    itemId: Long,
    playerId: Long,
    price: Double,
    discount: Double,
    paid: Double,
    debt: Double,
    date: Long,
    id: Long
  ): Unit

  public fun deleteById(id: Long): Unit
}
