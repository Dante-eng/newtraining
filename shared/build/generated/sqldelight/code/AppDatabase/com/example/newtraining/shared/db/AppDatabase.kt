package com.example.newtraining.shared.db

import com.example.newtraining.shared.db.shared.newInstance
import com.example.newtraining.shared.db.shared.schema
import com.squareup.sqldelight.Transacter
import com.squareup.sqldelight.db.SqlDriver

public interface AppDatabase : Transacter {
  public val appDatabaseQueries: AppDatabaseQueries

  public val itemQueries: ItemQueries

  public val playerQueries: PlayerQueries

  public val saleQueries: SaleQueries

  public companion object {
    public val Schema: SqlDriver.Schema
      get() = AppDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): AppDatabase =
        AppDatabase::class.newInstance(driver)
  }
}
