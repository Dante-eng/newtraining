package com.example.newtraining.shared

import com.example.newtraining.shared.db.AppDatabase
import com.example.newtraining.shared.db.DatabaseDriverFactory
import com.example.newtraining.shared.repository.ItemRepository
import com.example.newtraining.shared.repository.ItemRepositoryImpl
import com.example.newtraining.shared.repository.PlayerRepository
import com.example.newtraining.shared.repository.PlayerRepositoryImpl
import com.example.newtraining.shared.repository.SaleRepository
import com.example.newtraining.shared.repository.SaleRepositoryImpl

class SharedApp(driverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(driverFactory.createDriver())

    val itemRepository: ItemRepository = ItemRepositoryImpl(database)
    val playerRepository: PlayerRepository = PlayerRepositoryImpl(database)
    val saleRepository: SaleRepository = SaleRepositoryImpl(database)
} 