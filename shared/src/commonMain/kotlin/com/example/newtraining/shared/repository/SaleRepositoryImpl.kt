package com.example.newtraining.shared.repository

import com.example.newtraining.shared.model.Sale
import com.example.newtraining.shared.db.AppDatabase
import com.example.newtraining.shared.db.Sale as DbSale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SaleRepositoryImpl(private val database: AppDatabase) : SaleRepository {
    override suspend fun getAllSales(): List<Sale> = withContext(Dispatchers.Default) {
        database.saleQueries.selectAll().executeAsList().map { it.toSale() }
    }

    override suspend fun getSaleById(id: Long): Sale? = withContext(Dispatchers.Default) {
        database.saleQueries.selectById(id.toInt()).executeAsOneOrNull()?.toSale()
    }

    override suspend fun getSalesByPlayerId(playerId: Long): List<Sale> = withContext(Dispatchers.Default) {
        database.saleQueries.selectByPlayerId(playerId.toInt()).executeAsList().map { it.toSale() }
    }

    override suspend fun insertSale(sale: Sale): Long = withContext(Dispatchers.Default) {
        database.saleQueries.insertSale(
            itemId = sale.itemId.toInt(),
            playerId = sale.playerId.toInt(),
            price = sale.price,
            discount = sale.discount,
            paid = sale.paid,
            debt = sale.debt,
            date = sale.date
        )
        database.saleQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updateSale(sale: Sale) = withContext(Dispatchers.Default) {
        database.saleQueries.updateSale(
            itemId = sale.itemId.toInt(),
            playerId = sale.playerId.toInt(),
            price = sale.price,
            discount = sale.discount,
            paid = sale.paid,
            debt = sale.debt,
            date = sale.date,
            id = sale.id.toInt()
        )
    }

    override suspend fun deleteSale(id: Long) = withContext(Dispatchers.Default) {
        database.saleQueries.deleteById(id.toInt())
    }
}

private fun DbSale.toSale() = Sale(
    id = id,
    itemId = itemId,
    playerId = playerId,
    price = price,
    discount = discount,
    paid = paid,
    debt = debt,
    date = date
) 