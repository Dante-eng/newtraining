package com.example.newtraining.data.dao

import androidx.room.*
import com.example.newtraining.data.entity.TrainingPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingPlanDao {
    @Query("SELECT * FROM training_plans WHERE playerId = :playerId ORDER BY date DESC")
    fun getTrainingPlansForPlayer(playerId: Int): Flow<List<TrainingPlan>>

    @Query("SELECT * FROM training_plans WHERE id = :planId")
    fun getTrainingPlanById(planId: Int): Flow<TrainingPlan?>

    @Insert
    suspend fun insertTrainingPlan(plan: TrainingPlan)

    @Update
    suspend fun updateTrainingPlan(plan: TrainingPlan)

    @Delete
    suspend fun deleteTrainingPlan(plan: TrainingPlan)

    @Query("SELECT COUNT(*) FROM training_plans WHERE playerId = :playerId")
    suspend fun getPlansCountForPlayer(playerId: Int): Int

    @Query("SELECT * FROM training_plans ORDER BY date DESC")
    fun getAllTrainingPlans(): Flow<List<TrainingPlan>>
} 