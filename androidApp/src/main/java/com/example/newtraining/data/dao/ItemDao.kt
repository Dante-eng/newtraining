package com.example.newtraining.data.dao

import androidx.room.*
import com.example.newtraining.data.entity.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY name")
    fun getAllItems(): Flow<List<Item>>

    @Insert
    suspend fun insertItem(item: Item): Long

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Int): Item?
} 