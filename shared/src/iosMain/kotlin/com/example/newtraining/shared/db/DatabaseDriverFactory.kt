package com.example.newtraining.shared.db

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.native.driver.NativeSqliteDriver
import com.example.newtraining.shared.db.AppDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(AppDatabase.Schema, "app.db")
    }
} 