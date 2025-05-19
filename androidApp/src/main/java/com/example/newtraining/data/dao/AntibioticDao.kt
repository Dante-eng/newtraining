package com.example.newtraining.data.dao

import androidx.room.*
import com.example.newtraining.data.entity.Antibiotic
import kotlinx.coroutines.flow.Flow

@Dao
interface AntibioticDao {
    @Query("SELECT * FROM antibiotics ORDER BY name")
    fun getAllAntibiotics(): Flow<List<Antibiotic>>

    @Insert
    suspend fun insertAntibiotic(antibiotic: Antibiotic)

    @Update
    suspend fun updateAntibiotic(antibiotic: Antibiotic)

    @Delete
    suspend fun deleteAntibiotic(antibiotic: Antibiotic)
} 