package com.example.newtraining.shared.db

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter
import kotlin.Any
import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlin.Unit

public interface ItemQueries : Transacter {
  public fun <T : Any> selectAll(mapper: (
    id: Long,
    name: String,
    price: Double,
    type: String,
    sellPrice: Double?
  ) -> T): Query<T>

  public fun selectAll(): Query<Item>

  public fun <T : Any> selectById(id: Long, mapper: (
    id: Long,
    name: String,
    price: Double,
    type: String,
    sellPrice: Double?
  ) -> T): Query<T>

  public fun selectById(id: Long): Query<Item>

  public fun lastInsertRowId(): Query<Long>

  public fun insertItem(
    name: String,
    price: Double,
    type: String,
    sellPrice: Double?
  ): Unit

  public fun updateItem(
    name: String,
    price: Double,
    type: String,
    sellPrice: Double?,
    id: Long
  ): Unit

  public fun deleteById(id: Long): Unit
}
