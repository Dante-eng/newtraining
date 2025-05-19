package com.example.newtraining.ui.screens

import com.example.newtraining.data.entity.*

// Data classes for plan serialization/deserialization

data class TrainingPlanContent(
    val meals: List<String>,
    val mealFoods: List<List<MealFood>>,
    val workoutDays: List<WorkoutDay>,
    val supplements: List<AddedSupplement>,
    val hormones: List<AddedHormone>,
    val antibiotics: List<AddedAntibiotic>,
    val vitamins: List<AddedVitamin>,
    val images: List<String>,
    val isComplete: Boolean
)

data class MealFood(val name: String, val weight: String)
data class WorkoutEntry(val muscleGroup: MuscleGroup, val workout: Workout, val set: WorkoutSet)
data class WorkoutDay(val name: String, val entries: List<WorkoutEntry>)
data class AddedSupplement(val supplement: Supplement, val amount: String, val time: String, val comment: String)
data class AddedHormone(val hormone: Hormone, val amount: String, val time: String, val comment: String)
data class AddedAntibiotic(val antibiotic: Antibiotic, val amount: String, val time: String, val comment: String)
data class AddedVitamin(val vitamin: Vitamin, val amount: String, val time: String, val comment: String) 