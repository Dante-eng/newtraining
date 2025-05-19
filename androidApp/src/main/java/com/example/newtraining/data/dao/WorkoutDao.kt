package com.example.newtraining.data.dao

import androidx.room.*
import com.example.newtraining.data.entity.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE muscleGroupId = :muscleGroupId")
    fun getWorkoutsForMuscleGroup(muscleGroupId: Int): Flow<List<Workout>>

    @Insert
    suspend fun insertWorkout(workout: Workout)

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)
} 