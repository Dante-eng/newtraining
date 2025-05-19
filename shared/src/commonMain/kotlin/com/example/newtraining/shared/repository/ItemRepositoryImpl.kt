package com.example.newtraining.shared.repository

import com.example.newtraining.shared.model.Item
import com.example.newtraining.shared.db.AppDatabase
import com.example.newtraining.shared.db.Item as DbItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepositoryImpl(private val database: AppDatabase) : ItemRepository {
    override suspend fun getAllItems(): List<Item> = withContext(Dispatchers.Default) {
        database.itemQueries.selectAll().executeAsList().map { it.toItem() }
    }

    override suspend fun getItemById(id: Long): Item? = withContext(Dispatchers.Default) {
        database.itemQueries.selectById(id).executeAsOneOrNull()?.toItem()
    }

    override suspend fun insertItem(item: Item): Long = withContext(Dispatchers.Default) {
        database.itemQueries.insertItem(
            name = item.name,
            price = item.price,
            type = item.type,
            sellPrice = item.sellPrice
        )
        database.itemQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updateItem(item: Item) = withContext(Dispatchers.Default) {
        database.itemQueries.updateItem(
            name = item.name,
            price = item.price,
            type = item.type,
            sellPrice = item.sellPrice,
            id = item.id
        )
    }

    override suspend fun deleteItem(id: Long) = withContext(Dispatchers.Default) {
        database.itemQueries.deleteById(id)
    }
}

private fun DbItem.toItem() = Item(
    id = id,
    name = name,
    price = price,
    type = type,
    sellPrice = sellPrice
) 