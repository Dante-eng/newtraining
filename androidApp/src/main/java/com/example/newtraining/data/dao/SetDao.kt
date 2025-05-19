package com.example.newtraining.data.dao

import androidx.room.*
import com.example.newtraining.data.entity.WorkoutSet
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {
    @Query("SELECT * FROM workout_sets")
    fun getAllSets(): Flow<List<WorkoutSet>>

    @Insert
    suspend fun insertSet(set: WorkoutSet)

    @Update
    suspend fun updateSet(set: WorkoutSet)

    @Delete
    suspend fun deleteSet(set: WorkoutSet)
} 