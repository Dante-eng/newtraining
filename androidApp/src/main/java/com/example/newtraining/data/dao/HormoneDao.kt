package com.example.newtraining.data.dao

import androidx.room.*
import com.example.newtraining.data.entity.Hormone
import kotlinx.coroutines.flow.Flow

@Dao
interface HormoneDao {
    @Query("SELECT * FROM hormones ORDER BY name")
    fun getAllHormones(): Flow<List<Hormone>>

    @Insert
    suspend fun insertHormone(hormone: Hormone)

    @Update
    suspend fun updateHormone(hormone: Hormone)

    @Delete
    suspend fun deleteHormone(hormone: Hormone)
} 