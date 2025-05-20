package com.example.newtraining.shared.db

import com.squareup.sqldelight.db.SqlDriver
// import com.squareup.sqldelight.native.driver.NativeSqliteDriver // fallback for some environments
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import com.example.newtraining.shared.db.AppDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        throw NotImplementedError("Test minimal iOS build - no NativeSqliteDriver import")
    }
} 