package com.example.newtraining.data.dao

import androidx.room.*
import com.example.newtraining.data.entity.DietPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface DietPlanDao {
    @Query("SELECT * FROM diet_plans ORDER BY name")
    fun getAllDietPlans(): Flow<List<DietPlan>>

    @Insert
    suspend fun insertDietPlan(dietPlan: DietPlan)

    @Update
    suspend fun updateDietPlan(dietPlan: DietPlan)

    @Delete
    suspend fun deleteDietPlan(dietPlan: DietPlan)
} 