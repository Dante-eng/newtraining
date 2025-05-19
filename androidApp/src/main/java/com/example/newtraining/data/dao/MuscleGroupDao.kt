package com.example.newtraining.data.dao

import androidx.room.*
import com.example.newtraining.data.entity.MuscleGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface MuscleGroupDao {
    @Query("SELECT * FROM muscle_groups")
    fun getAllMuscleGroups(): Flow<List<MuscleGroup>>

    @Insert
    suspend fun insertMuscleGroup(muscleGroup: MuscleGroup)

    @Update
    suspend fun updateMuscleGroup(muscleGroup: MuscleGroup)

    @Delete
    suspend fun deleteMuscleGroup(muscleGroup: MuscleGroup)

    @Query("SELECT * FROM muscle_groups WHERE id = :id")
    suspend fun getMuscleGroupById(id: Int): MuscleGroup?
} 