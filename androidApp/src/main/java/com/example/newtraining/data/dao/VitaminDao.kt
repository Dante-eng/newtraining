package com.example.newtraining.data.dao

import androidx.room.*
import com.example.newtraining.data.entity.Vitamin
import kotlinx.coroutines.flow.Flow

@Dao
interface VitaminDao {
    @Query("SELECT * FROM vitamins")
    fun getAllVitamins(): Flow<List<Vitamin>>

    @Insert
    suspend fun insertVitamin(vitamin: Vitamin)

    @Update
    suspend fun updateVitamin(vitamin: Vitamin)

    @Delete
    suspend fun deleteVitamin(vitamin: Vitamin)

    @Query("SELECT * FROM vitamins WHERE id = :vitaminId")
    suspend fun getVitaminById(vitaminId: Int): Vitamin?
} 