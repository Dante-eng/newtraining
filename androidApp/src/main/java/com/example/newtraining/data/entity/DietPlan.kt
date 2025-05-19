package com.example.newtraining.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diet_plans")
data class DietPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val caloriesPer100g: String,
    val proteinPer100g: String,  // Protein in grams per 100g
    val fatPer100g: String,      // Fat in grams per 100g
    val carbsPer100g: String     // Carbohydrates in grams per 100g
) 