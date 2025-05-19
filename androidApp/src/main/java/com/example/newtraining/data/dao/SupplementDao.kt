package com.example.newtraining.data.dao

import androidx.room.*
import com.example.newtraining.data.entity.Supplement
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplementDao {
    @Query("SELECT * FROM supplements ORDER BY name")
    fun getAllSupplements(): Flow<List<Supplement>>

    @Insert
    suspend fun insertSupplement(supplement: Supplement)

    @Update
    suspend fun updateSupplement(supplement: Supplement)

    @Delete
    suspend fun deleteSupplement(supplement: Supplement)
} 