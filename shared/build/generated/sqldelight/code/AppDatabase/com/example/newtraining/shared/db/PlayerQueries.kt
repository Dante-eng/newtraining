package com.example.newtraining.shared.db

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter
import kotlin.Any
import kotlin.Long
import kotlin.String
import kotlin.Unit

public interface PlayerQueries : Transacter {
  public fun <T : Any> playerSelectAll(mapper: (
    id: Long,
    uniqueId: String,
    fullName: String,
    age: Long,
    height: Long,
    gender: String,
    medicalCondition: String,
    pictureUri: String?
  ) -> T): Query<T>

  public fun playerSelectAll(): Query<Player>

  public fun <T : Any> playerSelectById(id: Long, mapper: (
    id: Long,
    uniqueId: String,
    fullName: String,
    age: Long,
    height: Long,
    gender: String,
    medicalCondition: String,
    pictureUri: String?
  ) -> T): Query<T>

  public fun playerSelectById(id: Long): Query<Player>

  public fun lastInsertRowId(): Query<Long>

  public fun playerInsert(
    uniqueId: String,
    fullName: String,
    age: Long,
    height: Long,
    gender: String,
    medicalCondition: String,
    pictureUri: String?
  ): Unit

  public fun playerUpdate(
    uniqueId: String,
    fullName: String,
    age: Long,
    height: Long,
    gender: String,
    medicalCondition: String,
    pictureUri: String?,
    id: Long
  ): Unit

  public fun playerDeleteById(id: Long): Unit
}
