package com.example.newtraining.shared.repository

import com.example.newtraining.shared.model.Item

interface ItemRepository {
    suspend fun getAllItems(): List<Item>
    suspend fun getItemById(id: Long): Item?
    suspend fun insertItem(item: Item): Long
    suspend fun updateItem(item: Item)
    suspend fun deleteItem(id: Long)
} 